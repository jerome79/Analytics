/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.rootfinding.newton;

import com.opengamma.analytics.math.linearalgebra.Decomposition;
import com.opengamma.analytics.math.linearalgebra.DecompositionResult;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.strata.collect.ArgChecker;

/**
 * 
 */
public class JacobianDirectionFunction implements NewtonRootFinderDirectionFunction {
  private final Decomposition<?> _decomposition;

  public JacobianDirectionFunction(final Decomposition<?> decomposition) {
    ArgChecker.notNull(decomposition, "decomposition");
    _decomposition = decomposition;
  }

  @Override
  public DoubleMatrix1D getDirection(final DoubleMatrix2D estimate, final DoubleMatrix1D y) {
    ArgChecker.notNull(estimate, "estimate");
    ArgChecker.notNull(y, "y");
    final DecompositionResult result = _decomposition.evaluate(estimate);
    return result.solve(y);
  }

}
