/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.statistics.distribution;

import cern.jet.random.engine.RandomEngine;
import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.strata.collect.ArgChecker;

/**
 * 
 */
public class StudentTTwoTailedCriticalValueCalculator extends Function1D<Double, Double> {
  private final Function1D<Double, Double> _calc;

  public StudentTTwoTailedCriticalValueCalculator(final double nu) {
    ArgChecker.notNegative(nu, "nu");
    _calc = new StudentTOneTailedCriticalValueCalculator(nu);
  }

  public StudentTTwoTailedCriticalValueCalculator(final double nu, final RandomEngine engine) {
    ArgChecker.notNegative(nu, "nu");
    Validate.notNull(engine);
    _calc = new StudentTOneTailedCriticalValueCalculator(nu, engine);
  }

  @Override
  public Double evaluate(final Double x) {
    Validate.notNull(x, "x");
    ArgChecker.notNegative(x, "x");
    return _calc.evaluate(0.5 + 0.5 * x);
  }
}
