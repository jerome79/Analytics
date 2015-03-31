/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.random;

import java.util.ArrayList;
import java.util.List;

import cern.jet.random.engine.RandomEngine;
import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.analytics.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.strata.collect.ArgChecker;

/**
 * 
 */
public class NormalRandomNumberGenerator implements RandomNumberGenerator {
  private final ProbabilityDistribution<Double> _normal;

  public NormalRandomNumberGenerator(final double mean, final double sigma) {
    ArgChecker.notNegativeOrZero(sigma, "standard deviation");
    _normal = new NormalDistribution(mean, sigma);
  }

  public NormalRandomNumberGenerator(final double mean, final double sigma, final RandomEngine engine) {
    ArgChecker.notNegativeOrZero(sigma, "standard deviation");
    Validate.notNull(engine, "engine");
    _normal = new NormalDistribution(mean, sigma, engine);
  }

  @Override
  public double[] getVector(final int dimension) {
    ArgChecker.notNegative(dimension, "dimension");
    final double[] result = new double[dimension];
    for (int i = 0; i < dimension; i++) {
      result[i] = _normal.nextRandom();
    }
    return result;
  }

  @Override
  public List<double[]> getVectors(final int dimension, final int n) {
    if (dimension < 0) {
      throw new IllegalArgumentException("Dimension must be greater than zero");
    }
    if (n < 0) {
      throw new IllegalArgumentException("Number of values must be greater than zero");
    }
    final List<double[]> result = new ArrayList<>(n);
    double[] x;
    for (int i = 0; i < n; i++) {
      x = new double[dimension];
      for (int j = 0; j < dimension; j++) {
        x[j] = _normal.nextRandom();
      }
      result.add(x);
    }
    return result;
  }
}
