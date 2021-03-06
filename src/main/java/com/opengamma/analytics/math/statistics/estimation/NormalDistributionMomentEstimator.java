/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.statistics.estimation;

import java.util.function.Function;

import com.opengamma.analytics.math.statistics.descriptive.SampleMomentCalculator;
import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.analytics.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.strata.collect.ArgChecker;

/**
 * 
 */
public class NormalDistributionMomentEstimator extends DistributionParameterEstimator<Double> {
  private final Function<double[], Double> _first = new SampleMomentCalculator(1);
  private final Function<double[], Double> _second = new SampleMomentCalculator(2);

  @Override
  public ProbabilityDistribution<Double> evaluate(final double[] x) {
    ArgChecker.notNull(x, "x");
    ArgChecker.notEmpty(x, "x");
    final double m1 = _first.apply(x);
    return new NormalDistribution(m1, Math.sqrt(_second.apply(x) - m1 * m1));
  }

}
