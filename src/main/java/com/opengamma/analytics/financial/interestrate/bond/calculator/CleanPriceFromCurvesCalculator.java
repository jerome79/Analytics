/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.bond.calculator;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.analytics.financial.interestrate.bond.method.BondSecurityDiscountingMethod;
import com.opengamma.strata.collect.ArgChecker;

/**
 * Calculate clean price for bonds.
 * @deprecated Use {@link com.opengamma.analytics.financial.provider.calculator.issuer.CleanPriceFromCurvesCalculator}
 */
@Deprecated
public final class CleanPriceFromCurvesCalculator extends InstrumentDerivativeVisitorAdapter<YieldCurveBundle, Double> {

  /**
   * The calculator instance.
   */
  private static final CleanPriceFromCurvesCalculator s_instance = new CleanPriceFromCurvesCalculator();
  /**
   * The fixed coupon bond method.
   */
  private static final BondSecurityDiscountingMethod METHOD_BOND = BondSecurityDiscountingMethod.getInstance();

  /**
   * Return the calculator instance.
   * @return The instance.
   */
  public static CleanPriceFromCurvesCalculator getInstance() {
    return s_instance;
  }

  /**
   * Private constructor.
   */
  private CleanPriceFromCurvesCalculator() {
  }

  @Override
  public Double visitBondFixedSecurity(final BondFixedSecurity bond, final YieldCurveBundle curves) {
    ArgChecker.notNull(curves, "curves");
    ArgChecker.notNull(bond, "bond");
    return METHOD_BOND.cleanPriceFromCurves(bond, curves);
  }

}
