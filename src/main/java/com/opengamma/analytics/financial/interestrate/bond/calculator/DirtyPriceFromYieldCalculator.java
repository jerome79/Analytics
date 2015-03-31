/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.bond.calculator;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.analytics.financial.interestrate.bond.provider.BondSecurityDiscountingMethod;
import com.opengamma.strata.collect.ArgChecker;

/**
 * Calculates the dirty price of a bond from the yield.
 */
public final class DirtyPriceFromYieldCalculator extends InstrumentDerivativeVisitorAdapter<Double, Double> {
  /** A singleton instance */
  private static final DirtyPriceFromYieldCalculator INSTANCE = new DirtyPriceFromYieldCalculator();

  /**
   * Gets the singleton instance.
   * @return The instance
   */
  public static DirtyPriceFromYieldCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor
   */
  private DirtyPriceFromYieldCalculator() {
  }

  @Override
  public Double visitBondFixedSecurity(final BondFixedSecurity bond, final Double yield) {
    ArgChecker.notNull(bond, "bond");
    ArgChecker.notNull(yield, "yield");
    return BondSecurityDiscountingMethod.getInstance().dirtyPriceFromYield(bond, yield);
  }
}
