package com.mikkezavala.sat.interceptor;

import static com.mikkezavala.sat.domain.sat.SoapEndpoint.SOLICITA_DESCARGA;
import static com.mikkezavala.sat.template.SatWebServiceTemplate.WS_TEMPLATE;
import static com.mikkezavala.sat.util.Constant.SAT_DESCARGA_MASIVA_NS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import com.mikkezavala.sat.TestBase;
import com.mikkezavala.sat.domain.sat.cfdi.individual.SatClient;
import com.mikkezavala.sat.domain.sat.cfdi.individual.SatToken;
import com.mikkezavala.sat.domain.sat.cfdi.individual.request.Request;
import com.mikkezavala.sat.domain.sat.cfdi.individual.request.RequestDownload;
import com.mikkezavala.sat.exception.SoapSecurityException;
import com.mikkezavala.sat.repository.sat.SatRepository;
import com.mikkezavala.sat.template.WSTemplateProps;
import java.io.InputStream;
import java.time.ZonedDateTime;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.w3c.dom.NodeList;


/**
 * The type Sat client interceptor test.
 */
@ExtendWith(SpringExtension.class)
class SatClientInterceptorTest extends TestBase {

  @Mock
  private SatRepository repository;

  @InjectMocks
  private SatClientInterceptor interceptor;

  private final static String XML_FILE = "SOLICITUD-DESCARGA-REQUEST.xml";

  private final SaajSoapMessageFactory FACTORY = new SaajSoapMessageFactory(getMessageFactory());

  /**
   * Should intercept request.
   *
   * @throws Exception the exception
   */
  @Test
  public void shouldInterceptRequest() throws Exception {
    ZonedDateTime now = ZonedDateTime.now();
    ZonedDateTime endTime = now.plusDays(3);
    String pfxFile = loadResourceAsFile("PF_CFDI/" + RFC_TEST + ".p12").getAbsolutePath();

    InputStream source = loadResource(XML_FILE).getInputStream();
    WebServiceMessage payload = FACTORY.createWebServiceMessage(source);

    marshall(new RequestDownload().request(
            new Request().rfcRequest(RFC_TEST).rfcReceptor(RFC_TEST)
        ), payload.getPayloadResult()
    );

    SatToken token = SatToken.builder()
        .id(1)
        .created(now)
        .rfc(RFC_TEST)
        .expiration(endTime)
        .token("jwtToken").build();

    when(repository.tokenByRfc(any())).thenReturn(token);
    when(repository.satClientByRfc(any())).thenReturn(
        new SatClient().rfc(RFC_TEST).passwordPlain(RFC_TEST_PASS).keystore(pfxFile)
    );

    WSTemplateProps props = WSTemplateProps.builder()
        .rfc(RFC_TEST)
        .endpoint(SOLICITA_DESCARGA)
        .qualifiedName(new QName(SAT_DESCARGA_MASIVA_NS, "SolicitaDescarga")).build();

    MessageContext context = new DefaultMessageContext(payload, FACTORY);
    context.setProperty(WS_TEMPLATE, props);

    boolean intercepted = interceptor.handleRequest(context);

    SOAPMessage soapMessage = ((SaajSoapMessage) context.getRequest()).getSaajMessage();
    NodeList nl = soapMessage.getSOAPBody().getElementsByTagNameNS("*", "X509IssuerName");

    assertThat(nl).isNotNull();
    assertThat(intercepted).isTrue();

    assertThat(nl.getLength()).isEqualTo(1);
    assertThat(nl.item(0).getTextContent()).contains("CN=INGRID XODAR JIMENEZ");
  }

  @Test
  public void shouldInterceptRequestAndFailSignature() throws Exception {
    ZonedDateTime now = ZonedDateTime.now();
    ZonedDateTime endTime = now.plusDays(3);
    String pfxFile = loadResourceAsFile("PF_CFDI/" + RFC_TEST + ".p12").getAbsolutePath();

    InputStream source = loadResource(XML_FILE).getInputStream();
    WebServiceMessage payload = FACTORY.createWebServiceMessage(source);

    marshall(new RequestDownload().request(
            new Request().rfcRequest(RFC_TEST).rfcReceptor(RFC_TEST)
        ), payload.getPayloadResult()
    );

    SatToken token = SatToken.builder()
        .id(1)
        .created(now)
        .rfc(RFC_TEST)
        .expiration(endTime)
        .token("jwtToken").build();

    SatClient client = spy(
        new SatClient().rfc(RFC_TEST).passwordPlain(RFC_TEST_PASS).keystore(pfxFile));

    when(client.keystore()).thenThrow(new RuntimeException("Forced"));

    when(repository.tokenByRfc(any())).thenReturn(token);
    when(repository.satClientByRfc(any())).thenReturn(client);

    WSTemplateProps props = WSTemplateProps.builder()
        .rfc(RFC_TEST)
        .endpoint(SOLICITA_DESCARGA)
        .qualifiedName(new QName(SAT_DESCARGA_MASIVA_NS, "SolicitaDescarga")).build();

    MessageContext context = new DefaultMessageContext(payload, FACTORY);
    context.setProperty(WS_TEMPLATE, props);

    Exception exception = assertThrows(SoapSecurityException.class,
        () -> interceptor.handleRequest(context)
    );

    assertThat(exception.getMessage()).isEqualTo("Unable to create detached signature");
  }

}