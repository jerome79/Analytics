/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.horizon;


import java.time.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.bond.BillTotalReturnSwapDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.bond.definition.BillTotalReturnSwap;
import com.opengamma.analytics.financial.provider.calculator.issuer.PresentValueIssuerCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterIssuerProviderInterface;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.analytics.util.timeseries.zdt.ImmutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.analytics.util.timeseries.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.basics.currency.CurrencyAmount;
import com.opengamma.strata.basics.currency.FxMatrix;
import com.opengamma.strata.basics.currency.MultiCurrencyAmount;
import com.opengamma.strata.basics.date.HolidayCalendar;
import com.opengamma.strata.collect.ArgChecker;


/**
 * Calculates the difference in the present value of a bill total return swap between two dates without
 * rate slide i.e. assumes that the market moves in such a way that the discount factors or rates for the
 * same maturity <b>dates</b> will be equal.
 */
public final class BillTrsConstantSpreadHorizonCalculator extends HorizonCalculator<BillTotalReturnSwapDefinition, IssuerProviderInterface, ZonedDateTimeDoubleTimeSeries> {
  /** Rolls down a yield curve provider */
  private static final CurveProviderConstantSpreadRolldownFunction CURVE_ROLLDOWN = CurveProviderConstantSpreadRolldownFunction.getInstance();
  /** The present value calculator */
  private static final InstrumentDerivativeVisitor<ParameterIssuerProviderInterface, MultiCurrencyAmount> PV_CALCULATOR =
      PresentValueIssuerCalculator.getInstance();
  /** The singleton instance */
  private static final HorizonCalculator<BillTotalReturnSwapDefinition, IssuerProviderInterface, ZonedDateTimeDoubleTimeSeries> INSTANCE =
      new BillTrsConstantSpreadHorizonCalculator();

  /**
   * Gets the singleton instance.
   * @return The instance
   */
  public static HorizonCalculator<BillTotalReturnSwapDefinition, IssuerProviderInterface, ZonedDateTimeDoubleTimeSeries> getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor
   */
  private BillTrsConstantSpreadHorizonCalculator() {
  }

  @Override
  public MultiCurrencyAmount getTheta(BillTotalReturnSwapDefinition definition, ZonedDateTime date,
                                         IssuerProviderInterface data, int daysForward, HolidayCalendar calendar, 
                                         ZonedDateTimeDoubleTimeSeries fixingSeries) {
    ArgChecker.notNull(definition, "definition");
    ArgChecker.notNull(date, "date");
    ArgChecker.notNull(data, "data");
    ArgChecker.isTrue(daysForward == 1 || daysForward == -1, "daysForward must be either 1 or -1");
    BillTotalReturnSwap instrumentToday = definition.toDerivative(date, fixingSeries);
    ZonedDateTime horizonDate = date.plusDays(daysForward);
    double shiftTime = TimeCalculator.getTimeBetween(date, horizonDate);
    BillTotalReturnSwap instrumentTomorrow = definition.toDerivative(horizonDate, fixingSeries);
    IssuerProviderInterface dataTomorrow = (IssuerProviderInterface) CURVE_ROLLDOWN.rollDown(data, shiftTime);
    MultiCurrencyAmount fundingLegPvTomorrow = instrumentTomorrow.getFundingLeg().accept(PV_CALCULATOR, dataTomorrow);
    MultiCurrencyAmount fundingLegPvToday = instrumentToday.getFundingLeg().accept(PV_CALCULATOR, data);
    MultiCurrencyAmount billLegPvTomorrow = instrumentTomorrow.getAsset().accept(PV_CALCULATOR, dataTomorrow).multipliedBy(instrumentTomorrow.getQuantity());
    MultiCurrencyAmount billLegPvToday = instrumentToday.getAsset().accept(PV_CALCULATOR, data).multipliedBy(instrumentToday.getQuantity());
    Currency assetCurrency = instrumentToday.getAsset().getCurrency();
    Currency fundingCurrency = instrumentToday.getFundingLeg().getCurrency();

    FxMatrix fxMatrix = data.getMulticurveProvider().getFxRates();

    CurrencyAmount fundingLegTodayConverted = fxMatrix.convert(fundingLegPvToday.getAmount(fundingCurrency), assetCurrency);
    MultiCurrencyAmount pvToday = billLegPvToday.plus(fundingLegTodayConverted);

    CurrencyAmount fundingLegTomorrowConverted = fxMatrix.convert(fundingLegPvTomorrow.getAmount(fundingCurrency), assetCurrency);
    MultiCurrencyAmount pvTomorrow = billLegPvTomorrow.plus(fundingLegTomorrowConverted);
    return subtract(pvTomorrow, pvToday);
  }

  @Override
  public MultiCurrencyAmount getTheta(BillTotalReturnSwapDefinition definition, ZonedDateTime date,
                                         IssuerProviderInterface data, int daysForward, 
                                         HolidayCalendar calendar) {
    return getTheta(definition, date, data, daysForward, calendar, ImmutableZonedDateTimeDoubleTimeSeries.ofEmptyUTC());
  }

}
