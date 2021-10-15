package com.mikkezavala.sat.service;


import static com.mikkezavala.sat.util.Constant.ENV_PREFIX;

import com.mikkezavala.sat.exception.SoapParsingException;
import com.mikkezavala.sat.util.XmlUtil;
import java.io.StringReader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPMessage;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 * The type Soap handler.
 */
@Component
public class SoapHandler {

  private final SOAPFactory soapFactory;

  private final MessageFactory messageFactory;

  private final DocumentBuilderFactory builderFactory;

  /**
   * Instantiates a new Soap handler.
   *
   * @param messageFactory the message factory
   * @param soapFactory    the soap factory
   * @param builderFactory the builder factory
   */
  public SoapHandler(
      MessageFactory messageFactory, SOAPFactory soapFactory, DocumentBuilderFactory builderFactory
  ) {
    this.soapFactory = soapFactory;
    this.messageFactory = messageFactory;
    this.builderFactory = builderFactory;
  }

  /**
   * Create node of soap element.
   *
   * Creates an SOAPElement of the desired class instance
   *
   * @param <T>     the type parameter
   * @param request the request
   * @return the soap element
   */
  public <T> SOAPElement createNodeOf(T request) {
    try {
      String xml = XmlUtil.serialize(request, true);
      InputSource source = new InputSource(new StringReader(xml));
      DocumentBuilder builder = builderFactory.newDocumentBuilder();
      Document doc = builder.parse(source);

      return soapFactory.createElement(doc.getDocumentElement());
    } catch (Exception e) {
      throw new SoapParsingException(
          "Unable to create element of type: " + request.getClass().getSimpleName(), e
      );
    }
  }

  /**
   * Create envelope soap message.
   *
   * @return the soap message
   * @throws SOAPException the soap exception
   */
  public SOAPMessage createEnvelope() throws SOAPException {

    SOAPMessage soapMessage = messageFactory.createMessage();

    SOAPBody soapBody = soapMessage.getSOAPBody();
    SOAPEnvelope soapEnvelope = soapMessage.getSOAPPart().getEnvelope();

    soapBody.setPrefix(ENV_PREFIX);
    soapEnvelope.setPrefix(ENV_PREFIX);

    return soapMessage;
  }

  /**
   * Gets soap factory.
   *
   * @return the soap factory
   */
  public SOAPFactory getSoapFactory() {
    return this.soapFactory;
  }

}
