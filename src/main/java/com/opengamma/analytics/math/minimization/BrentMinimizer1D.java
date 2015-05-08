/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.minimization;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.exception.NumberIsTooSmallException;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.MaxIter;
import org.apache.commons.math3.optim.OptimizationData;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.univariate.BrentOptimizer;
import org.apache.commons.math3.optim.univariate.SearchInterval;
import org.apache.commons.math3.optim.univariate.UnivariateObjectiveFunction;
import org.apache.commons.math3.optim.univariate.UnivariateOptimizer;
import org.apache.commons.math3.optim.univariate.UnivariatePointValuePair;

import com.opengamma.analytics.math.MathException;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.util.wrapper.CommonsMathWrapper;
import com.opengamma.strata.collect.ArgChecker;

/**
 * This class is a wrapper for the <a href="http://commons.apache.org/math/api-2.1/org/apache/commons/math/optimization/univariate/BrentOptimizer.html">Commons Math library implementation</a>
 * of Brent minimization.
 */
public class BrentMinimizer1D implements ScalarMinimizer {
  //TODO this class doesn't work properly - e.g. if the curve is flat, the bounded method returns one of the bounds and the unbounded method shoots off to +/-infinity
  private static OptimizationData MAXEVAL = new MaxEval(10000);
  private static OptimizationData MAXITER = new MaxIter(10000);
  private static final GoalType MINIMIZE = GoalType.MINIMIZE;
  private static final double relTol = 1e-15;
  private static final double absTol = 1e-15;
  private static final UnivariateOptimizer OPTIMIZER = new BrentOptimizer(relTol, absTol);

  /**
   * {@inheritDoc}
   */
  @Override
  public double minimize(final Function1D<Double, Double> function, final double startPosition, final double lowerBound, final double upperBound) {
    ArgChecker.notNull(function, "function");
    OptimizationData searchInterval = new SearchInterval(lowerBound, upperBound);
    UnivariateFunction commonsFunction = CommonsMathWrapper.wrapUnivariate(function);
    OptimizationData objFunc = new UnivariateObjectiveFunction(commonsFunction);
    OptimizationData initialGuess = new InitialGuess(new double[] {startPosition });

    try {
      UnivariatePointValuePair pair = OPTIMIZER.optimize(searchInterval, objFunc, MINIMIZE, initialGuess, MAXEVAL, MAXITER);
      return pair.getPoint();
    } catch (NumberIsTooSmallException e) {
      throw new MathException(e);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Double minimize(final Function1D<Double, Double> function, final Double startPosition) {
    return this.minimize(function, startPosition.doubleValue(), -Double.MAX_VALUE, Double.MAX_VALUE);
  }
}
