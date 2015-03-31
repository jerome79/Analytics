/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.statistics.descriptive;

import java.util.function.DoubleBinaryOperator;

import com.opengamma.strata.collect.ArgChecker;

/**
 * 
 */
public class LognormalPearsonKurtosisFromVolatilityCalculator implements DoubleBinaryOperator {
  private static final LognormalFisherKurtosisFromVolatilityCalculator CALCULATOR =
      new LognormalFisherKurtosisFromVolatilityCalculator();

  @Override
  public double applyAsDouble(double sigma, double t) {
    ArgChecker.notNull(sigma, "sigma");
    ArgChecker.notNull(t, "t");
    return CALCULATOR.applyAsDouble(sigma, t) + 3;
  }

}
