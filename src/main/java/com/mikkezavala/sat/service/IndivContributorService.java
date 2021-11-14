package com.mikkezavala.sat.service;

import static com.mikkezavala.sat.domain.sat.cfdi.individual.validate.StateCode.READY;
import static com.mikkezavala.sat.util.Constant.TIME_ZONE;
import static com.mikkezavala.sat.util.ResourceUtil.getFromZip;
import static com.mikkezavala.sat.util.ResourceUtil.saveZip;

import com.mikkezavala.sat.domain.client.registered.RequestCFDI;
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
import com.mikkezavala.sat.repository.sat.SatRepository;
import com.mikkezavala.sat.service.sat.AuthService;
import com.mikkezavala.sat.service.sat.DownloadCFDIService;
import com.mikkezavala.sat.service.sat.RequestDownloadService;
import com.mikkezavala.sat.service.sat.ValidateRequestService;
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
public class IndivContributorService {

  private final SatRepository repository;

  private final AuthService authService;

  private final DownloadCFDIService downloadCFDIService;

  private final ValidateRequestService validateRequestService;

  private final RequestDownloadService requestDownloadService;

  private static final int BACKOFF_TIME = 240;

  private static final String LOGGER_PREFIX = "[SAT INDIVIDUAL SERVICE]: {} - {}";

  private static final Logger LOGGER = LoggerFactory.getLogger(IndivContributorService.class);

  public IndivContributorService(
      AuthService authService,
      DownloadCFDIService downloadCFDIService,
      ValidateRequestService validateRequestService,
      RequestDownloadService requestDownloadService,
      SatRepository repository
  ) {
    this.repository = repository;
    this.authService = authService;
    this.downloadCFDIService = downloadCFDIService;
    this.validateRequestService = validateRequestService;
    this.requestDownloadService = requestDownloadService;
  }

  /**
   * Gets receptor invoices.
   *
   * @param request the request
   * @return the receptor invoices
   */
  public Invoices getReceptorInvoices(RequestCFDI request) {

    String rfc = request.getRfc();
    List<Invoice> invoices = new ArrayList<>();
    Invoices.InvoicesBuilder builder = Invoices.builder();

    SatToken satToken = getToken(rfc);
    SatPacket satPacket = repository.packetByRfcAndDateEndAndDateStart(
        rfc, request.getDateEnd(), request.getDateStart()
    );

    if (
        Objects.nonNull(satPacket)
            && Objects.nonNull(satPacket.getRequestId())
            && Objects.nonNull(satToken)
    ) {

      int times = satPacket.getTimesRequested();
      LOGGER.info(LOGGER_PREFIX, "Requesting Validation", satPacket.getState());

      if (!StateCode.equals(satPacket.getState(), READY)) {
        Duration delta = Duration.between(
            satPacket.getLastRequested().withZoneSameLocal(ZoneId.of(TIME_ZONE)),
            ZonedDateTime.now().withZoneSameLocal(ZoneId.of(TIME_ZONE))
        );
        long waitTime = BACKOFF_TIME - delta.toMinutes();
        if (times > 5 && waitTime >= 0) {
          LOGGER.warn(LOGGER_PREFIX,
              "Backing off. No validation. Requested times",
              String.format("%s. Wait time: ~%s hrs", times, waitTime / 60)
          );
          builder.message("Backing off validation request. Wait time: " + waitTime + " minutes");
        } else {
          LOGGER.info(LOGGER_PREFIX, "Valid request and token", "Requesting Download Validation");
          satPacket = validateRequest(satPacket);
        }
      }
    } else {

      LOGGER.info(LOGGER_PREFIX, "Creating SAT Packet", null);

      Response response = requestDownload(request);
      if (Objects.nonNull(response)) {
        String requestId = response.result().requestId();
        LOGGER.info("Validating Download Request");
        satPacket = validateRequest(requestId, request);
      }
    }

    if (StateCode.equals(satPacket.getState(), READY)
        && (StringUtils.isEmpty(satPacket.getPath())
        || !satPacket.isConsumed())
    ) {
      LOGGER.info(LOGGER_PREFIX, "Downloading packetId = ", satPacket.getPacketId());
      satPacket = descarga(satPacket);
    }

    if (StringUtils.isNotEmpty(satPacket.getPath())) {
      invoices = getFromZip(satPacket.getPath(), Invoice.class);
      builder.message("SUCCESS");
    }

    return builder
        .invoices(invoices)
        .satState(StateCode.getCode(satPacket.getState())).build();
  }

  public SatToken getToken(String rfc) {

    SatToken satToken = repository.tokenByRfc(rfc);
    long tokenValidity = SoapUtil.getTokenDuration(satToken);
    if (tokenValidity <= 60) {

      LOGGER.info(LOGGER_PREFIX, "Removing old tokens based on last time = ", tokenValidity);
      repository.tokenDeleteByRfc(rfc);
      AuthResponse response = authService.getAuth(rfc);
      satToken = SatToken.builder()
          .token(response.getToken())
          .created(response.getTimestamp().getCreated())
          .expiration(response.getTimestamp().getExpires())
          .rfc(rfc).build();

      LOGGER.info(LOGGER_PREFIX,
          "Created Token; valid for = ", String.format(
              "%s seconds", SoapUtil.getTokenDuration(satToken)
          )
      );

      return repository.saveToken(satToken);
    }

    return satToken;
  }

  private Response requestDownload(RequestCFDI request) {
    try {
      return requestDownloadService.getRequest(request);
    } catch (Exception e) {
      LOGGER.error("failed creating request", e);
    }

    return null;
  }

  private SatPacket validateRequest(String requestId, RequestCFDI request) {
    try {
      String rfc = request.getRfc();
      ZonedDateTime nowTime = ZonedDateTime.now();
      ValidateResponse response = validateRequestService.getValidation(rfc, requestId);

      ValidateResult result = response.result();
      return repository.savePacket(SatPacket.builder()
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

  private SatPacket validateRequest(SatPacket packet) {
    try {

      String rfc = packet.getRfc();
      String requestId = packet.getRequestId();
      ValidateResponse response = validateRequestService.getValidation(rfc, requestId);

      ValidateResult result = response.result();
      return repository.savePacket(packet.toBuilder()
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

  private SatPacket descarga(SatPacket satPacket) {
    try {

      String rfc = satPacket.getRfc();
      String packedId = satPacket.getPacketId();
      String uuid = String.format("%s_%s", rfc, packedId);
      DownloadResponse out = downloadCFDIService.getDownload(rfc, packedId);

      String zip = saveZip(out.paquete(), uuid);
      return repository.savePacket(satPacket.toBuilder()
          .path(zip).consumed(true).status(READY.name()).build()
      );
    } catch (Exception e) {
      return satPacket;
    }
  }

}
