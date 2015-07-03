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
import com.opengamma.analytics.math.matrix.MatrixAlgebra;
import com.opengamma.strata.collect.ArgChecker;

/**
 * 
 */
public class DeltaMeanCalculator
    extends Function1D<Map<Integer, ParametricVaRDataBundle>, Double> {

  private static final MatrixAlgebra ALGEBRA = new CommonsMatrixAlgebra();

  @Override
  public Double evaluate(final Map<Integer, ParametricVaRDataBundle> data) {
    ArgChecker.notNull(data, "data");
    ArgChecker.isTrue(data.containsKey(1));
    ParametricVaRDataBundle deltaData = data.get(1);
    DoubleMatrix1D mean = deltaData.getExpectedReturn();
    DoubleMatrix1D delta = (DoubleMatrix1D) deltaData.getSensitivities();
    return ALGEBRA.getInnerProduct(delta, mean);
  }

}
