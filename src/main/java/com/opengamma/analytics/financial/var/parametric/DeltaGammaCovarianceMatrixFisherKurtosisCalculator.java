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
public class DeltaGammaCovarianceMatrixFisherKurtosisCalculator extends Function1D<Map<Integer, ParametricVaRDataBundle>, Double> {
  private static MatrixAlgebra s_algebra = new CommonsMatrixAlgebra();
  private final Function1D<Map<Integer, ParametricVaRDataBundle>, Double> _std;

  public DeltaGammaCovarianceMatrixFisherKurtosisCalculator() {
    _std = new DeltaGammaCovarianceMatrixStandardDeviationCalculator();
  }

  @Override
  public Double evaluate(final Map<Integer, ParametricVaRDataBundle> data) {
    ArgChecker.notNull(data, "data");
    final ParametricVaRDataBundle firstOrderData = data.get(1);
    ArgChecker.notNull(firstOrderData, "first order data");
    final ParametricVaRDataBundle secondOrderData = data.get(2);
    if (secondOrderData == null) {
      return 0.;
    }
    final DoubleMatrix1D delta = (DoubleMatrix1D) firstOrderData.getSensitivities();
    final Matrix<?> gamma = secondOrderData.getSensitivities();
    if (gamma == null || gamma.getNumberOfElements() == 0) {
      return 0.;
    }
    final DoubleMatrix2D gammaMatrix = (DoubleMatrix2D) gamma;
    final DoubleMatrix2D deltaCovariance = firstOrderData.getCovarianceMatrix();
    if (gammaMatrix.getNumberOfColumns() != deltaCovariance.getNumberOfColumns()) {
      throw new IllegalArgumentException("Gamma matrix and covariance matrix were incompatible sizes");
    }
    final Matrix<?> product = s_algebra.multiply(gammaMatrix, deltaCovariance);
    final double std = _std.evaluate(data);
    final double numerator = s_algebra.getTrace(s_algebra.getPower(product, 4)) + 12
        * s_algebra.getInnerProduct(delta, ((DoubleMatrix2D)s_algebra.multiply(s_algebra.multiply(deltaCovariance, s_algebra.getPower(product, 2)), delta)).getColumnVector(0)) + 3 * std * std;
    final double denominator = Math.pow(0.5 * s_algebra.getTrace(s_algebra.getPower(product, 2)) + s_algebra.getInnerProduct(delta, ((DoubleMatrix2D)s_algebra.multiply(deltaCovariance, delta)).getColumnVector(0)), 2);
    return numerator / denominator - 3;
  }

}
