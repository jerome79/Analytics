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
public class DeltaCovarianceMatrixStandardDeviationCalculator
    extends Function1D<Map<Integer, ParametricVaRDataBundle>, Double> {

  private static final MatrixAlgebra ALGEBRA = new CommonsMatrixAlgebra();

  @Override
  public Double evaluate(Map<Integer, ParametricVaRDataBundle> data) {
    ArgChecker.notNull(data, "data");
    ParametricVaRDataBundle firstOrderData = data.get(1);
    ArgChecker.notNull(firstOrderData, "first order data");
    Matrix<?> delta = firstOrderData.getSensitivities();
    int s1 = delta.getNumberOfElements();
    ArgChecker.isTrue(s1 > 0, "Value delta vector contained no data");
    DoubleMatrix2D covariance = firstOrderData.getCovarianceMatrix();
    DoubleMatrix1D covarianceVector = ((DoubleMatrix2D) ALGEBRA.multiply(covariance, delta)).getColumnVector(0);
    return Math.sqrt(ALGEBRA.getInnerProduct(delta, covarianceVector));
  }

}
