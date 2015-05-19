/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.strata.basics.extrapolator.CurveExtrapolator;
import com.opengamma.strata.collect.ArgChecker;

/**
 * Log-linear extrapolator: the extrapolant is exp(f(x)) where f(x) is a linear function 
 * which is smoothly connected with a log-interpolator exp(F(x)), such as {@link LogNaturalCubicMonotonicityPreservingInterpolator1D}, 
 * i.e., F'(x) = f'(x) at a respectivie endpoint. 
 */
public class LogLinearExtrapolator1D implements CurveExtrapolator, Extrapolator1D {

  /** The extrapolator name. */
  public static final String NAME = "LogLinear";

  private double _eps;

  public LogLinearExtrapolator1D() {
    this(1e-8);
  }

  /**
   * @param eps Bump parameter of finite difference approximation for the first derivative value
   */
  public LogLinearExtrapolator1D(double eps) {
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
  public double[] getNodeSensitivitiesForValue(Interpolator1DDataBundle data, Double value, Interpolator1D interpolator) {
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
    double y = Math.log(data.firstValue());
    double m = interpolator.firstDerivative(data, x) / interpolator.interpolate(data, x);
    return Math.exp(y + (value - x) * m);
  }

  private Double rightExtrapolate(Interpolator1DDataBundle data, Double value, Interpolator1D interpolator) {
    ArgChecker.notNull(data, "data");
    ArgChecker.notNull(value, "value");
    double x = data.lastKey();
    double y = Math.log(data.lastValue());
    double m = interpolator.firstDerivative(data, x) / interpolator.interpolate(data, x);
    return Math.exp(y + (value - x) * m);
  }

  private Double leftExtrapolateDerivative(Interpolator1DDataBundle data, Double value, Interpolator1D interpolator) {
    ArgChecker.notNull(data, "data");
    ArgChecker.notNull(value, "value");
    double x = data.firstKey();
    double y = Math.log(data.firstValue());
    double m = interpolator.firstDerivative(data, x) / interpolator.interpolate(data, x);
    return m * Math.exp(y + (value - x) * m);
  }

  private Double rightExtrapolateDerivative(Interpolator1DDataBundle data, Double value, Interpolator1D interpolator) {
    ArgChecker.notNull(data, "data");
    ArgChecker.notNull(value, "value");
    double x = data.lastKey();
    double y = Math.log(data.lastValue());
    double m = interpolator.firstDerivative(data, x) / interpolator.interpolate(data, x);
    return m * Math.exp(y + (value - x) * m);
  }

  private double[] getLeftSensitivities(Interpolator1DDataBundle data, Double value, Interpolator1D interpolator) {
    double eps = _eps * (data.lastKey() - data.firstKey());
    double x = data.firstKey();
    double resValueInterpolator = interpolator.interpolate(data, x + eps);
    double resValueExtrapolator = leftExtrapolate(data, value, interpolator);
    double[] result = interpolator.getNodeSensitivitiesForValue(data, x + eps);
    double factor1 = (value - x) / eps;
    double factor2 = factor1 * resValueExtrapolator / resValueInterpolator;

    int n = result.length;
    for (int i = 1; i < n; i++) {
      result[i] *= factor2;
    }
    result[0] = result[0] * factor2 + (1. - factor1) * resValueExtrapolator / data.firstValue();
    return result;
  }

  private double[] getRightSensitivities(Interpolator1DDataBundle data, Double value, Interpolator1D interpolator) {
    double eps = _eps * (data.lastKey() - data.firstKey());
    double x = data.lastKey();
    double resValueInterpolator = interpolator.interpolate(data, x - eps);
    double resValueExtrapolator = rightExtrapolate(data, value, interpolator);
    double[] result = interpolator.getNodeSensitivitiesForValue(data, x - eps);
    double factor1 = (value - x) / eps;
    double factor2 = factor1 * resValueExtrapolator / resValueInterpolator;

    int n = result.length;
    for (int i = 0; i < n - 1; i++) {
      result[i] *= -factor2;
    }
    result[n - 1] = (1. + factor1) * resValueExtrapolator / data.lastValue() - result[n - 1] * factor2;
    return result;
  }
}
