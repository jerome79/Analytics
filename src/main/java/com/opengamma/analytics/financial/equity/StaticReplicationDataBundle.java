/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity;

import java.util.Objects;

import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurface;
import com.opengamma.strata.collect.ArgChecker;

/**
 * Market data required to price instruments where the pricing models need a discounting curve, a forward (underlying) curve
 * and a Black implied volatility surface.
 */
public class StaticReplicationDataBundle {
  /** The discounting curve */
  private final YieldAndDiscountCurve _discountCurve;
  /** The forward curve */
  private final ForwardCurve _forwardCurve;
  /** The implied Black volatility surface */
  private final BlackVolatilitySurface<?> _volatilitySurface;

  /**
   * @param volSurf The volatility surface,
   * @param discCrv YieldAndDiscountCurve used to discount payments and in this case, also to compute the forward value of the underlying
   * @param forwardCurve the forward curve
   */
  public StaticReplicationDataBundle(final BlackVolatilitySurface<?> volSurf, final YieldAndDiscountCurve discCrv, final ForwardCurve forwardCurve) {
    ArgChecker.notNull(discCrv, "discountCurve");
    ArgChecker.notNull(volSurf, "volatilitySurface");
    ArgChecker.notNull(forwardCurve, "forwardCurve");
    _discountCurve = discCrv;
    _volatilitySurface = volSurf;
    _forwardCurve = forwardCurve;
  }

  /**
   * Create a data bundle based upon a new volatility surface
   * @param volSurf the volatility surface
   * @return StaticReplicationDataBundle
   */
  public StaticReplicationDataBundle withShiftedSurface(final BlackVolatilitySurface<?> volSurf) {
    ArgChecker.notNull(volSurf, "BlackVolatilitySurface");
    return new StaticReplicationDataBundle(volSurf, getDiscountCurve(), getForwardCurve());
  }

  /**
   * Create a data bundle based upon a new forward curve
   * @param forwardCurve the forward curve
   * @return StaticReplicationDataBundle
   */
  public StaticReplicationDataBundle withShiftedForwardCurve(final ForwardCurve forwardCurve) {
    ArgChecker.notNull(forwardCurve, "ForwardCurve");
    return new StaticReplicationDataBundle(getVolatilitySurface(), getDiscountCurve(), forwardCurve);
  }

  /**
   * Create a data bundle based upon a new discount curve
   * @param discCrv the discount curve
   * @return StaticReplicationDataBundle
   */
  public StaticReplicationDataBundle withShiftedDiscountCurve(final YieldAndDiscountCurve discCrv) {
    ArgChecker.notNull(discCrv, "YieldAndDiscountCurve");
    return new StaticReplicationDataBundle(getVolatilitySurface(), discCrv, getForwardCurve());
  }

  /**
   * Gets the discountCurve.
   * @return the discountCurve
   */
  public YieldAndDiscountCurve getDiscountCurve() {
    return _discountCurve;
  }

  /**
   * Gets the volatilitySurface.
   * @return the volatilitySurface
   */
  public BlackVolatilitySurface<?> getVolatilitySurface() {
    return _volatilitySurface;
  }

  /**
   * Gets the forwardUnderlying.
   * @return the forwardUnderlying
   */
  public ForwardCurve getForwardCurve() {
    return _forwardCurve;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _discountCurve.hashCode();
    result = prime * result + _forwardCurve.hashCode();
    result = prime * result + _volatilitySurface.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof StaticReplicationDataBundle)) {
      return false;
    }
    final StaticReplicationDataBundle other = (StaticReplicationDataBundle) obj;
    if (!Objects.equals(_discountCurve, other._discountCurve)) {
      return false;
    }
    if (!Objects.equals(_forwardCurve, other._forwardCurve)) {
      return false;
    }
    if (!Objects.equals(_volatilitySurface, other._volatilitySurface)) {
      return false;
    }
    return true;
  }

}
