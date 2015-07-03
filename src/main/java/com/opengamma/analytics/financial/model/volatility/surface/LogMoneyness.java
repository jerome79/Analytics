/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.surface;

import com.opengamma.strata.collect.ArgChecker;

/**
 * The strike type of logmoneyness. 
 * <p>
 * The logmoneyness is defined as {@code x = ln(strike/forward)}. 
 * The strike value and forward value should strictly positive. 
 */
public class LogMoneyness implements StrikeType {

  /**
   * The value of logmoneyness. 
   */
  private final double _value;

  /**
   * Obtains an instance of {@code LogMoneyness} with the value of logmoneyness. 
   * 
   * @param value  the value of logmoneyness
   */
  public LogMoneyness(double value) {
    _value = value;
  }

  /**
   * Obtains an instance of {@code LogMoneyness} with strike and forward. 
   * 
   * @param strike  the strike 
   * @param forward  the forward
   */
  public LogMoneyness(double strike, double forward) {
    ArgChecker.isTrue(strike > 0, "negative or zero strike");
    ArgChecker.isTrue(forward > 0, "negative or zero forward");
    _value = Math.log(strike / forward);
  }

  @Override
  public double value() {
    return _value;
  }

  @Override
  public StrikeType with(double value) {
    return new LogMoneyness(value);
  }

  @Override
  public String type() {
    return "LogMoneyness";
  }
}
