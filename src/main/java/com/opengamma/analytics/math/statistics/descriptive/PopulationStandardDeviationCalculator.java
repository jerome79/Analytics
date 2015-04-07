/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.statistics.descriptive;

import java.util.function.Function;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.strata.collect.ArgChecker;

/**
 * Calculates the population standard deviation of a series of data. The population standard deviation of a series of data is defined as the square root of 
 * the population variance (see {@link PopulationVarianceCalculator}).
 */
public class PopulationStandardDeviationCalculator implements Function<double[], Double> {
  private static final Function1D<double[], Double> VARIANCE = new PopulationVarianceCalculator();

  /**
   * @param x The array of data, not null, must contain at least two data points
   * @return The population standard deviation
   */
  @Override
  public Double apply(final double[] x) {
    ArgChecker.notNull(x, "x");
    ArgChecker.isTrue(x.length > 1, "Need at least two points to calculate standard deviation");
    return Math.sqrt(VARIANCE.evaluate(x));
  }

}
