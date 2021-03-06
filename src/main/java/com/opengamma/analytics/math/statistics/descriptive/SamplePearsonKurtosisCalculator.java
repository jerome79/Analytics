/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.statistics.descriptive;

import java.util.function.Function;

import com.opengamma.strata.collect.ArgChecker;

/**
 * The sample Pearson kurtosis gives a measure of how heavy the tails of a
 * distribution are with respect to the normal distribution (which has a
 * Pearson kurtosis of three). It is calculated using
 * $$
 * \begin{align*}
 * \text{Pearson kurtosis} = \text{Fisher kurtosis} + 3
 * \end{align*}
 * $$
 * where the Fisher kurtosis is calculated using {@link SampleFisherKurtosisCalculator}.
 */
public class SamplePearsonKurtosisCalculator implements Function<double[], Double> {
  private static final Function<double[], Double> KURTOSIS = new SampleFisherKurtosisCalculator();

  /**
   * @param x The array of data, not null. Must contain at least four data points.
   * @return The sample Pearson kurtosis
   */
  @Override
  public Double apply(final double[] x) {
    ArgChecker.notNull(x, "x");
    ArgChecker.isTrue(x.length >= 4, "Need at least four points to calculate kurtosis");
    return KURTOSIS.apply(x) + 3;
  }
}
