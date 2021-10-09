package com.mikkezavala.sat.service.sat;

import static com.mikkezavala.sat.domain.sat.SoapEndpoint.AUTENTICA;
import static com.mikkezavala.sat.domain.sat.SoapEndpoint.SOLICITA_DESCARGA;
import static com.mikkezavala.sat.domain.sat.SoapEndpoint.VALIDA_DESCARGA;

import com.mikkezavala.sat.domain.client.registered.RequestCfdi;
import com.mikkezavala.sat.domain.sat.auth.AuthResponse;
import com.mikkezavala.sat.domain.sat.cfdi.individual.SatPacket;
import com.mikkezavala.sat.domain.sat.cfdi.individual.SatToken;
import com.mikkezavala.sat.domain.sat.cfdi.individual.request.Response;
import com.mikkezavala.sat.domain.sat.cfdi.individual.validate.ValidateResponse;
import com.mikkezavala.sat.domain.sat.cfdi.individual.validate.ValidateResult;
import com.mikkezavala.sat.repository.SatPacketRepository;
import com.mikkezavala.sat.repository.SatTokenRepository;
import com.mikkezavala.sat.service.SoapService;
import com.mikkezavala.sat.util.SoapUtil;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class IndividualContributorService {

  private final SoapService service;

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

  public SatPacket getReceptorInvoices(RequestCfdi request) {

    String rfc = request.getRfc();

    SatPacket existentPacket = satPacketRepository.findSatPacketByRfcAndDateEndAndDateStart(
        rfc, request.getDateEnd(), request.getDateStart()
    );

    if (Objects.nonNull(existentPacket) && Objects.nonNull(existentPacket.getRequestId())) {
      String existentRfc = existentPacket.getRfc();
      SatToken satToken = repository.findFirstByRfc(existentRfc);
      ZonedDateTime expires = satToken.getExpiration();
      Duration tokenValidity = Duration.between(ZonedDateTime.now(), expires);

      if (tokenValidity.getSeconds() < 60) {
        LOGGER.info("Removing old tokens based on last time: {} ", tokenValidity.getSeconds());
        repository.deleteByRfc(existentRfc);
        satToken = getToken(existentRfc);

      }

      return validateRequest(existentPacket, satToken);

    } else {
      SatToken satToken = getToken(rfc);
      Response response = requestDownload(request, satToken);
      if (Objects.nonNull(response)) {
        String requestId = response.getResult().getRequestId();
        return validateRequest(requestId, request, satToken);
      }
    }
    return null;
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
      SatPacket packet = satPacketRepository.save(SatPacket.builder()
          .rfc(rfc)
          .status(result.getStatus())
          .state(result.getState().name())
          .message(result.getMessage())
          .dateEnd(request.getDateEnd())
          .dateStart(request.getDateStart())
          .timesRequested(1)
          .requestId(requestId).path("").build()
      );

      return packet;
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
          .timesRequested(packet.getTimesRequested() + 1).build()
      );
    } catch (Exception e) {
      LOGGER.error("failed validating request", e);
    }

    return null;
  }
}
