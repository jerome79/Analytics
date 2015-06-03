/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.fitting;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Arrays;
import java.util.BitSet;

import org.apache.commons.math3.random.Well44497b;
import org.slf4j.Logger;
import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.volatility.smile.function.SmileModelData;
import com.opengamma.analytics.financial.model.volatility.smile.function.VolatilityFunctionProvider;
import com.opengamma.analytics.math.differentiation.VectorFieldFirstOrderDifferentiator;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.statistics.leastsquare.LeastSquareResultsWithTransform;
import com.opengamma.strata.collect.ArgChecker;

/**
 * Test.
 */
@Test
public abstract class SmileModelFitterTest<T extends SmileModelData> {
  private static final double TIME_TO_EXPIRY = 7.0;
  private static final double F = 0.03;
  private static final Well44497b RANDOM = new Well44497b(0L);
  protected double[] _cleanVols;
  protected double[] _noisyVols;
  protected double[] _errors;
  protected VolatilityFunctionProvider<T> _model;
  protected SmileModelFitter<T> _fitter;
  protected SmileModelFitter<T> _nosiyFitter;

  protected double _chiSqEps = 1e-6;
  protected double _paramValueEps = 1e-6;

  abstract Logger getlogger();

  abstract VolatilityFunctionProvider<T> getModel();

  abstract T getModelData();

  abstract SmileModelFitter<T> getFitter(final double forward, final double[] strikes, final double timeToExpiry, final double[] impliedVols, double[] error, VolatilityFunctionProvider<T> model);

  abstract double[][] getStartValues();

  abstract double[] getRandomStartValues();

  abstract BitSet[] getFixedValues();

  public SmileModelFitterTest() {
    final VolatilityFunctionProvider<T> model = getModel();
    final T data = getModelData();
    final double[] strikes = new double[] {0.005, 0.01, 0.02, 0.03, 0.04, 0.05, 0.07, 0.1 };
    final int n = strikes.length;
    _noisyVols = new double[n];

    _errors = new double[n];
    _cleanVols = model.getVolatilityFunction(F, strikes, TIME_TO_EXPIRY).evaluate(data);
    Arrays.fill(_errors, 1e-4);
    for (int i = 0; i < n; i++) {
      _noisyVols[i] = _cleanVols[i] + RANDOM.nextDouble() * _errors[i];
    }

    _fitter = getFitter(F, strikes, TIME_TO_EXPIRY, _cleanVols, _errors, model);
    _nosiyFitter = getFitter(F, strikes, TIME_TO_EXPIRY, _noisyVols, _errors, model);
  }

  public void testExactFit() {

    final double[][] start = getStartValues();
    final BitSet[] fixed = getFixedValues();
    final int nStartPoints = start.length;
    ArgChecker.isTrue(fixed.length == nStartPoints);
    for (int trys = 0; trys < nStartPoints; trys++) {
      final LeastSquareResultsWithTransform results = _fitter.solve(new DoubleMatrix1D(start[trys]), fixed[trys]);
      final DoubleMatrix1D res = toStandardForm(results.getModelParameters());

      //debug
      final T fittedModel = _fitter.toSmileModelData(res);
      fittedModel.toString();

      assertEquals(0.0, results.getChiSq(), _chiSqEps);

      final int n = res.getNumberOfElements();
      final T data = getModelData();
      assertEquals(data.getNumberOfParameters(), n);
      for (int i = 0; i < n; i++) {
        assertEquals(data.getParameter(i), res.getEntry(i), _paramValueEps);
      }
    }
  }

  /**
   * Convert the fitted parameters to standard form - useful if there is degeneracy in the solution
   * @param from
   * @return The matrix in standard form
   */
  protected DoubleMatrix1D toStandardForm(final DoubleMatrix1D from) {
    return from;
  }

  public void testNoisyFit() {
    final double[][] start = getStartValues();
    final BitSet[] fixed = getFixedValues();
    final int nStartPoints = start.length;
    ArgChecker.isTrue(fixed.length == nStartPoints);
    for (int trys = 0; trys < nStartPoints; trys++) {
      final LeastSquareResultsWithTransform results = _fitter.solve(new DoubleMatrix1D(start[trys]), fixed[trys]);
      final DoubleMatrix1D res = toStandardForm(results.getModelParameters());
      final double eps = 1e-2;
      assertTrue(results.getChiSq() < 7);
      final int n = res.getNumberOfElements();
      final T data = getModelData();
      assertEquals(data.getNumberOfParameters(), n);
      for (int i = 0; i < n; i++) {
        assertEquals(data.getParameter(i), res.getEntry(i), eps);
      }
    }
  }

  public void testJacobian() {

    final T data = getModelData();

    final int n = data.getNumberOfParameters();
    final double[] temp = new double[n];
    for (int i = 0; i < n; i++) {
      temp[i] = data.getParameter(i);
    }
    final DoubleMatrix1D x = new DoubleMatrix1D(temp);

    testJacobian(x);
  }

  public void testRandomJacobian() {
    for (int i = 0; i < 10; i++) {
      final double[] temp = getRandomStartValues();
      final DoubleMatrix1D x = new DoubleMatrix1D(temp);
      try {
        testJacobian(x);
      } catch (final AssertionError e) {
        System.out.println("Jacobian test failed at " + x.toString());
        throw e;
      }
    }
  }

  private void testJacobian(final DoubleMatrix1D x) {

    final int n = x.getNumberOfElements();

    final Function1D<DoubleMatrix1D, DoubleMatrix1D> func = _fitter.getModelValueFunction();
    final Function1D<DoubleMatrix1D, DoubleMatrix2D> jacFunc = _fitter.getModelJacobianFunction();

    final VectorFieldFirstOrderDifferentiator differ = new VectorFieldFirstOrderDifferentiator();
    final Function1D<DoubleMatrix1D, DoubleMatrix2D> jacFuncFD = differ.differentiate(func);

    final DoubleMatrix2D jac = jacFunc.evaluate(x);
    final DoubleMatrix2D jacFD = jacFuncFD.evaluate(x);
    final int rows = jacFD.getNumberOfRows();
    final int cols = jacFD.getNumberOfColumns();

    assertEquals("incorrect rows in FD matrix", _cleanVols.length, rows);
    assertEquals("incorrect columns in FD matrix", n, cols);
    assertEquals("incorrect rows in matrix", rows, jac.getNumberOfRows());
    assertEquals("incorrect columns in matrix", cols, jac.getNumberOfColumns());

    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < cols; j++) {
        assertEquals("row: " + i + ", column: " + j, jacFD.getEntry(i, j), jac.getEntry(i, j), 2e-2);
      }
    }
  }

}
