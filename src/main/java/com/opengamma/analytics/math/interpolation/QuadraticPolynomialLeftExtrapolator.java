/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import java.io.Serializable;

import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.strata.basics.interpolator.CurveExtrapolator;
import com.opengamma.strata.collect.ArgChecker;

/**
 * This left extrapolator is designed for extrapolating a discount factor where the trivial point (0.,1.) is NOT involved in the data. 
 * The extrapolation is completed by applying a quadratic extrapolant on the discount factor (not log of the discount factor), 
 * where the point (0.,1.) is inserted and the first derivative value is assumed to be continuous at firstKey.
 */
public class QuadraticPolynomialLeftExtrapolator implements CurveExtrapolator, Extrapolator1D, Serializable {

  private static final long serialVersionUID = 1L;

  /** The extrapolator name. */
  public static final String NAME = "QuadraticLeft";

  private final double _eps;

  /**
   *
   */
  public QuadraticPolynomialLeftExtrapolator() {
    this(1e-8);
  }

  /**
   * @param eps Bump parameter of finite difference approximation for the first derivative value
   */
  public QuadraticPolynomialLeftExtrapolator(double eps) {
    _eps = eps;
  }

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public Double extrapolate(Interpolator1DDataBundle data, Double value, Interpolator1D interpolator) {
    ArgChecker.notNull(data, "data");
    ArgChecker.notNull(value, "value");
    if (data.firstKey() == 0.) {
      throw new IllegalArgumentException("The trivial point at key=0. is already included");
    }
    if (value < data.firstKey()) {
      return leftExtrapolate(data, value, interpolator);
    } else if (value > data.lastKey()) {
      throw new IllegalArgumentException("Value " + value + " was greater than data range");
    }
    throw new IllegalArgumentException("Value " + value + " was within data range");
  }

  @Override
  public double firstDerivative(Interpolator1DDataBundle data, Double value, Interpolator1D interpolator) {
    ArgChecker.notNull(data, "data");
    ArgChecker.notNull(value, "value");
    if (data.firstKey() == 0.) {
      throw new IllegalArgumentException("The trivial point at key=0. is already included");
    }
    if (value < data.firstKey()) {
      return leftExtrapolateDerivative(data, value, interpolator);
    } else if (value > data.lastKey()) {
      throw new IllegalArgumentException("Value " + value + " was greater than data range");
    }
    throw new IllegalArgumentException("Value " + value + " was within data range");
  }

  @Override
  public double[] getNodeSensitivitiesForValue(Interpolator1DDataBundle data, Double value, Interpolator1D interpolator) {
    ArgChecker.notNull(data, "data");
    if (data.firstKey() == 0.) {
      throw new IllegalArgumentException("The trivial point at key=0. is already included");
    }
    if (value < data.firstKey()) {
      return getLeftSensitivities(data, value, interpolator);
    } else if (value > data.lastKey()) {
      throw new IllegalArgumentException("Value " + value + " was greater than data range");
    }
    throw new IllegalArgumentException("Value " + value + " was within data range");
  }

  private Double leftExtrapolate(Interpolator1DDataBundle data, Double value, Interpolator1D interpolator) {
    ArgChecker.notNull(data, "data");
    ArgChecker.notNull(value, "value");
    double x = data.firstKey();
    double y = data.firstValue();
    double m = interpolator.firstDerivative(data, x);
    double quadCoef = m / x - (y - 1.) / x / x;
    double linCoef = -m + 2. * (y - 1.) / x;
    return quadCoef * value * value + linCoef * value + 1.;
  }

  private Double leftExtrapolateDerivative(Interpolator1DDataBundle data, Double value, Interpolator1D interpolator) {
    ArgChecker.notNull(data, "data");
    ArgChecker.notNull(value, "value");
    double x = data.firstKey();
    double y = data.firstValue();
    double m = interpolator.firstDerivative(data, x);
    double quadCoef = m / x - (y - 1.) / x / x;
    double linCoef = -m + 2. * (y - 1.) / x;
    return 2. * quadCoef * value + linCoef;
  }

  private double[] getLeftSensitivities(Interpolator1DDataBundle data, Double value, Interpolator1D interpolator) {
    double eps = _eps * (data.lastKey() - data.firstKey());
    double x = data.firstKey();
    double[] result = interpolator.getNodeSensitivitiesForValue(data, x + eps);

    int n = result.length;
    for (int i = 1; i < n; i++) {
      double tmp = result[i] * value / eps;
      result[i] = tmp / x * value - tmp;
    }
    double tmp = (result[0] - 1.) / eps;
    result[0] = (tmp / x - 1. / x / x) * value * value + (2. / x - tmp) * value;
    return result;
  }

}
