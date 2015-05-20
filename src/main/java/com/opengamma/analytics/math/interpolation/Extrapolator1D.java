/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.strata.collect.named.Named;

/**
 * Interface for extrapolators which can extrapolate beyond the ends of a set of data.
 */
public interface Extrapolator1D extends Named {

  /**
   * Returns an extrapolated output value for the specified input value, interpolator and data bundle.
   *
   * @param data  the data bundle associated with the interpolator
   * @param value  the input data point
   * @param interpolator  the interpolator used in conjunction with this extrapolator
   * @return an extrapolated output value for the specified input value, interpolator and data bundle
   */
  public abstract Double extrapolate(Interpolator1DDataBundle data, Double value, Interpolator1D interpolator);

  /**
   * Returns the first derivative of the data at the specified point.
   *
   * @param data  the data bundle associated with the interpolator
   * @param value  the input data point
   * @param interpolator  the interpolator used in conjunction with this extrapolator
   * @return the first derivative of the data at the specified point
   */
  public abstract double firstDerivative(Interpolator1DDataBundle data, Double value, Interpolator1D interpolator);

  /**
   * Returns the node sensitivities of the data at the specified point.
   *
   * @param data  the data bundle associated with the interpolator
   * @param value  the input data point
   * @param interpolator  the interpolator used in conjunction with this extrapolator
   * @return the node sensitivities of the data at the specified point
   */
  public abstract double[] getNodeSensitivitiesForValue(
      Interpolator1DDataBundle data,
      Double value,
      Interpolator1D interpolator);
}
