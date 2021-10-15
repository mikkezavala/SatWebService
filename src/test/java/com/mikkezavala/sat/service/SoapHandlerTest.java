package com.mikkezavala.sat.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.xmlunit.assertj3.XmlAssert.assertThat;

import com.mikkezavala.sat.TestBase;
import com.mikkezavala.sat.domain.sat.cfdi.individual.validate.ValidateResponse;
import com.mikkezavala.sat.exception.SoapParsingException;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Element;

public class SoapHandlerTest extends TestBase {

  private SoapHandler handler;

  private SOAPFactory soapFactory = spy(getSoapFactory());

  @BeforeEach
  void init() {

    soapFactory = spy(soapFactory);
    handler = new SoapHandler(
        getMessageFactory(), soapFactory, getBuilderFactory()
    );
  }

  @Test
  public void shouldThrowSoapParsingExceptionWhenInvalid() throws SOAPException {
    when(soapFactory.createElement(any(Element.class))).thenThrow(RuntimeException.class);
    Throwable throwable = assertThrows(SoapParsingException.class, () ->
        handler.createNodeOf(new ValidateResponse())
    );

    assertThat(throwable.getMessage()).isEqualTo(
        "Unable to create element of type: ValidateResponse");
  }
}
