/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.swap;

import java.time.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponDefinition;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.util.timeseries.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.strata.collect.ArgChecker;

/**
 * Class describing a swap with a fixed leg and a generic leg.
 */
public class SwapCouponFixedCouponDefinition extends SwapDefinition {

  /**
   * Constructor of the fixed-generic coupon swap from its two legs.
   * @param fixedLeg The fixed coupon leg.
   * @param otherLeg The other leg.
   */
  public SwapCouponFixedCouponDefinition(final AnnuityCouponFixedDefinition fixedLeg, final AnnuityDefinition<? extends CouponDefinition> otherLeg) {
    super(fixedLeg, otherLeg);
  }

  /**
   * The fixed leg of the swap.
   * @return Fixed leg.
   */
  public AnnuityCouponFixedDefinition getFixedLeg() {
    return (AnnuityCouponFixedDefinition) getFirstLeg();
  }

  @SuppressWarnings("unchecked")
  @Override
  public SwapFixedCoupon<Coupon> toDerivative(final ZonedDateTime date) {
    final Annuity<CouponFixed> fixedLeg = getFixedLeg().toDerivative(date);
    final Annuity<? extends Coupon> iborLeg = (Annuity<? extends Coupon>) getSecondLeg().toDerivative(date);
    return new SwapFixedCoupon<>(fixedLeg, (Annuity<Coupon>) iborLeg);
  }

  @SuppressWarnings("unchecked")
  @Override
  public SwapFixedCoupon<Coupon> toDerivative(final ZonedDateTime date, final ZonedDateTimeDoubleTimeSeries[] indexDataTS) {
    ArgChecker.notNull(indexDataTS, "index data time series array");
    ArgChecker.isTrue(indexDataTS.length > 0, "Should have at least one time series.");
    final Annuity<CouponFixed> fixedLeg = getFixedLeg().toDerivative(date);
    final Annuity<? extends Coupon> iborLeg = (Annuity<? extends Coupon>) getSecondLeg().toDerivative(date, indexDataTS[0]);
    return new SwapFixedCoupon<>(fixedLeg, (Annuity<Coupon>) iborLeg);
  }

}
