package com.mikkezavala.sat.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * This utility manages resource management from XML to some data structure.
 */
public class ResourceUtil {

  private static final String ZIP_PREFIX = "./zip/";

  private static final DocumentBuilderFactory DOC_FACTORY = DocumentBuilderFactory.newInstance();

  private static final Logger LOGGER = LoggerFactory.getLogger(ResourceUtil.class);

  /**
   * Save zip string.
   *
   * @param content the content
   * @param uuid    the uuid
   * @return the string
   */
  public static String saveZip(String content, String uuid) {
    byte[] bytes = Base64.getDecoder().decode(content);

    File dir = new File(ZIP_PREFIX);
    if (!dir.exists()) {
      try {
        FileUtils.forceMkdir(dir);
      } catch (IOException e) {
        LOGGER.error("Could not create: {}", dir.getPath());
      }
    }

    String zipFilePath = ZIP_PREFIX + uuid + ".zip";
    try (FileOutputStream outputStream = new FileOutputStream(zipFilePath)) {
      outputStream.write(bytes);
    } catch (IOException e) {
      LOGGER.error("Could not save Zip file");
    }

    return zipFilePath;
  }

  /**
   * Gets from zip.
   *
   * @param <T>   the type parameter
   * @param path  the path
   * @param clazz the clazz
   * @return the from zip
   */
  public static <T> List<T> getFromZip(String path, Class<T> clazz) {
    LOGGER.info("Reading ZipFile");
    ZipFile zipFile;
    String className = clazz.getName();
    try {
      zipFile = new ZipFile(path);
    } catch (IOException e) {
      LOGGER.error("Failed reading client package zip file for class: {}", className);
      return Collections.emptyList();
    }

    Enumeration<? extends ZipEntry> entries = zipFile.entries();
    List<T> invoices = new ArrayList<>();
    while (entries.hasMoreElements()) {
      ZipEntry entry = entries.nextElement();
      try (InputStream stream = zipFile.getInputStream(entry)) {

        DocumentBuilder builder = DOC_FACTORY.newDocumentBuilder();
        Document doc = builder.parse(stream);

        JAXBContext context = JAXBContext.newInstance(clazz);
        JAXBElement<T> content = context.createUnmarshaller().unmarshal(doc, clazz);

        invoices.add(content.getValue());
      } catch (IOException | SAXException | JAXBException | ParserConfigurationException e) {
        LOGGER.error("Failed parsing zip file into List<{}>", className);
      }
    }

    return invoices;
  }

}
