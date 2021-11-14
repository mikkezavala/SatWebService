package com.mikkezavala.sat;


import static com.mikkezavala.sat.util.SoapUtil.getFirstNode;
import static org.assertj.core.api.Assertions.assertThat;

import com.mikkezavala.sat.domain.sat.auth.AuthResponse;
import java.io.File;
import java.io.FileInputStream;
import java.util.Iterator;
import java.util.Objects;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.Node;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPMessage;
import org.junit.jupiter.api.Test;


/**
 * The type Auth test.
 */
public class AuthTest extends TestBase {

  /**
   * Should parse auth response.
   *
   * @throws Exception the exception
   */
  @Test
  public void shouldParseAuthResponse() throws Exception {

    File input = loadResourceAsFile("AUTH.xml");
    FileInputStream fos = new FileInputStream(input);

    MessageFactory factory = getMessageFactory();
    SOAPMessage message = factory.createMessage(new MimeHeaders(), fos);
    JAXBContext context = JAXBContext.newInstance(AuthResponse.class);

    SOAPElement authHeader = getFirstNode(
        Objects.requireNonNull(
            getFirstNode(message.getSOAPHeader())
        )
    );

    if (Objects.nonNull(authHeader)) {
      Iterator<Node> created = authHeader.getChildElements();
      while (created.hasNext()) {
        Node el = created.next();
        if (el instanceof SOAPElement) {
          SOAPElement soapElement = (SOAPElement) el;
          String name = soapElement.getElementName().getLocalName();
        }
      }
    }
    JAXBElement<AuthResponse> body = context.createUnmarshaller()
        .unmarshal(message.getSOAPBody().extractContentAsDocument(), AuthResponse.class);

    AuthResponse resolved = body.getValue();
    assertThat(resolved).isNotNull();
  }
}
