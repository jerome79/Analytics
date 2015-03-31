/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.forex.method;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.strata.basics.currency.MultiCurrencyAmount;

/**
 * Interface for Forex pricing methods.
 * @deprecated {@link YieldCurveBundle} is deprecated
 */
@Deprecated
public interface ForexPricingMethod {

  /**
   * Computes the present value of the instrument.
   * @param instrument The instrument.
   * @param curves The yield curves.
   * @return The present value.
   */
  MultiCurrencyAmount presentValue(final InstrumentDerivative instrument, final YieldCurveBundle curves);

  /**
   * Computes the currency exposure of the instrument.
   * @param instrument The instrument.
   * @param curves The yield curves.
   * @return The currency exposure.
   */
  MultiCurrencyAmount currencyExposure(InstrumentDerivative instrument, YieldCurveBundle curves);

  /**
   * Computes the curve sensitivity of the instrument.
   * @param instrument The instrument.
   * @param curves The yield curves.
   * @return The curve sensitivity.
   */
  MultipleCurrencyInterestRateCurveSensitivity presentValueCurveSensitivity(InstrumentDerivative instrument, YieldCurveBundle curves);

}
