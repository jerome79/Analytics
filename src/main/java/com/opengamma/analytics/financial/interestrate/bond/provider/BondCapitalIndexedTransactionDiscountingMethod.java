/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.bond.provider;

import com.opengamma.analytics.financial.interestrate.bond.definition.BondCapitalIndexedTransaction;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.provider.calculator.inflation.PresentValueCurveSensitivityDiscountingInflationCalculator;
import com.opengamma.analytics.financial.provider.calculator.inflation.PresentValueDiscountingInflationCalculator;
import com.opengamma.analytics.financial.provider.description.inflation.InflationIssuerProviderInterface;
import com.opengamma.analytics.financial.provider.description.inflation.ParameterInflationIssuerProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.inflation.MultipleCurrencyInflationSensitivity;
import com.opengamma.strata.basics.currency.MultiCurrencyAmount;
import com.opengamma.strata.collect.ArgChecker;

/**
 * Pricing method for inflation bond transaction. The price is computed by index estimation and discounting.
 */
public final class BondCapitalIndexedTransactionDiscountingMethod {

  /**
   * The unique instance of the class.
   */
  private static final BondCapitalIndexedTransactionDiscountingMethod INSTANCE = new BondCapitalIndexedTransactionDiscountingMethod();

  /**
   * Return the class instance.
   * @return The instance.
   */
  public static BondCapitalIndexedTransactionDiscountingMethod getInstance() {
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
  private static final BondCapitalIndexedSecurityDiscountingMethod METHOD_SECURITY = 
      new BondCapitalIndexedSecurityDiscountingMethod();

  /**
   * Computes the present value of a capital indexed bound transaction by index estimation and discounting.
   * @param bond The bond transaction.
   * @param provider The provider.
   * @return The present value.
   */
  public MultiCurrencyAmount presentValue(final BondCapitalIndexedTransaction<?> bond, final InflationIssuerProviderInterface provider) {
    double notional = bond.getNotionalStandard();
    final MultiCurrencyAmount pvBond = METHOD_SECURITY.presentValue(bond.getBondTransaction(), provider);
    final MultiCurrencyAmount pvSettlement = bond.getBondTransaction().getSettlement().
        accept(PVIC, provider.getInflationProvider()).multipliedBy(-bond.getQuantity() * (bond.getTransactionPrice() 
            + bond.getBondStandard().getAccruedInterest() / notional));
    return pvBond.multipliedBy(bond.getQuantity()).plus(pvSettlement);
  }

  /**
   * Computes the security present value from a quoted clean real price.
   * @param bond The bond transaction.
   * @param provider The provider.
   * @param cleanPriceReal The clean price.
   * @return The present value.
   */
  public MultiCurrencyAmount presentValueFromCleanPriceReal(final BondCapitalIndexedTransaction<Coupon> bond, final InflationIssuerProviderInterface provider, final double cleanPriceReal) {
    ArgChecker.notNull(bond, "Coupon");
    ArgChecker.notNull(provider, "Provider");
    final MultiCurrencyAmount pvBond = METHOD_SECURITY.presentValueFromCleanRealPrice(bond.getBondTransaction(), provider, cleanPriceReal);
    final MultiCurrencyAmount pvSettlement = bond.getBondTransaction().getSettlement().accept(PVIC, provider.getInflationProvider()).multipliedBy(
        bond.getQuantity() * bond.getBondTransaction().getCoupon().getNthPayment(0).getNotional());
    return pvBond.plus(pvSettlement);
  }

  /**
   * Computes the present value of a capital indexed bound transaction by index estimation and discounting.
   * @param bond The bond transaction.
   * @param provider The provider.
   * @return The present value.
   */
  public MultipleCurrencyInflationSensitivity presentValueCurveSensitivity(final BondCapitalIndexedTransaction<?> bond, 
      final ParameterInflationIssuerProviderInterface provider) {
    final MultipleCurrencyInflationSensitivity sensitivityBond = METHOD_SECURITY.presentValueCurveSensitivity(bond.getBondTransaction(), provider);
    final MultipleCurrencyInflationSensitivity sensitivitySettlement = bond.getBondTransaction().getSettlement().
        accept(PVCSIC, provider.getInflationProvider()).multipliedBy(-bond.getQuantity() * bond.getTransactionPrice());
    return sensitivityBond.multipliedBy(bond.getQuantity()).plus(sensitivitySettlement);
  }

}
