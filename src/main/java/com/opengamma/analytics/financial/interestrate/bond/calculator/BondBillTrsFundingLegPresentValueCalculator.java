/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.bond.calculator;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.bond.definition.BillTotalReturnSwap;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondTotalReturnSwap;
import com.opengamma.analytics.financial.interestrate.bond.provider.BondTotalReturnSwapDiscountingMethod;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderInterface;
import com.opengamma.strata.basics.currency.MultiCurrencyAmount;
import com.opengamma.strata.collect.ArgChecker;

/**
 * Calculates the present value of the funding leg of a bond total return swap. 
 */
public final class BondBillTrsFundingLegPresentValueCalculator extends InstrumentDerivativeVisitorAdapter<IssuerProviderInterface, MultiCurrencyAmount> {
  /** A singleton instance */
  private static final BondBillTrsFundingLegPresentValueCalculator INSTANCE = new BondBillTrsFundingLegPresentValueCalculator();

  /**
   * Gets the singleton instance.
   * @return The singleton instance
   */
  public static BondBillTrsFundingLegPresentValueCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private BondBillTrsFundingLegPresentValueCalculator() {
  }

  @Override
  public MultiCurrencyAmount visitBondTotalReturnSwap(final BondTotalReturnSwap bondTrs, final IssuerProviderInterface data) {
    ArgChecker.notNull(bondTrs, "bondTrs");
    ArgChecker.notNull(data, "data");
    return BondTotalReturnSwapDiscountingMethod.getInstance().presentValueFundingLeg(bondTrs, data);
  }

  @Override
  public MultiCurrencyAmount visitBillTotalReturnSwap(final BillTotalReturnSwap billTrs, final IssuerProviderInterface data) {
    ArgChecker.notNull(billTrs, "bondTrs");
    ArgChecker.notNull(data, "data");
    return BondTotalReturnSwapDiscountingMethod.getInstance().presentValueFundingLeg(billTrs, data);
  }

}
