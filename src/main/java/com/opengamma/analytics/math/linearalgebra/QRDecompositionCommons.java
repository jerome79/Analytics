/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.linearalgebra;

import org.apache.commons.math3.linear.QRDecomposition;
import org.apache.commons.math3.linear.RealMatrix;

import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.util.wrapper.CommonsMathWrapper;
import com.opengamma.strata.collect.ArgChecker;

/**
 * This class is a wrapper for the <a href="http://commons.apache.org/math/api-2.1/org/apache/commons/math/linear/QRDecompositionImpl.html">Commons Math library implementation</a> 
 * of QR decomposition.
 */
public class QRDecompositionCommons extends Decomposition<QRDecompositionResult> {

  /**
   * {@inheritDoc}
   */
  @Override
  public QRDecompositionResult evaluate(final DoubleMatrix2D x) {
    ArgChecker.notNull(x, "x");
    final RealMatrix temp = CommonsMathWrapper.wrap(x);
    final QRDecomposition qr = new QRDecomposition(temp);
    return new QRDecompositionCommonsResult(qr);
  }

}
