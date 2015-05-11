/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.schedule;

import java.time.LocalDate;
import java.time.ZonedDateTime;

import com.opengamma.strata.basics.schedule.Frequency;
import com.opengamma.strata.collect.ArgChecker;

/**
 * Factory to create schedules.
 */
public class ScheduleFactory {

  public static LocalDate[] getSchedule(final LocalDate startDate, final LocalDate endDate, final com.opengamma.strata.basics.schedule.Frequency frequency, final boolean endOfMonth,
      final boolean fromEnd,
      final boolean generateRecursive) {
    ArgChecker.notNull(startDate, "start date");
    ArgChecker.notNull(endDate, "end date");
    ArgChecker.notNull(frequency, "frequency");
    int periodsPerYear = frequency.eventsPerYear();
    return getSchedule(startDate, endDate, periodsPerYear, endOfMonth, fromEnd, generateRecursive);
  }

  public static LocalDate[] getSchedule(final LocalDate startDate, final LocalDate endDate, final int periodsPerYear, final boolean endOfMonth, final boolean fromEnd,
      final boolean generateRecursive) {
    ArgChecker.notNull(startDate, "start date");
    ArgChecker.notNull(endDate, "end date");
    ArgChecker.isTrue(periodsPerYear > 0);
    LocalDate[] result = null;
    if (periodsPerYear == 1) {
      if (endOfMonth) {
        if (fromEnd && endDate.getDayOfMonth() == endDate.lengthOfMonth()) {
          result = ScheduleCalculatorFactory.ANNUAL_EOM_CALCULATOR.getSchedule(startDate, endDate, fromEnd, generateRecursive);
        } else if (startDate.getDayOfMonth() == startDate.lengthOfMonth()) {
          result = ScheduleCalculatorFactory.ANNUAL_EOM_CALCULATOR.getSchedule(startDate, endDate, fromEnd, generateRecursive);
        } else {
          result = ScheduleCalculatorFactory.ANNUAL_CALCULATOR.getSchedule(startDate, endDate, fromEnd, generateRecursive);
        }
      } else {
        result = ScheduleCalculatorFactory.ANNUAL_CALCULATOR.getSchedule(startDate, endDate, fromEnd, generateRecursive);
      }
    } else if (periodsPerYear == 2) {
      if (endOfMonth) {
        if (fromEnd && endDate.getDayOfMonth() == endDate.lengthOfMonth()) {
          result = ScheduleCalculatorFactory.SEMI_ANNUAL_EOM_CALCULATOR.getSchedule(startDate, endDate, fromEnd, generateRecursive);
        } else if (startDate.getDayOfMonth() == startDate.lengthOfMonth()) {
          result = ScheduleCalculatorFactory.SEMI_ANNUAL_EOM_CALCULATOR.getSchedule(startDate, endDate, fromEnd, generateRecursive);
        } else {
          result = ScheduleCalculatorFactory.SEMI_ANNUAL_CALCULATOR.getSchedule(startDate, endDate, fromEnd, generateRecursive);
        }
      } else {
        result = ScheduleCalculatorFactory.SEMI_ANNUAL_CALCULATOR.getSchedule(startDate, endDate, fromEnd, generateRecursive);
      }
    } else if (periodsPerYear == 4) {
      if (endOfMonth) {
        if (fromEnd && endDate.getDayOfMonth() == endDate.lengthOfMonth()) {
          result = ScheduleCalculatorFactory.QUARTERLY_EOM_CALCULATOR.getSchedule(startDate, endDate, fromEnd, generateRecursive);
        } else if (startDate.getDayOfMonth() == startDate.lengthOfMonth()) {
          result = ScheduleCalculatorFactory.QUARTERLY_EOM_CALCULATOR.getSchedule(startDate, endDate, fromEnd, generateRecursive);
        } else {
          result = ScheduleCalculatorFactory.QUARTERLY_CALCULATOR.getSchedule(startDate, endDate, fromEnd, generateRecursive);
        }
      } else {
        result = ScheduleCalculatorFactory.QUARTERLY_CALCULATOR.getSchedule(startDate, endDate, fromEnd, generateRecursive);
      }
    } else if (periodsPerYear == 12) {
      if (endOfMonth) {
        if (fromEnd && endDate.getDayOfMonth() == endDate.lengthOfMonth()) {
          result = ScheduleCalculatorFactory.END_OF_MONTH_CALCULATOR.getSchedule(startDate, endDate, fromEnd, generateRecursive);
        } else if (startDate.getDayOfMonth() == startDate.lengthOfMonth()) {
          result = ScheduleCalculatorFactory.END_OF_MONTH_CALCULATOR.getSchedule(startDate, endDate, fromEnd, generateRecursive);
        } else {
          result = ScheduleCalculatorFactory.MONTHLY_CALCULATOR.getSchedule(startDate, endDate, fromEnd, generateRecursive);
        }
      } else {
        result = ScheduleCalculatorFactory.MONTHLY_CALCULATOR.getSchedule(startDate, endDate, fromEnd, generateRecursive);
      }
    } else if (periodsPerYear == 52) {
      if (endOfMonth) {
        throw new IllegalArgumentException("Cannot get EOM series for weekly frequency");
      }
      result = ScheduleCalculatorFactory.WEEKLY_CALCULATOR.getSchedule(startDate, endDate, fromEnd, generateRecursive);
    } else if (periodsPerYear == 364 || periodsPerYear == 365 || periodsPerYear == 366) {
      if (endOfMonth) {
        throw new IllegalArgumentException("Cannot get EOM series for daily frequency");
      }
      result = ScheduleCalculatorFactory.DAILY_CALCULATOR.getSchedule(startDate, endDate);
    }
    ArgChecker.notNull(result, "result");
    return result;
  }

  public static ZonedDateTime[] getSchedule(final ZonedDateTime startDate, final ZonedDateTime endDate, final Frequency frequency, final boolean endOfMonth, final boolean fromEnd,
      final boolean generateRecursive) {
    ArgChecker.notNull(startDate, "start date");
    ArgChecker.notNull(endDate, "end date");
    ArgChecker.notNull(frequency, "frequency");
    int periodsPerYear = frequency.eventsPerYear();
    return getSchedule(startDate, endDate, periodsPerYear, endOfMonth, fromEnd, generateRecursive);
  }

  public static ZonedDateTime[] getSchedule(final ZonedDateTime startDate, final ZonedDateTime endDate, final int periodsPerYear, final boolean endOfMonth, final boolean fromEnd,
      final boolean generateRecursive) {
    ArgChecker.notNull(startDate, "start date");
    ArgChecker.notNull(endDate, "end date");
    ArgChecker.isTrue(periodsPerYear > 0);
    ZonedDateTime[] result = null;
    if (periodsPerYear == 1) {
      if (endOfMonth) {
        if (fromEnd && endDate.getDayOfMonth() == endDate.toLocalDate().lengthOfMonth()) {
          result = ScheduleCalculatorFactory.ANNUAL_EOM_CALCULATOR.getSchedule(startDate, endDate, fromEnd, generateRecursive);
        } else if (startDate.getDayOfMonth() == startDate.toLocalDate().lengthOfMonth()) {
          result = ScheduleCalculatorFactory.ANNUAL_EOM_CALCULATOR.getSchedule(startDate, endDate, fromEnd, generateRecursive);
        } else {
          result = ScheduleCalculatorFactory.ANNUAL_CALCULATOR.getSchedule(startDate, endDate, fromEnd, generateRecursive);
        }
      } else {
        result = ScheduleCalculatorFactory.ANNUAL_CALCULATOR.getSchedule(startDate, endDate, fromEnd, generateRecursive);
      }
    } else if (periodsPerYear == 2) {
      if (endOfMonth) {
        if (fromEnd && endDate.getDayOfMonth() == endDate.toLocalDate().lengthOfMonth()) {
          result = ScheduleCalculatorFactory.SEMI_ANNUAL_EOM_CALCULATOR.getSchedule(startDate, endDate, fromEnd, generateRecursive);
        } else if (startDate.getDayOfMonth() == startDate.toLocalDate().lengthOfMonth()) {
          result = ScheduleCalculatorFactory.SEMI_ANNUAL_EOM_CALCULATOR.getSchedule(startDate, endDate, fromEnd, generateRecursive);
        } else {
          result = ScheduleCalculatorFactory.SEMI_ANNUAL_CALCULATOR.getSchedule(startDate, endDate, fromEnd, generateRecursive);
        }
      } else {
        result = ScheduleCalculatorFactory.SEMI_ANNUAL_CALCULATOR.getSchedule(startDate, endDate, fromEnd, generateRecursive);
      }
    } else if (periodsPerYear == 4) {
      if (endOfMonth) {
        if (fromEnd && endDate.getDayOfMonth() == endDate.toLocalDate().lengthOfMonth()) {
          result = ScheduleCalculatorFactory.QUARTERLY_EOM_CALCULATOR.getSchedule(startDate, endDate, fromEnd, generateRecursive);
        } else if (startDate.getDayOfMonth() == startDate.toLocalDate().lengthOfMonth()) {
          result = ScheduleCalculatorFactory.QUARTERLY_EOM_CALCULATOR.getSchedule(startDate, endDate, fromEnd, generateRecursive);
        } else {
          result = ScheduleCalculatorFactory.QUARTERLY_CALCULATOR.getSchedule(startDate, endDate, fromEnd, generateRecursive);
        }
      } else {
        result = ScheduleCalculatorFactory.QUARTERLY_CALCULATOR.getSchedule(startDate, endDate, fromEnd, generateRecursive);
      }
    } else if (periodsPerYear == 12) {
      if (endOfMonth) {
        if (fromEnd && endDate.getDayOfMonth() == endDate.toLocalDate().lengthOfMonth()) {
          result = ScheduleCalculatorFactory.END_OF_MONTH_CALCULATOR.getSchedule(startDate, endDate, fromEnd, generateRecursive);
        } else if (startDate.getDayOfMonth() == startDate.toLocalDate().lengthOfMonth()) {
          result = ScheduleCalculatorFactory.END_OF_MONTH_CALCULATOR.getSchedule(startDate, endDate, fromEnd, generateRecursive);
        } else {
          result = ScheduleCalculatorFactory.MONTHLY_CALCULATOR.getSchedule(startDate, endDate, fromEnd, generateRecursive);
        }
      } else {
        result = ScheduleCalculatorFactory.MONTHLY_CALCULATOR.getSchedule(startDate, endDate, fromEnd, generateRecursive);
      }
    } else if (periodsPerYear == 52) {
      if (endOfMonth) {
        throw new IllegalArgumentException("Cannot get EOM series for weekly frequency");
      }
      result = ScheduleCalculatorFactory.WEEKLY_CALCULATOR.getSchedule(startDate, endDate, fromEnd, generateRecursive);
    } else if (periodsPerYear == 364 || periodsPerYear == 365 || periodsPerYear == 366) {
      if (endOfMonth) {
        throw new IllegalArgumentException("Cannot get EOM series for daily frequency");
      }
      result = ScheduleCalculatorFactory.DAILY_CALCULATOR.getSchedule(startDate, endDate);
    }
    ArgChecker.notNull(result, "result");
    return result;
  }
}
