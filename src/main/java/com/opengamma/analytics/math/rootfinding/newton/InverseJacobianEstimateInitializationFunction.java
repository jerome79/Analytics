/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.rootfinding.newton;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.linearalgebra.Decomposition;
import com.opengamma.analytics.math.linearalgebra.DecompositionResult;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.matrix.DoubleMatrixUtils;
import com.opengamma.strata.collect.ArgChecker;

/**
 * 
 */
public class InverseJacobianEstimateInitializationFunction implements NewtonRootFinderMatrixInitializationFunction {
  private final Decomposition<?> _decomposition;

  public InverseJacobianEstimateInitializationFunction(final Decomposition<?> decomposition) {
    ArgChecker.notNull(decomposition, "decomposition");
    _decomposition = decomposition;
  }

  @Override
  public DoubleMatrix2D getInitializedMatrix(Function1D<DoubleMatrix1D, DoubleMatrix2D> jacobianFunction, final DoubleMatrix1D x) {
    ArgChecker.notNull(jacobianFunction, "jacobianFunction");
    ArgChecker.notNull(x, "x");
    final DoubleMatrix2D estimate = jacobianFunction.evaluate(x);
    final DecompositionResult decompositionResult = _decomposition.evaluate(estimate);
    return decompositionResult.solve(DoubleMatrixUtils.getIdentityMatrix2D(x.getNumberOfElements()));
  }

}
