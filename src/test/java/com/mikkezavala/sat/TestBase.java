package com.mikkezavala.sat;

import static com.mikkezavala.sat.util.Constant.ENVELOPE_NS;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPMessage;
import org.springframework.util.ResourceUtils;

abstract public class TestBase {

  protected Map<String, String> getNSContext() {
    Map<String, String> ns = new HashMap<>();
    ns.put("s", ENVELOPE_NS);
    return ns;
  }

  protected String soapToString(SOAPMessage message) throws Exception {
    String xml = "";
    try (OutputStream os = new ByteArrayOutputStream()) {
      message.writeTo(os);
      xml = os.toString();
    }

    return xml;
  }

  protected MessageFactory getMessageFactory() {
    try {
      return MessageFactory.newInstance();
    } catch (SOAPException e) {
      throw new RuntimeException("Failed to create MessageFactory");
    }
  }

  protected SOAPFactory getSoapFactory() {
    try {
      return SOAPFactory.newInstance();
    } catch (SOAPException e) {
      throw new RuntimeException("Failed to create SOAPFactory");
    }
  }

  protected DocumentBuilderFactory getBuilderFactory() {
    DocumentBuilderFactory builder = DocumentBuilderFactory.newInstance();
    builder.setNamespaceAware(true);

    return builder;
  }

  public File loadResource(String file) throws FileNotFoundException {
    return ResourceUtils.getFile("classpath:" + file);
  }

}
