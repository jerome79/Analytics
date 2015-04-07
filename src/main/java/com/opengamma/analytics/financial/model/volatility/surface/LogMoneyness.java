/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.surface;

import com.opengamma.strata.collect.ArgChecker;

/**
 * Defined as x = ln(strike/forward)
 */
public class LogMoneyness implements StrikeType {

  private double _value;

  public LogMoneyness(final double value) {
    _value = value;
  }

  public LogMoneyness(final double strike, final double forward) {
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

}
