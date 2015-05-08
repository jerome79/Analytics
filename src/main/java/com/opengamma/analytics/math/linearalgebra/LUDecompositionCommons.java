/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.linearalgebra;

import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.RealMatrix;

import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.util.wrapper.CommonsMathWrapper;
import com.opengamma.strata.collect.ArgChecker;

/**
 * This class is a wrapper for the <a href="http://commons.apache.org/proper/commons-math/javadocs/api-3.5/org/apache/commons/math3/linear/LUDecomposition.html">Commons Math3 library implementation</a> 
 * of LU decomposition.
 */
public class LUDecompositionCommons extends Decomposition<LUDecompositionResult> {

  /**
   * {@inheritDoc}
   */
  @Override
  public LUDecompositionResult evaluate(final DoubleMatrix2D x) {
    ArgChecker.notNull(x, "x");
    final RealMatrix temp = CommonsMathWrapper.wrap(x);
    final LUDecomposition lu = new LUDecomposition(temp);
    return new LUDecompositionCommonsResult(lu);
  }

}
