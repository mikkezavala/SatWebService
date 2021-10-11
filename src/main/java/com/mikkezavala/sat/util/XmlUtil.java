package com.mikkezavala.sat.util;

import java.io.StringWriter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XmlUtil {


  private static final Logger LOGGER = LoggerFactory.getLogger(XmlUtil.class);

  private static <T> Marshaller marshaller(T element) throws JAXBException {
    JAXBContext jaxbContext = JAXBContext.newInstance(element.getClass());
    Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

    jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

    return jaxbMarshaller;
  }

  public static <T> String serialize(T element, boolean partial) {
    try {
      Marshaller jax = marshaller(element);

      if (partial) {
        jax.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
      }

      StringWriter sw = new StringWriter();
      jax.marshal(element, sw);
      return sw.toString();

    } catch (JAXBException e) {
      LOGGER.error("Error occurred parsing {} to XML String", element, e);
    }

    return "";
  }

}
