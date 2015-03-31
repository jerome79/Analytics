/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.horizon;


import java.time.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.bond.BondTransactionDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.provider.calculator.issuer.PresentValueIssuerCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterIssuerProviderInterface;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.strata.basics.currency.MultiCurrencyAmount;
import com.opengamma.strata.basics.date.HolidayCalendar;
import com.opengamma.strata.collect.ArgChecker;

/**
 * Calculates the difference in the present value of a bond between two dates without rate slide
 * i.e. assumes that the market moves in such a way that the discount factors or rates for the same
 * maturity <b>dates</b> will be equal.
 */
public final class BondConstantSpreadHorizonCalculator extends HorizonCalculator<BondTransactionDefinition<?, ?>, IssuerProviderInterface, Double> {
  /** Rolls down a yield curve provider */
  private static final CurveProviderConstantSpreadRolldownFunction CURVE_ROLLDOWN = CurveProviderConstantSpreadRolldownFunction.getInstance();
  /** The present value calculator */
  private static final InstrumentDerivativeVisitor<ParameterIssuerProviderInterface, MultiCurrencyAmount> PV_CALCULATOR =
      PresentValueIssuerCalculator.getInstance();
  /** The singleton instance */
  private static final HorizonCalculator<BondTransactionDefinition<?, ?>, IssuerProviderInterface, Double> INSTANCE =
      new BondConstantSpreadHorizonCalculator();

  /**
   * Gets the singleton instance.
   * @return The instance
   */
  public static HorizonCalculator<BondTransactionDefinition<?, ?>, IssuerProviderInterface, Double> getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor
   */
  private BondConstantSpreadHorizonCalculator() {
  }

  @Override
  public MultiCurrencyAmount getTheta(final BondTransactionDefinition<?, ?> definition, final ZonedDateTime date, final IssuerProviderInterface data,
      final int daysForward, final HolidayCalendar calendar) {
    ArgChecker.notNull(definition, "definition");
    ArgChecker.notNull(date, "date");
    ArgChecker.notNull(data, "data");
    ArgChecker.isTrue(daysForward == 1 || daysForward == -1, "daysForward must be either 1 or -1");
    final InstrumentDerivative instrumentToday = definition.toDerivative(date);
    final ZonedDateTime horizonDate = date.plusDays(daysForward);
    final double shiftTime = TimeCalculator.getTimeBetween(date, horizonDate);
    final InstrumentDerivative instrumentTomorrow = definition.toDerivative(horizonDate);
    final ParameterIssuerProviderInterface dataTomorrow = (ParameterIssuerProviderInterface) CURVE_ROLLDOWN.rollDown(data, shiftTime);
    final MultiCurrencyAmount pvTomorrow = instrumentTomorrow.accept(PV_CALCULATOR, dataTomorrow);
    final MultiCurrencyAmount pvToday = instrumentToday.accept(PV_CALCULATOR, data);
    return subtract(pvTomorrow, pvToday);
  }

}
