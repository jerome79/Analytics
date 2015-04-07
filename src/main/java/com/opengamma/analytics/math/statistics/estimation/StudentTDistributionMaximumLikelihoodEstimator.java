/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.statistics.estimation;

import java.util.function.DoubleUnaryOperator;
import java.util.function.Function;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.function.special.GammaFunction;
import com.opengamma.analytics.math.minimization.GoldenSectionMinimizer1D;
import com.opengamma.analytics.math.minimization.ScalarMinimizer;
import com.opengamma.analytics.math.statistics.descriptive.MeanCalculator;
import com.opengamma.analytics.math.statistics.descriptive.PopulationStandardDeviationCalculator;
import com.opengamma.analytics.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.analytics.math.statistics.distribution.StudentTDistribution;
import com.opengamma.strata.collect.ArgChecker;

/**
 * 
 */
public class StudentTDistributionMaximumLikelihoodEstimator extends DistributionParameterEstimator<Double> {
  // TODO add error estimates
  private final ScalarMinimizer _minimizer = new GoldenSectionMinimizer1D();
  private final DoubleUnaryOperator _gamma = new GammaFunction();
  private final Function1D<double[], Double> _mean = new MeanCalculator();
  private final Function<double[], Double> _std = new PopulationStandardDeviationCalculator();

  @Override
  public ProbabilityDistribution<Double> evaluate(final double[] x) {
    ArgChecker.notNull(x, "x");
    ArgChecker.notEmpty(x, "x");
    final double[] standardized = getStandardizedData(x);
    final Function1D<Double, Double> f = new Function1D<Double, Double>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final Double nu) {
        double sum = 0;
        for (final double t : standardized) {
          sum += Math.log(_gamma.applyAsDouble((nu + 1) / 2.) * Math.pow(1 + t * t / (nu - 2), -(nu + 1) / 2.) / Math.sqrt(Math.PI * (nu - 2)) / _gamma.applyAsDouble(nu / 2.));
        }
        return -sum;
      }

    };
    return new StudentTDistribution(_minimizer.minimize(f, 0.0, 3., 10.));
  }

  protected double[] getStandardizedData(final double[] x) {
    final double mean = _mean.evaluate(x);
    final double std = _std.apply(x);
    final double[] z = new double[x.length];
    for (int i = 0; i < x.length; i++) {
      z[i] = (x[i] - mean) / std;
    }
    return z;
  }
}
