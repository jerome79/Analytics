/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.var.parametric;

import java.util.Map;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.matrix.CommonsMatrixAlgebra;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.matrix.Matrix;
import com.opengamma.analytics.math.matrix.MatrixAlgebra;
import com.opengamma.strata.collect.ArgChecker;

/**
 * 
 */
public class DeltaGammaCovarianceMatrixSkewnessCalculator
    extends Function1D<Map<Integer, ParametricVaRDataBundle>, Double> {

  private static MatrixAlgebra ALGEBRA = new CommonsMatrixAlgebra();

  @Override
  public Double evaluate(final Map<Integer, ParametricVaRDataBundle> data) {
    ArgChecker.notNull(data, "data");
    ParametricVaRDataBundle firstOrderData = data.get(1);
    ArgChecker.notNull(firstOrderData, "first order data");
    ParametricVaRDataBundle secondOrderData = data.get(2);
    if (secondOrderData == null) {
      return 0.;
    }
    DoubleMatrix1D delta = (DoubleMatrix1D) firstOrderData.getSensitivities();
    Matrix<?> gamma = secondOrderData.getSensitivities();
    if (gamma == null || gamma.getNumberOfElements() == 0) {
      return 0.;
    }
    DoubleMatrix2D gammaMatrix = (DoubleMatrix2D) gamma;
    DoubleMatrix2D deltaCovariance = firstOrderData.getCovarianceMatrix();
    if (gammaMatrix.getNumberOfColumns() != deltaCovariance.getNumberOfColumns()) {
      throw new IllegalArgumentException("Gamma matrix and covariance matrix were incompatible sizes");
    }
    Matrix<?> product = ALGEBRA.multiply(gammaMatrix, deltaCovariance);
    double numerator = ALGEBRA.getTrace(ALGEBRA.getPower(product, 3)) +
        3 * ALGEBRA.getInnerProduct(delta, ((DoubleMatrix2D) ALGEBRA.multiply(
            ALGEBRA.multiply(deltaCovariance, product), delta)).getColumnVector(0));
    double denominator = Math.pow(
        0.5 * ALGEBRA.getTrace(ALGEBRA.getPower(product, 2)) +
            ALGEBRA.getInnerProduct(delta, ((DoubleMatrix2D) ALGEBRA.multiply(deltaCovariance, delta)).getColumnVector(0)), 1.5);
    return numerator / denominator;
  }

}
