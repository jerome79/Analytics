/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;

import com.opengamma.strata.collect.ArgChecker;


/**
 * The class describes variable notionals. 
 */
public class VariableNotionalProvider implements NotionalProvider {

  private LocalDate[] _dates;
  private final double[] _notionals;

  /**
   * Construct notional provider for variable notional
   * @param dates Array of dates specifying variable notionals
   * @param notionals The notionals
   */
  public VariableNotionalProvider(LocalDate[] dates, double[] notionals) {
    ArgChecker.notEmpty(dates, "dates");
    ArgChecker.notEmpty(notionals, "notionals");
    int nDates = dates.length;
    ArgChecker.isTrue(notionals.length == nDates, "dates and notionals should have the same length");
    _dates = Arrays.copyOf(dates, nDates);
    _notionals = Arrays.copyOf(notionals, nDates);
  }

  /**
   * Constructor used when schedule is not yet known
   * @param notionals The notionals
   */
  public VariableNotionalProvider(double[] notionals) {
    ArgChecker.notEmpty(notionals, "notionals");
    _dates = null;
    _notionals = Arrays.copyOf(notionals, notionals.length);
  }

  /**
   * Construct the provide with dates
   * @param dateList The dates
   * @return VariableNotionalProvider
   */
  public VariableNotionalProvider withZonedDateTime(List<ZonedDateTime> dateList) {
    ArgChecker.notEmpty(dateList, "dates");
    int nDates = dateList.size();
    ArgChecker.isTrue(_notionals.length == nDates, "zonedDates and notionals should have the same length");
    LocalDate[] dates = new LocalDate[nDates];
    for (int i = 0; i < nDates; ++i) {
      dates[i] = dateList.get(i).toLocalDate();
    }
    return new VariableNotionalProvider(dates, _notionals);
  }

  @Override
  public double getAmount(final LocalDate date) {
    if (_dates == null) {
      throw new IllegalArgumentException("date set is null");
    }

    for (int i = 0; i < _dates.length; ++i) {
      if (_dates[i].equals(date)) {
        return _notionals[i];
      }
    }
    throw new IllegalArgumentException("The date " + date + " is not found in the date set");
  }

  /**
   * Access notionals 
   * @return _notionals
   */
  public double[] getNotionals() {
    return _notionals;
  }

  /**
   * Access dates
   * @return _dates
   */
  public LocalDate[] getDates() {
    return _dates;
  }
}
