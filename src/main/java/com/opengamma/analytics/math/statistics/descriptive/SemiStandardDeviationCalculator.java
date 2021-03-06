/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.statistics.descriptive;

import java.util.function.Function;

import com.opengamma.strata.collect.ArgChecker;

/**
 * The semi-standard deviation of a series of data is the partial moment (see {@link PartialMomentCalculator}) calculated with the mean as the threshold.
 */
public class SemiStandardDeviationCalculator implements Function<double[], Double> {
  private static final MeanCalculator MEAN = new MeanCalculator();
  private final boolean _useDownSide;

  /**
   * Creates calculator with the default value for useDownSide (= true)
   */
  public SemiStandardDeviationCalculator() {
    _useDownSide = true;
  }

  /**
   * Creates calculator
   * @param useDownSide If true, data below the mean is used in the calculation
   */
  public SemiStandardDeviationCalculator(final boolean useDownSide) {
    _useDownSide = useDownSide;
  }

  /**
   * @param x The array of data, not null
   * @return The semi-standard deviation
   */
  @Override
  public Double apply(final double[] x) {
    ArgChecker.notNull(x, "x");
    final double mean = MEAN.evaluate(x);
    final int n = x.length;
    return new PartialMomentCalculator(mean, _useDownSide).apply(x) * n / (n - 1);
  }

}
