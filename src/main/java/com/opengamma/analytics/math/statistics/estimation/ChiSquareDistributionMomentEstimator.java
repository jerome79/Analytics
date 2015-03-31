/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.statistics.estimation;

import java.util.function.Function;

import com.opengamma.analytics.math.statistics.descriptive.SampleMomentCalculator;
import com.opengamma.analytics.math.statistics.distribution.ChiSquareDistribution;
import com.opengamma.analytics.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.strata.collect.ArgChecker;

/**
 * 
 */
public class ChiSquareDistributionMomentEstimator extends DistributionParameterEstimator<Double> {
  private final Function<double[], Double> _first = new SampleMomentCalculator(1);

  @Override
  public ProbabilityDistribution<Double> evaluate(final double[] x) {
    ArgChecker.notEmpty(x, "x");
    return new ChiSquareDistribution(_first.apply(x));
  }
}
