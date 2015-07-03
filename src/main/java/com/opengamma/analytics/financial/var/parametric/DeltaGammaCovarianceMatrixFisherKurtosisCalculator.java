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
public class DeltaGammaCovarianceMatrixFisherKurtosisCalculator
    extends Function1D<Map<Integer, ParametricVaRDataBundle>, Double> {

  private static final MatrixAlgebra ALGEBRA = new CommonsMatrixAlgebra();
  private final Function1D<Map<Integer, ParametricVaRDataBundle>, Double> _std;

  public DeltaGammaCovarianceMatrixFisherKurtosisCalculator() {
    _std = new DeltaGammaCovarianceMatrixStandardDeviationCalculator();
  }

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
    double std = _std.evaluate(data);
    double numerator = ALGEBRA.getTrace(ALGEBRA.getPower(product, 4)) +
        12
        *
        ALGEBRA.getInnerProduct(delta, ((DoubleMatrix2D) ALGEBRA.multiply(
            ALGEBRA.multiply(deltaCovariance, ALGEBRA.getPower(product, 2)), delta)).getColumnVector(0)) + 3 * std * std;
    double denominator = Math
        .pow(
            0.5 *
                ALGEBRA.getTrace(ALGEBRA.getPower(product, 2)) +
                ALGEBRA.getInnerProduct(delta, ((DoubleMatrix2D) ALGEBRA.multiply(deltaCovariance, delta)).getColumnVector(0)),
            2);
    return numerator / denominator - 3;
  }

}
