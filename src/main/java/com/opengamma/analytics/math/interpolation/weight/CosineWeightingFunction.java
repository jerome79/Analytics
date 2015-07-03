/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation.weight;

import com.opengamma.strata.collect.ArgChecker;

/**
 * Weighting function based on {@code Math.cos}.
 */
public final class CosineWeightingFunction extends WeightingFunction {

  private static final CosineWeightingFunction INSTANCE = new CosineWeightingFunction();

  public static CosineWeightingFunction getInstance() {
    return INSTANCE;
  }

  private CosineWeightingFunction() {
  }

  @Override
  public double getWeight(final double y) {
    ArgChecker.inRangeInclusive(y, 0d, 1d, "y");
    return Math.cos(Math.PI / 6 * (2.0 * y * y + y - 3.0));
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
    return "Cosine weighting function";
  }

}
