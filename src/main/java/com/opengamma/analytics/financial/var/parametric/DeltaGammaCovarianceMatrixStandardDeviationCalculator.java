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
public class DeltaGammaCovarianceMatrixStandardDeviationCalculator
    extends Function1D<Map<Integer, ParametricVaRDataBundle>, Double> {

  private static final MatrixAlgebra ALGEBRA = new CommonsMatrixAlgebra();

  @Override
  public Double evaluate(Map<Integer, ParametricVaRDataBundle> data) {
    ArgChecker.notNull(data, "data");
    ParametricVaRDataBundle firstOrderData = data.get(1);
    ParametricVaRDataBundle secondOrderData = data.get(2);
    double deltaStd = 0;
    double gammaStd = 0;
    if (firstOrderData != null) {
      DoubleMatrix1D delta = (DoubleMatrix1D) firstOrderData.getSensitivities();
      DoubleMatrix2D deltaCovariance = firstOrderData.getCovarianceMatrix();
      deltaStd = ALGEBRA.getInnerProduct(delta, ((DoubleMatrix2D) ALGEBRA.multiply(deltaCovariance, delta)).getColumnVector(0));
    }
    if (secondOrderData != null) {
      Matrix<?> gamma = secondOrderData.getSensitivities();
      DoubleMatrix2D gammaCovariance = secondOrderData.getCovarianceMatrix();
      gammaStd = 0.5 * ALGEBRA.getTrace(ALGEBRA.getPower(ALGEBRA.multiply(gamma, gammaCovariance), 2));
    }
    return Math.sqrt(deltaStd + gammaStd);
  }

}
