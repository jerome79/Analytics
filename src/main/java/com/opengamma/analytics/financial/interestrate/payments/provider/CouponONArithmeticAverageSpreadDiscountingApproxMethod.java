/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.provider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponONArithmeticAverageSpread;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.ForwardSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.SimplyCompoundedForwardSensitivity;
import com.opengamma.strata.basics.currency.MultiCurrencyAmount;
import com.opengamma.strata.collect.ArgChecker;
import com.opengamma.strata.collect.tuple.DoublesPair;

/**
 * Class describing the pricing of Fed Fund swap-like floating coupon (arithmetic average on overnight rates) by 
 * estimation and discounting (no convexity adjustment is computed). The estimation is done through an approximation.
 * <p>Reference: Overnight Indexes Related Products. OpenGamma Documentation n. 20, Version 1.0, February 2013.
 */
public final class CouponONArithmeticAverageSpreadDiscountingApproxMethod {

  /**
   * The method unique instance.
   */
  private static final CouponONArithmeticAverageSpreadDiscountingApproxMethod INSTANCE =
      new CouponONArithmeticAverageSpreadDiscountingApproxMethod();

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static CouponONArithmeticAverageSpreadDiscountingApproxMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private CouponONArithmeticAverageSpreadDiscountingApproxMethod() {
  }

  /**
   * Computes the present value.
   * @param coupon The coupon.
   * @param multicurve The multi-curve provider.
   * @return The present value.
   */
  public MultiCurrencyAmount presentValue(final CouponONArithmeticAverageSpread coupon,
      final MulticurveProviderInterface multicurve) {
    ArgChecker.notNull(coupon, "Coupon");
    ArgChecker.notNull(multicurve, "Multi-curve provider");
    double[] tFixingPeriods = coupon.getFixingPeriodTimes();
    int nbFixingPeriods = tFixingPeriods.length;
    final double tStart = tFixingPeriods[0];
    final double tEnd = tFixingPeriods[nbFixingPeriods - 1];
    final double delta = coupon.getFixingPeriodRemainingAccrualFactor();
    final double rateAccruedCompounded = multicurve.getSimplyCompoundForwardRate(coupon.getIndex(), tStart, tEnd, delta) * delta;
    final double rateAccrued = coupon.getRateAccrued() + Math.log(1.0 + rateAccruedCompounded);
    final double df = multicurve.getDiscountFactor(coupon.getCurrency(), coupon.getPaymentTime());
    final double pv = df * (rateAccrued * coupon.getNotional() + coupon.getSpreadAmount());
    return MultiCurrencyAmount.of(coupon.getCurrency(), pv);
  }

  /**
   * Computes the present value curve sensitivity.
   * @param coupon The coupon.
   * @param multicurve The multi-curve provider.
   * @return The present value curve sensitivities.
   */
  public MultipleCurrencyMulticurveSensitivity presentValueCurveSensitivity(final CouponONArithmeticAverageSpread coupon,
      final MulticurveProviderInterface multicurve) {
    ArgChecker.notNull(coupon, "Coupon");
    ArgChecker.notNull(multicurve, "Multi-curve provider");
    // Forward sweep
    double[] tFixingPeriods = coupon.getFixingPeriodTimes();
    int nbFixingPeriods = tFixingPeriods.length;
    final double tStart = tFixingPeriods[0];
    final double tEnd = tFixingPeriods[nbFixingPeriods - 1];
    final double delta = coupon.getFixingPeriodRemainingAccrualFactor();
    final double rateAccruedCompounded = multicurve.getSimplyCompoundForwardRate(coupon.getIndex(), tStart, tEnd, delta)
        * delta;
    final double rateAccrued = coupon.getRateAccrued() + Math.log(1.0 + rateAccruedCompounded);
    final double df = multicurve.getDiscountFactor(coupon.getCurrency(), coupon.getPaymentTime());
    // Backward sweep
    final double pvBar = 1.0;
    final double dfBar = (rateAccrued * coupon.getNotional() + coupon.getSpreadAmount()) * pvBar;
    final double rateAccruedBar = df * coupon.getNotional() * pvBar;
    final double rateAccruedCompoundedBar = rateAccruedBar / (1.0 + rateAccruedCompounded);
    final double forwardBar = delta * rateAccruedCompoundedBar;
    final Map<String, List<DoublesPair>> mapDsc = new HashMap<>();
    final List<DoublesPair> listDiscounting = new ArrayList<>();
    listDiscounting.add(DoublesPair.of(coupon.getPaymentTime(), -coupon.getPaymentTime() * df * dfBar));
    mapDsc.put(multicurve.getName(coupon.getCurrency()), listDiscounting);
    final Map<String, List<ForwardSensitivity>> mapFwd = new HashMap<>();
    final List<ForwardSensitivity> listForward = new ArrayList<>();
    listForward.add(new SimplyCompoundedForwardSensitivity(tStart, tEnd, delta, forwardBar));
    mapFwd.put(multicurve.getName(coupon.getIndex()), listForward);
    final MultipleCurrencyMulticurveSensitivity result =
        MultipleCurrencyMulticurveSensitivity.of(coupon.getCurrency(), MulticurveSensitivity.of(mapDsc, mapFwd));
    return result;
  }

}
