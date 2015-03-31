/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.statistics.descriptive.robust;

import java.util.Arrays;
import java.util.function.Function;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.statistics.descriptive.MeanCalculator;

/**
 * 
 */
public class TrimmedMeanCalculator implements Function<double[], Double> {
  private static final Function1D<double[], Double> MEAN_CALCULATOR = new MeanCalculator();
  private final double _gamma;

  public TrimmedMeanCalculator(final double gamma) {
    Validate.isTrue(gamma >= 0 && gamma <= 1, "Gamma must be between 0 and 1, have {}", gamma);
    _gamma = gamma > 0.5 ? 1 - gamma : gamma;
  }

  @Override
  public Double apply(final double[] x) {
    Validate.notNull(x, "x was null");
    final int length = x.length;
    Validate.isTrue(length > 0, "x was empty");
    final int value = (int) Math.round(length * _gamma);
    final double[] copy = Arrays.copyOf(x, length);
    Arrays.sort(copy);
    final double[] trimmed = new double[length - 2 * value];
    System.arraycopy(x, value, trimmed, 0, trimmed.length);
    return MEAN_CALCULATOR.evaluate(trimmed);
  }
}
