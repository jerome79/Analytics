/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.bond.calculator;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondTotalReturnSwap;
import com.opengamma.analytics.financial.provider.calculator.discounting.PV01CurveParametersCalculator;
import com.opengamma.analytics.financial.provider.calculator.issuer.PresentValueCurveSensitivityIssuerCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterIssuerProviderInterface;
import com.opengamma.analytics.util.amount.ReferenceAmount;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.collect.ArgChecker;
import com.opengamma.strata.collect.tuple.Pair;

/**
 * Calculates the PV01s for a bond total return swap. 
 * @deprecated Use the standard PV01CurveParametersCalculator.
 */
@Deprecated
public final class BondTrsPV01Calculator extends InstrumentDerivativeVisitorAdapter<ParameterIssuerProviderInterface, ReferenceAmount<Pair<String, Currency>>> {
  /** A singleton instance */
  private static final BondTrsPV01Calculator INSTANCE = new BondTrsPV01Calculator();
  /** The PV01 calculator */
  private static final InstrumentDerivativeVisitor<ParameterIssuerProviderInterface, ReferenceAmount<Pair<String, Currency>>> CALCULATOR =
      new PV01CurveParametersCalculator<>(PresentValueCurveSensitivityIssuerCalculator.getInstance());

  /**
   * Gets the singleton instance.
   * @return The singleton instance
   */
  public static BondTrsPV01Calculator getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private BondTrsPV01Calculator() {
  }

  @Override
  public ReferenceAmount<Pair<String, Currency>> visitBondTotalReturnSwap(final BondTotalReturnSwap bondTrs, final ParameterIssuerProviderInterface data) {
    ArgChecker.notNull(bondTrs, "bondTrs");
    ArgChecker.notNull(data, "data");
    return bondTrs.accept(CALCULATOR, data);
  }

}
