package com.mikkezavala.sat.service.sat;

import static com.mikkezavala.sat.domain.sat.SoapEndpoint.AUTENTICA;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.springframework.ws.test.client.RequestMatchers.connectionTo;
import static org.springframework.ws.test.client.ResponseCreators.withSoapEnvelope;

import com.mikkezavala.sat.domain.sat.auth.AuthResponse;
import com.mikkezavala.sat.template.SatWebServiceTemplate;
import java.io.File;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ws.client.core.WebServiceMessageCallback;
import org.springframework.ws.client.core.WebServiceMessageExtractor;
import org.springframework.ws.test.client.MockWebServiceServer;

/**
 * The type Auth service test.
 */
class AuthServiceTest extends AbstractSatServiceTest {

  private AuthService service;

  private MockWebServiceServer mockServer;

  private final SatWebServiceTemplate template = spy(new SatWebServiceTemplate(getTestMarshaller()));

  @BeforeEach
  protected void init() throws IOException {

    File pfxFile = loadResourceAsFile("PF_CFDI/" + RFC_TEST + ".p12");
    when(repository.tokenByRfc(anyString())).thenReturn(getMockToken());
    when(repository.satClientByRfc(anyString())).thenReturn(getMockedClient(pfxFile.getPath()));

    mockServer = MockWebServiceServer.createServer(template);

    service = new AuthService(template, repository);
  }

  /**
   * Should return token and timestamp.
   *
   * @throws IOException the io exception
   */
  @Test
  public void shouldReturnTokenAndTimestamp() throws IOException {

    mockServer.expect(connectionTo(AUTENTICA.getEndpoint()))
        .andRespond(withSoapEnvelope(loadResource("AUTH-RESPONSE.xml")));

    AuthResponse res = service.getAuth(RFC_TEST);

    assertThat(res).isNotNull();
    assertThat(res.getTimestamp()).isNotNull();
    assertThat(res.getToken()).isEqualTo("eyJWT_TOKEN");
    assertThat(res.getTimestamp().getCreated()).isEqualTo("2021-10-14T06:37:13.446Z");
    assertThat(res.getTimestamp().getExpires()).isEqualTo("2021-10-14T06:42:13.446Z");

  }

  /**
   * Should return empty when fails.
   */
  @Test
  @SuppressWarnings("unchecked")
  public void shouldReturnEmptyWhenFails() {

    doThrow(new RuntimeException()).when(template).sendAndReceiveWithProps(
        any(), any(WebServiceMessageCallback.class), any(WebServiceMessageExtractor.class)
    );

    AuthResponse res = service.getAuth(RFC_TEST);

    assertThat(res).isNotNull();
    assertThat(res.getToken()).isNull();
    assertThat(res.getTimestamp()).isNull();

  }

}