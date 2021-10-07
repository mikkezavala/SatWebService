package com.mikkezavala.sat.service;

import static com.mikkezavala.sat.util.SoapUtil.ENV_PREFIX;

import com.mikkezavala.sat.util.XmlUtil;
import java.io.StringReader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPMessage;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

@Component
public class SoapHandler {

  private final SOAPFactory soapFactory;

  private final MessageFactory messageFactory;

  private final DocumentBuilderFactory builderFactory;

  public SoapHandler(
      MessageFactory messageFactory, SOAPFactory soapFactory, DocumentBuilderFactory builderFactory
  ) {
    this.soapFactory = soapFactory;
    this.messageFactory = messageFactory;
    this.builderFactory = builderFactory;
  }

  public <T> SOAPElement createNodeOf(T request)
      throws Exception {
    String xml = XmlUtil.serialize(request, true);
    InputSource source = new InputSource(new StringReader(xml));
    DocumentBuilder builder = builderFactory.newDocumentBuilder();
    Document doc = builder.parse(source);

    return soapFactory.createElement(doc.getDocumentElement());
  }

  public SOAPMessage createEnvelope() throws Exception {

    SOAPMessage soapMessage = messageFactory.createMessage();

    SOAPBody soapBody = soapMessage.getSOAPBody();
    SOAPEnvelope soapEnvelope = soapMessage.getSOAPPart().getEnvelope();

    soapBody.setPrefix(ENV_PREFIX);
    soapEnvelope.setPrefix(ENV_PREFIX);

    return soapMessage;
  }

  public SOAPFactory getSoapFactory() {
    return this.soapFactory;
  }

}
