/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.method;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionPremiumTransaction;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackCubeBundle;
import com.opengamma.analytics.util.amount.SurfaceValue;
import com.opengamma.strata.basics.currency.CurrencyAmount;
import com.opengamma.strata.collect.ArgChecker;

/**
 * Method for the pricing of interest rate future options with up-front premium. The pricing is done with a Black approach on the future rate (1.0-price).
 * The Black parameters are represented by (expiration-strike-delay) surfaces. The "delay" is the time between option expiration and future last trading date,
 * i.e. 0 for quarterly options and x for x-year mid-curve options. The future prices are computed without convexity adjustments.
 * @deprecated The parent class is deprecated
 */
@Deprecated
public final class InterestRateFutureOptionPremiumTransactionBlackSurfaceMethod extends InterestRateFutureOptionPremiumTransactionMethod {

  /** Calculates values for the security */
  private static final InterestRateFutureOptionPremiumSecurityBlackSurfaceMethod SECURITY_METHOD = InterestRateFutureOptionPremiumSecurityBlackSurfaceMethod.getInstance();

  /**
   * Creates the method unique instance.
   */
  private static final InterestRateFutureOptionPremiumTransactionBlackSurfaceMethod INSTANCE = new InterestRateFutureOptionPremiumTransactionBlackSurfaceMethod();

  /**
   * Return the method unique instance.
   * @return The instance.
   */
  public static InterestRateFutureOptionPremiumTransactionBlackSurfaceMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private InterestRateFutureOptionPremiumTransactionBlackSurfaceMethod() {
    super(InterestRateFutureOptionPremiumSecurityBlackSurfaceMethod.getInstance());
  }

  @Override
  public CurrencyAmount presentValue(final InstrumentDerivative instrument, final YieldCurveBundle curves) {
    ArgChecker.isTrue(instrument instanceof InterestRateFutureOptionPremiumTransaction, "The instrument should be a InterestRateFutureOptionPremiumTransaction");
    final InterestRateFutureOptionPremiumTransaction transaction = (InterestRateFutureOptionPremiumTransaction) instrument;
    final double price = SECURITY_METHOD.optionPrice(transaction.getUnderlyingSecurity(), curves);
    final double pvTransaction = presentValueFromPrice(transaction, curves, price);
    return CurrencyAmount.of(transaction.getUnderlyingSecurity().getCurrency(), pvTransaction);
  }

  /**
   * Computes the present value volatility sensitivity of a transaction.
   * @param transaction The future option transaction.
   * @param blackData The curve and Black volatility data.
   * @return The present value curve sensitivity.
   */
  public double vega(final InterestRateFutureOptionPremiumTransaction transaction, final YieldCurveWithBlackCubeBundle blackData) {
    final double securitySensitivity = SECURITY_METHOD.optionPriceVega(transaction.getUnderlyingSecurity(), blackData);
    final double txnSensitivity = securitySensitivity
        * transaction.getQuantity()
        * transaction.getUnderlyingSecurity().getUnderlyingFuture().getNotional()
        * transaction.getUnderlyingSecurity().getUnderlyingFuture().getPaymentAccrualFactor();
    return txnSensitivity;
  }

  /**
   * Computes the theta of a transaction.
   * @param transaction The future option transaction.
   * @param blackData The curve and Black volatility data.
   * @return The theta.
   */
  public double theta(final InterestRateFutureOptionPremiumTransaction transaction, final YieldCurveWithBlackCubeBundle blackData) {
    final double securitySensitivity = SECURITY_METHOD.optionPriceTheta(transaction.getUnderlyingSecurity(), blackData);
    final double txnSensitivity = securitySensitivity
        * transaction.getQuantity()
        * transaction.getUnderlyingSecurity().getUnderlyingFuture().getNotional()
        * transaction.getUnderlyingSecurity().getUnderlyingFuture().getPaymentAccrualFactor();
    return txnSensitivity;
  }

  /**
   * Computes the present value volatility sensitivity of a transaction.
   * @param transaction The future option transaction.
   * @param blackData The curve and Black volatility data.
   * @return The present value curve sensitivity.
   */
  public SurfaceValue presentValueBlackSensitivity(final InterestRateFutureOptionPremiumTransaction transaction, final YieldCurveWithBlackCubeBundle blackData) {
    SurfaceValue securitySensitivity = ((InterestRateFutureOptionPremiumSecurityBlackSurfaceMethod) getSecurityMethod()).priceBlackSensitivity(transaction.getUnderlyingSecurity(), blackData);
    securitySensitivity = SurfaceValue.multiplyBy(securitySensitivity, transaction.getQuantity() * transaction.getUnderlyingSecurity().getUnderlyingFuture().getNotional()
        * transaction.getUnderlyingSecurity().getUnderlyingFuture().getPaymentAccrualFactor());
    return securitySensitivity;
  }

  /**
   * Computes the present value delta of a transaction.
   * This is with respect to futures price
   * @param transaction The future option transaction.
   * @param blackData The curve and Black volatility data.
   * @return The present value curve sensitivity.
   */
  public double deltaWrtFuturesPrice(final InterestRateFutureOptionPremiumTransaction transaction, final YieldCurveWithBlackCubeBundle blackData) {
    final double securityDelta = SECURITY_METHOD.optionPriceDelta(transaction.getUnderlyingSecurity(), blackData);
    final double txnDelta = securityDelta
        * transaction.getQuantity()
        * transaction.getUnderlyingSecurity().getUnderlyingFuture().getNotional()
        * transaction.getUnderlyingSecurity().getUnderlyingFuture().getPaymentAccrualFactor();
    return txnDelta;
  }

  /**
   * Computes the present value delta of a transaction.
   * This is with respect to futures price
   * @param transaction The future option transaction.
   * @param blackData The curve and Black volatility data.
   * @return The present value curve sensitivity.
   */
  public InterestRateCurveSensitivity deltaWrtCurve(final InterestRateFutureOptionPremiumTransaction transaction, final YieldCurveWithBlackCubeBundle blackData) {
    final InterestRateCurveSensitivity securityDelta = SECURITY_METHOD.priceCurveSensitivity(transaction.getUnderlyingSecurity(), blackData);
    final double scaleFactor = transaction.getQuantity() * transaction.getUnderlyingSecurity().getUnderlyingFuture().getNotional()
        * transaction.getUnderlyingSecurity().getUnderlyingFuture().getPaymentAccrualFactor();
    return securityDelta.multipliedBy(scaleFactor);
  }

  /**
   * Computes the present value gamma of a transaction.
   * This is with respect to either futures price, or rate=1-price
   * @param transaction The future option transaction.
   * @param blackData The curve and Black volatility data.
   * @return The present value curve sensitivity.
   */
  public double presentValueGamma(final InterestRateFutureOptionPremiumTransaction transaction, final YieldCurveWithBlackCubeBundle blackData) {
    final double securityGamma = SECURITY_METHOD.optionPriceGamma(transaction.getUnderlyingSecurity(), blackData);
    final double txnGamma = securityGamma
        * transaction.getQuantity()
        * transaction.getUnderlyingSecurity().getUnderlyingFuture().getNotional()
        * transaction.getUnderlyingSecurity().getUnderlyingFuture().getPaymentAccrualFactor();
    return txnGamma;
  }

  /**
   * Interpolates on the Black Volatility Surface at expiry and strike of optionTransaction
   * @param optionTransaction InterestRateFutureOptionPremiumTransaction
   * @param curveBundle YieldCurveWithBlackSwaptionBundle
   * @return Lognormal Implied Volatility
   */
  public Double impliedVolatility(final InterestRateFutureOptionPremiumTransaction optionTransaction, final YieldCurveBundle curveBundle) {
    ArgChecker.notNull(optionTransaction, "optionTransaction");
    return SECURITY_METHOD.impliedVolatility(optionTransaction.getUnderlyingSecurity(), curveBundle);
  }

}
