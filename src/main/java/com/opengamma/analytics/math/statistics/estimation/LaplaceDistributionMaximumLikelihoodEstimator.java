/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.statistics.estimation;

import java.util.function.Function;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.statistics.descriptive.MedianCalculator;
import com.opengamma.analytics.math.statistics.distribution.LaplaceDistribution;
import com.opengamma.analytics.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.strata.collect.ArgChecker;

/**
 * 
 */
public class LaplaceDistributionMaximumLikelihoodEstimator extends DistributionParameterEstimator<Double> {
  private final Function<double[], Double> _median = new MedianCalculator();

  @Override
  public ProbabilityDistribution<Double> evaluate(final double[] x) {
    Validate.notNull(x, "x");
    ArgChecker.notEmpty(x, "x");
    final double median = _median.apply(x);
    final int n = x.length;
    double b = 0;
    for (double aX : x) {
      b += Math.abs(aX - median);
    }
    return new LaplaceDistribution(median, b / n);
  }

}
