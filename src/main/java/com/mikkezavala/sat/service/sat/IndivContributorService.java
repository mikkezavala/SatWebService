package com.mikkezavala.sat.service.sat;

import static com.mikkezavala.sat.domain.sat.SoapEndpoint.AUTENTICA;
import static com.mikkezavala.sat.domain.sat.SoapEndpoint.DESCARGA_MASIVA;
import static com.mikkezavala.sat.domain.sat.SoapEndpoint.SOLICITA_DESCARGA;
import static com.mikkezavala.sat.domain.sat.SoapEndpoint.VALIDA_DESCARGA;
import static com.mikkezavala.sat.domain.sat.cfdi.individual.validate.StateCode.READY;
import static com.mikkezavala.sat.util.Constant.TIME_ZONE;
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

/**
 * The type Individual contributor service.
 */
@Service
public class IndivContributorService {


  private final SoapUtil soapUtil;

  private final SoapService service;

  private final SatTokenRepository repository;

  private final SatPacketRepository satPacketRepository;

  private static final int BACKOFF_TIME = 240;

  private static final Logger LOGGER = LoggerFactory.getLogger(IndivContributorService.class);

  /**
   * Instantiates a new Individual contributor service.
   *
   * @param soapUtil
   * @param service             the service
   * @param repository          the repository
   * @param satPacketRepository the sat packet repository
   */
  public IndivContributorService(
      SoapUtil soapUtil,
      SoapService service,
      SatTokenRepository repository,
      SatPacketRepository satPacketRepository
  ) {
    this.soapUtil = soapUtil;
    this.service = service;
    this.repository = repository;
    this.satPacketRepository = satPacketRepository;
  }

  /**
   * Gets receptor invoices.
   *
   * @param request the request
   * @return the receptor invoices
   */
  public Invoices getReceptorInvoices(RequestCfdi request) {

    String rfc = request.getRfc();
    List<Invoice> invoices = new ArrayList<>();
    Invoices.InvoicesBuilder builder = Invoices.builder();

    SatToken satToken = repository.findFirstByRfc(rfc);
    SatPacket satPacket = satPacketRepository.findSatPacketByRfcAndDateEndAndDateStart(
        rfc, request.getDateEnd(), request.getDateStart()
    );

    if (
        Objects.nonNull(satPacket)
            && Objects.nonNull(satPacket.getRequestId())
            && Objects.nonNull(satToken)
    ) {
      String existentRfc = satPacket.getRfc();
      int times = satPacket.getTimesRequested();
      ZonedDateTime expires = satToken.getExpiration();
      Duration tokenValidity = Duration.between(ZonedDateTime.now(), expires);

      LOGGER.info("SAT Packet In {}. Requesting Validation", satPacket.getState());

      if (tokenValidity.getSeconds() < 60) {
        LOGGER.info("Removing old tokens based on last time: {} ", tokenValidity.getSeconds());
        repository.deleteByRfc(existentRfc);
        satToken = getToken(existentRfc);
      }

      if (!StateCode.equals(satPacket.getState(), READY)) {
        Duration delta = Duration.between(
            satPacket.getLastRequested().withZoneSameLocal(ZoneId.of(TIME_ZONE)),
            ZonedDateTime.now().withZoneSameLocal(ZoneId.of(TIME_ZONE))
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
          LOGGER.info("Valid request and token. Requesting Download Validation");
          satPacket = validateRequest(satPacket, satToken);
        }
      }
    } else {
      LOGGER.info("Creating SAT Packet");
      satToken = getToken(rfc);
      Response response = requestDownload(request, satToken);
      if (Objects.nonNull(response)) {
        String requestId = response.getResult().getRequestId();
        LOGGER.info("Validating Download Request");
        satPacket = validateRequest(requestId, request, satToken);
      }
    }

    if (StateCode.equals(satPacket.getState(), READY)
        && (StringUtils.isEmpty(satPacket.getPath())
        || !satPacket.isConsumed())
    ) {
      LOGGER.info("Downloading packet");
      satPacket = descarga(satPacket, satToken);
    }

    if (StringUtils.isNotEmpty(satPacket.getPath())) {
      invoices = getFromZip(satPacket.getPath(), Invoice.class);
    }

    return builder
        .invoices(invoices)
        .satState(StateCode.getCode(satPacket.getState())).build();
  }

  private SatToken getToken(String rfc) {
    try {
      AuthResponse response = soapUtil.callWebService(
          service.auth(rfc), AuthResponse.class, AUTENTICA, null
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
      return soapUtil.callWebService(
          service.request(rfc, request.getDateStart(), request.getDateEnd()), Response.class,
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
      ZonedDateTime nowTime = ZonedDateTime.now();
      ZonedDateTime expires = token.getExpiration();
      Duration tokenValidity = Duration.between(nowTime, expires);

      LOGGER.info("Token Valid for (in validateRequest): {} seconds", tokenValidity.getSeconds());
      ValidateResponse response = soapUtil.callWebService(
          service.validation(requestId, rfc
          ), ValidateResponse.class, VALIDA_DESCARGA, runtimeToken
      );

      ValidateResult result = response.result();

      return satPacketRepository.save(SatPacket.builder()
          .rfc(rfc)
          .path("")
          .timesRequested(1)
          .requestId(requestId)
          .lastRequested(nowTime)
          .status(result.status())
          .message(result.message())
          .dateEnd(request.getDateEnd())
          .state(result.state().name())
          .dateStart(request.getDateStart())
          .packetId(StringUtils.join(result.IdsPaquetes(), ",")).build()
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
      ValidateResponse response = soapUtil.callWebService(
          service.validation(packet.getRequestId(), packet.getRfc()
          ), ValidateResponse.class, VALIDA_DESCARGA, runtimeToken
      );

      ValidateResult result = response.result();
      return satPacketRepository.save(packet.toBuilder()
          .status(result.status())
          .message(result.message())
          .state(result.state().name())
          .packetId(StringUtils.join(result.IdsPaquetes(), ","))
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
      DownloadResponse out = soapUtil.callWebService(
          service.download(packedId, rfc), DownloadResponse.class, DESCARGA_MASIVA, token.getToken()
      );

      String zip = saveZip(out.paquete(), uuid);
      return satPacketRepository.save(satPacket.toBuilder()
          .path(zip).consumed(true).status(READY.name()).build()
      );
    } catch (Exception e) {
      return satPacket;
    }
  }

}
