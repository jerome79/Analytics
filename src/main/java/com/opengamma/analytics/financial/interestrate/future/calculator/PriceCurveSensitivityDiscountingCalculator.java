/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.calculator;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuture;
import com.opengamma.analytics.financial.interestrate.future.method.BondFutureDiscountingMethod;
import com.opengamma.strata.collect.ArgChecker;

/**
 * Calculate security prices curve sensitivity for futures (bond and interest rate).
 * @deprecated {@link YieldCurveBundle} is deprecated
 */
@Deprecated
public final class PriceCurveSensitivityDiscountingCalculator extends InstrumentDerivativeVisitorAdapter<YieldCurveBundle, InterestRateCurveSensitivity> {

  /**
   * The calculator instance.
   */
  private static final PriceCurveSensitivityDiscountingCalculator s_instance = new PriceCurveSensitivityDiscountingCalculator();
  /**
   * The method to compute bond future prices.
   */
  private static final BondFutureDiscountingMethod METHOD_BOND_FUTURE = BondFutureDiscountingMethod.getInstance();

  /**
   * Return the calculator instance.
   * @return The instance.
   */
  public static PriceCurveSensitivityDiscountingCalculator getInstance() {
    return s_instance;
  }

  /**
   * Private constructor.
   */
  private PriceCurveSensitivityDiscountingCalculator() {
  }

  @Override
  public InterestRateCurveSensitivity visitBondFuture(final BondFuture future, final YieldCurveBundle curves) {
    ArgChecker.notNull(curves, "curves");
    ArgChecker.notNull(future, "future");
    return METHOD_BOND_FUTURE.priceCurveSensitivity(future, curves);
  }

}
