package com.mikkezavala.sat.exception;

/**
 * The type Soap parsing exception.
 */
public class SoapParsingException extends RuntimeException {

  /**
   * Instantiates a new Soap parsing exception.
   *
   * @param message   the message
   * @param throwable the throwable
   */
  public SoapParsingException(String message, Throwable throwable) {
    super(message, throwable);
  }
}
