/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.surface;

import com.opengamma.strata.collect.ArgChecker;

/**
 * This is defined as strike/forward
 */
public class Moneyness implements StrikeType {

  private final double _value;

  public Moneyness(final double value) {
    ArgChecker.isTrue(value >= 0, "negative moneyness");
    _value = value;
  }

  public Moneyness(final double strike, final double forward) {
    ArgChecker.isTrue(strike >= 0, "negative strike");
    ArgChecker.isTrue(forward > 0, "negative or zero forward");
    _value = strike / forward;
  }

  @Override
  public double value() {
    return _value;
  }



  @Override
  public Moneyness with(double value) {
    return new Moneyness(value);
  }

}
