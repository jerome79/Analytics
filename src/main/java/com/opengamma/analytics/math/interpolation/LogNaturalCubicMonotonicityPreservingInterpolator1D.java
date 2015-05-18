/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import com.opengamma.analytics.math.function.PiecewisePolynomialWithSensitivityFunction1D;
import com.opengamma.analytics.math.interpolation.data.ArrayInterpolator1DDataBundle;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DLogPiecewisePoynomialDataBundle;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.strata.basics.interpolator.OneDimensionalInterpolator;
import com.opengamma.strata.collect.ArgChecker;

/**
 * Find a interpolant F(x) = exp( f(x) ) where f(x) is a Natural cubic spline with Monotonicity cubic fileter. 
 * 
 * The natural cubic spline is determined by {@link LogNaturalSplineHelper}, where the tridiagonal algorithm is used to solve a linear system. 
 * Since {@link PiecewisePolynomialResultsWithSensitivity} in {@link Interpolator1DLogPiecewisePoynomialDataBundle} contains information on f(x) (NOT F(x)), 
 * computation done by {@link PiecewisePolynomialWithSensitivityFunction1D} MUST be exponentiated.
 */
public class LogNaturalCubicMonotonicityPreservingInterpolator1D extends PiecewisePolynomialInterpolator1D implements OneDimensionalInterpolator {

  /** Serialization version */
  private static final long serialVersionUID = 1L;

  private static final PiecewisePolynomialWithSensitivityFunction1D FUNC = new PiecewisePolynomialWithSensitivityFunction1D();

  /** The interpolator name. */
  private static final String NAME = "LogNaturalCubicWithMonotonicity";

  /**
   * 
   */
  public LogNaturalCubicMonotonicityPreservingInterpolator1D() {
    super(new MonotonicityPreservingCubicSplineInterpolator(new LogNaturalSplineHelper()));
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
    return new Interpolator1DLogPiecewisePoynomialDataBundle(new ArrayInterpolator1DDataBundle(x, logY, false), new MonotonicityPreservingCubicSplineInterpolator(new LogNaturalSplineHelper()));
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
    return new Interpolator1DLogPiecewisePoynomialDataBundle(new ArrayInterpolator1DDataBundle(x, logY, true), new MonotonicityPreservingCubicSplineInterpolator(new LogNaturalSplineHelper()));
  }

  @Override
  public String getName() {
    return NAME;
  }
}
