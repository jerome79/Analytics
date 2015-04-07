/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.bond.provider;

import com.opengamma.analytics.financial.interestrate.bond.definition.BondCapitalIndexedTransaction;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.provider.calculator.inflation.PresentValueCurveSensitivityDiscountingInflationCalculator;
import com.opengamma.analytics.financial.provider.calculator.inflation.PresentValueDiscountingInflationCalculator;
import com.opengamma.analytics.financial.provider.description.inflation.InflationProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.inflation.MultipleCurrencyInflationSensitivity;
import com.opengamma.strata.basics.currency.MultiCurrencyAmount;
import com.opengamma.strata.collect.ArgChecker;

/**
 *  Pricing method for inflation bond transaction. The price is computed by index estimation and discounting. (without issuer)
 */
public final class BondCapitalIndexedTransactionDiscountingMethodWithoutIssuer {

  /**
   * The unique instance of the class.
   */
  private static final BondCapitalIndexedTransactionDiscountingMethodWithoutIssuer INSTANCE = new BondCapitalIndexedTransactionDiscountingMethodWithoutIssuer();

  /**
   * Return the class instance.
   * @return The instance.
   */
  public static BondCapitalIndexedTransactionDiscountingMethodWithoutIssuer getInstance() {
    return INSTANCE;
  }

  /**
   * The present value inflation calculator (for the different parts of the bond transaction).
   */
  private static final PresentValueDiscountingInflationCalculator PVIC = PresentValueDiscountingInflationCalculator.getInstance();
  private static final PresentValueCurveSensitivityDiscountingInflationCalculator PVCSIC = PresentValueCurveSensitivityDiscountingInflationCalculator.getInstance();
  /**
   * The method used for security computation.
   */
  private static final BondCapitalIndexedSecurityDiscountingMethodWithoutIssuer METHOD_SECURITY = new BondCapitalIndexedSecurityDiscountingMethodWithoutIssuer();

  /**
   * Computes the present value of a capital indexed bound transaction by index estimation and discounting.
   * @param bond The bond transaction.
   * @param provider The provider.
   * @return The present value.
   */
  public MultiCurrencyAmount presentValue(final BondCapitalIndexedTransaction<?> bond, final InflationProviderInterface provider) {
    final MultiCurrencyAmount pvBond = METHOD_SECURITY.presentValue(bond.getBondTransaction(), provider);
    final MultiCurrencyAmount pvSettlement = bond.getBondTransaction().getSettlement().accept(PVIC, provider).multipliedBy(
        bond.getQuantity() * bond.getBondTransaction().getCoupon().getNthPayment(0).getNotional());
    return pvBond.multipliedBy(bond.getQuantity()).plus(pvSettlement);
  }

  /**
   * Computes the security present value from a quoted clean real price.
   * @param bond The bond transaction.
   * @param provider The provider.
   * @param cleanPriceReal The clean price.
   * @return The present value.
   */
  public MultiCurrencyAmount presentValueFromCleanPriceReal(final BondCapitalIndexedTransaction<Coupon> bond, final InflationProviderInterface provider, final double cleanPriceReal) {
    ArgChecker.notNull(bond, "Coupon");
    ArgChecker.notNull(provider, "Provider");
    final MultiCurrencyAmount pvBond = METHOD_SECURITY.presentValueFromCleanPriceReal(bond.getBondTransaction(), provider, cleanPriceReal);
    final MultiCurrencyAmount pvSettlement = bond.getBondTransaction().getSettlement().accept(PVIC, provider).multipliedBy(
        bond.getQuantity() * bond.getBondTransaction().getCoupon().getNthPayment(0).getNotional());
    return pvBond.plus(pvSettlement);
  }

  /**
   * Computes the present value of a capital indexed bound transaction by index estimation and discounting.
   * @param bond The bond transaction.
   * @param provider The provider.
   * @return The present value.
   */
  public MultipleCurrencyInflationSensitivity presentValueCurveSensitivity(final BondCapitalIndexedTransaction<?> bond, final InflationProviderInterface provider) {
    final MultipleCurrencyInflationSensitivity sensitivityBond = METHOD_SECURITY.presentValueCurveSensitivity(bond.getBondTransaction(), provider);
    final MultipleCurrencyInflationSensitivity sensitivitySettlement = bond.getBondTransaction().getSettlement().accept(PVCSIC, provider).multipliedBy(
        bond.getQuantity() * bond.getBondTransaction().getCoupon().getNthPayment(0).getNotional());
    return sensitivityBond.multipliedBy(bond.getQuantity()).plus(sensitivitySettlement);
  }

}
