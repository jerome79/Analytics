/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.surface;

import com.opengamma.strata.collect.ArgChecker;

/**
 * The strike type of moneyness. 
 * <p>
 * The moneyness is defined as strike/forward. 
 * The strike should be nonnegative and the forward should be strictly positive.
 */
public class Moneyness implements StrikeType {

  /**
   * The value of moneyness. 
   */
  private final double _value;

  /**
   * Obtains an instance of {@code Moneyness} with the value of moneyness.
   * 
   * @param value  the value of moneyness
   */
  public Moneyness(double value) {
    ArgChecker.isTrue(value >= 0, "negative moneyness");
    _value = value;
  }

  /**
   * Obtains an instance of {@code Moneyness} with strike and forward.
   * 
   * @param strike  the strike
   * @param forward  the forward
   */
  public Moneyness(double strike, double forward) {
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

  @Override
  public String type() {
    return "Moneyness";
  }
}
