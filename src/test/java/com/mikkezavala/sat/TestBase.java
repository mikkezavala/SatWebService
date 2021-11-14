package com.mikkezavala.sat;

import static com.mikkezavala.sat.configuration.WebServiceClientConfig.PACKAGES;

import com.mikkezavala.sat.domain.sat.cfdi.individual.SatClient;
import com.mikkezavala.sat.domain.sat.cfdi.individual.SatToken;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPException;
import javax.xml.transform.Result;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.util.ResourceUtils;

abstract public class TestBase {

  protected final String RFC_TEST = "XOJI740919U48";

  protected final String RFC_TEST_PASS = "12345678a";


  protected MessageFactory getMessageFactory() {
    try {
      return MessageFactory.newInstance();
    } catch (SOAPException e) {
      throw new RuntimeException("Failed to create MessageFactory");
    }
  }

  protected String extractFile(String testResource) {
    try (InputStream zipFile = new FileInputStream(loadResourceAsFile(testResource))) {
      return Base64.getEncoder().encodeToString(IOUtils.toByteArray(zipFile));
    } catch (IOException e) {
      throw new RuntimeException("Reading ZIP file failed");
    }
  }

  public Resource loadResource(String resource) {
    return new ClassPathResource(resource);
  }

  public File loadResourceAsFile(String file) throws FileNotFoundException {
    return ResourceUtils.getFile("classpath:" + file);
  }

  protected String zipAsBase64(File zipContent) throws IOException {
    byte[] buffer = new byte[1024];
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try (ZipOutputStream zos = new ZipOutputStream(baos)) {
      try (FileInputStream fis = new FileInputStream(zipContent)) {
        zos.putNextEntry(new ZipEntry(zipContent.getName()));
        int length;
        while ((length = fis.read(buffer)) > 0) {
          zos.write(buffer, 0, length);
        }
        zos.closeEntry();
      }
    }
    byte[] bytes = baos.toByteArray();
    return Base64.getEncoder().encodeToString(bytes);
  }

  protected SatToken getMockToken() {
    ZonedDateTime now = ZonedDateTime.now();
    return SatToken.builder()
        .id(99999)
        .created(now)
        .rfc(RFC_TEST)
        .token("ejFake-Token")
        .expiration(now.plusDays(5)).build();
  }

  protected SatClient getMockedClient(String keyStorePath) {
    return new SatClient().id(9999).keystore(keyStorePath).rfc(RFC_TEST)
        .passwordPlain(RFC_TEST_PASS);
  }

  protected Jaxb2Marshaller getTestMarshaller() {
    Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
    marshaller.setPackagesToScan(PACKAGES);

    return marshaller;
  }

  protected void marshall(Object o, Result result) {
    getTestMarshaller().marshal(o, result);
  }
}
