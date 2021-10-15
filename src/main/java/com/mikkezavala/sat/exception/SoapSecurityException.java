package com.mikkezavala.sat.exception;

/**
 * The type Soap security exception.
 */
public class SoapSecurityException extends RuntimeException {

  /**
   * Instantiates a new Soap security exception.
   *
   * @param message   the message
   * @param throwable the throwable
   */
  public SoapSecurityException(String message, Throwable throwable) {
    super(message, throwable);
  }
}
