/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.strata.collect.ArgChecker;

/**
 *
 */
public class CombinedInterpolatorExtrapolator extends Interpolator1D {

  private static final long serialVersionUID = 1L;
  private final Interpolator1D _interpolator;
  private final Extrapolator1D _leftExtrapolator;
  private final Extrapolator1D _rightExtrapolator;

  public CombinedInterpolatorExtrapolator(Interpolator1D interpolator) {
    ArgChecker.notNull(interpolator, "interpolator");

    _interpolator = interpolator;
    _leftExtrapolator = null;
    _rightExtrapolator = null;
  }

  public CombinedInterpolatorExtrapolator(Interpolator1D interpolator, Extrapolator1D extrapolator) {
    ArgChecker.notNull(interpolator, "interpolator");
    ArgChecker.notNull(extrapolator, "extrapolator");

    _interpolator = interpolator;
    _leftExtrapolator = extrapolator;
    _rightExtrapolator = extrapolator;
  }

  public CombinedInterpolatorExtrapolator(
      Interpolator1D interpolator,
      Extrapolator1D leftExtrapolator,
      Extrapolator1D rightExtrapolator) {

    ArgChecker.notNull(interpolator, "interpolator");
    ArgChecker.notNull(leftExtrapolator, "left extrapolator");
    ArgChecker.notNull(rightExtrapolator, "right extrapolator");

    _interpolator = interpolator;
    _leftExtrapolator = leftExtrapolator;
    _rightExtrapolator = rightExtrapolator;
  }

  @Override
  public Interpolator1DDataBundle getDataBundle(double[] x, double[] y) {
    return _interpolator.getDataBundle(x, y);
  }

  @Override
  public Interpolator1DDataBundle getDataBundleFromSortedArrays(double[] x, double[] y) {
    return _interpolator.getDataBundleFromSortedArrays(x, y);
  }

  /**
   * Package-private getter only used in tests.
   *
   * @return the interpolator
   */
  Interpolator1D getInterpolator() {
    return _interpolator;
  }

  /**
   * Package-private getter only used in tests.
   *
   * @return the left extrapolator
   */
  Extrapolator1D getLeftExtrapolator() {
    return _leftExtrapolator;
  }

  /**
   * Package-private getter only used in tests.
   *
   * @return the right extrapolator
   */
  Extrapolator1D getRightExtrapolator() {
    return _rightExtrapolator;
  }

  //TODO  fail earlier if there's no extrapolators?
  @Override
  public Double interpolate(Interpolator1DDataBundle data, Double value) {
    ArgChecker.notNull(data, "data");
    ArgChecker.notNull(value, "value");

    if (value < data.firstKey()) {
      if (_leftExtrapolator != null) {
        return _leftExtrapolator.extrapolate(data, value, _interpolator);
      }
    } else if (value > data.lastKey()) {
      if (_rightExtrapolator != null) {
        return _rightExtrapolator.extrapolate(data, value, _interpolator);
      }
    }
    return _interpolator.interpolate(data, value);
  }

  @Override
  public double firstDerivative(Interpolator1DDataBundle data, Double value) {
    ArgChecker.notNull(data, "data");
    ArgChecker.notNull(value, "value");

    if (value < data.firstKey()) {
      if (_leftExtrapolator != null) {
        return _leftExtrapolator.firstDerivative(data, value, _interpolator);
      }
    } else if (value > data.lastKey()) {
      if (_rightExtrapolator != null) {
        return _rightExtrapolator.firstDerivative(data, value, _interpolator);
      }
    }
    return _interpolator.firstDerivative(data, value);
  }

  @Override
  public double[] getNodeSensitivitiesForValue(Interpolator1DDataBundle data, Double value) {
    ArgChecker.notNull(data, "data");
    ArgChecker.notNull(value, "value");

    if (value < data.firstKey()) {
      if (_leftExtrapolator != null) {
        return _leftExtrapolator.getNodeSensitivitiesForValue(data, value, _interpolator);
      }
    } else if (value > data.lastKey()) {
      if (_rightExtrapolator != null) {
        return _rightExtrapolator.getNodeSensitivitiesForValue(data, value, _interpolator);
      }
    }
    return _interpolator.getNodeSensitivitiesForValue(data, value);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("Interpolator[interpolator=");
    sb.append(_interpolator.toString());
    sb.append(", left extrapolator=");
    sb.append(_leftExtrapolator.toString());
    sb.append(", right extrapolator=");
    sb.append(_rightExtrapolator.toString());
    sb.append("]");
    return sb.toString();
  }
}
