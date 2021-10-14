package com.mikkezavala.sat.util;

import static com.mikkezavala.sat.domain.sat.SoapEndpoint.DESCARGA_MASIVA;
import static org.assertj.core.api.Assertions.assertThat;

import com.mikkezavala.sat.TestBase;
import com.mikkezavala.sat.domain.sat.cfdi.individual.download.Download;
import com.mikkezavala.sat.domain.sat.cfdi.individual.download.DownloadRequest;
import com.mikkezavala.sat.domain.sat.cfdi.individual.download.DownloadResponse;
import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.w3c.dom.Document;

/**
 * The type Soap util test.
 */
@ExtendWith(SpringExtension.class)
public class SoapUtilTest extends TestBase {

  private SoapUtil soapUtil;

  private final MessageFactory messageFactory = getMessageFactory();

  /**
   * Init.
   */
  @BeforeEach
  void init() {
    soapUtil = new SoapUtil(messageFactory, getSoapConnection());
  }

  /**
   * Simple parsing.
   *
   * @throws Exception the exception
   */
  @Test
  public void simpleParsing() throws Exception {

    DownloadResponse res = soapUtil.callWebService(buildMessage(), DownloadResponse.class, DESCARGA_MASIVA, null);

    assertThat(res.getPaquete()).isEmpty();
  }

  private SOAPMessage buildMessage() throws SOAPException {
    return messageFactory.createMessage();

  }

}
