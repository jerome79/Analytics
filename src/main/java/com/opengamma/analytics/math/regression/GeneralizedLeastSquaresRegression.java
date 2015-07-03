/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.regression;

import com.opengamma.analytics.math.matrix.CommonsMatrixAlgebra;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;

/**
 * 
 */
public class GeneralizedLeastSquaresRegression extends LeastSquaresRegression {
  private static CommonsMatrixAlgebra s_algebra = new CommonsMatrixAlgebra();

  @Override
  public LeastSquaresRegressionResult regress(double[][] x, double[][] weights, double[] y, boolean useIntercept) {
    if (weights == null) {
      throw new IllegalArgumentException("Cannot perform GLS regression without an array of weights");
    }
    checkData(x, weights, y);
    double[][] dep = addInterceptVariable(x, useIntercept);
    double[] indep = new double[y.length];
    double[][] wArray = new double[y.length][y.length];
    for (int i = 0; i < y.length; i++) {
      indep[i] = y[i];
      for (int j = 0; j < y.length; j++) {
        wArray[i][j] = weights[i][j];
      }
    }
    DoubleMatrix2D matrix = new DoubleMatrix2D(dep);
    DoubleMatrix1D vector = new DoubleMatrix1D(indep);
    DoubleMatrix2D w = new DoubleMatrix2D(wArray);
    DoubleMatrix2D transpose = s_algebra.getTranspose(matrix);
    DoubleMatrix2D betasVector = (DoubleMatrix2D)
        s_algebra.multiply(
            s_algebra.multiply(
                s_algebra.multiply(
                    s_algebra.getInverse(s_algebra.multiply(transpose, s_algebra.multiply(w, matrix))), transpose),
                w),
            vector);
    double[] yModel = super.writeArrayAsVector(((DoubleMatrix2D) s_algebra.multiply(matrix, betasVector)).toArray());
    double[] betas = super.writeArrayAsVector(betasVector.toArray());
    return getResultWithStatistics(x, y, betas, yModel, useIntercept);
  }

  private LeastSquaresRegressionResult getResultWithStatistics(
      double[][] x, double[] y, double[] betas, double[] yModel, boolean useIntercept) {

    int n = x.length;
    double[] residuals = new double[n];
    for (int i = 0; i < n; i++) {
      residuals[i] = y[i] - yModel[i];
    }
    return new WeightedLeastSquaresRegressionResult(betas, residuals, 0.0, null, 0.0, 0.0, null, null, useIntercept);
  }
}
