package com.mikkezavala.sat.service.sat;

import static com.mikkezavala.sat.domain.sat.SoapEndpoint.AUTENTICA;
import static com.mikkezavala.sat.domain.sat.SoapEndpoint.SOLICITA_DESCARGA;
import static com.mikkezavala.sat.domain.sat.SoapEndpoint.VALIDA_DESCARGA;
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
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

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

  private IndividualContributorService service;

  private static String RFC_CUSTOMER = "XOJI740919U48";

  @BeforeEach
  void init() throws Exception {

    File pfxFile = loadResource("PF_CFDI/XOJI740919U48.pfx");
    SatClient satClient = new SatClient();
    satClient.setId(1);
    satClient.setRfc(RFC_CUSTOMER);
    satClient.setKeystore(pfxFile.getPath());
    satClient.setPasswordPlain("12345678a");

    Timestamp ts = new Timestamp();
    ts.setCreated(ZonedDateTime.now());
    ts.setExpires(ts.getCreated().plusDays(2));

    AuthResponse authResponse = new AuthResponse();
    authResponse.setToken("FAKE-TOKEN");
    authResponse.setTimestamp(ts);

    token = SatToken.builder()
        .id(1)
        .token("ejToken")
        .rfc(satClient.getRfc())
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
    service = new IndividualContributorService(
        soapUtil, soapService, satTokenRepository, satPcktRepository
    );
  }

  @Test
  public void shouldReturnInProgress() throws Exception {
    RequestCfdi request = new RequestCfdi();
    String satPacketId = UUID.randomUUID().toString();
    ValidateResponse validation = new ValidateResponse();
    ValidateResult validationResult = new ValidateResult();

    validationResult.setIdsPaquetes(Collections.singletonList(satPacketId));
    validationResult.setState(StateCode.IN_PROGRESS);
    validationResult.setStatus("5000");
    validationResult.setCfdiCount(5);
    validation.setResult(validationResult);

    when(soapUtil.callWebService(
        any(), any(), eq(VALIDA_DESCARGA),
        anyString())
    ).thenReturn(validation);

    when(satPcktRepository.findSatPacketByRfcAndDateEndAndDateStart(
        anyString(), any(ZonedDateTime.class), any(ZonedDateTime.class))
    ).thenReturn(
        SatPacket.builder().packetId(satPacketId).state(StateCode.IN_PROGRESS.name()).build()
    );

    ZonedDateTime dateEnd = ZonedDateTime.now().plusDays(5);
    ZonedDateTime dateStart = dateEnd.minusDays(5);
    request.setRfc(RFC_CUSTOMER);
    request.setDateEnd(dateEnd);
    request.setDateStart(dateStart);

    Invoices invoices = service.getReceptorInvoices(request);
    assertThat(invoices.getSatState()).isEqualTo(StateCode.IN_PROGRESS);
  }

  @Test
  public void shouldReturnReady() throws Exception {
    RequestCfdi request = new RequestCfdi();
    ZonedDateTime now = ZonedDateTime.now();

    String requestId = UUID.randomUUID().toString();
    String satPacketId = UUID.randomUUID().toString();

    ValidateResponse validation = new ValidateResponse();
    ValidateResult validationResult = new ValidateResult();

    validationResult.setIdsPaquetes(Collections.singletonList(satPacketId));
    validationResult.setState(StateCode.READY);
    validationResult.setStatus("5000");
    validationResult.setCfdiCount(5);
    validation.setResult(validationResult);

    when(soapUtil.callWebService(
        any(), any(), eq(VALIDA_DESCARGA),
        anyString())
    ).thenReturn(validation);

    when(satPcktRepository.findSatPacketByRfcAndDateEndAndDateStart(
        anyString(), any(ZonedDateTime.class), any(ZonedDateTime.class))
    ).thenReturn(SatPacket.builder()
        .rfc(RFC_CUSTOMER)
        .timesRequested(1)
        .requestId(requestId)
        .packetId(satPacketId)
        .state(StateCode.READY.name()).build()
    );

    when(satTokenRepository.findFirstByRfc(anyString())).thenReturn(SatToken.builder()
        .id(1)
        .token("fakToken")
        .rfc(RFC_CUSTOMER)
        .expiration(now.minusMinutes(5))
        .created(ZonedDateTime.now().minusMinutes(3)).build()
    );

    ZonedDateTime dateEnd = ZonedDateTime.now().plusDays(5);
    ZonedDateTime dateStart = dateEnd.minusDays(5);
    request.setRfc(RFC_CUSTOMER);
    request.setDateEnd(dateEnd);
    request.setDateStart(dateStart);

    Invoices invoices = service.getReceptorInvoices(request);
    assertThat(invoices.getSatState()).isEqualTo(StateCode.READY);
  }
}
