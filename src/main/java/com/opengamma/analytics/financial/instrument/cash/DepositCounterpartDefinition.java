/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.cash;

import java.time.Period;
import java.time.ZonedDateTime;
import java.util.Objects;

import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.index.GeneratorDeposit;
import com.opengamma.analytics.financial.interestrate.cash.derivative.DepositCounterpart;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.collect.ArgChecker;

/**
 * Class describing a deposit to a specific counterpart. Used in particular for counterpart dependent valuation.
 */
public class DepositCounterpartDefinition extends CashDefinition {

  /**
   * The counterpart name.
   */
  private final String _name;

  /**
   * Constructor from all details.
   * @param currency The deposit currency.
   * @param startDate The deposit start date.
   * @param endDate The deposit end date.
   * @param notional The deposit notional.
   * @param rate The deposit rate.
   * @param accrualFactor The deposit accrual factor.
   * @param name The counterpart name.
   */
  public DepositCounterpartDefinition(final Currency currency, final ZonedDateTime startDate, final ZonedDateTime endDate, final double notional, final double rate,
      final double accrualFactor, final String name) {
    super(currency, startDate, endDate, notional, rate, accrualFactor);
    ArgChecker.notNull(name, "Name");
    _name = name;
  }

  /**
   * Build a counterpart deposit from the start date and the tenor.
   * @param startDate The deposit start date.
   * @param tenor The deposit tenor.
   * @param notional The deposit notional.
   * @param rate The deposit rate.
   * @param generator The deposit generator.
   * @param name The counterpart name.
   * @return The deposit.
   */
  public static DepositCounterpartDefinition fromStart(final ZonedDateTime startDate, final Period tenor, final double notional, final double rate, final GeneratorDeposit generator,
      final String name) {
    ArgChecker.notNull(startDate, "Start date");
    ArgChecker.notNull(tenor, "Tenor");
    ArgChecker.notNull(generator, "Generator");
    ArgChecker.notNull(name, "Name");
    final ZonedDateTime endDate = ScheduleCalculator.getAdjustedDate(startDate, tenor, generator);
    final double accrualFactor = generator.getDayCount().yearFraction(startDate, endDate, generator.getCalendar());
    return new DepositCounterpartDefinition(generator.getCurrency(), startDate, endDate, notional, rate, accrualFactor, name);
  }

  /**
   * Build a overnight counterpart deposit from the financial description and the start (or settlement) date.
   * @param startDate The deposit start date.
   * @param notional The deposit notional.
   * @param rate The deposit rate.
   * @param generator The deposit generator.
   * @param name The counterpart name.
   * @return The deposit.
   */
  public static DepositCounterpartDefinition fromStart(final ZonedDateTime startDate, final double notional, final double rate, final GeneratorDeposit generator, final String name) {
    ArgChecker.notNull(startDate, "Start date");
    ArgChecker.notNull(generator, "Generator");
    ArgChecker.notNull(name, "Name");
    final ZonedDateTime endDate = ScheduleCalculator.getAdjustedDate(startDate, 1, generator.getCalendar());
    final double accrualFactor = generator.getDayCount().yearFraction(startDate, endDate, generator.getCalendar());
    return new DepositCounterpartDefinition(generator.getCurrency(), startDate, endDate, notional, rate, accrualFactor, name);
  }

  /**
   * Build a counterpart deposit from the trade date and the tenor.
   * @param tradeDate The deposit trade date. The start date is the trade date plus the generator spot lag.
   * @param tenor The deposit tenor.
   * @param notional The deposit notional.
   * @param rate The deposit rate.
   * @param generator The deposit generator.
   * @param name The counterpart name.
   * @return The deposit.
   */
  public static DepositCounterpartDefinition fromTrade(final ZonedDateTime tradeDate, final Period tenor, final double notional, final double rate, final GeneratorDeposit generator,
      final String name) {
    ArgChecker.notNull(tradeDate, "Start date");
    ArgChecker.notNull(tenor, "Tenor");
    ArgChecker.notNull(generator, "Generator");
    ArgChecker.notNull(name, "Name");
    final ZonedDateTime startDate = ScheduleCalculator.getAdjustedDate(tradeDate, generator.getSpotLag(), generator.getCalendar());
    final ZonedDateTime endDate = ScheduleCalculator.getAdjustedDate(startDate, tenor, generator);
    final double accrualFactor = generator.getDayCount().yearFraction(startDate, endDate, generator.getCalendar());
    return new DepositCounterpartDefinition(generator.getCurrency(), startDate, endDate, notional, rate, accrualFactor, name);
  }

  /**
   * Build an over-night counterpart deposit from the financial description and the trade date.
   * @param tradeDate The deposit trade date. The start date is the trade date plus the generator spot lag.
   * @param start The number of business days to start date.
   * @param notional The deposit notional.
   * @param rate The deposit rate.
   * @param generator The deposit generator.
   * @param name The counterpart name.
   * @return The deposit.
   */
  public static DepositCounterpartDefinition fromTrade(final ZonedDateTime tradeDate, final int start, final double notional, final double rate, final GeneratorDeposit generator, final String name) {
    ArgChecker.notNull(tradeDate, "Trade date");
    ArgChecker.notNull(generator, "Generator");
    ArgChecker.notNull(name, "Name");
    final ZonedDateTime startDate = ScheduleCalculator.getAdjustedDate(tradeDate, start, generator.getCalendar());
    final ZonedDateTime endDate = ScheduleCalculator.getAdjustedDate(startDate, 1, generator.getCalendar());
    final double accrualFactor = generator.getDayCount().yearFraction(startDate, endDate, generator.getCalendar());
    return new DepositCounterpartDefinition(generator.getCurrency(), startDate, endDate, notional, rate, accrualFactor, name);
  }

  /**
   * Gets the counterpart name.
   * @return The name.
   */
  public String getCounterpartName() {
    return _name;
  }


  @Override
  public DepositCounterpart toDerivative(final ZonedDateTime date) {
    ArgChecker.isTrue(!date.isAfter(getEndDate()), "date is after end date");
    final double startTime = TimeCalculator.getTimeBetween(date, getStartDate());
    if (startTime < 0) {
      return new DepositCounterpart(getCurrency(), 0, TimeCalculator.getTimeBetween(date, getEndDate()), getNotional(), 0, getRate(), getAccrualFactor(), _name);
    }
    return new DepositCounterpart(getCurrency(), startTime, TimeCalculator.getTimeBetween(date, getEndDate()), getNotional(), getNotional(), getRate(), getAccrualFactor(), _name);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + _name.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final DepositCounterpartDefinition other = (DepositCounterpartDefinition) obj;
    if (!Objects.equals(_name, other._name)) {
      return false;
    }
    return true;
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    ArgChecker.notNull(visitor, "visitor");
    return visitor.visitDepositCounterpartDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    ArgChecker.notNull(visitor, "visitor");
    return visitor.visitDepositCounterpartDefinition(this);
  }

}
