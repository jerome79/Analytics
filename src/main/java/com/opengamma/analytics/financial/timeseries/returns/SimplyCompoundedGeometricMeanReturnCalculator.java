/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.timeseries.returns;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.strata.collect.ArgChecker;

/**
 * 
 */
public class SimplyCompoundedGeometricMeanReturnCalculator extends Function1D<double[], Double> {

  @Override
  public Double evaluate(final double[] x) {
    ArgChecker.notNull(x, "x");
    ArgChecker.notEmpty(x, "x");
    final int n = x.length;
    double mult = 1 + x[0];
    for (int i = 1; i < n; i++) {
      mult *= 1 + x[i];
    }
    return Math.pow(mult, 1. / n) - 1;
  }
}
