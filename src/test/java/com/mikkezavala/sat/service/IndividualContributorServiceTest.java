package com.mikkezavala.sat.service;

import static com.mikkezavala.sat.domain.sat.cfdi.individual.validate.StateCode.IN_PROGRESS;
import static com.mikkezavala.sat.domain.sat.cfdi.individual.validate.StateCode.READY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.mikkezavala.sat.TestBase;
import com.mikkezavala.sat.domain.client.registered.RequestCFDI;
import com.mikkezavala.sat.domain.sat.auth.AuthResponse;
import com.mikkezavala.sat.domain.sat.auth.Timestamp;
import com.mikkezavala.sat.domain.sat.cfdi.individual.Invoices;
import com.mikkezavala.sat.domain.sat.cfdi.individual.SatClient;
import com.mikkezavala.sat.domain.sat.cfdi.individual.SatPacket;
import com.mikkezavala.sat.domain.sat.cfdi.individual.SatToken;
import com.mikkezavala.sat.domain.sat.cfdi.individual.download.DownloadResponse;
import com.mikkezavala.sat.domain.sat.cfdi.individual.entity.Invoice;
import com.mikkezavala.sat.domain.sat.cfdi.individual.request.Response;
import com.mikkezavala.sat.domain.sat.cfdi.individual.request.Result;
import com.mikkezavala.sat.domain.sat.cfdi.individual.validate.StateCode;
import com.mikkezavala.sat.domain.sat.cfdi.individual.validate.ValidateResponse;
import com.mikkezavala.sat.domain.sat.cfdi.individual.validate.ValidateResult;
import com.mikkezavala.sat.repository.sat.SatRepository;
import com.mikkezavala.sat.service.sat.AuthService;
import com.mikkezavala.sat.service.sat.DownloadCFDIService;
import com.mikkezavala.sat.service.sat.RequestDownloadService;
import com.mikkezavala.sat.service.sat.ValidateRequestService;
import java.io.File;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.UUID;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
public class IndividualContributorServiceTest extends TestBase {

  @Mock
  private SatRepository repository;

  @Mock
  private AuthService authService;

  @Mock
  private DownloadCFDIService downloadCFDIService;

  @Mock
  private RequestDownloadService requestDownloadService;

  @Mock
  private ValidateRequestService validateRequestService;


  private IndivContributorService service;

  private static final Pattern BACKOFF_PATTERN = Pattern.compile(
      "Backing off validation request\\. Wait time: \\d+ minutes");

  @BeforeEach
  void init() throws Exception {

    File pfxFile = loadResourceAsFile("PF_CFDI/" + RFC_TEST + ".p12");
    SatClient satClient = getMockedClient(pfxFile.getPath());

    Timestamp ts = new Timestamp();
    ts.setCreated(ZonedDateTime.now());
    ts.setExpires(ts.getCreated().plusDays(2));

    AuthResponse authResponse = new AuthResponse();
    authResponse.setToken("FAKE-TOKEN");
    authResponse.setTimestamp(ts);

    SatToken token = SatToken.builder()
        .id(1)
        .token("ejToken")
        .rfc(satClient.rfc())
        .created(ts.getCreated())
        .expiration(ts.getExpires()).build();

    Response response = new Response().result(
        new Result().status("3").requestId(UUID.randomUUID().toString())
    );

    SatPacket packet = SatPacket.builder().packetId(UUID.randomUUID().toString())
        .lastRequested(ZonedDateTime.now()).state("IN_PROGRESS").build();

    when(authService.getAuth(anyString())).thenReturn(authResponse);
    when(requestDownloadService.getRequest(any())).thenReturn(response);
    when(repository.saveToken(any(SatToken.class))).thenReturn(token);
    when(repository.savePacket(any(SatPacket.class))).thenReturn(packet);
    when(repository.satClientByRfc(anyString())).thenReturn(satClient);

    service = new IndivContributorService(
        authService, downloadCFDIService, validateRequestService, requestDownloadService,
        repository
    );
  }

  @Test
  public void shouldReturnInProgress() {
    ZonedDateTime dateEnd = ZonedDateTime.now().plusDays(5);
    ZonedDateTime dateStart = dateEnd.minusDays(5);

    String satPacketId = UUID.randomUUID().toString();
    RequestCFDI request = RequestCFDI.builder()
        .rfc(RFC_TEST).dateEnd(dateEnd).dateStart(dateStart).build();

    ValidateResponse validation = mockValidateResult(satPacketId, IN_PROGRESS);

    when(validateRequestService.getValidation(anyString(), anyString())).thenReturn(validation);
    when(repository.packetByRfcAndDateEndAndDateStart(
        anyString(), any(ZonedDateTime.class), any(ZonedDateTime.class))
    ).thenReturn(
        SatPacket.builder().packetId(satPacketId).state(IN_PROGRESS.name()).build()
    );

    Invoices invoices = service.getReceptorInvoices(request);
    assertThat(invoices.getSatState()).isEqualTo(IN_PROGRESS);
  }

  @Test
  public void shouldValidateRequest() {

    ZonedDateTime dateEnd = ZonedDateTime.now().plusDays(5);
    ZonedDateTime dateStart = dateEnd.minusDays(5);

    String requestId = UUID.randomUUID().toString();
    String satPacketId = UUID.randomUUID().toString();
    RequestCFDI request = RequestCFDI.builder()
        .rfc(RFC_TEST).dateEnd(dateEnd).dateStart(dateStart).build();

    when(repository.packetByRfcAndDateEndAndDateStart(
        anyString(), any(ZonedDateTime.class), any(ZonedDateTime.class))
    ).thenReturn(SatPacket.builder()
        .rfc(RFC_TEST)
        .requestId(requestId)
        .timesRequested(1)
        .packetId(satPacketId)
        .state(IN_PROGRESS.name())
        .lastRequested(ZonedDateTime.now().minusMinutes(1)).build()
    );

    when(repository.tokenByRfc(anyString())).thenReturn(SatToken.builder()
        .id(1)
        .rfc(RFC_TEST)
        .token("fakeToken")
        .created(ZonedDateTime.now().minusMinutes(3))
        .expiration(ZonedDateTime.now().minusMinutes(5)).build()
    );

    Invoices invoices = service.getReceptorInvoices(request);
    assertThat(invoices.getSatState()).isEqualTo(IN_PROGRESS);
  }

  @Test
  public void shouldValidateRequestBackoff() {
    ZonedDateTime dateEnd = ZonedDateTime.now().plusDays(5);
    ZonedDateTime dateStart = dateEnd.minusDays(5);

    RequestCFDI request = RequestCFDI.builder()
        .rfc(RFC_TEST).dateEnd(dateEnd).dateStart(dateStart).build();

    String requestId = UUID.randomUUID().toString();
    String satPacketId = UUID.randomUUID().toString();

    SatPacket packet = SatPacket.builder()
        .rfc(RFC_TEST)
        .timesRequested(6)
        .requestId(requestId)
        .packetId(satPacketId)
        .state(IN_PROGRESS.name())
        .lastRequested(ZonedDateTime.now().plusHours(2)).build();

    when(repository.packetByRfcAndDateEndAndDateStart(
        anyString(), any(ZonedDateTime.class), any(ZonedDateTime.class))
    ).thenReturn(packet);
    when(repository.savePacket(any())).thenReturn(packet.toBuilder().message("Accepted").build());
    when(repository.tokenByRfc(anyString())).thenReturn(SatToken.builder()
        .id(1)
        .rfc(RFC_TEST)
        .token("fakeToken")
        .created(ZonedDateTime.now().minusMinutes(3))
        .expiration(ZonedDateTime.now().minusMinutes(5)).build()
    );

    Invoices invoices = service.getReceptorInvoices(request);

    assertThat(invoices.getSatState()).isEqualTo(IN_PROGRESS);
    assertThat(invoices.getMessage()).matches(BACKOFF_PATTERN);

  }

  @Test
  public void shouldReturnReady() {
    ZonedDateTime now = ZonedDateTime.now();
    ZonedDateTime dateEnd = now.plusDays(5);
    ZonedDateTime dateStart = dateEnd.minusDays(5);

    RequestCFDI request = RequestCFDI.builder()
        .rfc(RFC_TEST).dateEnd(dateEnd).dateStart(dateStart).build();

    String requestId = UUID.randomUUID().toString();
    String satPacketId = UUID.randomUUID().toString();
    ValidateResponse validation = mockValidateResult(satPacketId, IN_PROGRESS);
    DownloadResponse downloadResponse = new DownloadResponse().paquete(extractFile("demo.zip"));

    SatPacket satPacket = SatPacket.builder()
        .rfc(RFC_TEST)
        .timesRequested(1)
        .requestId(requestId)
        .packetId(satPacketId)
        .state(READY.name()).build();

    SatPacket updated = satPacket.toBuilder()
        .path(String.format("./zip/%s_%s.zip", RFC_TEST, satPacketId)).build();

    when(repository.packetByRfcAndDateEndAndDateStart(
        anyString(), any(ZonedDateTime.class), any(ZonedDateTime.class))
    ).thenReturn(satPacket);
    when(validateRequestService.getValidation(anyString(), anyString())).thenReturn(validation);
    when(downloadCFDIService.getDownload(anyString(), anyString())).thenReturn(downloadResponse);
    when(repository.savePacket(any(SatPacket.class))).thenReturn(updated);
    when(repository.tokenByRfc(anyString())).thenReturn(SatToken.builder()
        .id(1)
        .token("fakeToken")
        .rfc(RFC_TEST)
        .expiration(now.minusMinutes(5))
        .created(ZonedDateTime.now().minusMinutes(3)).build()
    );

    Invoices invoices = service.getReceptorInvoices(request);
    assertThat(invoices.getSatState()).isEqualTo(READY);

    Invoice invoice = invoices.getInvoices().get(0);
    assertThat(invoice.folio()).isEqualTo("2052571552");
    assertThat(invoice.issuer().rfc()).isEqualTo("BBA830831LJ2");
    assertThat(invoice.receptor().rfc()).isEqualTo("XXXX8503016C3");
    assertThat(invoice.concepts().getConcept().get(0).getAmount()).isEqualTo(1.0);
    assertThat(invoice.concepts().getConcept().get(0).getServiceCode()).isEqualTo("92356500");
  }

  @Test
  public void shouldReturnHappyPath() {
    ZonedDateTime now = ZonedDateTime.now();
    ZonedDateTime dateEnd = now.plusDays(5);
    ZonedDateTime dateStart = dateEnd.minusDays(5);

    RequestCFDI request = RequestCFDI.builder()
        .rfc(RFC_TEST).dateEnd(dateEnd).dateStart(dateStart).build();

    String requestId = UUID.randomUUID().toString();
    String satPacketId = UUID.randomUUID().toString();

    ValidateResponse validation = mockValidateResult(satPacketId, IN_PROGRESS);
    DownloadResponse downloadResponse = new DownloadResponse().paquete(extractFile("demo.zip"));

    when(validateRequestService.getValidation(anyString(), anyString())).thenReturn(validation);
    when(downloadCFDIService.getDownload(anyString(), anyString())).thenReturn(downloadResponse);

    SatPacket satPacket = SatPacket.builder()
        .rfc(RFC_TEST)
        .timesRequested(1)
        .requestId(requestId)
        .packetId(satPacketId)
        .state(READY.name()).build();

    when(repository.packetByRfcAndDateEndAndDateStart(
        anyString(), any(ZonedDateTime.class), any(ZonedDateTime.class))
    ).thenReturn(null);

    SatPacket updated = satPacket.toBuilder()
        .path(String.format("./zip/%s_%s.zip", RFC_TEST, satPacketId)).build();

    when(repository.savePacket(any(SatPacket.class))).thenReturn(updated);
    when(repository.tokenByRfc(anyString())).thenReturn(SatToken.builder()
        .id(1)
        .token("fakeToken")
        .rfc(RFC_TEST)
        .expiration(now.minusMinutes(5))
        .created(ZonedDateTime.now().minusMinutes(3)).build()
    );

    Invoices invoices = service.getReceptorInvoices(request);
    assertThat(invoices.getSatState()).isEqualTo(READY);

    Invoice invoice = invoices.getInvoices().get(0);
    assertThat(invoice.folio()).isEqualTo("2052571552");
    assertThat(invoice.issuer().rfc()).isEqualTo("BBA830831LJ2");
    assertThat(invoice.receptor().rfc()).isEqualTo("XXXX8503016C3");
    assertThat(invoice.concepts().getConcept().get(0).getAmount()).isEqualTo(1.0);
    assertThat(invoice.concepts().getConcept().get(0).getServiceCode()).isEqualTo("92356500");
  }

  @Test
  public void shouldReturnInvoicesFromExistentPackage() throws Exception {

    ZonedDateTime dateEnd = ZonedDateTime.now().plusDays(5);
    ZonedDateTime dateStart = dateEnd.minusDays(5);

    RequestCFDI request = RequestCFDI.builder()
        .rfc(RFC_TEST).dateEnd(dateEnd).dateStart(dateStart).build();

    String requestId = "MOCKED";
    String satPacketId = "MOCKED_PID";
    String satPacketContent = zipAsBase64(loadResourceAsFile("RESPUESTA-DESCARGA.xml"));

    DownloadResponse downloadResponse = new DownloadResponse().paquete(satPacketContent);
    when(downloadCFDIService.getDownload(anyString(), anyString())).thenReturn(downloadResponse);

    Path zipPath = Path.of("./zip", RFC_TEST + "_" + satPacketId + ".zip");
    SatPacket satPacket = SatPacket.builder()
        .rfc(RFC_TEST)
        .packetId(satPacketId)
        .state(READY.name())
        .path(zipPath.toString()).build();

    when(repository.packetByRfcAndDateEndAndDateStart(
        anyString(), any(ZonedDateTime.class), any(ZonedDateTime.class))
    ).thenReturn(SatPacket.builder()
        .rfc(RFC_TEST)
        .requestId(requestId)
        .timesRequested(1)
        .packetId(satPacketId)
        .state(READY.name())
        .lastRequested(ZonedDateTime.now().minusMinutes(1)).build()
    );

    when(repository.tokenByRfc(anyString())).thenReturn(SatToken.builder()
        .id(1)
        .rfc(RFC_TEST)
        .token("fakeToken")
        .created(ZonedDateTime.now().minusMinutes(3))
        .expiration(ZonedDateTime.now().minusMinutes(5)).build()
    );

    when(repository.savePacket(any())).thenReturn(satPacket);

    Invoices invoices = service.getReceptorInvoices(request);

    assertThat(invoices.getInvoices()).hasSize(1);
    assertThat(invoices.getSatState()).isEqualTo(READY);

  }

  private ValidateResponse mockValidateResult(String satPacketId, StateCode stateCode) {
    ValidateResult validationResult = new ValidateResult()
        .IdsPaquetes(Collections.singletonList(satPacketId))
        .state(stateCode)
        .status("5000")
        .cfdiCount(5);

    return new ValidateResponse().result(validationResult);
  }
}
