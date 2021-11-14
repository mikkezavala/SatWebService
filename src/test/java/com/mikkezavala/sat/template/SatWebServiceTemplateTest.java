package com.mikkezavala.sat.template;

import static com.mikkezavala.sat.domain.sat.SoapEndpoint.SOLICITA_DESCARGA;
import static com.mikkezavala.sat.util.Constant.DEFAULT_REQUEST_TYPE;
import static com.mikkezavala.sat.util.Constant.FORMATTER;
import static com.mikkezavala.sat.util.Constant.SAT_DESCARGA_MASIVA_NS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.springframework.ws.test.client.RequestMatchers.connectionTo;
import static org.springframework.ws.test.client.ResponseCreators.withSoapEnvelope;

import com.mikkezavala.sat.TestBase;
import com.mikkezavala.sat.domain.sat.cfdi.individual.download.DownloadResponse;
import com.mikkezavala.sat.domain.sat.cfdi.individual.request.Request;
import com.mikkezavala.sat.domain.sat.cfdi.individual.request.RequestDownload;
import java.io.IOException;
import java.time.ZonedDateTime;
import javax.xml.namespace.QName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.springframework.oxm.Marshaller;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.ws.client.WebServiceClientException;
import org.springframework.ws.client.core.WebServiceMessageCallback;
import org.springframework.ws.client.core.WebServiceMessageExtractor;
import org.springframework.ws.test.client.MockWebServiceServer;

@ExtendWith(SpringExtension.class)
class SatWebServiceTemplateTest extends TestBase {

  @Spy
  private Marshaller marshaller = getTestMarshaller();

  private SatWebServiceTemplate serviceTemplate;

  private MockWebServiceServer mockServer;

  @BeforeEach
  protected void init() {
    this.serviceTemplate = spy(new SatWebServiceTemplate(marshaller));
    this.mockServer = MockWebServiceServer.createServer(serviceTemplate);
  }

  @Test
  public void shouldMarshalSendAndReceiveWithProps() throws IOException {
    ZonedDateTime now = ZonedDateTime.now();
    ZonedDateTime endDate = now.plusDays(2);

    WSTemplateProps props = WSTemplateProps.builder()
        .rfc(RFC_TEST)
        .endpoint(SOLICITA_DESCARGA)
        .qualifiedName(new QName(SAT_DESCARGA_MASIVA_NS, "SolicitaDescarga")).build();

    RequestDownload req = new RequestDownload().request(new Request()
        .rfcRequest(RFC_TEST)
        .rfcReceptor(RFC_TEST)
        .requestType(DEFAULT_REQUEST_TYPE)
        .dateEnd(endDate.format(FORMATTER))
        .dateStart(now.format(FORMATTER))
    );

    mockServer.expect(connectionTo(props.getEndpoint().getEndpoint()))
        .andRespond(withSoapEnvelope(loadResource("RESPUESTA-DESCARGA.xml")));

    DownloadResponse response =
        (DownloadResponse) serviceTemplate.marshalSendAndReceiveWithProps(props, req);

    assertThat(response).isNotNull();
    assertThat(response.paquete()).isNotNull();
  }

  @Test
  @SuppressWarnings("unchecked")
  public void shouldFailWithTransport() throws IOException {

    WSTemplateProps props = WSTemplateProps.builder()
        .rfc(RFC_TEST)
        .endpoint(SOLICITA_DESCARGA)
        .qualifiedName(new QName(SAT_DESCARGA_MASIVA_NS, "SolicitaDescarga")).build();

    when(
        serviceTemplate.getMessageFactory().createWebServiceMessage()
    ).thenThrow(new RuntimeException("Failed to connect"));

    mockServer.expect(connectionTo(props.getEndpoint().getEndpoint()))
        .andRespond(withSoapEnvelope(loadResource("RESPUESTA-DESCARGA.xml")));

    Exception exception = assertThrows(WebServiceClientException.class, () ->
        serviceTemplate.sendAndReceiveWithProps(
            props, mock(WebServiceMessageCallback.class), mock(WebServiceMessageExtractor.class)
        )
    );

    assertThat(exception.getMessage()).isEqualTo("Could not use transport: Failed to connect");

  }

  @Test
  public void shouldFailWithIllegalWhenMarshalNotPresent() throws IOException {
    ZonedDateTime now = ZonedDateTime.now();
    ZonedDateTime endDate = now.plusDays(2);

    WSTemplateProps props = WSTemplateProps.builder()
        .rfc(RFC_TEST)
        .endpoint(SOLICITA_DESCARGA)
        .qualifiedName(new QName(SAT_DESCARGA_MASIVA_NS, "SolicitaDescarga")).build();

    RequestDownload req = new RequestDownload().request(new Request()
        .rfcRequest(RFC_TEST)
        .rfcReceptor(RFC_TEST)
        .requestType(DEFAULT_REQUEST_TYPE)
        .dateEnd(endDate.format(FORMATTER))
        .dateStart(now.format(FORMATTER))
    );

    mockServer.expect(connectionTo(props.getEndpoint().getEndpoint()))
        .andRespond(withSoapEnvelope(loadResource("RESPUESTA-DESCARGA.xml")));

    when(
        serviceTemplate.getMarshaller()
    ).thenReturn(null);

    Exception exception = assertThrows(WebServiceClientException.class,
        () -> serviceTemplate.marshalSendAndReceiveWithProps(props, req)
    );

    assertThat(exception.getMessage()).isEqualTo(
        "Could not use transport: No marshaller registered. Check configuration of WebServiceTemplate."
    );

  }

}