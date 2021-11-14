package com.mikkezavala.sat.configuration;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

/**
 * The type Web service client config.
 */
@Configuration
public class WebServiceClientConfig {

  /**
   * The constant PACKAGES.
   */
  public static final String PACKAGES = "com.mikkezavala.sat.domain";

  /**
   * Jaxb 2 marshaller jaxb 2 marshaller.
   *
   * @return the jaxb 2 marshaller
   */
  @Bean
  public Jaxb2Marshaller jaxb2Marshaller() {
    Jaxb2Marshaller jaxb2Marshaller = new Jaxb2Marshaller();
    jaxb2Marshaller.setPackagesToScan(PACKAGES);
    return jaxb2Marshaller;
  }

  /**
   * Message factory message factory. å
   *
   * @return the message factory
   */
  @Bean
  public MessageFactory messageFactory() {
    try {
      return MessageFactory.newInstance();
    } catch (SOAPException e) {
      throw new RuntimeException("Error setting up Message Factory", e);
    }
  }

  /**
   * Soap factory soap factory.
   *
   * @return the soap factory
   */
  @Bean
  public SOAPFactory soapFactory() {
    try {
      return SOAPFactory.newInstance();
    } catch (SOAPException e) {
      throw new RuntimeException("Error setting up SoapFactory");
    }
  }

  /**
   * Soap connection soap connection.
   *
   * @return the soap connection
   */
  @Bean
  public SOAPConnection soapConnection() {
    try {
      return SOAPConnectionFactory.newInstance().createConnection();
    } catch (SOAPException e) {
      throw new RuntimeException("Error setting up SoapFactory");
    }
  }

  /**
   * Document factory document builder factory.
   *
   * @return the document builder factory
   */
  @Bean
  public DocumentBuilderFactory documentFactory() {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true);
    return factory;
  }
}
