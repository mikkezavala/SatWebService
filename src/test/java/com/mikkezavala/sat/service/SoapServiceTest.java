package com.mikkezavala.sat.service;

import static com.mikkezavala.sat.util.Constant.FORMATTER;
import static com.mikkezavala.sat.util.Constant.WSS_SEC_EXT_NS;
import static com.mikkezavala.sat.util.Constant.WSS_UTILITY_NS;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.xmlunit.assertj3.XmlAssert.assertThat;

import com.mikkezavala.sat.TestBase;
import com.mikkezavala.sat.domain.sat.cfdi.individual.SatClient;
import com.mikkezavala.sat.repository.SatClientRepository;
import java.io.File;
import java.io.FileNotFoundException;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.UUID;
import javax.xml.soap.SOAPMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * The type Soap service test.
 */
@ExtendWith(SpringExtension.class)
public class SoapServiceTest extends TestBase {

  @Mock
  private SatClientRepository mockRepository;

  @InjectMocks
  private SoapService service;

  /**
   * Init.
   *
   * @throws FileNotFoundException the file not found exception
   */
  @BeforeEach
  void init() throws FileNotFoundException {

    File pfxFile = loadResource("PF_CFDI/XOJI740919U48.pfx");
    SatClient satClient = new SatClient();
    satClient.setId(1);
    satClient.setRfc("XOJI740919U48");
    satClient.setKeystore(pfxFile.getPath());
    satClient.setPasswordPlain("12345678a");

    SoapHandler handler = new SoapHandler(
        getMessageFactory(), getSoapFactory(), getBuilderFactory()
    );

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

    SOAPMessage message = service.autentica("XOJI740919U48");

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

  @Test
  public void shouldCreateDownloadRequest() throws Exception {

    Map<String, String> context = getNSContext();
    ZonedDateTime startDateDate = ZonedDateTime.now();
    ZonedDateTime endDate = ZonedDateTime.now().plusDays(2);

    SOAPMessage message = service.solicita("XOJI740919U48", startDateDate, endDate);

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
    ).isEqualTo("XOJI740919U48");
    assertThat(xml).withNamespaceContext(context).valueByXPath(
        "//s:Envelope/s:Body/*[local-name()='SolicitaDescarga']/*[local-name()='solicitud']/@RfcSolicitante"
    ).isEqualTo("XOJI740919U48");
    assertThat(xml).withNamespaceContext(context).valueByXPath(
        "//s:Envelope/s:Body/*[local-name()='SolicitaDescarga']/*[local-name()='solicitud']/@TipoSolicitud"
    ).isEqualTo("CFDI");
  }

  @Test
  public void shouldCreateDownloadValidation() throws Exception {

    Map<String, String> context = getNSContext();

    String uuid = UUID.randomUUID().toString();
    SOAPMessage message = service.valida(uuid, "XOJI740919U48");

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
    ).isEqualTo("XOJI740919U48");

  }
}
