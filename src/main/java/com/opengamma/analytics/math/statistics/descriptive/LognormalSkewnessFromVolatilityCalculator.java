/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.statistics.descriptive;

import java.util.function.DoubleBinaryOperator;

/**
 * 
 */
public class LognormalSkewnessFromVolatilityCalculator implements DoubleBinaryOperator {

  @Override
  public double applyAsDouble(double sigma, double t) {
    final double y = Math.sqrt(Math.exp(sigma * sigma * t) - 1);
    return y * (3 + y * y);
  }

}
