/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.statistics.distribution;

import cern.jet.random.engine.RandomEngine;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.util.CompareUtils;
import com.opengamma.strata.collect.ArgChecker;

/**
 *
 */
public class StudentTOneTailedCriticalValueCalculator extends Function1D<Double, Double> {
  private final ProbabilityDistribution<Double> _dist;

  public StudentTOneTailedCriticalValueCalculator(final double nu) {
    ArgChecker.notNegative(nu, "nu");
    _dist = new StudentTDistribution(nu);
  }

  public StudentTOneTailedCriticalValueCalculator(final double nu, final RandomEngine engine) {
    ArgChecker.notNegative(nu, "nu");
    ArgChecker.notNull(engine, "engine");
    _dist = new StudentTDistribution(nu, engine);
  }

  @Override
  public Double evaluate(final Double x) {
    ArgChecker.notNull(x, "x");
    ArgChecker.notNegative(x, "x");
    if (CompareUtils.closeEquals(x, 0.5, 1e-14)) {
      return 0.5;
    }
    return _dist.getInverseCDF(x);
  }
}
