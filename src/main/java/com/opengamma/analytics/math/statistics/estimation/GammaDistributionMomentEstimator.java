/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.statistics.estimation;

import java.util.function.Function;

import com.opengamma.analytics.math.statistics.descriptive.SampleMomentCalculator;
import com.opengamma.analytics.math.statistics.distribution.GammaDistribution;
import com.opengamma.analytics.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.strata.collect.ArgChecker;

/**
 * 
 */
public class GammaDistributionMomentEstimator extends DistributionParameterEstimator<Double> {
  private final Function<double[], Double> _first = new SampleMomentCalculator(1);
  private final Function<double[], Double> _second = new SampleMomentCalculator(2);

  @Override
  public ProbabilityDistribution<Double> evaluate(final double[] x) {
    ArgChecker.notNull(x, "x");
    ArgChecker.notEmpty(x, "x");
    final double m1 = _first.apply(x);
    final double m2 = _second.apply(x);
    final double m1Sq = m1 * m1;
    final double k = m1Sq / (m2 - m1Sq);
    final double theta = m1 / k;
    return new GammaDistribution(k, theta);
  }

}
