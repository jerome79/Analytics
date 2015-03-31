/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation;

import com.opengamma.strata.collect.ArgChecker;

/**
 * 
 */
// TODO this belongs with interpolators
public final class SineWeightingFunction extends WeightingFunction {
  private static final SineWeightingFunction s_instance = new SineWeightingFunction();

  public static SineWeightingFunction getInstance() {
    return s_instance;
  }

  private SineWeightingFunction() {
  }

  @Override
  public double getWeight(final double y) {
    ArgChecker.isInRangeInclusive(0, 1, y);
    return 0.5 * (Math.sin(Math.PI * (y - 0.5)) + 1);
  }

  @Override
  public String toString() {
    return "Sine weighting function";
  }

  @Override
  public int hashCode() {
    return toString().hashCode();
  }

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
}
