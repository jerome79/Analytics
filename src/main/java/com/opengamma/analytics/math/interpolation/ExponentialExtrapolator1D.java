/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.strata.basics.interpolator.CurveExtrapolator;
import com.opengamma.strata.collect.ArgChecker;

/**
 * Extrapolator based on a exponential function. Outside the data range the function is an exponential exp(m*x) where m is such that 
 *  - on the left: exp(m * data.firstKey()) = data.firstValue()
 *  - on the right: exp(m * data.lastKey()) = data.lastValue()
 */
public class ExponentialExtrapolator1D implements CurveExtrapolator, Extrapolator1D {

  /** The extrapolator name. */
  public static final String NAME = "Exponential";

  @Override
  public Double extrapolate(Interpolator1DDataBundle data, Double value, Interpolator1D interpolator) {
    ArgChecker.notNull(data, "data");
    ArgChecker.notNull(value, "value");
    if (value < data.firstKey()) {
      return leftExtrapolate(data, value);
    } else if (value > data.lastKey()) {
      return rightExtrapolate(data, value);
    }
    throw new IllegalArgumentException("Value " + value + " was within data range");
  }

  @Override
  public double firstDerivative(Interpolator1DDataBundle data, Double value, Interpolator1D interpolator) {
    ArgChecker.notNull(data, "data");
    ArgChecker.notNull(value, "value");
    if (value < data.firstKey()) {
      return leftExtrapolateDerivative(data, value);
    } else if (value > data.lastKey()) {
      return rightExtrapolateDerivative(data, value);
    }
    throw new IllegalArgumentException("Value " + value + " was within data range");
  }

  @Override
  public double[] getNodeSensitivitiesForValue(Interpolator1DDataBundle data, Double value, Interpolator1D interpolator) {
    ArgChecker.notNull(data, "data");
    if (value < data.firstKey()) {
      return getLeftSensitivities(data, value);
    } else if (value > data.lastKey()) {
      return getRightSensitivities(data, value);
    }
    throw new IllegalArgumentException("Value " + value + " was within data range");
  }

  @Override
  public String getName() {
    return NAME;
  }

  private Double leftExtrapolate(Interpolator1DDataBundle data, Double value) {
    ArgChecker.notNull(data, "data");
    ArgChecker.notNull(value, "value");
    double x = data.firstKey();
    double y = data.firstValue();
    double m = Math.log(y) / x;
    return Math.exp(m * value);
  }

  private Double rightExtrapolate(Interpolator1DDataBundle data, Double value) {
    ArgChecker.notNull(data, "data");
    ArgChecker.notNull(value, "value");
    double x = data.lastKey();
    double y = data.lastValue();
    double m = Math.log(y) / x;
    return Math.exp(m * value);
  }

  private Double leftExtrapolateDerivative(Interpolator1DDataBundle data, Double value) {
    ArgChecker.notNull(data, "data");
    ArgChecker.notNull(value, "value");
    double x = data.firstKey();
    double y = data.firstValue();
    double m = Math.log(y) / x;
    return m * Math.exp(m * value);
  }

  private Double rightExtrapolateDerivative(Interpolator1DDataBundle data, Double value) {
    ArgChecker.notNull(data, "data");
    ArgChecker.notNull(value, "value");
    double x = data.lastKey();
    double y = data.lastValue();
    double m = Math.log(y) / x;
    return m * Math.exp(m * value);
  }

  private double[] getLeftSensitivities(Interpolator1DDataBundle data, double value) {
    ArgChecker.notNull(data, "data");
    ArgChecker.notNull(value, "value");
    double x = data.firstKey();
    double y = data.firstValue();
    double m = Math.log(y) / x;
    double ex = Math.exp(m * value);
    double[] result = new double[data.size()];
    result[0] = ex * value / (x * y);
    return result;
  }

  private double[] getRightSensitivities(Interpolator1DDataBundle data, Double value) {
    ArgChecker.notNull(data, "data");
    ArgChecker.notNull(value, "value");
    double x = data.lastKey();
    double y = data.lastValue();
    double m = Math.log(y) / x;
    double ex = Math.exp(m * value);
    double[] result = new double[data.size()];
    result[data.size() - 1] = ex * value / (x * y);
    return result;
  }

}
