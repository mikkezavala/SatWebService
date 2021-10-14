package com.mikkezavala.sat.util;

import static com.mikkezavala.sat.domain.sat.SoapEndpoint.AUTENTICA;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.mikkezavala.sat.TestBase;
import com.mikkezavala.sat.domain.sat.auth.Auth;
import com.mikkezavala.sat.domain.sat.auth.AuthResponse;
import java.time.ZonedDateTime;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * The type Soap util test.
 */
@ExtendWith(SpringExtension.class)
public class SoapUtilTest extends TestBase {

  private SoapUtil soapUtil;

  @Mock
  private SOAPConnection soapConnection;

  private final MessageFactory messageFactory = getMessageFactory();

  /**
   * Init.
   */
  @BeforeEach
  void init() {
    soapUtil = new SoapUtil(messageFactory, soapConnection);
  }

  /**
   * Simple parsing.
   *
   * @throws Exception the exception
   */
  @Test
  public void shouldParse() throws Exception {
    when(soapConnection.call(any(), any())).thenReturn(response("AUTH-RESPONSE.xml"));
    SOAPMessage message = buildMessage(new Auth());
    AuthResponse res = soapUtil.callWebService(message, AuthResponse.class, AUTENTICA, null);

    assertThat(res.getToken()).isEqualTo("eyJWT_TOKEN");
    assertThat(res.getTimestamp().getExpires()).isInstanceOf(ZonedDateTime.class);
    assertThat(res.getTimestamp().getCreated()).isInstanceOf(ZonedDateTime.class);
  }

}
