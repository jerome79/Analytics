/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity.variance;

import java.time.ZonedDateTime;

import com.opengamma.analytics.convention.businessday.BusinessDayDateUtils;
import com.opengamma.analytics.convention.calendar.Calendar;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.varianceswap.VarianceSwapDefinition;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.collect.ArgChecker;
import com.opengamma.strata.collect.timeseries.LocalDateDoubleTimeSeries;

/**
 * An equity variance swap is a forward contract of the realized variance of an underlying stock or index.
 */
public class EquityVarianceSwapDefinition extends VarianceSwapDefinition {
  /** Should the dividends be corrected for when pricing */
  private final boolean _correctForDividends;

  /**
   * Constructor based upon vega (volatility) parameterisation - strike and notional.
   * 
   * @param obsStartDate Date of first observation, not null
   * @param obsEndDate Date of final observation, not null
   * @param settlementDate Date of cash settlement, not null
   * @param currency Currency of cash settlement, not null
   * @param calendar Specification of good business days (and holidays), not null
   * @param annualizationFactor Number of business days per year, not null
   * @param volStrike Fair value of volatility, the square root of variance, struck at trade date
   * @param volNotional Trade pays the difference between realized and strike variance multiplied by 0.5 * volNotional / volStrike
   * @param correctForDividends Whether to correct for dividends when pricing
   */
  public EquityVarianceSwapDefinition(final ZonedDateTime obsStartDate, final ZonedDateTime obsEndDate, final ZonedDateTime settlementDate, final Currency currency, final Calendar calendar,
      final double annualizationFactor, final double volStrike, final double volNotional, final boolean correctForDividends) {
    super(obsStartDate, obsEndDate, settlementDate, currency, calendar, annualizationFactor, volStrike, volNotional);
    _correctForDividends = correctForDividends;
  }

  /**
   * Static constructor of a variance swap using a vega parameterisation of the contract.
   * @param obsStartDate Date of the first observation, not null
   * @param obsEndDate Date of the last observation, not null
   * @param settlementDate The settlement date, not null
   * @param currency The currency, not null
   * @param calendar The calendar used for calculating good business days, not null
   * @param annualizationFactor The annualisation factor
   * @param volStrike The volatility strike
   * @param volNotional The volatility notional
   * @param correctForDividends Whether to correct for dividends in pricing
   * @return The contract definition
   */
  public static EquityVarianceSwapDefinition fromVegaParams(final ZonedDateTime obsStartDate, final ZonedDateTime obsEndDate, final ZonedDateTime settlementDate, final Currency currency,
      final Calendar calendar, final double annualizationFactor, final double volStrike, final double volNotional, final boolean correctForDividends) {
    return new EquityVarianceSwapDefinition(obsStartDate, obsEndDate, settlementDate, currency, calendar, annualizationFactor, volStrike, volNotional, correctForDividends);
  }

  /**
   * Static constructor of a variance swap using a variance parameterisation of the contract.
   * @param obsStartDate Date of the first observation, not null
   * @param obsEndDate Date of the last observation, not null
   * @param settlementDate The settlement date, not null
   * @param currency The currency, not null
   * @param calendar The calendar used for calculating good business days, not null
   * @param annualizationFactor The annualisation factor
   * @param varStrike The variance strike, not negative
   * @param varNotional The variance notional
   * @param correctForDividends Whether to correct for dividends in pricing
   * @return The contract definition
   */
  public static EquityVarianceSwapDefinition fromVarianceParams(final ZonedDateTime obsStartDate, final ZonedDateTime obsEndDate, final ZonedDateTime settlementDate, final Currency currency,
      final Calendar calendar, final double annualizationFactor, final double varStrike, final double varNotional, final boolean correctForDividends) {
    ArgChecker.notNegative(varStrike, "variance strike");
    final double volStrike = Math.sqrt(varStrike);
    final double volNotional = 2 * varNotional * volStrike;
    return new EquityVarianceSwapDefinition(obsStartDate, obsEndDate, settlementDate, currency, calendar, annualizationFactor, volStrike, volNotional, correctForDividends);
  }

  /**
   * Whether to correct for dividends when pricing.
   * @return Whether to correct for dividends
   */
  public boolean correctForDividends() {
    return _correctForDividends;
  }

  @Override
  public EquityVarianceSwap toDerivative(final ZonedDateTime valueDate) {
    return toDerivative(valueDate, LocalDateDoubleTimeSeries.empty());
  }

  /**
   * {@inheritDoc} The definition is responsible for constructing a view of the variance swap as of a particular date.
   * In particular, it resolves calendars. The VarianceSwap needs an array of observations, as well as its *expected* length.
   * The actual number of observations may be less than that expected at trade inception because of a market disruption event.
   * ( For an example of a market disruption event, see http://cfe.cboe.com/Products/Spec_VT.aspx )
   * @param valueDate Date at which valuation will occur, not null
   * @param underlyingTimeSeries Time series of underlying observations, not null
   * @return VarianceSwap derivative as of date
   */
  @Override
  public EquityVarianceSwap toDerivative(final ZonedDateTime valueDate, final LocalDateDoubleTimeSeries underlyingTimeSeries) {
    ArgChecker.notNull(valueDate, "date");
    ArgChecker.notNull(underlyingTimeSeries, "A TimeSeries of observations must be provided. If observations have not begun, please pass an empty series.");
    final double timeToObsStart = TimeCalculator.getTimeBetween(valueDate, getObsStartDate());
    final double timeToObsEnd = TimeCalculator.getTimeBetween(valueDate, getObsEndDate());
    final double timeToSettlement = TimeCalculator.getTimeBetween(valueDate, getSettlementDate());
    LocalDateDoubleTimeSeries realizedTS;
    realizedTS = timeToObsStart > 0 ?
        LocalDateDoubleTimeSeries.empty() :
        underlyingTimeSeries.subSeries(getObsStartDate().toLocalDate(), valueDate.toLocalDate());

    final double[] observations = realizedTS.values().toArray();
    final double[] observationWeights = {}; // TODO Case 2011-06-29 Calendar Add functionality for non-trivial weighting of observations
    ZonedDateTime finalObsDate = getObsEndDate().isAfter(valueDate) ? valueDate : getObsEndDate();
    int nGoodBusinessDays = finalObsDate.isAfter(getObsStartDate()) ? BusinessDayDateUtils.getWorkingDaysInclusive(getObsStartDate(), finalObsDate, getCalendar()) : 0;
    final int nObsDisrupted = nGoodBusinessDays - observations.length;
    ArgChecker.isTrue(nObsDisrupted >= 0, "Have more observations {} than good business days {}", observations.length, nGoodBusinessDays);
    return new EquityVarianceSwap(timeToObsStart, timeToObsEnd, timeToSettlement, getVarStrike(), getVarNotional(), getCurrency(), getAnnualizationFactor(), getObsExpected(), nObsDisrupted,
        observations, observationWeights, correctForDividends());
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + (_correctForDividends ? 1231 : 1237);
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof EquityVarianceSwapDefinition)) {
      return false;
    }
    if (!super.equals(obj)) {
      return false;
    }
    final EquityVarianceSwapDefinition other = (EquityVarianceSwapDefinition) obj;
    if (_correctForDividends != other._correctForDividends) {
      return false;
    }
    return true;
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    ArgChecker.notNull(visitor, "visitor");
    return visitor.visitEquityVarianceSwapDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    ArgChecker.notNull(visitor, "visitor");
    return visitor.visitEquityVarianceSwapDefinition(this);
  }
}
