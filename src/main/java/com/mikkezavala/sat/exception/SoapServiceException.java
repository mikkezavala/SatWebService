package com.mikkezavala.sat.exception;

/**
 * The type Soap service exception.
 *
 * This creates an unchecked exception when SOAP Service fails
 */
public class SoapServiceException extends RuntimeException {

  /**
   * Instantiates a new Soap service exception.
   *
   * @param message   the message
   * @param throwable the throwable
   */
  public SoapServiceException(String message, Throwable throwable) {
    super(message, throwable);
  }
}
