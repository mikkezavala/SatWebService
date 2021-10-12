package com.mikkezavala.sat;

import static com.mikkezavala.sat.util.Constant.ENVELOPE_NS;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
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

  public File loadResource(String file) throws FileNotFoundException {
    return ResourceUtils.getFile("classpath:" + file);
  }

}
