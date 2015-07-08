/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.rootfinding;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.analysis.solvers.LaguerreSolver;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.exception.TooManyEvaluationsException;

import com.google.common.math.DoubleMath;
import com.opengamma.analytics.math.MathException;
import com.opengamma.analytics.math.function.RealPolynomialFunction1D;
import com.opengamma.strata.collect.ArgChecker;

/**
 * Class that calculates the real roots of a polynomial using Laguerre's method. This class is a wrapper for the
 * <a href="http://commons.apache.org/math/api-2.1/org/apache/commons/math/analysis/solvers/LaguerreSolver.html">Commons Math library implementation</a>
 * of Laguerre's method.
 */
//TODO Have a complex and real root finder
public class LaguerrePolynomialRealRootFinder implements Polynomial1DRootFinder<Double> {
  private static final LaguerreSolver ROOT_FINDER = new LaguerreSolver();
  private static final double EPS = 1e-16;

  /**
   * {@inheritDoc}
   * @throws MathException If there are no real roots; if the Commons method could not evaluate the function; if the Commons method could not converge.
   */
  @Override
  public Double[] getRoots(final RealPolynomialFunction1D function) {
    ArgChecker.notNull(function, "function");
    try {
      final Complex[] roots = ROOT_FINDER.solveAllComplex(function.getCoefficients(), 0);
      final List<Double> realRoots = new ArrayList<>();
      for (final Complex c : roots) {
        if (DoubleMath.fuzzyEquals(c.getImaginary(), 0d, EPS)) {
          realRoots.add(c.getReal());
        }
      }
      if (realRoots.isEmpty()) {
        throw new MathException("Could not find any real roots");
      }
      return realRoots.toArray(new Double[realRoots.size()]);
    } catch (TooManyEvaluationsException e) {
      throw new MathException(e);
    }
  }
}
