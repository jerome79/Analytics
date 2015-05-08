/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.linearalgebra;

import org.apache.commons.math3.linear.CholeskyDecomposition;
import org.apache.commons.math3.linear.RealMatrix;

import com.opengamma.analytics.math.MathException;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.util.wrapper.CommonsMathWrapper;
import com.opengamma.strata.collect.ArgChecker;

/**
 * This class is a wrapper for the <a href="http://commons.apache.org/math/api-2.1/org/apache/commons/math/linear/CholeskyDecompositionImpl.html">Commons Math library implementation</a> 
 * of Cholesky decomposition.
 */
public class CholeskyDecompositionCommons extends Decomposition<CholeskyDecompositionResult> {

  /**
   * {@inheritDoc}
   */
  @Override
  public CholeskyDecompositionResult evaluate(final DoubleMatrix2D x) {
    ArgChecker.notNull(x, "x");
    final RealMatrix temp = CommonsMathWrapper.wrap(x);
    CholeskyDecomposition cholesky;
    try {
      cholesky = new CholeskyDecomposition(temp);
    } catch (Exception e) {
      throw new MathException(e.toString());
    }
    return new CholeskyDecompositionCommonsResult(cholesky);
  }

}
