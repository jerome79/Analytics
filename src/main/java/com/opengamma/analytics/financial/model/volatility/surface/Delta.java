/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.surface;

import com.opengamma.strata.collect.ArgChecker;

/**
 * The strike type of absolute delta. 
 * <p>
 * The absolute delta of a call option, D_c. This is in the range (0,1), where 0.5 is ATM (Delta-Neutral Straddle DNS), 
 * D_c > 0.5 are ITM  and D_c < 0.5 are OTM options.
 * The absolute delta of a put option is related by D_p = D_c - 1. Since prices are normally quoted for OTM options, 
 * D_c < 0.5 will be from calls, while D_c > 0.5 (D_p > -0.5) will be from puts.
 */
public class Delta implements StrikeType {

  /**
   * The value of absolute delta. 
   */
  private final double _value;

  /**
   * Obtains an instance of {@code Delta} with the value of absolute delta. 
   * 
   * @param value  the value of absolute delta
   */
  public Delta(double value) {
    ArgChecker.isTrue(value >= 0 && value <= 1.0, "Delta must be in the range (0,1)");
    _value = value;
  }

  @Override
  public double value() {
    return _value;
  }

  @Override
  public Delta with(double value) {
    return new Delta(value);
  }

  @Override
  public String type() {
    return "Delta";
  }
}
