package com.mikkezavala.sat.exception;

/**
 * The type Fiel exception.
 */
public class FielException extends RuntimeException {

  /**
   * Instantiates a new Fiel exception.
   *
   * @param message the message
   */
  public FielException(String message) {
    super(message);
  }

  /**
   * Instantiates a new Fiel exception.
   *
   * @param message   the message
   * @param throwable the throwable
   */
  public FielException(String message, Throwable throwable) {
    super(message, throwable);
  }
}
