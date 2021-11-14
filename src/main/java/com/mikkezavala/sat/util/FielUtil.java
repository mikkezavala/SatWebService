package com.mikkezavala.sat.util;

import java.nio.file.Path;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The type Fiel util.
 */
public class FielUtil {

  private static final String COMMAND_PLACEHOLDER = "./create-p12.sh -r %s -c %s -k %s -p %s";

  private static final Logger LOGGER = LoggerFactory.getLogger(FielUtil.class);

  /**
   * Generate pfx int.
   *
   * @param rfc  the rfc
   * @param cert the cert
   * @param key  the key
   * @param pass the pass
   * @return the int
   */
  public static int generateP12(String rfc, Path cert, Path key, String pass) {

    String command = String.format(COMMAND_PLACEHOLDER, rfc, cert, key, pass);

    LOGGER.info("Generating P12");
    CommandLine cmd = CommandLine.parse(command);
    DefaultExecutor exec = new DefaultExecutor();

    try {
      return exec.execute(cmd);
    } catch (Exception e) {
      LOGGER.error("Error generating P12");
    }

    return -1;
  }
}
