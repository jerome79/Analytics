/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.minimization;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.util.CompareUtils;
import com.opengamma.strata.collect.ArgChecker;

/**
 * 
 */
public abstract class MinimumBracketer {
  private static final double ZERO = 1e-15;
  /**
   * 
   */
  protected static final double GOLDEN = 0.61803399;

  public abstract double[] getBracketedPoints(Function1D<Double, Double> f, double xLower, double xUpper);

  protected void checkInputs(final Function1D<Double, Double> f, final double xLower, final double xUpper) {
    ArgChecker.notNull(f, "function");
    if (CompareUtils.closeEquals(xLower, xUpper, ZERO)) {
      throw new IllegalArgumentException("Lower and upper values were not distinct");
    }
  }
}
