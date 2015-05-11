/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.provider;

import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginTransaction;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.strata.basics.currency.MultiCurrencyAmount;
import com.opengamma.strata.collect.ArgChecker;

/**
 * Method for the pricing of interest rate future options with daily margining.
 * @param <DATA_TYPE> Data type. Extends ParameterProviderInterface.
 */
public abstract class InterestRateFutureOptionMarginTransactionGenericMethod<DATA_TYPE extends ParameterProviderInterface> {

  /**
   * The method to compute the underlying security price and price curve sensitivity.
   */
  private final InterestRateFutureOptionMarginSecurityGenericMethod<DATA_TYPE> _methodSecurity;

  /**
   * Constructor.
   * @param methodSecurity The method to compute the underlying security price and price curve sensitivity.
   */
  public InterestRateFutureOptionMarginTransactionGenericMethod(InterestRateFutureOptionMarginSecurityGenericMethod<DATA_TYPE> methodSecurity) {
    _methodSecurity = methodSecurity;
  }

  /**
   * Returns the method to compute the underlying security price and price curve sensitivity.
   * @return The method.
   */
  public InterestRateFutureOptionMarginSecurityGenericMethod<DATA_TYPE> getSecurityMethod() {
    return _methodSecurity;
  }

  /**
   * Compute the present value of a future transaction from a quoted price.
   * @param option The future option.
   * @param price The quoted price.
   * @return The present value.
   */
  public MultiCurrencyAmount presentValueFromPrice(final InterestRateFutureOptionMarginTransaction option, final double price) {
    ArgChecker.notNull(option, "Option on STIR futures");
    double pv = (price - option.getReferencePrice()) * option.getUnderlyingSecurity().getUnderlyingFuture().getPaymentAccrualFactor() *
        option.getUnderlyingSecurity().getUnderlyingFuture().getNotional()
        * option.getQuantity();
    return MultiCurrencyAmount.of(option.getUnderlyingSecurity().getCurrency(), pv);
  }

  /**
   * Computes the present value of a transaction.
   * @param transaction The future option transaction.
   * @param data The data provider.
   * @return The present value.
   */
  public MultiCurrencyAmount presentValue(final InterestRateFutureOptionMarginTransaction transaction, final DATA_TYPE data) {
    ArgChecker.notNull(transaction, "Transaction on option on STIR futures");
    ArgChecker.notNull(data, "data provider");
    double priceSecurity = _methodSecurity.price(transaction.getUnderlyingSecurity(), data);
    MultiCurrencyAmount pvTransaction = presentValueFromPrice(transaction, priceSecurity);
    return pvTransaction;
  }

  /**
   * Computes the present value curve sensitivity of a transaction.
   * @param transaction The future option transaction.
   * @param data The data provider.
   * @return The present value curve sensitivity.
   */
  public MultipleCurrencyMulticurveSensitivity presentValueCurveSensitivity(final InterestRateFutureOptionMarginTransaction transaction, final DATA_TYPE data) {
    ArgChecker.notNull(transaction, "Transaction on option on STIR futures");
    ArgChecker.notNull(data, "data provider");
    MulticurveSensitivity securitySensitivity = _methodSecurity.priceCurveSensitivity(transaction.getUnderlyingSecurity(), data);
    return MultipleCurrencyMulticurveSensitivity.of(
        transaction.getCurrency(),
        securitySensitivity.multipliedBy(transaction.getQuantity() * transaction.getUnderlyingSecurity().getUnderlyingFuture().getNotional()
            * transaction.getUnderlyingSecurity().getUnderlyingFuture().getPaymentAccrualFactor()));
  }

}
