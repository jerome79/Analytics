/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.surface;

import java.util.Objects;

import com.opengamma.analytics.math.surface.Surface;
import com.opengamma.strata.collect.ArgChecker;

/**
 * for a model of forward rates that follow the SDE df = a(f,t)dt + b(f,t)dw this describes the drift function (of forward, f, and time, t)
 */
public class DriftSurface {
  private final Surface<Double, Double, Double> _surface;

  public DriftSurface(final Surface<Double, Double, Double> surface) {
    ArgChecker.notNull(surface, "surface");
    _surface = surface;
  }

  public Surface<Double, Double, Double> getSurface() {
    return _surface;
  }

  public double getDrift(final double f, final double t) {
    return _surface.getZValue(f, t);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _surface.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final DriftSurface other = (DriftSurface) obj;
    return Objects.equals(_surface, other._surface);
  }

}
