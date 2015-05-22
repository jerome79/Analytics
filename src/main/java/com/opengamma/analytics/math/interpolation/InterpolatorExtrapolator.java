/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.strata.basics.interpolator.CurveExtrapolator;

/**
 * Extrapolator that does no extrapolation itself and delegates to the interpolator for all operations.
 * <p>
 * This reproduces the old behaviour in {@link CombinedInterpolatorExtrapolator} when the extrapolators were
 * null. This extrapolator is used in place of a null extrapolator which allows the extrapolators to be non-null
 * and makes for simpler and cleaner code where the extrapolators are used.
 */
public final class InterpolatorExtrapolator implements CurveExtrapolator, Extrapolator1D {

  /** The interpolator name. */
  public static final String NAME = "Interpolator";

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public Double extrapolate(Interpolator1DDataBundle data, Double value, Interpolator1D interpolator) {
    return interpolator.interpolate(data, value);
  }

  @Override
  public double firstDerivative(Interpolator1DDataBundle data, Double value, Interpolator1D interpolator) {
    return interpolator.firstDerivative(data, value);
  }

  @Override
  public double[] getNodeSensitivitiesForValue(Interpolator1DDataBundle data, Double value, Interpolator1D interpolator) {
    return interpolator.getNodeSensitivitiesForValue(data, value);
  }
}
