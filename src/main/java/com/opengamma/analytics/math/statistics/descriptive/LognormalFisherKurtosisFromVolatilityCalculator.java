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
public class LognormalFisherKurtosisFromVolatilityCalculator implements DoubleBinaryOperator {

  @Override
  public double applyAsDouble(final double sigma, final double t) {
    final double y = Math.sqrt(Math.exp(sigma * sigma * t) - 1);
    final double y2 = y * y;
    return y2 * (16 + y2 * (15 + y2 * (6 + y2)));
  }

}
