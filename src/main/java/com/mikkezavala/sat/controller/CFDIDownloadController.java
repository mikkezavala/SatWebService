package com.mikkezavala.sat.controller;

import static com.mikkezavala.sat.domain.sat.SoapEndpoint.AUTENTICA;
import static com.mikkezavala.sat.domain.sat.SoapEndpoint.VALIDA_DESCARGA;

import com.mikkezavala.sat.domain.sat.SoapEndpoint;
import com.mikkezavala.sat.domain.sat.auth.AuthResponse;
import com.mikkezavala.sat.domain.sat.cfdi.individual.download.DownloadResponse;
import com.mikkezavala.sat.domain.sat.cfdi.individual.entity.Invoice;
import com.mikkezavala.sat.domain.sat.cfdi.individual.request.Response;
import com.mikkezavala.sat.domain.sat.cfdi.individual.validate.ValidateResponse;
import com.mikkezavala.sat.service.SoapService;
import com.mikkezavala.sat.util.SoapUtil;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.w3c.dom.Document;

@RestController
@RequestMapping(path = "/v1")
public class CFDIDownloadController {

  private static final String ZIP_PREFIX = "./zip/";
  private static final Logger LOGGER = LoggerFactory.getLogger(CFDIDownloadController.class);
  private final SoapService service;

  public CFDIDownloadController(SoapService service) {
    this.service = service;
  }

  @PostMapping("/token")
  public AuthResponse getAuthToken(@RequestBody Map<String, String> body) {
    try {
      return SoapUtil.callWebService(
          service.autentica(body.get("rfc")), AuthResponse.class, AUTENTICA, null
      );
    } catch (Exception e) {
      LOGGER.error("failed getting token", e);
    }

    return null;
  }

  @PostMapping("/cfdi/request")
  public Response requestDownload(
      @RequestBody Map<String, String> body,
      @RequestHeader("Authorization") String token
  ) {

//    try {
//      return SoapUtil.callWebService(service.solicita(body.get("rfc")), Response.class,
//          SOLICITA_DESCARGA, token);
//    } catch (Exception e) {
//      LOGGER.error("failed creatinf request");
//    }

    return null;
  }

  @PostMapping("/cfdi/validate")
  public ValidateResponse validateRequest(
      @RequestBody Map<String, String> body,
      @RequestHeader("Authorization") String token
  ) {

    try {
      return SoapUtil.callWebService(
          service.valida(
              body.get("requestId"), body.get("rfc")
          ), ValidateResponse.class, VALIDA_DESCARGA, token
      );
    } catch (Exception e) {
      LOGGER.error("failed validating request", e);
    }

    return null;
  }

  @PostMapping("/cfdi/download")
  public List<Invoice> download(
      @RequestBody Map<String, String> body,
      @RequestHeader("Authorization") String token
  ) {
    String packedId = body.get("packetId");
    try {
      DownloadResponse salida = SoapUtil.callWebService(
          service.descarga(packedId, body.get("rfc")),
          DownloadResponse.class,
          SoapEndpoint.DESCARGA_MASIVA, token
      );

      byte[] bytes = Base64.getDecoder().decode(salida.getPaquete());

      File dir = new File(ZIP_PREFIX);
      if (!dir.exists()) {
        FileUtils.forceMkdir(dir);
      }

      String zipFilePath = ZIP_PREFIX + packedId + ".zip";
      FileOutputStream outputStream = new FileOutputStream(zipFilePath);
      outputStream.write(bytes);
      outputStream.close();

      ZipFile zipFile = new ZipFile(zipFilePath);
      Enumeration<? extends ZipEntry> entries = zipFile.entries();
      List<Invoice> invoices = new ArrayList<>();
      while (entries.hasMoreElements()) {
        ZipEntry entry = entries.nextElement();
        InputStream stream = zipFile.getInputStream(entry);

        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = builder.parse(stream);

        JAXBContext context = JAXBContext.newInstance(Invoice.class);
        JAXBElement<Invoice> content = context.createUnmarshaller()
            .unmarshal(doc, Invoice.class);

        invoices.add(content.getValue());
      }

      return invoices;

    } catch (Exception e) {
      LOGGER.error("failed creating zip", e);
    }

    return Collections.emptyList();
  }

}
