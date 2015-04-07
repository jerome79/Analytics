/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.statistics.estimation;

import java.util.function.Function;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.statistics.descriptive.MeanCalculator;
import com.opengamma.analytics.math.statistics.descriptive.PopulationStandardDeviationCalculator;
import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.analytics.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.strata.collect.ArgChecker;

/**
 * 
 */
public class NormalDistributionMaximumLikelihoodEstimator extends DistributionParameterEstimator<Double> {
  // TODO add error estimates
  private final Function1D<double[], Double> _mean = new MeanCalculator();
  private final Function<double[], Double> _std = new PopulationStandardDeviationCalculator();

  @Override
  public ProbabilityDistribution<Double> evaluate(final double[] x) {
    ArgChecker.notNull(x, "x");
    ArgChecker.notEmpty(x, "x");
    return new NormalDistribution(_mean.evaluate(x), _std.apply(x));
  }

}
