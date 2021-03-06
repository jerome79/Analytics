/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.convention;

import static java.time.temporal.TemporalAdjusters.lastDayOfMonth;

import java.time.LocalDate;

import com.opengamma.strata.basics.schedule.StubConvention;
import com.opengamma.strata.collect.ArgChecker;

/**
 * Utility to calculate the stub type.
 */
public final class StubCalculator {

  /**
   * Restricted constructor.
   */
  private StubCalculator() {
  }

  //-------------------------------------------------------------------------
  /**
   * Calculates the start stub type from a schedule and number of payments per year.
   * <p>
   * The {@code DateProvider[]} argument allows callers to pass in arrays of any class
   * that implements {@code DateProvider}, such as {@code LocalDate[]}.
   * 
   * @param schedule  the schedule, at least size 2, not null
   * @param paymentsPerYear  the number of payments per year, one, two, three, four, six or twelve
   * @return the stub type, not null
   */
  public static StubConvention getStartStubType(LocalDate[] schedule, int paymentsPerYear) {
    return getStartStubType(schedule, paymentsPerYear, false);
  }

  /**
   * Calculates the start stub type from a schedule, number of payments per year and the end of month flag.
   * <p>
   * The {@code DateProvider[]} argument allows callers to pass in arrays of any class
   * that implements {@code DateProvider}, such as {@code LocalDate[]}.
   * 
   * @param schedule  the schedule, at least size 2, not null
   * @param paymentsPerYear  the number of payments per year, one, two, three, four, six or twelve
   * @param isEndOfMonthConvention  whether to use end of month rules
   * @return the stub type, not null
   */
  public static StubConvention getStartStubType(LocalDate[] schedule, double paymentsPerYear, boolean isEndOfMonthConvention) {
    ArgChecker.noNulls(schedule, "schedule");
    ArgChecker.isTrue(paymentsPerYear > 0, "Must be at least one payment per year");
    ArgChecker.isTrue(12 % paymentsPerYear == 0, "Payment per year must be 1, 2, 3, 4, 6 or 12");

    int months = (int) (12 / paymentsPerYear);
    LocalDate first = schedule[0];
    LocalDate second = schedule[1];
    LocalDate date;
    if (isEndOfMonthConvention && second.equals(second.with(lastDayOfMonth()))) {
      date = second.minusMonths(months);
      date = date.with(lastDayOfMonth());
    } else {
      date = second.minusMonths(months);
    }
    if (date.equals(first)) {
      return StubConvention.NONE;
    }
    if (date.isBefore(first)) {
      return StubConvention.SHORT_INITIAL;
    }
    return StubConvention.LONG_INITIAL;
  }

  //-------------------------------------------------------------------------
  /**
   * Calculates the end stub type from a schedule and number of payments per year.
   * <p>
   * The {@code DateProvider[]} argument allows callers to pass in arrays of any class
   * that implements {@code DateProvider}, such as {@code LocalDate[]}.
   * 
   * @param schedule  the schedule, at least size 2, not null
   * @param paymentsPerYear  the number of payments per year, one, two, three, four, six or twelve
   * @return the stub type, not null
   */
  public static StubConvention getEndStubType(LocalDate[] schedule, int paymentsPerYear) {
    return getEndStubType(schedule, paymentsPerYear, false);
  }

  /**
   * Calculates the end stub type from a schedule, number of payments per year and the end of month flag.
   * <p>
   * The {@code DateProvider[]} argument allows callers to pass in arrays of any class
   * that implements {@code DateProvider}, such as {@code LocalDate[]}.
   * 
   * @param schedule  the schedule, at least size 2, not null
   * @param paymentsPerYear  the number of payments per year, one, two, three, four, six or twelve
   * @param isEndOfMonthConvention  whether to use end of month rules
   * @return the stub type, not null
   */
  public static StubConvention getEndStubType(LocalDate[] schedule, double paymentsPerYear, boolean isEndOfMonthConvention) {
    ArgChecker.noNulls(schedule, "schedule");
    ArgChecker.isTrue(paymentsPerYear > 0, "Must be at least one payment per year");
    ArgChecker.isTrue(12 % paymentsPerYear == 0, "Payment per year must be 1, 2, 3, 4, 6 or 12");

    int months = (int) (12 / paymentsPerYear);
    int n = schedule.length;
    LocalDate first = schedule[n - 2];
    LocalDate second = schedule[n - 1];
    LocalDate date;
    if (isEndOfMonthConvention && first.equals(first.with(lastDayOfMonth()))) {
      date = first.plusMonths(months);
      date = date.with(lastDayOfMonth());
    } else {
      date = first.plusMonths(months);
    }
    if (date.equals(second)) {
      return StubConvention.NONE;
    }
    if (date.isAfter(second)) {
      return StubConvention.SHORT_FINAL;
    }
    return StubConvention.LONG_FINAL;
  }

}
