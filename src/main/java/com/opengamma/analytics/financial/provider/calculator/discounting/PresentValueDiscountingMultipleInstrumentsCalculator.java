/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.discounting;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorSameMethodAdapter;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.strata.basics.currency.MultiCurrencyAmount;
import com.opengamma.strata.collect.ArgChecker;
import com.opengamma.strata.collect.tuple.Pair;

/**
 * Calculator of the present value as a multiple currency amount using cash-flow discounting and forward estimation.
 * Allows multiple instruments to be passed in and the aggregate PV to be calculated.
 */
public final class PresentValueDiscountingMultipleInstrumentsCalculator extends
    InstrumentDerivativeVisitorSameMethodAdapter<Pair<InstrumentDerivative[], MulticurveProviderInterface>, MultiCurrencyAmount> {

  /**
   * The unique instance of the calculator.
   */
  private static final PresentValueDiscountingCalculator CALCULATOR = PresentValueDiscountingCalculator.getInstance();

  /**
   * The unique instance of this visitor.
   */
  private static final PresentValueDiscountingMultipleInstrumentsCalculator INSTANCE = new PresentValueDiscountingMultipleInstrumentsCalculator();

  /**
   * Get an instance of this calculator.
   * @return the instance
   */
  public static PresentValueDiscountingMultipleInstrumentsCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private PresentValueDiscountingMultipleInstrumentsCalculator() {
  }

  @Override
  public MultiCurrencyAmount visit(InstrumentDerivative derivative) {
    throw new UnsupportedOperationException(this.getClass().getSimpleName() + " requires data passed in");
  }

  /**
   * Price instrument plus others.
   *
   * @param derivative the derivative
   * @param data Array of other derivatives to price, not null, may be empty. The curve provider to price with.
   * @return the multiple currency amount discounted pv.
   */
  @Override
  public MultiCurrencyAmount visit(InstrumentDerivative derivative, Pair<InstrumentDerivative[], MulticurveProviderInterface> data) {
    ArgChecker.notNull(data, "data");
    final MulticurveProviderInterface curves = data.getSecond();
    ArgChecker.notNull(curves, "multicurveprovider");
    ArgChecker.noNulls(data.getFirst(), "instruments");
    MultiCurrencyAmount amount = derivative.accept(CALCULATOR, curves);
    for (final InstrumentDerivative other : data.getFirst()) {
      amount = amount.plus(other.accept(CALCULATOR, curves));
    }
    return amount;
  }

}
