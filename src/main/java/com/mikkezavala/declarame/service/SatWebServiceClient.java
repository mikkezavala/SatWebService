package com.mikkezavala.declarame.service;


import javax.xml.soap.SOAPMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SatWebServiceClient {

  private final SoapService service;

  private static final Logger LOGGER = LoggerFactory.getLogger(SatWebServiceClient.class);

  public SatWebServiceClient(SoapService service) {
    this.service = service;
  }

  public void response() throws Exception {
    SOAPMessage msg = service.autentica();
    service.outputSOAPMessageToFile(msg);

    LOGGER.info("Response:\n{}", service.callTheWebServiceFromFile());

  }
}



