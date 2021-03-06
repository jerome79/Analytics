/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.function.special;

import com.google.common.math.DoubleMath;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.strata.collect.ArgChecker;

/**
 * Class implementing the Dirac delta function, defined as:
 * $$
 * \begin{align*}
 * \delta(x)=
 * \begin{cases}
 * \infty & \text{ when } x = 0\\
 *  0     & \text{ otherwise}
 * \end{cases}
 * \end{align*}
 * $$
 */
public class DiracDeltaFunction extends Function1D<Double, Double> {

  @Override
  public Double evaluate(final Double x) {
    ArgChecker.notNull(x, "x");
    return DoubleMath.fuzzyEquals(x, 0d, 1e-16) ? Double.POSITIVE_INFINITY : 0;
  }

}
