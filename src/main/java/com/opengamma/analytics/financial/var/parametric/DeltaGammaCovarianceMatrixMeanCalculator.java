/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.var.parametric;

import java.util.Map;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.matrix.CommonsMatrixAlgebra;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.matrix.Matrix;
import com.opengamma.analytics.math.matrix.MatrixAlgebra;
import com.opengamma.strata.collect.ArgChecker;

/**
 * 
 */
public class DeltaGammaCovarianceMatrixMeanCalculator extends Function1D<Map<Integer, ParametricVaRDataBundle>, Double> {
  private static MatrixAlgebra _algebra = new CommonsMatrixAlgebra();

  @Override
  public Double evaluate(final Map<Integer, ParametricVaRDataBundle> data) {
    ArgChecker.notNull(data, "data");
    final ParametricVaRDataBundle firstOrderData = data.get(1);
    ArgChecker.notNull(firstOrderData, "first order data");
    final ParametricVaRDataBundle secondOrderData = data.get(2);
    if (secondOrderData == null) {
      return 0.;
    }
    final Matrix<?> gamma = secondOrderData.getSensitivities();
    if (gamma == null || gamma.getNumberOfElements() == 0) {
      return 0.;
    }
    final DoubleMatrix2D covariance = firstOrderData.getCovarianceMatrix();
    return 0.5 * _algebra.getTrace(_algebra.multiply(gamma, covariance));
  }


}
