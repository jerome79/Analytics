/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import com.opengamma.analytics.math.interpolation.data.ArrayInterpolator1DDataBundle;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.strata.collect.ArgChecker;

/**
 * 
 */
public class StepInterpolator1D extends Interpolator1D {
  private static final long serialVersionUID = 1L;

  @Override
  public Double interpolate(final Interpolator1DDataBundle data, final Double value) {
    ArgChecker.notNull(value, "value");
    ArgChecker.notNull(data, "data bundle");
    return data.get(data.getLowerBoundKey(value));
  }

  @Override
  public double firstDerivative(final Interpolator1DDataBundle data, final Double x) {
    ArgChecker.notNull(x, "value");
    ArgChecker.notNull(data, "data bundle");
    return 0.;
  }

  @Override
  public Interpolator1DDataBundle getDataBundle(final double[] x, final double[] y) {
    return new ArrayInterpolator1DDataBundle(x, y);
  }

  @Override
  public Interpolator1DDataBundle getDataBundleFromSortedArrays(final double[] x, final double[] y) {
    return new ArrayInterpolator1DDataBundle(x, y, true);
  }

  @Override
  public double[] getNodeSensitivitiesForValue(Interpolator1DDataBundle data, Double value) {
    return getFiniteDifferenceSensitivities(data, value);
  }

}
