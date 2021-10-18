package com.mikkezavala.sat.service.sat;

import static com.mikkezavala.sat.domain.sat.SoapEndpoint.AUTENTICA;
import static com.mikkezavala.sat.domain.sat.SoapEndpoint.DESCARGA_MASIVA;
import static com.mikkezavala.sat.domain.sat.SoapEndpoint.SOLICITA_DESCARGA;
import static com.mikkezavala.sat.domain.sat.SoapEndpoint.VALIDA_DESCARGA;
import static com.mikkezavala.sat.domain.sat.cfdi.individual.validate.StateCode.IN_PROGRESS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.when;

import com.mikkezavala.sat.TestBase;
import com.mikkezavala.sat.domain.client.registered.RequestCfdi;
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
import com.mikkezavala.sat.repository.SatClientRepository;
import com.mikkezavala.sat.repository.SatPacketRepository;
import com.mikkezavala.sat.repository.SatTokenRepository;
import com.mikkezavala.sat.service.SoapHandler;
import com.mikkezavala.sat.service.SoapService;
import com.mikkezavala.sat.util.SoapUtil;
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

/**
 * The type Individual contributor service test.
 */
@ExtendWith(SpringExtension.class)
public class IndividualContributorServiceTest extends TestBase {

  @Mock
  private SatTokenRepository satTokenRepository;

  @Mock
  private SatPacketRepository satPcktRepository;

  @Mock
  private SatClientRepository satClientRepository;

  @Mock
  private SoapUtil soapUtil;

  private SatToken token;

  private IndivContributorService service;

  private static final Pattern BACKOFF_PATTERN = Pattern.compile(
      "Backing off validation request\\. Wait time: \\d+ minutes");

  /**
   * Init.
   *
   * @throws Exception the exception
   */
  @BeforeEach
  void init() throws Exception {

    File pfxFile = loadResource("PF_CFDI/" + RFC_TEST + ".pfx");
    SatClient satClient = new SatClient()
        .id(1)
        .rfc(RFC_TEST)
        .keystore(pfxFile.getPath())
        .passwordPlain(RFC_TEST_PASS);

    Timestamp ts = new Timestamp();
    ts.setCreated(ZonedDateTime.now());
    ts.setExpires(ts.getCreated().plusDays(2));

    AuthResponse authResponse = new AuthResponse();
    authResponse.setToken("FAKE-TOKEN");
    authResponse.setTimestamp(ts);

    token = SatToken.builder()
        .id(1)
        .token("ejToken")
        .rfc(satClient.rfc())
        .created(ts.getCreated())
        .expiration(ts.getExpires()).build();

    Response response = new Response();
    Result result = new Result();
    result.setStatus("3");
    result.setRequestId(UUID.randomUUID().toString());
    response.setResult(result);

    SatPacket packet = SatPacket.builder().packetId(UUID.randomUUID().toString())
        .lastRequested(ZonedDateTime.now()).state("IN_PROGRESS").build();

    when(soapUtil.callWebService(
        any(), any(), eq(AUTENTICA), nullable(String.class))
    ).thenReturn(authResponse);

    when(soapUtil.callWebService(
        any(), any(), eq(SOLICITA_DESCARGA), eq(token.getToken()))
    ).thenReturn(response);

    when(satTokenRepository.save(any(SatToken.class))).thenReturn(token);
    when(satPcktRepository.save(any(SatPacket.class))).thenReturn(packet);
    when(satClientRepository.findSatClientByRfc(anyString())).thenReturn(satClient);

    SoapHandler handler = new SoapHandler(
        getMessageFactory(), getSoapFactory(), getBuilderFactory()
    );

    SoapService soapService = new SoapService(handler, satClientRepository);
    service = new IndivContributorService(
        soapUtil, soapService, satTokenRepository, satPcktRepository
    );
  }

  /**
   * Should return in progress.
   *
   * @throws Exception the exception
   */
  @Test
  public void shouldReturnInProgress() throws Exception {
    RequestCfdi request = new RequestCfdi();
    String satPacketId = UUID.randomUUID().toString();
    ValidateResponse validation = mockValidateResult(satPacketId, IN_PROGRESS);

    when(soapUtil.callWebService(
        any(), any(), eq(VALIDA_DESCARGA),
        anyString())
    ).thenReturn(validation);

    when(satPcktRepository.findSatPacketByRfcAndDateEndAndDateStart(
        anyString(), any(ZonedDateTime.class), any(ZonedDateTime.class))
    ).thenReturn(
        SatPacket.builder().packetId(satPacketId).state(IN_PROGRESS.name()).build()
    );

    ZonedDateTime dateEnd = ZonedDateTime.now().plusDays(5);
    ZonedDateTime dateStart = dateEnd.minusDays(5);
    request.setRfc(RFC_TEST);
    request.setDateEnd(dateEnd);
    request.setDateStart(dateStart);

    Invoices invoices = service.getReceptorInvoices(request);
    assertThat(invoices.getSatState()).isEqualTo(IN_PROGRESS);
  }

  /**
   * Should validate request.
   */
  @Test
  public void shouldValidateRequest() {
    RequestCfdi request = new RequestCfdi();
    String requestId = UUID.randomUUID().toString();
    String satPacketId = UUID.randomUUID().toString();

    when(satPcktRepository.findSatPacketByRfcAndDateEndAndDateStart(
        anyString(), any(ZonedDateTime.class), any(ZonedDateTime.class))
    ).thenReturn(SatPacket.builder()
        .rfc(RFC_TEST)
        .requestId(requestId)
        .timesRequested(1)
        .packetId(satPacketId)
        .state(IN_PROGRESS.name())
        .lastRequested(ZonedDateTime.now().minusMinutes(1)).build()
    );

    when(satTokenRepository.findFirstByRfc(anyString())).thenReturn(SatToken.builder()
        .id(1)
        .rfc(RFC_TEST)
        .token("fakeToken")
        .created(ZonedDateTime.now().minusMinutes(3))
        .expiration(ZonedDateTime.now().minusMinutes(5)).build()
    );

    ZonedDateTime dateEnd = ZonedDateTime.now().plusDays(5);
    ZonedDateTime dateStart = dateEnd.minusDays(5);
    request.setRfc(RFC_TEST);
    request.setDateEnd(dateEnd);
    request.setDateStart(dateStart);

    Invoices invoices = service.getReceptorInvoices(request);
    assertThat(invoices.getSatState()).isEqualTo(IN_PROGRESS);
  }

  /**
   * Should validate request backoff.
   *
   * @throws Exception the exception
   */
  @Test
  public void shouldValidateRequestBackoff() throws Exception {
    RequestCfdi request = new RequestCfdi();
    String requestId = UUID.randomUUID().toString();
    String satPacketId = UUID.randomUUID().toString();
    ValidateResponse validation = mockValidateResult(satPacketId, IN_PROGRESS);

    SatPacket packet = SatPacket.builder()
        .rfc(RFC_TEST)
        .timesRequested(6)
        .requestId(requestId)
        .packetId(satPacketId)
        .state(IN_PROGRESS.name())
        .lastRequested(ZonedDateTime.now().plusHours(2)).build();

    when(soapUtil.callWebService(
        any(), any(), eq(VALIDA_DESCARGA),
        anyString())
    ).thenReturn(validation);

    when(soapUtil.callWebService(
        any(), any(), eq(VALIDA_DESCARGA),
        anyString())
    ).thenReturn(validation);

    when(satPcktRepository.findSatPacketByRfcAndDateEndAndDateStart(
        anyString(), any(ZonedDateTime.class), any(ZonedDateTime.class))
    ).thenReturn(packet);

    when(satPcktRepository.save(any())).thenReturn(packet.toBuilder().message("Accepted").build());

    when(satTokenRepository.findFirstByRfc(anyString())).thenReturn(SatToken.builder()
        .id(1)
        .rfc(RFC_TEST)
        .token("fakeToken")
        .created(ZonedDateTime.now().minusMinutes(3))
        .expiration(ZonedDateTime.now().minusMinutes(5)).build()
    );

    ZonedDateTime dateEnd = ZonedDateTime.now().plusDays(5);
    ZonedDateTime dateStart = dateEnd.minusDays(5);
    request.setRfc(RFC_TEST);
    request.setDateEnd(dateEnd);
    request.setDateStart(dateStart);

    Invoices invoices = service.getReceptorInvoices(request);

    assertThat(invoices.getSatState()).isEqualTo(IN_PROGRESS);
    assertThat(invoices.getMessage()).matches(BACKOFF_PATTERN);

  }

  /**
   * Should return ready.
   *
   * @throws Exception the exception
   */
  @Test
  public void shouldReturnReady() throws Exception {
    RequestCfdi request = new RequestCfdi();
    ZonedDateTime now = ZonedDateTime.now();

    String requestId = UUID.randomUUID().toString();
    String satPacketId = UUID.randomUUID().toString();
    ValidateResponse validation = mockValidateResult(satPacketId, IN_PROGRESS);
    DownloadResponse downloadResponse = new DownloadResponse().paquete(extractFile("demo.zip"));

    SatPacket satPacket = SatPacket.builder()
        .rfc(RFC_TEST)
        .timesRequested(1)
        .requestId(requestId)
        .packetId(satPacketId)
        .state(StateCode.READY.name()).build();

    when(soapUtil.callWebService(
        any(), any(), eq(VALIDA_DESCARGA),
        anyString())
    ).thenReturn(validation);

    when(satPcktRepository.findSatPacketByRfcAndDateEndAndDateStart(
        anyString(), any(ZonedDateTime.class), any(ZonedDateTime.class))
    ).thenReturn(satPacket);

    when(soapUtil.callWebService(
        any(), any(), eq(DESCARGA_MASIVA), anyString()
    )).thenReturn(downloadResponse);

    SatPacket updated = satPacket.toBuilder()
        .path(String.format("./zip/%s_%s.zip", RFC_TEST, satPacketId)).build();

    when(satPcktRepository.save(any(SatPacket.class))).thenReturn(updated);
    when(satTokenRepository.findFirstByRfc(anyString())).thenReturn(SatToken.builder()
        .id(1)
        .token("fakeToken")
        .rfc(RFC_TEST)
        .expiration(now.minusMinutes(5))
        .created(ZonedDateTime.now().minusMinutes(3)).build()
    );

    ZonedDateTime dateEnd = ZonedDateTime.now().plusDays(5);
    ZonedDateTime dateStart = dateEnd.minusDays(5);
    request.setRfc(RFC_TEST);
    request.setDateEnd(dateEnd);
    request.setDateStart(dateStart);

    Invoices invoices = service.getReceptorInvoices(request);
    assertThat(invoices.getSatState()).isEqualTo(StateCode.READY);

    Invoice invoice = invoices.getInvoices().get(0);
    assertThat(invoice.folio()).isEqualTo("2052571552");
    assertThat(invoice.issuer().rfc()).isEqualTo("BBA830831LJ2");
    assertThat(invoice.receptor().rfc()).isEqualTo("XXXX8503016C3");
    assertThat(invoice.concepts().getConcept().get(0).getAmount()).isEqualTo(1.0);
    assertThat(invoice.concepts().getConcept().get(0).getServiceCode()).isEqualTo("92356500");
  }

  /**
   * Should return happy path.
   *
   * @throws Exception the exception
   */
  @Test
  public void shouldReturnHappyPath() throws Exception {
    RequestCfdi request = new RequestCfdi();
    ZonedDateTime now = ZonedDateTime.now();

    String requestId = UUID.randomUUID().toString();
    String satPacketId = UUID.randomUUID().toString();

    ValidateResponse validation = mockValidateResult(satPacketId, IN_PROGRESS);
    DownloadResponse downloadResponse = new DownloadResponse().paquete(extractFile("demo.zip"));

    SatPacket satPacket = SatPacket.builder()
        .rfc(RFC_TEST)
        .timesRequested(1)
        .requestId(requestId)
        .packetId(satPacketId)
        .state(StateCode.READY.name()).build();

    when(soapUtil.callWebService(
        any(), any(), eq(VALIDA_DESCARGA),
        anyString())
    ).thenReturn(validation);

    when(satPcktRepository.findSatPacketByRfcAndDateEndAndDateStart(
        anyString(), any(ZonedDateTime.class), any(ZonedDateTime.class))
    ).thenReturn(null);

    when(soapUtil.callWebService(
        any(), any(), eq(DESCARGA_MASIVA), anyString()
    )).thenReturn(downloadResponse);

    SatPacket updated = satPacket.toBuilder()
        .path(String.format("./zip/%s_%s.zip", RFC_TEST, satPacketId)).build();

    when(satPcktRepository.save(any(SatPacket.class))).thenReturn(updated);
    when(satTokenRepository.findFirstByRfc(anyString())).thenReturn(SatToken.builder()
        .id(1)
        .token("fakeToken")
        .rfc(RFC_TEST)
        .expiration(now.minusMinutes(5))
        .created(ZonedDateTime.now().minusMinutes(3)).build()
    );

    ZonedDateTime dateEnd = ZonedDateTime.now().plusDays(5);
    ZonedDateTime dateStart = dateEnd.minusDays(5);
    request.setRfc(RFC_TEST);
    request.setDateEnd(dateEnd);
    request.setDateStart(dateStart);

    Invoices invoices = service.getReceptorInvoices(request);
    assertThat(invoices.getSatState()).isEqualTo(StateCode.READY);

    Invoice invoice = invoices.getInvoices().get(0);
    assertThat(invoice.folio()).isEqualTo("2052571552");
    assertThat(invoice.issuer().rfc()).isEqualTo("BBA830831LJ2");
    assertThat(invoice.receptor().rfc()).isEqualTo("XXXX8503016C3");
    assertThat(invoice.concepts().getConcept().get(0).getAmount()).isEqualTo(1.0);
    assertThat(invoice.concepts().getConcept().get(0).getServiceCode()).isEqualTo("92356500");
  }

  /**
   * Should return invoices from existent package.
   *
   * @throws Exception the exception
   */
  @Test
  public void shouldReturnInvoicesFromExistentPackage() throws Exception {
    RequestCfdi request = new RequestCfdi();
    String requestId = "MOCKED";
    String satPacketId = "MOCKED_PID";
    String satPacketContent = zipAsBase64(loadResource("RESPUESTA-DESCARGA.xml"));
    DownloadResponse downloadResponse = new DownloadResponse().paquete(satPacketContent);

    ZonedDateTime dateEnd = ZonedDateTime.now().plusDays(5);
    ZonedDateTime dateStart = dateEnd.minusDays(5);

    Path zipPath = Path.of("./zip", RFC_TEST + "_" + satPacketId + ".zip");
    SatPacket satPacket = SatPacket.builder()
        .rfc(RFC_TEST)
        .packetId(satPacketId)
        .state(StateCode.READY.name())
        .path(zipPath.toString()).build();

    when(soapUtil.callWebService(
        any(), any(), eq(VALIDA_DESCARGA), nullable(String.class))
    ).thenReturn(mockValidateResult(satPacketId, IN_PROGRESS));

    when(soapUtil.callWebService(
        any(), any(), eq(DESCARGA_MASIVA), nullable(String.class))
    ).thenReturn(downloadResponse);

    when(satPcktRepository.findSatPacketByRfcAndDateEndAndDateStart(
        anyString(), any(ZonedDateTime.class), any(ZonedDateTime.class))
    ).thenReturn(SatPacket.builder()
        .rfc(RFC_TEST)
        .requestId(requestId)
        .timesRequested(1)
        .packetId(satPacketId)
        .state(IN_PROGRESS.name())
        .lastRequested(ZonedDateTime.now().minusMinutes(1)).build()
    );

    when(satTokenRepository.findFirstByRfc(anyString())).thenReturn(SatToken.builder()
        .id(1)
        .rfc(RFC_TEST)
        .token("fakeToken")
        .created(ZonedDateTime.now().minusMinutes(3))
        .expiration(ZonedDateTime.now().minusMinutes(5)).build()
    );

    when(satPcktRepository.save(any())).thenReturn(satPacket);

    request.setRfc(RFC_TEST);
    request.setDateEnd(dateEnd);
    request.setDateStart(dateStart);

    Invoices invoices = service.getReceptorInvoices(request);

    assertThat(invoices.getInvoices()).hasSize(1);
    assertThat(invoices.getSatState()).isEqualTo(StateCode.READY);

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
