/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import com.opengamma.analytics.math.function.PiecewisePolynomialFunction1D;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DPiecewisePoynomialWithExtraKnotsDataBundle;
import com.opengamma.strata.basics.extrapolator.CurveExtrapolator;
import com.opengamma.strata.collect.ArgChecker;

/**
 * Given a data set {xValues[i], yValues[i]}, extrapolate {x[i], x[i] * y[i]} by a polynomial function defined by ProductPiecewisePolynomialInterpolator1D, 
 * that is, use polynomial coefficients for the leftmost (rightmost) interval obtained in ProductPiecewisePolynomialInterpolator1D.
 */
public class ProductPolynomialExtrapolator1D implements CurveExtrapolator, Extrapolator1D {

  /** The extrapolator name. */
  public static final String NAME = "ProductPolynomial";

  private static final double SMALL = 1e-14;

  private final PiecewisePolynomialFunction1D _func;

  /**
   * The extrapolator using PiecewisePolynomialWithSensitivityFunction1D
   */
  public ProductPolynomialExtrapolator1D() {
    this(new PiecewisePolynomialFunction1D());
  }

  /**
   * The extrapolator using a specific polynomial function
   *
   * @param func The polynomial function
   */
  public ProductPolynomialExtrapolator1D(PiecewisePolynomialFunction1D func) {
    ArgChecker.notNull(func, "func");
    _func = func;
  }

  @Override
  public String getName() {
    return NAME;
  }

  /**
   * {@inheritDoc}
   * For small Math.abs(value), this method returns the exact value if clamped at (0,0), 
   * otherwise this returns a reference value
   */
  @Override
  public Double extrapolate(Interpolator1DDataBundle data, Double value, Interpolator1D interpolator) {
    ArgChecker.notNull(data, "data");
    ArgChecker.notNull(value, "value");
    ArgChecker.isTrue(value < data.firstKey() || value > data.lastKey(), "value was within data range");
    ArgChecker.isTrue(interpolator instanceof ProductPiecewisePolynomialInterpolator1D,
                      "This interpolator should be used with ProductPiecewisePolynomialInterpolator1D");
    ArgChecker.isTrue(data instanceof Interpolator1DPiecewisePoynomialWithExtraKnotsDataBundle);

    ProductPiecewisePolynomialInterpolator1D ppInterpolator = (ProductPiecewisePolynomialInterpolator1D) interpolator;
    Interpolator1DPiecewisePoynomialWithExtraKnotsDataBundle polyData =
        (Interpolator1DPiecewisePoynomialWithExtraKnotsDataBundle) data;
    return ppInterpolator.interpolate(polyData, value, _func, SMALL);
  }

  /**
   * {@inheritDoc}
   * For small Math.abs(value), this method returns the exact value if clamped at (0,0), 
   * otherwise this returns a reference value
   */
  @Override
  public double firstDerivative(Interpolator1DDataBundle data, Double value, Interpolator1D interpolator) {
    ArgChecker.notNull(data, "data");
    ArgChecker.notNull(value, "value");
    ArgChecker.isTrue(value < data.firstKey() || value > data.lastKey(), "value was within data range");
    ArgChecker.isTrue(interpolator instanceof ProductPiecewisePolynomialInterpolator1D,
                      "This interpolator should be used with ProductPiecewisePolynomialInterpolator1D");
    ArgChecker.isTrue(data instanceof Interpolator1DPiecewisePoynomialWithExtraKnotsDataBundle);

    Interpolator1DPiecewisePoynomialWithExtraKnotsDataBundle polyData =
        (Interpolator1DPiecewisePoynomialWithExtraKnotsDataBundle) data;
    ProductPiecewisePolynomialInterpolator1D ppInterpolator = (ProductPiecewisePolynomialInterpolator1D) interpolator;
    return ppInterpolator.firstDerivative(polyData, value, _func, SMALL);
  }

  /**
   * {@inheritDoc}
   * For small Math.abs(value), this method returns the exact value if clamped at (0,0), 
   * otherwise this returns a reference value
   */
  @Override
  public double[] getNodeSensitivitiesForValue(Interpolator1DDataBundle data, Double value, Interpolator1D interpolator) {
    ArgChecker.notNull(data, "data");
    ArgChecker.notNull(value, "value");
    ArgChecker.isTrue(value < data.firstKey() || value > data.lastKey(), "value was within data range");
    ArgChecker.isTrue(interpolator instanceof ProductPiecewisePolynomialInterpolator1D,
                      "This interpolator should be used with ProductPiecewisePolynomialInterpolator1D");
    ArgChecker.isTrue(data instanceof Interpolator1DPiecewisePoynomialWithExtraKnotsDataBundle);

    Interpolator1DPiecewisePoynomialWithExtraKnotsDataBundle polyData =
        (Interpolator1DPiecewisePoynomialWithExtraKnotsDataBundle) data;
    ProductPiecewisePolynomialInterpolator1D ppInterpolator = (ProductPiecewisePolynomialInterpolator1D) interpolator;
    return ppInterpolator.getNodeSensitivitiesForValue(polyData, value, _func, SMALL);
  }
}
