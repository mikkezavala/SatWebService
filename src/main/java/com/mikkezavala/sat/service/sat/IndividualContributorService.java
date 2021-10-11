package com.mikkezavala.sat.service.sat;

import static com.mikkezavala.sat.domain.sat.SoapEndpoint.AUTENTICA;
import static com.mikkezavala.sat.domain.sat.SoapEndpoint.DESCARGA_MASIVA;
import static com.mikkezavala.sat.domain.sat.SoapEndpoint.SOLICITA_DESCARGA;
import static com.mikkezavala.sat.domain.sat.SoapEndpoint.VALIDA_DESCARGA;
import static com.mikkezavala.sat.util.ResourceUtil.getFromZip;
import static com.mikkezavala.sat.util.ResourceUtil.saveZip;

import com.mikkezavala.sat.domain.client.registered.RequestCfdi;
import com.mikkezavala.sat.domain.sat.auth.AuthResponse;
import com.mikkezavala.sat.domain.sat.cfdi.individual.Invoices;
import com.mikkezavala.sat.domain.sat.cfdi.individual.SatPacket;
import com.mikkezavala.sat.domain.sat.cfdi.individual.SatToken;
import com.mikkezavala.sat.domain.sat.cfdi.individual.download.DownloadResponse;
import com.mikkezavala.sat.domain.sat.cfdi.individual.entity.Invoice;
import com.mikkezavala.sat.domain.sat.cfdi.individual.request.Response;
import com.mikkezavala.sat.domain.sat.cfdi.individual.validate.StateCode;
import com.mikkezavala.sat.domain.sat.cfdi.individual.validate.ValidateResponse;
import com.mikkezavala.sat.domain.sat.cfdi.individual.validate.ValidateResult;
import com.mikkezavala.sat.repository.SatPacketRepository;
import com.mikkezavala.sat.repository.SatTokenRepository;
import com.mikkezavala.sat.service.SoapService;
import com.mikkezavala.sat.util.SoapUtil;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class IndividualContributorService {


  private final SoapService service;
  private final int BACKOFF_TIME = 240;
  private final SatTokenRepository repository;
  private final SatPacketRepository satPacketRepository;
  private static final Logger LOGGER = LoggerFactory.getLogger(IndividualContributorService.class);

  public IndividualContributorService(
      SoapService service, SatTokenRepository repository, SatPacketRepository satPacketRepository
  ) {
    this.service = service;
    this.repository = repository;
    this.satPacketRepository = satPacketRepository;
  }

  public Invoices getReceptorInvoices(RequestCfdi request) {

    String rfc = request.getRfc();
    List<Invoice> invoices = new ArrayList<>();
    Invoices.InvoicesBuilder builder = Invoices.builder();
    SatPacket satPacket = satPacketRepository.findSatPacketByRfcAndDateEndAndDateStart(
        rfc, request.getDateEnd(), request.getDateStart()
    );

    if (Objects.nonNull(satPacket) && Objects.nonNull(satPacket.getRequestId())) {
      String existentRfc = satPacket.getRfc();
      int times = satPacket.getTimesRequested();
      SatToken satToken = repository.findFirstByRfc(existentRfc);
      ZonedDateTime expires = satToken.getExpiration();
      Duration tokenValidity = Duration.between(ZonedDateTime.now(), expires);

      if (tokenValidity.getSeconds() < 60) {
        LOGGER.info("Removing old tokens based on last time: {} ", tokenValidity.getSeconds());
        repository.deleteByRfc(existentRfc);
        satToken = getToken(existentRfc);
      }

      if (!StateCode.valueOf(satPacket.getState()).equals(StateCode.READY)) {
        Duration delta = Duration.between(
            satPacket.getLastRequested().withZoneSameLocal(ZoneId.of("UTC")),
            ZonedDateTime.now()
        );
        long waitTime = BACKOFF_TIME - delta.toMinutes();
        if (times > 5 && waitTime >= 0) {
          LOGGER.warn(
              "Backing off. No validation made. Requested times: {}. Wait time: ~{} hrs",
              times,
              waitTime / 60
          );
          builder.message("Backing off validation request. Wait time: " + waitTime + " minutes");
        } else {
          satPacket = validateRequest(satPacket, satToken);
        }
      } else if (StringUtils.isEmpty(satPacket.getPath()) || !satPacket.isConsumed()) {
        satPacket = descarga(satPacket, satToken);
      }

      if (StringUtils.isNotEmpty(satPacket.getPath())) {
        invoices = getFromZip(satPacket.getPath(), Invoice.class);
      }

    } else {
      SatToken satToken = getToken(rfc);
      Response response = requestDownload(request, satToken);
      if (Objects.nonNull(response)) {
        String requestId = response.getResult().getRequestId();
        satPacket = validateRequest(requestId, request, satToken);
        if (
            Objects.nonNull(satPacket) && StateCode.valueOf(satPacket.getState())
                .equals(StateCode.READY)
        ) {
          satPacket = descarga(satPacket, satToken);
        }
      }
    }
    return builder
        .satState(StateCode.valueOf(satPacket.getState())).invoices(invoices).build();
  }

  private SatToken getToken(String rfc) {
    try {
      AuthResponse response = SoapUtil.callWebService(
          service.autentica(rfc), AuthResponse.class, AUTENTICA, null
      );
      ZonedDateTime tokenTo = response.getTimestamp().getExpires();
      Duration tokenValidity = Duration.between(ZonedDateTime.now(), tokenTo);

      LOGGER.info("Token Valid for: {} seconds", tokenValidity.getSeconds());
      return repository.save(SatToken.builder()
          .token(response.getToken())
          .created(response.getTimestamp().getCreated())
          .expiration(response.getTimestamp().getExpires())
          .rfc(rfc).build());
    } catch (Exception e) {
      LOGGER.error("Failed getting token for: {}", rfc, e);
    }
    return null;
  }

  private Response requestDownload(RequestCfdi request, SatToken token) {
    try {
      String rfc = request.getRfc();
      return SoapUtil.callWebService(
          service.solicita(rfc, request.getDateStart(), request.getDateEnd()), Response.class,
          SOLICITA_DESCARGA, token.getToken());
    } catch (Exception e) {
      LOGGER.error("failed creating request", e);
    }

    return null;
  }

  private SatPacket validateRequest(String requestId, RequestCfdi request, SatToken token) {
    try {
      String rfc = request.getRfc();
      String runtimeToken = token.getToken();
      ZonedDateTime expires = token.getExpiration();
      Duration tokenValidity = Duration.between(ZonedDateTime.now(), expires);

      LOGGER.info("Token Valid for (in validateRequest): {} seconds", tokenValidity.getSeconds());
      ValidateResponse response = SoapUtil.callWebService(
          service.valida(requestId, rfc
          ), ValidateResponse.class, VALIDA_DESCARGA, runtimeToken
      );

      ValidateResult result = response.getResult();

      return satPacketRepository.save(SatPacket.builder()
          .rfc(rfc)
          .path("")
          .timesRequested(1)
          .requestId(requestId)
          .status(result.getStatus())
          .message(result.getMessage())
          .dateEnd(request.getDateEnd())
          .state(result.getState().name())
          .dateStart(request.getDateStart())
          .packetId(StringUtils.join(result.getIdsPaquetes(), ",")).build()
      );
    } catch (Exception e) {
      LOGGER.error("failed validating request", e);
    }

    return null;
  }

  private SatPacket validateRequest(SatPacket packet, SatToken token) {
    try {
      String runtimeToken = token.getToken();
      ZonedDateTime expires = token.getExpiration();
      Duration tokenValidity = Duration.between(ZonedDateTime.now(), expires);

      LOGGER.info("Token Valid for (in validateRequest): {} seconds", tokenValidity.getSeconds());
      ValidateResponse response = SoapUtil.callWebService(
          service.valida(packet.getRequestId(), packet.getRfc()
          ), ValidateResponse.class, VALIDA_DESCARGA, runtimeToken
      );

      ValidateResult result = response.getResult();
      return satPacketRepository.save(packet.toBuilder()
          .status(result.getStatus())
          .message(result.getMessage())
          .state(result.getState().name())
          .packetId(StringUtils.join(result.getIdsPaquetes(), ","))
          .timesRequested(packet.getTimesRequested() + 1).build()
      );
    } catch (Exception e) {
      LOGGER.error("failed validating request", e);
    }

    return packet;
  }

  private SatPacket descarga(SatPacket satPacket, SatToken token) {
    try {

      String rfc = satPacket.getRfc();
      String packedId = satPacket.getPacketId();
      String uuid = String.format("%s_%s", rfc, packedId);
      DownloadResponse out = SoapUtil.callWebService(
          service.descarga(packedId, rfc), DownloadResponse.class, DESCARGA_MASIVA, token.getToken()
      );

      String zip = saveZip(out.getPaquete(), uuid);
      return satPacketRepository.save(satPacket.toBuilder()
          .path(zip).consumed(true).build()
      );
    } catch (Exception e) {
      return satPacket;
    }
  }
}
