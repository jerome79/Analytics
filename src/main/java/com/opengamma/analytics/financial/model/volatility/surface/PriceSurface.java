/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.surface;

import com.opengamma.analytics.math.surface.Surface;
import com.opengamma.strata.collect.ArgChecker;

/**
 * A surface with gives the price of a European call as a function of time to maturity and strike
 */
public class PriceSurface {

  private final Surface<Double, Double, Double> _surface;

  /**
   * 
   * @param surface  The time to maturity should be the first coordinate and the strike the second 
   */
  public PriceSurface(final Surface<Double, Double, Double> surface) {
    ArgChecker.notNull(surface, "surface");
    _surface = surface;
  }

  /**
   * 
   * @param t time to maturity
   * @param k strike
   * @return The price of a European call
   */
  public Double getPrice(final double t, final double k) {
    return _surface.getZValue(t, k);
  }

  public Surface<Double, Double, Double> getSurface() {
    return _surface;
  }

}
