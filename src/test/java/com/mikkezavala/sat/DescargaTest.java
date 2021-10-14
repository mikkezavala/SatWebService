package com.mikkezavala.sat;


import static org.assertj.core.api.Assertions.assertThat;

import com.mikkezavala.sat.domain.sat.cfdi.individual.entity.Invoice;
import java.io.File;
import java.io.FileInputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

public class DescargaTest extends TestBase {

  @Test
  public void shouldParseDomain() throws Exception {

    File inpt = loadResource("CFDI.xml");
    FileInputStream fos = new FileInputStream(inpt);

    DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    Document doc = builder.parse(fos);

    JAXBContext context = JAXBContext.newInstance(Invoice.class);
    JAXBElement<Invoice> body = context.createUnmarshaller()
        .unmarshal(doc, Invoice.class);

    Invoice resolved = body.getValue();
    assertThat(resolved.getComplement().getPayments().getPayment().getAmount()).isEqualTo(599.0);
  }

  @Test
  public void shouldParsePayrollDomain() throws Exception {

    File inpt = loadResource("CFDI-NOMINA.xml");
    FileInputStream fos = new FileInputStream(inpt);

    DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    Document doc = builder.parse(fos);

    JAXBContext context = JAXBContext.newInstance(Invoice.class);
    JAXBElement<Invoice> body = context.createUnmarshaller()
        .unmarshal(doc, Invoice.class);

    Invoice resolved = body.getValue();
    assertThat(resolved.getComplement().getPayroll().getTotalDeductions()).isEqualTo(24860.95);
  }

  @Test
  public void shouldParseConcepts() throws Exception {

    File inpt = loadResource("CFDI-CONCEPTS.xml");
    FileInputStream fos = new FileInputStream(inpt);

    DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    Document doc = builder.parse(fos);

    JAXBContext context = JAXBContext.newInstance(Invoice.class);
    JAXBElement<Invoice> body = context.createUnmarshaller()
        .unmarshal(doc, Invoice.class);

    Invoice resolved = body.getValue();
    assertThat(resolved.getIssuer().getRfc()).isEqualTo("BBA830831LJ2");
  }


}
