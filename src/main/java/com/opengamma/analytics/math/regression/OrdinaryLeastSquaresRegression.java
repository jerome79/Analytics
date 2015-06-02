/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.regression;

import org.apache.commons.math3.distribution.TDistribution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.analytics.math.matrix.CommonsMatrixAlgebra;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;

/**
 * 
 */
public class OrdinaryLeastSquaresRegression extends LeastSquaresRegression {
  private static Logger s_logger = LoggerFactory.getLogger(OrdinaryLeastSquaresRegression.class);
  private CommonsMatrixAlgebra _algebra = new CommonsMatrixAlgebra();

  @Override
  public LeastSquaresRegressionResult regress(final double[][] x, final double[][] weights, final double[] y, final boolean useIntercept) {
    if (weights != null) {
      s_logger.info("Weights were provided for OLS regression: they will be ignored");
    }
    return regress(x, y, useIntercept);
  }

  public LeastSquaresRegressionResult regress(final double[][] x, final double[] y, final boolean useIntercept) {
    checkData(x, y);
    double[][] indep = addInterceptVariable(x, useIntercept);
    double[] dep = new double[y.length];
    for (int i = 0; i < y.length; i++) {
      dep[i] = y[i];
    }
    DoubleMatrix2D matrix = new DoubleMatrix2D(indep);
    DoubleMatrix1D vector = new DoubleMatrix1D(dep);
    DoubleMatrix2D transpose = _algebra.getTranspose(matrix);
    DoubleMatrix2D betasVector = (DoubleMatrix2D) _algebra.multiply(_algebra.multiply(_algebra.getInverse(_algebra.multiply(transpose, matrix)), transpose), vector);
    double[] yModel = super.writeArrayAsVector(((DoubleMatrix2D) _algebra.multiply(matrix, betasVector)).toArray());
    double[] betas = super.writeArrayAsVector(betasVector.toArray());
    return getResultWithStatistics(x, y, betas, yModel, transpose, matrix, useIntercept);
  }

  private LeastSquaresRegressionResult getResultWithStatistics(final double[][] x, final double[] y, final double[] betas, final double[] yModel, final DoubleMatrix2D transpose,
      final DoubleMatrix2D matrix, final boolean useIntercept) {
    double yMean = 0.;
    for (double y1 : y) {
      yMean += y1;
    }
    yMean /= y.length;
    double totalSumOfSquares = 0.;
    double errorSumOfSquares = 0.;
    int n = x.length;
    int k = betas.length;
    double[] residuals = new double[n];
    double[] stdErrorBetas = new double[k];
    double[] tStats = new double[k];
    double[] pValues = new double[k];
    for (int i = 0; i < n; i++) {
      totalSumOfSquares += (y[i] - yMean) * (y[i] - yMean);
      residuals[i] = y[i] - yModel[i];
      errorSumOfSquares += residuals[i] * residuals[i];
    }
    double regressionSumOfSquares = totalSumOfSquares - errorSumOfSquares;
    double[][] covarianceBetas = convertArray(_algebra.getInverse(_algebra.multiply(transpose, matrix)).toArray());
    double rSquared = regressionSumOfSquares / totalSumOfSquares;
    double adjustedRSquared = 1. - (1 - rSquared) * (n - 1.) / (n - k);
    double meanSquareError = errorSumOfSquares / (n - k);
    TDistribution studentT = new TDistribution(n - k);
    for (int i = 0; i < k; i++) {
      stdErrorBetas[i] = Math.sqrt(meanSquareError * covarianceBetas[i][i]);
      tStats[i] = betas[i] / stdErrorBetas[i];
      pValues[i] = 1 - studentT.cumulativeProbability(Math.abs(tStats[i]));
    }
    return new LeastSquaresRegressionResult(betas, residuals, meanSquareError, stdErrorBetas, rSquared, adjustedRSquared, tStats, pValues, useIntercept);
  }

}
