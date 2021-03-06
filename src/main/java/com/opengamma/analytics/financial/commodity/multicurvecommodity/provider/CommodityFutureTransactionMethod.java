/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.commodity.multicurvecommodity.provider;

import com.opengamma.analytics.financial.commodity.multicurvecommodity.derivative.CommodityFutureTransaction;
import com.opengamma.analytics.financial.provider.sensitivity.commodity.CommoditySensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.commodity.MultipleCurrencyCommoditySensitivity;
import com.opengamma.strata.basics.currency.MultiCurrencyAmount;
import com.opengamma.strata.collect.ArgChecker;

/**
 * Methods for the pricing of commodity futures generic to all models.
 */
public abstract class CommodityFutureTransactionMethod {

  /**
   * Compute the present value of a future transaction from a quoted price.
   * @param future The future.
   * @param price The quoted price.
   * @return The present value.
   */
  public MultiCurrencyAmount presentValueFromPrice(final CommodityFutureTransaction future, final double price) {
    final double pv = price - future.getReferencePrice() * future.getQuantity();
    return MultiCurrencyAmount.of(future.getCurrency(), pv);
  }

  /**
   * Compute the present value sensitivity to rates of a interest rate future by discounting.
   * @param future The future.
   * @param priceSensitivity The sensitivity of the futures price.
   * @return The present value rate sensitivity.
   */
  public MultipleCurrencyCommoditySensitivity presentValueCurveSensitivity(final CommodityFutureTransaction future, final CommoditySensitivity priceSensitivity) {
    ArgChecker.notNull(future, "Future");
    return MultipleCurrencyCommoditySensitivity.of(future.getCurrency(), priceSensitivity.multipliedBy(future.getQuantity() * future.getUnitAmount()));
  }
}
