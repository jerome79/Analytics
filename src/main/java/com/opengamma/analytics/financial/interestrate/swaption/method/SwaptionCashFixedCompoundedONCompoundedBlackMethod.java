/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.swaption.method;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.ParRateCalculator;
import com.opengamma.analytics.financial.interestrate.PresentValueCalculator;
import com.opengamma.analytics.financial.interestrate.PresentValueCurveSensitivityCalculator;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.method.PricingMethod;
import com.opengamma.analytics.financial.interestrate.sensitivity.PresentValueSwaptionSurfaceSensitivity;
import com.opengamma.analytics.financial.interestrate.swap.method.SwapFixedCompoundingONCompoundingDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionCashFixedCompoundedONCompounded;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionCashFixedIbor;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackSwaptionBundle;
import com.opengamma.strata.basics.currency.CurrencyAmount;
import com.opengamma.strata.collect.ArgChecker;

/**
 *  Class used to compute the price and sensitivity of a cash-settled swaption with the Black model.
 *  @deprecated Use {@link com.opengamma.analytics.financial.interestrate.swaption.provider.SwaptionCashFixedIborBlackMethod}
 */
// TODO: Complete the code when the definition of cash settlement is clear for those swaptions.
@Deprecated
public final class SwaptionCashFixedCompoundedONCompoundedBlackMethod implements PricingMethod {

  /**
   * The method unique instance.
   */
  private static final SwaptionCashFixedCompoundedONCompoundedBlackMethod INSTANCE = new SwaptionCashFixedCompoundedONCompoundedBlackMethod();

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static SwaptionCashFixedCompoundedONCompoundedBlackMethod getInstance() {
    return INSTANCE;
  }

  /**
   * The present value curve sensitivity calculator.
   */
  private static final PresentValueCurveSensitivityCalculator PV_SENSITIVITY_CALCULATOR = PresentValueCurveSensitivityCalculator.getInstance();
  /**
   * The present value calculator.
   */
  private static final PresentValueCalculator PVC = PresentValueCalculator.getInstance();

  /**
   * Private constructor.
   */
  private SwaptionCashFixedCompoundedONCompoundedBlackMethod() {
  }

  /**
   * The swap method.
   */
  private static final SwapFixedCompoundingONCompoundingDiscountingMethod METHOD_SWAP = SwapFixedCompoundingONCompoundingDiscountingMethod.getInstance();
  /**
   * The par rate calculator.
   */
  private static final ParRateCalculator PRC = ParRateCalculator.getInstance();

  /**
   * Computes the present value of a cash-settled European swaption in the Black model.
   * @param swaption The swaption.
   * @param curveBlack The curves with Black volatility data.
   * @return The present value.
   */
  public CurrencyAmount presentValue(final SwaptionCashFixedCompoundedONCompounded swaption, final YieldCurveWithBlackSwaptionBundle curveBlack) {
    ArgChecker.isTrue(false, "Method not implemented");
    //    // Implementation comment: cash-settled swaptions make sense only for constant strike, the computation of coupon equivalent is not required.
    return null;
  }

  @Override
  public CurrencyAmount presentValue(final InstrumentDerivative instrument, final YieldCurveBundle curves) {
    ArgChecker.isTrue(instrument instanceof SwaptionCashFixedIbor, "Physical delivery swaption");
    ArgChecker.isTrue(curves instanceof YieldCurveWithBlackSwaptionBundle, "Bundle should contain Black Swaption data");
    return presentValue(instrument, curves);
  }

  /**
   * Computes the present value rate sensitivity to rates of a cash-settled European swaption in the Black model.
   * @param swaption The swaption.
   * @param curveBlack The curves with Black volatility data.
   * @return The present value curve sensitivity.
   */
  public InterestRateCurveSensitivity presentValueCurveSensitivity(final SwaptionCashFixedCompoundedONCompounded swaption, final YieldCurveWithBlackSwaptionBundle curveBlack) {
    ArgChecker.isTrue(false, "Method not implemented");
    //    // Derivative of the forward with respect to the rates.
    //    // Derivative of the cash annuity with respect to the forward.
    //    // Implementation note: strictly speaking, the strike equivalent is curve dependent; that dependency is ignored.
    return null;
  }

  /**
   * Computes the present value sensitivity to the Black volatility (also called vega) of a cash-settled European swaption in the Black swaption model.
   * @param swaption The swaption.
   * @param curveBlack The curves with Black volatility data.
   * @return The present value Black sensitivity.
   */
  public PresentValueSwaptionSurfaceSensitivity presentValueBlackSensitivity(final SwaptionCashFixedCompoundedONCompounded swaption, final YieldCurveWithBlackSwaptionBundle curveBlack) {
    ArgChecker.isTrue(false, "Method not implemented");
    return null;
  }

  /**
   * Computes the implied Black volatility of the vanilla swaption.
   * @param swaption The swaption.
   * @param curves The yield curve bundle.
   * @return The implied volatility.
   */
  public double impliedVolatility(final SwaptionCashFixedCompoundedONCompounded swaption, final YieldCurveBundle curves) {
    ArgChecker.notNull(curves, "Curves");
    ArgChecker.isTrue(curves instanceof YieldCurveWithBlackSwaptionBundle, "Yield curve bundle should contain Black swaption data");
    final YieldCurveWithBlackSwaptionBundle curvesBlack = (YieldCurveWithBlackSwaptionBundle) curves;
    ArgChecker.notNull(swaption, "Forex option");
    final double tenor = swaption.getMaturityTime();
    final double volatility = curvesBlack.getBlackParameters().getVolatility(swaption.getTimeToExpiry(), tenor);
    return volatility;
  }
}
