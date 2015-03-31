/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.provider;

import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIbor;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.InterpolatedSmileFunction;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.strata.basics.currency.MultiCurrencyAmount;
import com.opengamma.strata.collect.ArgChecker;

/**
 * Computes an Ibor coupon in arrears by replication
 */
public class CouponIborInArrearsSmileModelReplicationMethod {

  private final CapFloorIborInArrearsSmileModelCapGenericReplicationMethod _method;

  /**
   * @param smileFunction Interpolated and extrapolated smile
   */
  public CouponIborInArrearsSmileModelReplicationMethod(final InterpolatedSmileFunction smileFunction) {
    _method = new CapFloorIborInArrearsSmileModelCapGenericReplicationMethod(smileFunction);
  }

  /**
   * Computes the present value of an Ibor coupon in arrears by replication. 
   * The coupon is price as an cap with strike 0.
   * @param coupon Ibor coupon in arrears
   * @param curves The curves
   * @return The present value
   */
  public MultiCurrencyAmount presentValue(final CouponIbor coupon, final MulticurveProviderInterface curves) {
    ArgChecker.notNull(coupon, "coupon");
    ArgChecker.notNull(curves, "curves");
    CapFloorIbor cap0 = CapFloorIbor.from(coupon, 0.0, true);
    return _method.presentValue(cap0, curves);
  }

  /**
   * Computes pv sensitivity
   * @param coupon Ibor coupon in arrears
   * @param curves The curves
   * @return The sensitivity
   */
  public MultipleCurrencyMulticurveSensitivity presentValueCurveSensitivity(final CouponIbor coupon,
      final MulticurveProviderInterface curves) {
    ArgChecker.notNull(coupon, "coupon");
    ArgChecker.notNull(curves, "curves");
    CapFloorIbor cap0 = CapFloorIbor.from(coupon, 0.0, true);
    return _method.presentValueCurveSensitivity(cap0, curves);
  }
}
