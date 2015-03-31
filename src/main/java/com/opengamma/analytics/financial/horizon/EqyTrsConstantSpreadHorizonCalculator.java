/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.horizon;


import java.time.ZonedDateTime;

import com.opengamma.analytics.financial.equity.trs.definition.EquityTotalReturnSwap;
import com.opengamma.analytics.financial.equity.trs.definition.EquityTotalReturnSwapDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterProviderInterface;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.analytics.util.timeseries.zdt.ImmutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.analytics.util.timeseries.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.strata.basics.currency.MultiCurrencyAmount;
import com.opengamma.strata.basics.date.HolidayCalendar;
import com.opengamma.strata.collect.ArgChecker;

/**
 * Calculates the difference in the present value of an equity total return swap between two dates without
 * rate slide i.e. assumes that the market moves in such a way that the discount factors or rates for the
 * same maturity <b>dates</b> will be equal.
 * <p>
 * Only the funding leg is considered in this calculation.
 */
public final class EqyTrsConstantSpreadHorizonCalculator extends HorizonCalculator<EquityTotalReturnSwapDefinition, MulticurveProviderInterface, ZonedDateTimeDoubleTimeSeries> {
  /** Rolls down a yield curve provider */
  private static final CurveProviderConstantSpreadRolldownFunction CURVE_ROLLDOWN = CurveProviderConstantSpreadRolldownFunction.getInstance();
  /** The present value calculator */
  private static final InstrumentDerivativeVisitor<ParameterProviderInterface, MultiCurrencyAmount> PV_CALCULATOR =
      PresentValueDiscountingCalculator.getInstance();
  /** The singleton instance */
  private static final HorizonCalculator<EquityTotalReturnSwapDefinition, MulticurveProviderInterface, ZonedDateTimeDoubleTimeSeries> INSTANCE =
      new EqyTrsConstantSpreadHorizonCalculator();

  /**
   * Gets the singleton instance.
   * @return The instance
   */
  public static HorizonCalculator<EquityTotalReturnSwapDefinition, MulticurveProviderInterface, ZonedDateTimeDoubleTimeSeries> getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor
   */
  private EqyTrsConstantSpreadHorizonCalculator() {
  }

  @Override
  public MultiCurrencyAmount getTheta(final EquityTotalReturnSwapDefinition definition, final ZonedDateTime date, final MulticurveProviderInterface data,
      final int daysForward, final HolidayCalendar calendar, final ZonedDateTimeDoubleTimeSeries fixingSeries) {
    ArgChecker.notNull(definition, "definition");
    ArgChecker.notNull(date, "date");
    ArgChecker.notNull(data, "data");
    ArgChecker.isTrue(daysForward == 1 || daysForward == -1, "daysForward must be either 1 or -1");
    final EquityTotalReturnSwap instrumentToday = definition.toDerivative(date, fixingSeries);
    final ZonedDateTime horizonDate = date.plusDays(daysForward);
    final double shiftTime = TimeCalculator.getTimeBetween(date, horizonDate);
    final EquityTotalReturnSwap instrumentTomorrow = definition.toDerivative(horizonDate, fixingSeries);
    final MulticurveProviderInterface dataTomorrow = (MulticurveProviderInterface) CURVE_ROLLDOWN.rollDown(data, shiftTime);
    final MultiCurrencyAmount pvTomorrow = instrumentTomorrow.getFundingLeg().accept(PV_CALCULATOR, dataTomorrow);
    final MultiCurrencyAmount pvToday = instrumentToday.getFundingLeg().accept(PV_CALCULATOR, data);
    return subtract(pvTomorrow, pvToday);
  }

  @Override
  public MultiCurrencyAmount getTheta(final EquityTotalReturnSwapDefinition definition, final ZonedDateTime date, final MulticurveProviderInterface data,
      final int daysForward, final HolidayCalendar calendar) {
    return getTheta(definition, date, data, daysForward, calendar, ImmutableZonedDateTimeDoubleTimeSeries.ofEmptyUTC());
  }

}
