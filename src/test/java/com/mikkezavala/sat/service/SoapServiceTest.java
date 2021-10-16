package com.mikkezavala.sat.service;

import static com.mikkezavala.sat.util.Constant.DEFAULT_REQUEST_TYPE;
import static com.mikkezavala.sat.util.Constant.FORMATTER;
import static com.mikkezavala.sat.util.Constant.WSS_SEC_EXT_NS;
import static com.mikkezavala.sat.util.Constant.WSS_UTILITY_NS;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.xmlunit.assertj3.XmlAssert.assertThat;

import com.mikkezavala.sat.TestBase;
import com.mikkezavala.sat.domain.sat.cfdi.individual.SatClient;
import com.mikkezavala.sat.exception.SoapSecurityException;
import com.mikkezavala.sat.exception.SoapServiceException;
import com.mikkezavala.sat.repository.SatClientRepository;
import java.io.File;
import java.io.FileNotFoundException;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.UUID;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * The type Soap service test.
 */
@ExtendWith(SpringExtension.class)
public class SoapServiceTest extends TestBase {

  @Mock
  private SatClientRepository mockRepository;

  private SoapHandler handler;

  private SoapService service;

  /**
   * Init.
   *
   * @throws FileNotFoundException the file not found exception
   */
  @BeforeEach
  void init() throws FileNotFoundException {

    File pfxFile = loadResource("PF_CFDI/" + RFC_TEST + ".pfx");
    SatClient satClient = new SatClient()
        .id(1)
        .rfc(RFC_TEST)
        .keystore(pfxFile.getPath())
        .passwordPlain(RFC_TEST_PASS);

    handler = spy(new SoapHandler(
        getMessageFactory(), getSoapFactory(), getBuilderFactory()
    ));

    service = new SoapService(handler, mockRepository);
    when(mockRepository.findSatClientByRfc(anyString())).thenReturn(satClient);
  }

  /**
   * Should create soap auth request.
   *
   * @throws Exception the exception
   */
  @Test
  public void shouldCreateSoapAuthRequest() throws Exception {

    Map<String, String> context = getNSContext();
    context.put("o", WSS_SEC_EXT_NS);
    context.put("u", WSS_UTILITY_NS);

    SOAPMessage message = service.auth(RFC_TEST);

    String xml = soapToString(message);
    assertThat(xml).withNamespaceContext(context).hasXPath("//s:Envelope");
    assertThat(xml).withNamespaceContext(context).hasXPath("//s:Envelope/s:Header");
    assertThat(xml).withNamespaceContext(context).hasXPath("//s:Envelope/s:Header/o:Security");
    assertThat(xml).withNamespaceContext(context)
        .valueByXPath("//s:Envelope/s:Header/o:Security/o:BinarySecurityToken").isNotEmpty();

    assertThat(xml).withNamespaceContext(context)
        .hasXPath("//s:Envelope/s:Header/o:Security/u:Timestamp");
    assertThat(xml).withNamespaceContext(context)
        .valueByXPath("//s:Envelope/s:Header/o:Security/u:Timestamp/u:Created").isNotEmpty();
    assertThat(xml).withNamespaceContext(context)
        .valueByXPath("//s:Envelope/s:Header/o:Security/u:Timestamp/u:Expires").isNotEmpty();

    assertThat(xml).withNamespaceContext(context).hasXPath("//s:Envelope/s:Body").anyMatch(n ->
        n.getFirstChild().getLocalName().equals("Autentica")
    );
  }

  /**
   * Should return soap service exception when auth message invalid.
   *
   * @throws Exception the exception
   */
  @Test
  public void shouldReturnSoapServiceExceptionWhenAuthMessageInvalid() throws Exception {
    when(handler.createEnvelope()).thenThrow(SOAPException.class);
    assertThrows(SoapServiceException.class, () -> service.auth(RFC_TEST));
  }

  /**
   * Should return soap service exception when request message invalid.
   *
   * @throws Exception the exception
   */
  @Test
  public void shouldReturnSoapServiceExceptionWhenRequestMessageInvalid() throws Exception {
    when(handler.createEnvelope()).thenThrow(SOAPException.class);
    assertThrows(SoapServiceException.class, () ->
        service.request(RFC_TEST, ZonedDateTime.now(), ZonedDateTime.now().plusDays(11))
    );
  }

  /**
   * Should return soap service exception when validate message invalid.
   *
   * @throws Exception the exception
   */
  @Test
  public void shouldReturnSoapServiceExceptionWhenValidateMessageInvalid() throws Exception {
    when(handler.createEnvelope()).thenThrow(SOAPException.class);
    assertThrows(SoapServiceException.class, () ->
        service.validation(UUID.randomUUID().toString(), RFC_TEST)
    );
  }

  /**
   * Should return soap service exception when download message invalid.
   *
   * @throws Exception the exception
   */
  @Test
  public void shouldReturnSoapServiceExceptionWhenDownloadMessageInvalid() throws Exception {
    when(handler.createEnvelope()).thenThrow(SOAPException.class);
    assertThrows(SoapServiceException.class, () ->
        service.download(UUID.randomUUID().toString(), RFC_TEST)
    );
  }

  /**
   * Should return key store exception when incorrect pass.
   *
   * @throws Exception the exception
   */
  @Test
  public void shouldReturnKeyStoreExceptionWhenIncorrectPass() throws Exception {

    File pfxFile = loadResource("PF_CFDI/" + RFC_TEST + ".pfx");
    SatClient satClient = new SatClient()
        .id(1)
        .rfc(RFC_TEST)
        .keystore(pfxFile.getPath())
        .passwordPlain("55555");

    when(mockRepository.findSatClientByRfc(anyString())).thenReturn(satClient);

    Throwable throwable = assertThrows(SoapSecurityException.class, () ->
        service.auth(RFC_TEST)
    );

    assertThat(throwable.getMessage()).isEqualTo("Unable to decode certificate");
  }

  /**
   * Should return key store exception when no key store.
   */
  @Test
  public void shouldReturnKeyStoreExceptionWhenNoKeyStore() {

    when(mockRepository.findSatClientByRfc(anyString())).thenThrow(new RuntimeException());

    Throwable throwable = assertThrows(SoapSecurityException.class, () ->
        service.validation(UUID.randomUUID().toString(), RFC_TEST)
    );

    assertThat(throwable.getMessage()).isEqualTo("Unable to create detached signature");
  }

  /**
   * Should return certificate encoding exception when cert failed.
   */
  @Test
  public void shouldReturnCertificateEncodingExceptionWhenCertFailed() {

    when(mockRepository.findSatClientByRfc(anyString())).thenThrow(new RuntimeException());

    Throwable throwable = assertThrows(SoapSecurityException.class, () ->
        service.auth(RFC_TEST)
    );

    assertThat(throwable.getMessage()).isEqualTo("Unable to decode certificate");
  }

  /**
   * Should create download request.
   *
   * @throws Exception the exception
   */
  @Test
  public void shouldCreateDownloadRequest() throws Exception {

    Map<String, String> context = getNSContext();
    ZonedDateTime startDateDate = ZonedDateTime.now();
    ZonedDateTime endDate = ZonedDateTime.now().plusDays(2);

    SOAPMessage message = service.request(RFC_TEST, startDateDate, endDate);

    String xml = soapToString(message);
    assertThat(xml).withNamespaceContext(context).hasXPath("//s:Envelope");
    assertThat(xml).withNamespaceContext(context).hasXPath(
        "//s:Envelope/s:Body/*[local-name()='SolicitaDescarga']/*[local-name()='solicitud']/*[local-name()='Signature']"
    );
    assertThat(xml).withNamespaceContext(context).valueByXPath(
        "//s:Envelope/s:Body/*[local-name()='SolicitaDescarga']/*[local-name()='solicitud']/*[local-name()='Signature']"
    ).isNotEmpty();
    assertThat(xml).withNamespaceContext(context).valueByXPath(
        "//s:Envelope/s:Body/*[local-name()='SolicitaDescarga']/*[local-name()='solicitud']/@FechaInicial"
    ).isEqualTo(startDateDate.format(FORMATTER));
    assertThat(xml).withNamespaceContext(context).valueByXPath(
        "//s:Envelope/s:Body/*[local-name()='SolicitaDescarga']/*[local-name()='solicitud']/@FechaFinal"
    ).isEqualTo(endDate.format(FORMATTER));
    assertThat(xml).withNamespaceContext(context).valueByXPath(
        "//s:Envelope/s:Body/*[local-name()='SolicitaDescarga']/*[local-name()='solicitud']/@RfcReceptor"
    ).isEqualTo(RFC_TEST);
    assertThat(xml).withNamespaceContext(context).valueByXPath(
        "//s:Envelope/s:Body/*[local-name()='SolicitaDescarga']/*[local-name()='solicitud']/@RfcSolicitante"
    ).isEqualTo(RFC_TEST);
    assertThat(xml).withNamespaceContext(context).valueByXPath(
        "//s:Envelope/s:Body/*[local-name()='SolicitaDescarga']/*[local-name()='solicitud']/@TipoSolicitud"
    ).isEqualTo(DEFAULT_REQUEST_TYPE);
  }

  /**
   * Should create download validation.
   *
   * @throws Exception the exception
   */
  @Test
  public void shouldCreateDownloadValidation() throws Exception {

    Map<String, String> context = getNSContext();

    String uuid = UUID.randomUUID().toString();
    SOAPMessage message = service.validation(uuid, RFC_TEST);

    String xml = soapToString(message);
    assertThat(xml).withNamespaceContext(context).hasXPath("//s:Envelope");
    assertThat(xml).withNamespaceContext(context).hasXPath(
        "//s:Envelope/s:Body/*[local-name()='VerificaSolicitudDescarga']/*[local-name()='solicitud']"
    );
    assertThat(xml).withNamespaceContext(context).valueByXPath(
        "//s:Envelope/s:Body/*[local-name()='VerificaSolicitudDescarga']/*[local-name()='solicitud']/*[local-name()='Signature']"
    ).isNotEmpty();
    assertThat(xml).withNamespaceContext(context).valueByXPath(
        "//s:Envelope/s:Body/*[local-name()='VerificaSolicitudDescarga']/*[local-name()='solicitud']/@IdSolicitud"
    ).isEqualTo(uuid);
    assertThat(xml).withNamespaceContext(context).valueByXPath(
        "//s:Envelope/s:Body/*[local-name()='VerificaSolicitudDescarga']/*[local-name()='solicitud']/@RfcSolicitante"
    ).isEqualTo(RFC_TEST);
  }

  /**
   * Should create download.
   *
   * @throws Exception the exception
   */
  @Test
  public void shouldCreateDownload() throws Exception {

    Map<String, String> context = getNSContext();

    String uuid = UUID.randomUUID().toString();
    SOAPMessage message = service.download(uuid, RFC_TEST);

    String xml = soapToString(message);
    assertThat(xml).withNamespaceContext(context).hasXPath("//s:Envelope");
    assertThat(xml).withNamespaceContext(context).hasXPath(
        "//s:Envelope/s:Body/*[local-name()='PeticionDescargaMasivaTercerosEntrada']/*[local-name()='peticionDescarga']"
    );
    assertThat(xml).withNamespaceContext(context).valueByXPath(
        "//s:Envelope/s:Body/*[local-name()='PeticionDescargaMasivaTercerosEntrada']/*[local-name()='peticionDescarga']/*[local-name()='Signature']"
    ).isNotEmpty();
    assertThat(xml).withNamespaceContext(context).valueByXPath(
        "//s:Envelope/s:Body/*[local-name()='PeticionDescargaMasivaTercerosEntrada']/*[local-name()='peticionDescarga']/@IdPaquete"
    ).isEqualTo(uuid);
    assertThat(xml).withNamespaceContext(context).valueByXPath(
        "//s:Envelope/s:Body/*[local-name()='PeticionDescargaMasivaTercerosEntrada']/*[local-name()='peticionDescarga']/@RfcSolicitante"
    ).isEqualTo(RFC_TEST);
  }
}
