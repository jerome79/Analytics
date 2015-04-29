/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics;

/**
 * Exception thrown when pricing fails.
 */
public final class MathsException
    extends RuntimeException {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * Creates an instance based on a message.
   * 
   * @param message  the message, null tolerant
   */
  public MathsException(String message) {
    super(message);
  }

  /**
   * Creates an instance based on a message and cause.
   * 
   * @param message  the message, null tolerant
   * @param cause  the cause, null tolerant
   */
  public MathsException(String message, Throwable cause) {
    super(message, cause);
  }

}
