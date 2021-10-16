package com.mikkezavala.sat.util;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The type Fiel util.
 */
public class FielUtil {

  private static final Logger LOGGER = LoggerFactory.getLogger(FielUtil.class);

  /**
   * Run script int.
   *
   * @param command the command
   * @return the int
   */
  public static int runScript(String command) {
    LOGGER.info("Generating PFX");
    CommandLine cmd = CommandLine.parse(command);
    DefaultExecutor exec = new DefaultExecutor();
    exec.setExitValue(0);
    try {
      return exec.execute(cmd);
    } catch (Exception e) {
      LOGGER.error("Error generating PFX");
    }

    return -1;
  }
}
