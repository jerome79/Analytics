/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.index;

import java.time.ZonedDateTime;
import java.util.Objects;

import com.opengamma.analytics.convention.daycount.DayCount;
import com.opengamma.analytics.convention.daycount.DayCountUtils;
import com.opengamma.analytics.financial.instrument.cash.DepositCounterpartDefinition;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.basics.date.HolidayCalendar;
import com.opengamma.strata.collect.ArgChecker;

/**
 * Class with the description of overnight deposit characteristics (conventions, calendar, ...).
 */
public class GeneratorDepositONCounterpart extends GeneratorInstrument<GeneratorAttributeIR> {

  /**
   * The index currency. Not null.
   */
  private final Currency _currency;
  /**
   * The calendar associated to the index. Not null.
   */
  private final HolidayCalendar _calendar;
  /**
   * The day count convention associated to the generator. Not null.
   */
  private final DayCount _dayCount;
  /**
   * The counterpart name. Not null.
   */
  private final String _nameCounterpart;

  /**
   * Deposit generator from all the financial details.
   * @param nameGenerator The generator name. Not null.
   * @param currency The index currency. Not null.
   * @param calendar The calendar associated to the index. Not null.
   * @param dayCount The day count convention associated to the index.
   * @param nameCounterpart The counterpart name. Not null.
   */
  public GeneratorDepositONCounterpart(final String nameGenerator, final Currency currency, final HolidayCalendar calendar, final DayCount dayCount, final String nameCounterpart) {
    super(nameGenerator);
    ArgChecker.notNull(currency, "Currency");
    ArgChecker.notNull(calendar, "HolidayCalendar");
    ArgChecker.notNull(dayCount, "Day count");
    ArgChecker.notNull(nameCounterpart, "Counterpart name");
    _currency = currency;
    _calendar = calendar;
    _dayCount = dayCount;
    _nameCounterpart = nameCounterpart;
  }

  /**
   * Gets the index currency.
   * @return The currency.
   */
  public Currency getCurrency() {
    return _currency;
  }

  /**
   * Gets the calendar associated to the index.
   * @return The calendar.
   */
  public HolidayCalendar getCalendar() {
    return _calendar;
  }

  /**
   * Gets the day count convention associated to the index.
   * @return The day count convention.
   */
  public DayCount getDayCount() {
    return _dayCount;
  }

  /**
   * Gets the counterpart name.
   * @return The name.
   */
  public String getNameCounterpart() {
    return _nameCounterpart;
  }

  /**
   * Generate an overnight deposit for the given counterpart.
   * @param date The reference date.
   * @param rate The deposit rate.
   * @param notional The deposit notional.
   * @param attribute The ON deposit attributes. The deposit starts at today+start period. Only the start period is used.
   * @return The overnight deposit.
   */
  @Override
  public DepositCounterpartDefinition generateInstrument(final ZonedDateTime date, final double rate, final double notional, final GeneratorAttributeIR attribute) {
    ArgChecker.notNull(date, "Reference date");
    final ZonedDateTime startDate = ScheduleCalculator.getAdjustedDate(date, attribute.getStartPeriod(), _calendar);
    final ZonedDateTime endDate = ScheduleCalculator.getAdjustedDate(startDate, 1, _calendar);
    final double accrualFactor = DayCountUtils.yearFraction(_dayCount, startDate, endDate, _calendar);
    return new DepositCounterpartDefinition(_currency, startDate, endDate, notional, rate, accrualFactor, _nameCounterpart);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + _calendar.hashCode();
    result = prime * result + _currency.hashCode();
    result = prime * result + _dayCount.hashCode();
    result = prime * result + _nameCounterpart.hashCode();
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
    final GeneratorDepositONCounterpart other = (GeneratorDepositONCounterpart) obj;
    if (!Objects.equals(_calendar, other._calendar)) {
      return false;
    }
    if (!Objects.equals(_currency, other._currency)) {
      return false;
    }
    if (!Objects.equals(_dayCount, other._dayCount)) {
      return false;
    }
    if (!Objects.equals(_nameCounterpart, other._nameCounterpart)) {
      return false;
    }
    return true;
  }

}
