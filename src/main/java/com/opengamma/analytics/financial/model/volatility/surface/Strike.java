/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.surface;


/**
 * The strike type of strike.
 */
public class Strike implements StrikeType {

  /**
   * The value of strike.
   */
  private final double _value;

  /**
   * Obtains an instance of {@code Strike} with the value of strike. 
   * 
   * @param value  the value of strike
   */
  public Strike(double value) {
    _value = value;
  }

  @Override
  public double value() {
    return _value;
  }

  @Override
  public Strike with(double value) {
    return new Strike(value);
  }

  @Override
  public String type() {
    return "Strike";
  }
}
