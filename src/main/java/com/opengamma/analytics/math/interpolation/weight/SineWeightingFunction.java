/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation.weight;

import com.opengamma.strata.collect.ArgChecker;

/**
 * Weighting function based on {@code Math.sin}.
 */
public final class SineWeightingFunction extends WeightingFunction {

  private static final SineWeightingFunction INSTANCE = new SineWeightingFunction();

  public static SineWeightingFunction getInstance() {
    return INSTANCE;
  }

  private SineWeightingFunction() {
  }

  @Override
  public double getWeight(final double y) {
    ArgChecker.inRangeInclusive(y, 0d, 1d, "y");
    return 0.5 * (Math.sin(Math.PI * (y - 0.5)) + 1);
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean equals(final Object obj) {
    if (obj == null) {
      return false;
    }
    if (this == obj) {
      return true;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    return toString().hashCode();
  }

  @Override
  public String toString() {
    return "Sine weighting function";
  }

}
