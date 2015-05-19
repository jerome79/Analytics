/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.strata.basics.extrapolator.CurveExtrapolator;
import com.opengamma.strata.collect.ArgChecker;

/**
 * 
 */
public class FlatExtrapolator1D implements CurveExtrapolator, Extrapolator1D {

  /** The extrapolator name. */
  public static final String NAME = "Flat";

  @Override
  public Double extrapolate(final Interpolator1DDataBundle data, final Double value, Interpolator1D interpolator) {
    ArgChecker.notNull(data, "data");
    ArgChecker.notNull(value, "value");
    if (value < data.firstKey()) {
      return data.firstValue();
    } else if (value > data.lastKey()) {
      return data.lastValue();
    }
    throw new IllegalArgumentException("Value " + value + " was within data range");
  }

  @Override
  public double firstDerivative(final Interpolator1DDataBundle data, final Double value, Interpolator1D interpolator) {
    ArgChecker.notNull(data, "data");
    ArgChecker.notNull(value, "value");
    if (value < data.firstKey()) {
      return 0.;
    } else if (value > data.lastKey()) {
      return 0.;
    }
    throw new IllegalArgumentException("Value " + value + " was within data range");
  }

  @Override
  public double[] getNodeSensitivitiesForValue(
      final Interpolator1DDataBundle data,
      final Double value,
      Interpolator1D interpolator) {

    ArgChecker.notNull(data, "data");

    final int n = data.size();
    if (value < data.firstKey()) {
      final double[] result = new double[n];
      result[0] = 1;
      return result;
    } else if (value > data.lastKey()) {
      final double[] result = new double[n];
      result[n - 1] = 1;
      return result;
    }
    throw new IllegalArgumentException("Value " + value + " was within data range");
  }

  @Override
  public String getName() {
    return NAME;
  }
}
