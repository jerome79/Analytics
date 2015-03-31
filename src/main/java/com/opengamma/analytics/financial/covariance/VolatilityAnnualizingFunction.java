/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.covariance;

import com.opengamma.analytics.math.function.Function;
import com.opengamma.strata.collect.ArgChecker;

/**
 * Annualizes volatility by a number of periods in a year.
 */
public class VolatilityAnnualizingFunction implements Function<Double, Double> {
  /** The number of periods in a year */
  private final double _periodsPerYear;

  /**
   * @param periodsPerYear The number of periods in a year, greater than zero.
   */
  public VolatilityAnnualizingFunction(final double periodsPerYear) {
    ArgChecker.isTrue(periodsPerYear > 0, "periods per year");
    _periodsPerYear = periodsPerYear;
  }

  @Override
  public Double evaluate(final Double... x) {
    ArgChecker.notEmpty(x, "x");
    ArgChecker.noNulls(x, "x");
    return Math.sqrt(_periodsPerYear / x[0]);
  }

}
