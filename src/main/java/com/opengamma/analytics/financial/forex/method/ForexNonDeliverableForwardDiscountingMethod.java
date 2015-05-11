/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.forex.method;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opengamma.analytics.financial.forex.derivative.ForexNonDeliverableForward;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.strata.basics.currency.CurrencyAmount;
import com.opengamma.strata.basics.currency.MultiCurrencyAmount;
import com.opengamma.strata.collect.ArgChecker;
import com.opengamma.strata.collect.tuple.DoublesPair;

/**
 * Pricing method for Forex non-deliverable forward transactions by discounting.
 * @deprecated {@link YieldCurveBundle} is deprecated.
 * Use {@link com.opengamma.analytics.financial.forex.provider.ForexNonDeliverableForwardDiscountingMethod}
 */
@Deprecated
public final class ForexNonDeliverableForwardDiscountingMethod implements ForexPricingMethod {

  /**
   * The method unique instance.
   */
  private static final ForexNonDeliverableForwardDiscountingMethod INSTANCE = new ForexNonDeliverableForwardDiscountingMethod();

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static ForexNonDeliverableForwardDiscountingMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private ForexNonDeliverableForwardDiscountingMethod() {
  }

  /**
   * Computes the present value of the non-deliverable forward. The present value is in currency2 and equal to P*N*(1-X/F) where
   * <i>P</i> is the currency2 discount factor for the payment date, <i>N</i> is the notional, <i>X</i> is NDF rate and <i>F</i> the estimated forward exchange rate at the payment date.
   * @param ndf The non-deliverable forward.
   * @param curves The curve bundle (with FX rates).
   * @return The present value.
   */
  public MultiCurrencyAmount presentValue(final ForexNonDeliverableForward ndf, final YieldCurveBundle curves) {
    final double df2 = curves.getCurve(ndf.getDiscountingCurve2Name()).getDiscountFactor(ndf.getPaymentTime());
    final double df1 = curves.getCurve(ndf.getDiscountingCurve1Name()).getDiscountFactor(ndf.getPaymentTime());
    final double spot = curves.getFxRates().fxRate(ndf.getCurrency2(), ndf.getCurrency1());
    final double pv2 = ndf.getNotionalCurrency2() * (df2 - ndf.getExchangeRate() / spot * df1);
    return MultiCurrencyAmount.of(ndf.getCurrency2(), pv2);
  }

  @Override
  public MultiCurrencyAmount presentValue(final InstrumentDerivative instrument, final YieldCurveBundle curves) {
    ArgChecker.isTrue(instrument instanceof ForexNonDeliverableForward, "Derivative should be ForexNonDeliverableForward");
    return presentValue((ForexNonDeliverableForward) instrument, curves);
  }

  /**
   * Computes the currency exposure of the non-deliverable forward. The currency exposure is P_2 * N in currency2 and -P_1 * N * X in currency1
   * where <i>P_2</i> is the currency2 discount factor for the payment date, <i>N</i> is the notional, <i>P_1</i> is the currency1 discount factor for the payment date
   * and <i>X</i> is NDF rate.
   * @param ndf The non-deliverable forward.
   * @param curves The curve bundle (with FX rates).
   * @return The currency exposure.
   */
  public MultiCurrencyAmount currencyExposure(final ForexNonDeliverableForward ndf, final YieldCurveBundle curves) {
    final double df2 = curves.getCurve(ndf.getDiscountingCurve2Name()).getDiscountFactor(ndf.getPaymentTime());
    final double df1 = curves.getCurve(ndf.getDiscountingCurve1Name()).getDiscountFactor(ndf.getPaymentTime());
    final double pv1 = -ndf.getNotionalCurrency2() * ndf.getExchangeRate() * df1;
    final double pv2 = ndf.getNotionalCurrency2() * df2;
    return MultiCurrencyAmount.of(
        CurrencyAmount.of(ndf.getCurrency1(), pv1),
        CurrencyAmount.of(ndf.getCurrency2(), pv2));

  }

  @Override
  public MultiCurrencyAmount currencyExposure(final InstrumentDerivative instrument, final YieldCurveBundle curves) {
    ArgChecker.isTrue(instrument instanceof ForexNonDeliverableForward, "Derivative should be ForexNonDeliverableForward");
    return currencyExposure((ForexNonDeliverableForward) instrument, curves);
  }

  /**
   * Computes the forward exchange rate associated to the NDF (1 Cyy2 = fwd Cyy1).
   * @param ndf The non-deliverable forward.
   * @param curves The curve bundle (with FX rates).
   * @return The forward rate.
   */
  public double forwardForexRate(final ForexNonDeliverableForward ndf, final YieldCurveBundle curves) {
    final double dfDelivery = curves.getCurve(ndf.getDiscountingCurve2Name()).getDiscountFactor(ndf.getPaymentTime());
    final double dfNonDelivery = curves.getCurve(ndf.getDiscountingCurve1Name()).getDiscountFactor(ndf.getPaymentTime());
    final double spot = curves.getFxRates().fxRate(ndf.getCurrency2(), ndf.getCurrency1());
    return spot * dfDelivery / dfNonDelivery;
  }

  /**
   * The present value curve sensitivity for the non-deliverable forward.
   * @param ndf The non-deliverable forward.
   * @param curves The curve bundle (with FX rates).
   * @return The present value currency exposure.
   */
  public MultipleCurrencyInterestRateCurveSensitivity presentValueCurveSensitivity(final ForexNonDeliverableForward ndf, final YieldCurveBundle curves) {
    final double df2 = curves.getCurve(ndf.getDiscountingCurve2Name()).getDiscountFactor(ndf.getPaymentTime());
    final double df1 = curves.getCurve(ndf.getDiscountingCurve1Name()).getDiscountFactor(ndf.getPaymentTime());
    final double spot = curves.getFxRates().fxRate(ndf.getCurrency2(), ndf.getCurrency1());
    // Backward sweep
    final double pvBar = 1.0;
    final double df1Bar = -ndf.getNotionalCurrency2() * ndf.getExchangeRate() / spot * pvBar;
    final double df2Bar = ndf.getNotionalCurrency2() * pvBar;
    final Map<String, List<DoublesPair>> resultMap = new HashMap<>();
    final List<DoublesPair> listDiscounting1 = new ArrayList<>();
    listDiscounting1.add(DoublesPair.of(ndf.getPaymentTime(), -ndf.getPaymentTime() * df1 * df1Bar));
    resultMap.put(ndf.getDiscountingCurve1Name(), listDiscounting1);
    final List<DoublesPair> listDiscounting2 = new ArrayList<>();
    listDiscounting2.add(DoublesPair.of(ndf.getPaymentTime(), -ndf.getPaymentTime() * df2 * df2Bar));
    resultMap.put(ndf.getDiscountingCurve2Name(), listDiscounting2);
    final InterestRateCurveSensitivity result = new InterestRateCurveSensitivity(resultMap);
    return MultipleCurrencyInterestRateCurveSensitivity.of(ndf.getCurrency2(), result);
  }

  @Override
  public MultipleCurrencyInterestRateCurveSensitivity presentValueCurveSensitivity(final InstrumentDerivative instrument, final YieldCurveBundle curves) {
    ArgChecker.isTrue(instrument instanceof ForexNonDeliverableForward, "Forex non-deliverable forward");
    return presentValueCurveSensitivity((ForexNonDeliverableForward) instrument, curves);
  }

}
