package com.mikkezavala.sat.configuration;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebServiceClientConfig {

  @Bean
  public MessageFactory messageFactory() {
    try {
      return MessageFactory.newInstance();
    } catch (SOAPException e) {
      throw new RuntimeException("Error setting up Message Factory", e);
    }
  }

  @Bean
  public SOAPFactory soapFactory() {
    try {
      return SOAPFactory.newInstance();
    } catch (SOAPException e) {
      throw new RuntimeException("Error setting up SoapFactory");
    }
  }

  @Bean
  public DocumentBuilderFactory documentFactory() {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true);
    return factory;
  }
}
