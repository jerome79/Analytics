/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.linearalgebra;

import cern.colt.matrix.linalg.SingularValueDecomposition;

import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.util.wrapper.ColtMathWrapper;
import com.opengamma.strata.collect.ArgChecker;

/**
 * This class is a wrapper for the <a href="http://acs.lbl.gov/software/colt/api/cern/colt/matrix/linalg/SingularValueDecomposition.html">Colt Math library implementation</a>
 * of singular value decomposition.
 */
public class SVDecompositionColt extends Decomposition<SVDecompositionResult> {

  @Override
  public SVDecompositionResult evaluate(DoubleMatrix2D x) {
    ArgChecker.notNull(x, "x");
    MatrixValidate.notNaNOrInfinite(x);
    cern.colt.matrix.DoubleMatrix2D coltMatrix = ColtMathWrapper.wrap(x);
    SingularValueDecomposition svd = new SingularValueDecomposition(coltMatrix);
    return new SVDecompositionColtResult(svd);
  }

}
