/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.integration;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.analysis.integration.RombergIntegrator;
import org.apache.commons.math.analysis.integration.UnivariateRealIntegrator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.analytics.math.MathException;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.util.wrapper.CommonsMathWrapper;
import com.opengamma.strata.collect.ArgChecker;

/**
 * 
 * Romberg's method estimates an integral by repeatedly using <a href="http://en.wikipedia.org/wiki/Richardson_extrapolation">Richardson extrapolation</a> 
 * on the extended trapezium rule {@link ExtendedTrapezoidIntegrator1D}. 
 * <p>
 * This class is a wrapper for the <a href="http://commons.apache.org/math/api-2.1/org/apache/commons/math/analysis/integration/RombergIntegrator.html">Commons Math library implementation</a> 
 * of Romberg integration.
 */
public class RombergIntegrator1D extends Integrator1D<Double, Double> {
  private static final Logger s_logger = LoggerFactory.getLogger(RombergIntegrator1D.class);
  private final UnivariateRealIntegrator _integrator = new RombergIntegrator();

  /**
   * Romberg integration method. Note that the Commons implementation fails if the lower bound is larger than the upper - 
   * in this case, the bounds are reversed and the result negated. 
   * @param f The function to integrate, not null
   * @param lower The lower bound, not null
   * @param upper The upper bound, not null
   * @return The result of the integration
   */
  @Override
  public Double integrate(final Function1D<Double, Double> f, final Double lower, final Double upper) {
    ArgChecker.notNull(f, "f");
    ArgChecker.notNull(lower, "lower bound");
    ArgChecker.notNull(upper, "upper bound");
    try {
      if (lower < upper) {
        return _integrator.integrate(CommonsMathWrapper.wrapUnivariate(f), lower, upper);
      }
      s_logger.info("Upper bound was less than lower bound; swapping bounds and negating result");
      return -_integrator.integrate(CommonsMathWrapper.wrapUnivariate(f), upper, lower);
    } catch (final FunctionEvaluationException e) {
      throw new MathException(e);
    } catch (final org.apache.commons.math.ConvergenceException e) {
      throw new MathException(e);
    }
  }
}
