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
public class LinearExtrapolator1D implements CurveExtrapolator, Extrapolator1D {

  /** The extrapolator name. */
  public static final String NAME = "Linear";

  private final double _eps;

  public LinearExtrapolator1D() {
    this(1e-8);
  }

  public LinearExtrapolator1D(double eps) {
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

    if (value < data.firstKey()) {
      return leftExtrapolate(data, value, interpolator);
    } else if (value > data.lastKey()) {
      return rightExtrapolate(data, value, interpolator);
    }
    throw new IllegalArgumentException("Value " + value + " was within data range");
  }

  @Override
  public double firstDerivative(Interpolator1DDataBundle data, Double value, Interpolator1D interpolator) {
    ArgChecker.notNull(data, "data");
    ArgChecker.notNull(value, "value");

    if (value < data.firstKey()) {
      return leftExtrapolateDerivative(data, value, interpolator);
    } else if (value > data.lastKey()) {
      return rightExtrapolateDerivative(data, value, interpolator);
    }
    throw new IllegalArgumentException("Value " + value + " was within data range");
  }

  @Override
  public double[] getNodeSensitivitiesForValue(
      Interpolator1DDataBundle data,
      Double value,
      Interpolator1D interpolator) {

    ArgChecker.notNull(data, "data");

    if (value < data.firstKey()) {
      return getLeftSensitivities(data, value, interpolator);
    } else if (value > data.lastKey()) {
      return getRightSensitivities(data, value, interpolator);
    }
    throw new IllegalArgumentException("Value " + value + " was within data range");
  }

  private Double leftExtrapolate(Interpolator1DDataBundle data, Double value, Interpolator1D interpolator) {
    ArgChecker.notNull(data, "data");
    ArgChecker.notNull(value, "value");

    double x = data.firstKey();
    double y = data.firstValue();
    double eps = _eps * (data.lastKey() - x);
    double m = (interpolator.interpolate(data, x + eps) - y) / eps;
    return y + (value - x) * m;
  }

  private Double rightExtrapolate(Interpolator1DDataBundle data, Double value, Interpolator1D interpolator) {
    ArgChecker.notNull(data, "data");
    ArgChecker.notNull(value, "value");
    double x = data.lastKey();
    double y = data.lastValue();
    double eps = _eps * (x - data.firstKey());
    double m = (y - interpolator.interpolate(data, x - eps)) / eps;
    return y + (value - x) * m;
  }

  private Double leftExtrapolateDerivative(Interpolator1DDataBundle data, Double value, Interpolator1D interpolator) {
    ArgChecker.notNull(data, "data");
    ArgChecker.notNull(value, "value");
    double x = data.firstKey();
    double y = data.firstValue();
    double eps = _eps * (data.lastKey() - x);
    double m = (interpolator.interpolate(data, x + eps) - y) / eps;
    return m;
  }

  private Double rightExtrapolateDerivative(Interpolator1DDataBundle data, Double value, Interpolator1D interpolator) {
    ArgChecker.notNull(data, "data");
    ArgChecker.notNull(value, "value");
    double x = data.lastKey();
    double y = data.lastValue();
    double eps = _eps * (x - data.firstKey());
    double m = (y - interpolator.interpolate(data, x - eps)) / eps;
    return m;
  }

  private double[] getLeftSensitivities(Interpolator1DDataBundle data, double value, Interpolator1D interpolator) {
    double eps = _eps * (data.lastKey() - data.firstKey());
    double x = data.firstKey();
    double[] result = interpolator.getNodeSensitivitiesForValue(data, x + eps);
    int n = result.length;
    for (int i = 1; i < n; i++) {
      result[i] = result[i] * (value - x) / eps;
    }
    result[0] = 1 + (result[0] - 1) * (value - x) / eps;
    return result;
  }

  private double[] getRightSensitivities(Interpolator1DDataBundle data, Double value, Interpolator1D interpolator) {
    double eps = _eps * (data.lastKey() - data.firstKey());
    double x = data.lastKey();
    double[] result = interpolator.getNodeSensitivitiesForValue(data, x - eps);
    int n = result.length;
    for (int i = 0; i < n - 1; i++) {
      result[i] = -result[i] * (value - x) / eps;
    }
    result[n - 1] = 1 + (1 - result[n - 1]) * (value - x) / eps;
    return result;
  }
}
