package com.mikkezavala.sat.util;

import static com.mikkezavala.sat.domain.sat.SoapEndpoint.SOLICITA_DESCARGA;
import static com.mikkezavala.sat.util.Constant.FORMATTER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.mikkezavala.sat.TestBase;
import com.mikkezavala.sat.domain.sat.cfdi.individual.request.Request;
import com.mikkezavala.sat.domain.sat.cfdi.individual.request.RequestDownload;
import com.mikkezavala.sat.domain.sat.cfdi.individual.request.Response;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.time.ZonedDateTime;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

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
  public void simpleParsing() throws Exception {
    when(soapConnection.call(any(), any())).thenReturn(response("SOLICITA.xml"));
    SOAPMessage message = buildMessage();
    Response res = soapUtil.callWebService(message, Response.class, SOLICITA_DESCARGA, "Fake Token");

    assertThat(res.getResult().getStatus()).isEqualTo("5000");
    assertThat(res.getResult().getMessage()).isEqualTo("Solicitud Aceptada");
    assertThat(res.getResult().getRequestId()).isEqualTo("a5e7658b-a749-4527-8181-12029161c5e1");
  }

  private SOAPMessage buildMessage() throws Exception {

    SOAPMessage message = getMessageFactory().createMessage();
    RequestDownload requestDownload = new RequestDownload().request(new Request()
        .rfcRequest("FAKE")
        .rfcReceptor("FAKE")
        .requestType("CFDI_FAKE")
        .dateEnd(FORMATTER.format(ZonedDateTime.now().minusMinutes(10)))
        .dateStart(FORMATTER.format(ZonedDateTime.now()))
    );

    String xml = XmlUtil.serialize(requestDownload, true);
    InputSource source = new InputSource(new StringReader(xml));
    DocumentBuilder builder = getBuilderFactory().newDocumentBuilder();
    Document doc = builder.parse(source);

    SOAPBody body = message.getSOAPBody();
    SOAPElement downloadNode = getSoapFactory().createElement(doc.getDocumentElement());

    body.addChildElement(downloadNode);

    return message;
  }

}
