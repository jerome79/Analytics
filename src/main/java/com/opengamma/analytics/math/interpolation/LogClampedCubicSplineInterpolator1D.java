/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import com.opengamma.analytics.math.function.PiecewisePolynomialWithSensitivityFunction1D;
import com.opengamma.analytics.math.interpolation.data.ArrayInterpolator1DDataBundle;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DLogPiecewisePoynomialDataBundle;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.strata.collect.ArgChecker;

/**
 * Find a interpolant F(x) = exp( f(x) ) where f(x) is a cubic spline with clamped endpoint conditions {@link ClampedCubicSplineInterpolator1D}. 
 */
public class LogClampedCubicSplineInterpolator1D extends PiecewisePolynomialInterpolator1D {

  /** Serialization version */
  private static final long serialVersionUID = 1L;

  private static final PiecewisePolynomialWithSensitivityFunction1D FUNC = new PiecewisePolynomialWithSensitivityFunction1D();

  /**
   * 
   */
  public LogClampedCubicSplineInterpolator1D() {
    super(new CubicSplineInterpolator());
  }

  @Override
  public Double interpolate(final Interpolator1DDataBundle data, final Double value) {
    ArgChecker.notNull(value, "value");
    ArgChecker.notNull(data, "data bundle");
    ArgChecker.isTrue(data instanceof Interpolator1DLogPiecewisePoynomialDataBundle);
    final Interpolator1DLogPiecewisePoynomialDataBundle polyData = (Interpolator1DLogPiecewisePoynomialDataBundle) data;
    final DoubleMatrix1D res = FUNC.evaluate(polyData.getPiecewisePolynomialResultsWithSensitivity(), value);
    return Math.exp(res.getEntry(0));
  }

  @Override
  public double firstDerivative(final Interpolator1DDataBundle data, final Double value) {
    ArgChecker.notNull(value, "value");
    ArgChecker.notNull(data, "data bundle");
    ArgChecker.isTrue(data instanceof Interpolator1DLogPiecewisePoynomialDataBundle);
    final Interpolator1DLogPiecewisePoynomialDataBundle polyData = (Interpolator1DLogPiecewisePoynomialDataBundle) data;
    final DoubleMatrix1D resValue = FUNC.evaluate(polyData.getPiecewisePolynomialResultsWithSensitivity(), value);
    final DoubleMatrix1D resDerivative = FUNC.differentiate(polyData.getPiecewisePolynomialResultsWithSensitivity(), value);
    return Math.exp(resValue.getEntry(0)) * resDerivative.getEntry(0);
  }

  @Override
  public double[] getNodeSensitivitiesForValue(final Interpolator1DDataBundle data, final Double value) {
    ArgChecker.notNull(value, "value");
    ArgChecker.notNull(data, "data bundle");
    ArgChecker.isTrue(data instanceof Interpolator1DLogPiecewisePoynomialDataBundle);
    final Interpolator1DLogPiecewisePoynomialDataBundle polyData = (Interpolator1DLogPiecewisePoynomialDataBundle) data;
    final double[] resSense = FUNC.nodeSensitivity(polyData.getPiecewisePolynomialResultsWithSensitivity(), value).getData();
    final double resValue = Math.exp(FUNC.evaluate(polyData.getPiecewisePolynomialResultsWithSensitivity(), value).getEntry(0));
    final double[] knotValues = data.getValues();
    final int nKnots = knotValues.length;
    final double[] res = new double[nKnots];
    for (int i = 0; i < nKnots; ++i) {
      res[i] = resSense[i] * resValue / knotValues[i];
    }
    return res;
  }

  @Override
  public Interpolator1DDataBundle getDataBundle(final double[] x, final double[] y) {
    ArgChecker.notNull(y, "y");
    final int nData = y.length;
    final double[] logY = new double[nData];
    for (int i = 0; i < nData; ++i) {
      ArgChecker.isTrue(y[i] > 0., "y should be positive");
      logY[i] = Math.log(y[i]);
    }
    return new Interpolator1DLogPiecewisePoynomialDataBundle(new ArrayInterpolator1DDataBundle(x, logY, false), getInterpolator(), 0., 0.);
  }

  @Override
  public Interpolator1DDataBundle getDataBundleFromSortedArrays(final double[] x, final double[] y) {
    ArgChecker.notNull(y, "y");
    final int nData = y.length;
    final double[] logY = new double[nData];
    for (int i = 0; i < nData; ++i) {
      ArgChecker.isTrue(y[i] > 0., "y should be positive");
      logY[i] = Math.log(y[i]);
    }
    return new Interpolator1DLogPiecewisePoynomialDataBundle(new ArrayInterpolator1DDataBundle(x, logY, true), getInterpolator(), 0., 0.);
  }

  @Override
  public Interpolator1DDataBundle getDataBundle(final double[] x, final double[] y, final double leftCond, final double rightCond) {
    throw new IllegalArgumentException("Sort array and use getDataBundleFromSortedArrays");
  }

  @Override
  public Interpolator1DDataBundle getDataBundleFromSortedArrays(final double[] x, final double[] y, final double leftCond, final double rightCond) {
    ArgChecker.notNull(y, "y");
    final int nData = y.length;
    final double[] logY = new double[nData];
    for (int i = 0; i < nData; ++i) {
      ArgChecker.isTrue(y[i] > 0., "y should be positive");
      logY[i] = Math.log(y[i]);
    }
    double leftBare = leftCond / y[0];
    double rightBare = rightCond / y[nData - 1];
    return new Interpolator1DLogPiecewisePoynomialDataBundle(new ArrayInterpolator1DDataBundle(x, logY, true), getInterpolator(), leftBare, rightBare);
  }

}
