/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.regression;

import org.apache.commons.math3.distribution.TDistribution;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.DiagonalMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.analytics.math.matrix.CommonsMatrixAlgebra;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;

/**
 * 
 */
public class WeightedLeastSquaresRegression extends LeastSquaresRegression {
  private static final Logger s_logger = LoggerFactory.getLogger(WeightedLeastSquaresRegression.class);
  private static CommonsMatrixAlgebra s_algebra = new CommonsMatrixAlgebra();

  @Override
  public LeastSquaresRegressionResult regress(final double[][] x, final double[][] weights, final double[] y, final boolean useIntercept) {
    if (weights == null) {
      throw new IllegalArgumentException("Cannot perform WLS regression without an array of weights");
    }
    checkData(x, weights, y);
    s_logger
        .info("Have a two-dimensional array for what should be a one-dimensional array of weights. The weights used in this regression will be the diagonal elements only");
    final double[] w = new double[weights.length];
    for (int i = 0; i < w.length; i++) {
      w[i] = weights[i][i];
    }
    return regress(x, w, y, useIntercept);
  }

  public LeastSquaresRegressionResult regress(final double[][] x, final double[] weights, final double[] y, final boolean useIntercept) {
    if (weights == null) {
      throw new IllegalArgumentException("Cannot perform WLS regression without an array of weights");
    }
    checkData(x, weights, y);
    final double[][] dep = addInterceptVariable(x, useIntercept);
    final double[] indep = new double[y.length];
    final double[] w = new double[weights.length];
    for (int i = 0; i < y.length; i++) {
      indep[i] = y[i];
      w[i] = weights[i];
    }
    DoubleMatrix2D matrix = new DoubleMatrix2D(dep);
    DoubleMatrix1D vector = new DoubleMatrix1D(indep);
    RealMatrix wDiag = new DiagonalMatrix(w);
    DoubleMatrix2D transpose = s_algebra.getTranspose(matrix);

    DoubleMatrix2D wDiagTimesMatrix = new DoubleMatrix2D(wDiag.multiply(new Array2DRowRealMatrix(matrix.getData())).getData());
    DoubleMatrix2D tmp = (DoubleMatrix2D) s_algebra.multiply(s_algebra.getInverse(s_algebra.multiply(transpose, wDiagTimesMatrix)), transpose);

    DoubleMatrix2D wTmpTimesDiag = new DoubleMatrix2D(wDiag.preMultiply(new Array2DRowRealMatrix(tmp.getData())).getData());
    DoubleMatrix2D betasVector = (DoubleMatrix2D) s_algebra.multiply(wTmpTimesDiag, vector);
    double[] yModel =  super.writeArrayAsVector(((DoubleMatrix2D)s_algebra.multiply(matrix, betasVector)).getData());
    double[] betas = super.writeArrayAsVector(betasVector.toArray());
    return getResultWithStatistics(x, convertArray(wDiag.getData()), y, betas, yModel, transpose, matrix, useIntercept);
  }

  private LeastSquaresRegressionResult getResultWithStatistics(final double[][] x, final double[][] w, final double[] y, final double[] betas,
      final double[] yModel, final DoubleMatrix2D transpose, final DoubleMatrix2D matrix, final boolean useIntercept) {
    double yMean = 0.;
    for (final double y1 : y) {
      yMean += y1;
    }
    yMean /= y.length;
    double totalSumOfSquares = 0.;
    double errorSumOfSquares = 0.;
    final int n = x.length;
    final int k = betas.length;
    final double[] residuals = new double[n];
    final double[] standardErrorsOfBeta = new double[k];
    final double[] tStats = new double[k];
    final double[] pValues = new double[k];
    for (int i = 0; i < n; i++) {
      totalSumOfSquares += w[i][i] * (y[i] - yMean) * (y[i] - yMean);
      residuals[i] = y[i] - yModel[i];
      errorSumOfSquares += w[i][i] * residuals[i] * residuals[i];
    }
    final double regressionSumOfSquares = totalSumOfSquares - errorSumOfSquares;
    final double[][] covarianceBetas = convertArray(s_algebra.getInverse(s_algebra.multiply(transpose, matrix)).toArray());
    final double rSquared = regressionSumOfSquares / totalSumOfSquares;
    final double adjustedRSquared = 1. - (1 - rSquared) * (n - 1) / (n - k);
    final double meanSquareError = errorSumOfSquares / (n - k);
    final TDistribution studentT = new TDistribution(n - k);
    for (int i = 0; i < k; i++) {
      standardErrorsOfBeta[i] = Math.sqrt(meanSquareError * covarianceBetas[i][i]);
      tStats[i] = betas[i] / standardErrorsOfBeta[i];
      pValues[i] = 1 - studentT.cumulativeProbability(Math.abs(tStats[i]));
    }
    return new WeightedLeastSquaresRegressionResult(betas, residuals, meanSquareError, standardErrorsOfBeta, rSquared, adjustedRSquared, tStats, pValues,
        useIntercept);
  }
}
