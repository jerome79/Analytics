/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.horizon;


import java.time.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.bond.BondTotalReturnSwapDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondTotalReturnSwap;
import com.opengamma.analytics.financial.provider.calculator.issuer.PresentValueIssuerCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterIssuerProviderInterface;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.analytics.util.timeseries.zdt.ImmutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.analytics.util.timeseries.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.basics.currency.CurrencyAmount;
import com.opengamma.strata.basics.currency.MultiCurrencyAmount;
import com.opengamma.strata.collect.ArgChecker;

/**
 * Calculates the difference in the present value of a bond total return swap between two dates without
 * rate slide i.e. assumes that the market moves in such a way that the discount factors or rates for the
 * same maturity <b>dates</b> will be equal.
 */
public final class BondTrsConstantSpreadHorizonCalculator extends HorizonCalculator<BondTotalReturnSwapDefinition, IssuerProviderInterface, ZonedDateTimeDoubleTimeSeries> {
  /** Rolls down a yield curve provider */
  private static final CurveProviderConstantSpreadRolldownFunction CURVE_ROLLDOWN = CurveProviderConstantSpreadRolldownFunction.getInstance();
  /** The present value calculator */
  private static final InstrumentDerivativeVisitor<ParameterIssuerProviderInterface, MultiCurrencyAmount> PV_CALCULATOR =
      PresentValueIssuerCalculator.getInstance();
  /** The singleton instance */
  private static final HorizonCalculator<BondTotalReturnSwapDefinition, IssuerProviderInterface, ZonedDateTimeDoubleTimeSeries> INSTANCE =
      new BondTrsConstantSpreadHorizonCalculator();

  /**
   * Gets the singleton instance.
   * @return The instance
   */
  public static HorizonCalculator<BondTotalReturnSwapDefinition, IssuerProviderInterface, ZonedDateTimeDoubleTimeSeries> getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor
   */
  private BondTrsConstantSpreadHorizonCalculator() {
  }

  @Override
  public MultiCurrencyAmount getTheta(final BondTotalReturnSwapDefinition definition, final ZonedDateTime date,
                                         final IssuerProviderInterface data, final int daysForward, final Calendar calendar, 
                                         final ZonedDateTimeDoubleTimeSeries fixingSeries) {
    ArgChecker.notNull(definition, "definition");
    ArgChecker.notNull(date, "date");
    ArgChecker.notNull(data, "data");
    ArgChecker.isTrue(daysForward == 1 || daysForward == -1, "daysForward must be either 1 or -1");
    final BondTotalReturnSwap instrumentToday = definition.toDerivative(date, fixingSeries);
    final ZonedDateTime horizonDate = date.plusDays(daysForward);
    final double shiftTime = TimeCalculator.getTimeBetween(date, horizonDate);
    final BondTotalReturnSwap instrumentTomorrow = definition.toDerivative(horizonDate, fixingSeries);
    final IssuerProviderInterface dataTomorrow = (IssuerProviderInterface) CURVE_ROLLDOWN.rollDown(data, shiftTime);
    final MultiCurrencyAmount fundingLegPvTomorrow = instrumentTomorrow.getFundingLeg().accept(PV_CALCULATOR, dataTomorrow);
    final MultiCurrencyAmount fundingLegPvToday = instrumentToday.getFundingLeg().accept(PV_CALCULATOR, data);
    final MultiCurrencyAmount bondLegPvTomorrow = instrumentTomorrow.getAsset().accept(PV_CALCULATOR, dataTomorrow).multipliedBy(instrumentTomorrow.getQuantity());
    final MultiCurrencyAmount bondLegPvToday = instrumentToday.getAsset().accept(PV_CALCULATOR, data).multipliedBy(instrumentToday.getQuantity());
    final Currency assetCurrency = instrumentToday.getAsset().getCurrency();
    final Currency fundingCurrency = instrumentToday.getFundingLeg().getCurrency();
    final double fxRate = data.getMulticurveProvider().getFxRate(fundingCurrency, assetCurrency);
    final MultiCurrencyAmount pvToday = bondLegPvToday.plus(CurrencyAmount.of(assetCurrency, fundingLegPvToday.getAmount(fundingCurrency).getAmount() * fxRate));
    final MultiCurrencyAmount pvTomorrow = bondLegPvTomorrow.plus(CurrencyAmount.of(assetCurrency, fundingLegPvTomorrow.getAmount(fundingCurrency).getAmount() * fxRate));
    return subtract(pvTomorrow, pvToday);
  }

  @Override
  public MultiCurrencyAmount getTheta(final BondTotalReturnSwapDefinition definition, final ZonedDateTime date, final IssuerProviderInterface data,
      final int daysForward, final Calendar calendar) {
    return getTheta(definition, date, data, daysForward, calendar, ImmutableZonedDateTimeDoubleTimeSeries.ofEmptyUTC());
  }

}
