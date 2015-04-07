/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.index;

import java.time.Period;
import java.time.ZonedDateTime;
import java.util.Objects;

import com.opengamma.analytics.financial.instrument.fra.ForwardRateAgreementDefinition;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.basics.date.HolidayCalendar;
import com.opengamma.strata.collect.ArgChecker;

/**
 * Class with the description of swap characteristics.
 */
public class GeneratorFRA extends GeneratorInstrument<GeneratorAttributeIR> {

  /**
   * The Ibor index underlying the FRA.
   */
  private final IborIndex _iborIndex;
  /**
   * The holiday calendar associated with the ibor index.
   */
  private final HolidayCalendar _calendar;

  /**
   * Constructor from the details. The business day conventions, end-of-month and spot lag are from the Ibor index.
   * @param name The generator name. Not null.
   * @param iborIndex The Ibor index of the floating leg.
   * @param calendar The holiday calendar for the ibor leg.
   */
  public GeneratorFRA(final String name, final IborIndex iborIndex, final HolidayCalendar calendar) {
    super(name);
    ArgChecker.notNull(iborIndex, "ibor index");
    ArgChecker.notNull(calendar, "calendar");
    _iborIndex = iborIndex;
    _calendar = calendar;
  }

  /**
   * Gets the _iborIndex field.
   * @return the _iborIndex
   */
  public IborIndex getIborIndex() {
    return _iborIndex;
  }

  /**
   * Gets the generator currency.
   * @return The currency.
   */
  public Currency getCurrency() {
    return _iborIndex.getCurrency();
  }

  /**
   * Gets the generator calendar.
   * @return The calendar.
   */
  public HolidayCalendar getCalendar() {
    return _calendar;
  }

  /**
   * {@inheritDoc}
   * The FRA is from spot+(endtenor-_iborIndex.getTenor()) to spot + endtenor. The start period is not used.
   */
  @Override
  public ForwardRateAgreementDefinition generateInstrument(final ZonedDateTime date, final double rate, final double notional, final GeneratorAttributeIR attribute) {
    ArgChecker.notNull(date, "Reference date");
    ArgChecker.notNull(attribute, "Attributes");
    final Period startPeriod = attribute.getEndPeriod().minus(_iborIndex.getTenor());
    return ForwardRateAgreementDefinition.fromTrade(date, startPeriod, notional, _iborIndex, rate, _calendar);
  }

  @Override
  public String toString() {
    return getName();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + _iborIndex.hashCode();
    result = prime * result + _calendar.hashCode();
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
    final GeneratorFRA other = (GeneratorFRA) obj;
    if (!Objects.equals(_iborIndex, other._iborIndex)) {
      return false;
    }
    if (!Objects.equals(_calendar, other._calendar)) {
      return false;
    }
    return true;
  }

}
