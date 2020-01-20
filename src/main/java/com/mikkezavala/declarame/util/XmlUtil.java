package com.mikkezavala.declarame.util;

import java.io.StringWriter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XmlUtil {
  
  public final static String WSS_SEC_EXT_NS = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";

  public final static String WSS_UTILITY_NS = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd";

  public final static String WSS_TOKEN_PROFILE_NS = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-x509-token-profile-1.0#X509v3";

  public final static String WSS_MESSAGE_SECURITY_NS = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-soap-message-security-1.0#Base64Binary";

  private static final Logger LOGGER = LoggerFactory.getLogger(XmlUtil.class);

  public static <T> String serialize(T element, boolean partial) {
    try {
      JAXBContext jaxbContext = JAXBContext.newInstance(element.getClass());
      Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

      jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

      if (partial) {
        jaxbMarshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
      }

      StringWriter sw = new StringWriter();
      jaxbMarshaller.marshal(element, sw);
      return sw.toString();

    } catch (JAXBException e) {
      LOGGER.error("Error occurred parsing {} to XML String", element, e);
    }

    return "";
  }
}
