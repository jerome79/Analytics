/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.convention.daycount;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;

import com.opengamma.analytics.convention.StubCalculator;
import com.opengamma.strata.basics.date.HolidayCalendar;
import com.opengamma.strata.basics.schedule.StubConvention;
import com.opengamma.strata.collect.ArgChecker;

/**
 * Utility to calculate the accrued interest.
 */
public final class AccruedInterestCalculator {

  /**
   * Restricted constructor.
   */
  private AccruedInterestCalculator() {
  }

  /**
   * Calculates the accrued interest for a {@code ZonedDateTime}.
   * 
   * @param dayCount  the day count convention, not null
   * @param settlementDate  the settlement date, not null
   * @param nominalDates  the nominalDates, not null, no null elements
   * @param coupon  the coupon value
   * @param paymentsPerYear  the number of payments per year, one, two, three, four, six or twelve
   * @param isEndOfMonthConvention  whether to use end of month rules
   * @param exDividendDays the number of ex-dividend days
   * @param calendar The working day calendar to be used in calculating ex-dividend dates, not null
   * @return the accrued interest
   */
  public static double getAccruedInterest(final DayCount dayCount, final ZonedDateTime settlementDate, final ZonedDateTime[] nominalDates, final double coupon, final int paymentsPerYear,
      final boolean isEndOfMonthConvention, final int exDividendDays, final HolidayCalendar calendar) {
    ArgChecker.notNull(dayCount, "day-count");
    ArgChecker.notNull(settlementDate, "date");
    ArgChecker.noNulls(nominalDates, "nominalDates");
    ArgChecker.notNull(calendar, "calendar");
    ArgChecker.isTrue(paymentsPerYear > 0, "payments per year must be greater than zero");
    ArgChecker.isTrue(exDividendDays >= 0, "ex dividend days must be zero or greater");
    final int i = Arrays.binarySearch(nominalDates, settlementDate);
    if (i > 0) {
      return 0;
    }
    final int index = -i - 2;
    final int length = nominalDates.length;
    ArgChecker.isTrue(index >= 0, "Settlement date is before first accrual date");
    ArgChecker.isTrue(index < length, "Settlement date is after maturity date");
    final double accruedInterest = getAccruedInterest(dayCount, index, length, nominalDates[index], settlementDate, nominalDates[index + 1], coupon, paymentsPerYear, isEndOfMonthConvention);
    ZonedDateTime exDividendDate = nominalDates[index + 1];
    for (int j = 0; j < exDividendDays; j++) {
      while (!calendar.isBusinessDay(exDividendDate.toLocalDate())) {
        exDividendDate = exDividendDate.minusDays(1);
      }
      exDividendDate = exDividendDate.minusDays(1);
    }
    if (exDividendDays != 0 && exDividendDate.isBefore(settlementDate)) {
      return accruedInterest - coupon;
    }
    return accruedInterest;
  }

  /**
   * Calculates the accrued interest for a {@code ZonedDateTime}.
   * 
   * @param dayCount  the day count convention, not null
   * @param settlementDate  the settlement date, not null
   * @param nominalDates  the nominalDates, not null, no null elements
   * @param coupon  the coupon value
   * @param paymentsPerYear  the number of payments per year, one, two, three, four, six or twelve
   * @param isEndOfMonthConvention  whether to use end of month rules
   * @param exDividendDays the number of ex-dividend days
   * @param index The index of the previous coupon in the nominalDates array
   * @param calendar The working day calendar to be used in calculating ex-dividend dates, not null
   * @return the accrued interest
   */
  public static double getAccruedInterest(final DayCount dayCount, final ZonedDateTime settlementDate, final ZonedDateTime[] nominalDates, final double coupon, final double paymentsPerYear,
      final boolean isEndOfMonthConvention, final int exDividendDays, final int index, final HolidayCalendar calendar) {
    ArgChecker.notNull(dayCount, "day-count");
    ArgChecker.notNull(settlementDate, "date");
    ArgChecker.noNulls(nominalDates, "nominalDates");
    ArgChecker.notNull(calendar, "calendar");
    ArgChecker.isTrue(paymentsPerYear > 0, "payments per year must be greater than zero");
    ArgChecker.isTrue(exDividendDays >= 0, "ex dividend days must be zero or greater");
    final int length = nominalDates.length;
    ArgChecker.isTrue(index >= 0 && index < length, "index must be valid");
    final double accruedInterest = getAccruedInterest(dayCount, index, length, nominalDates[index], settlementDate, nominalDates[index + 1], coupon, paymentsPerYear, isEndOfMonthConvention);
    ZonedDateTime exDividendDate = nominalDates[index + 1];
    for (int i = 0; i < exDividendDays; i++) {
      while (!calendar.isBusinessDay(exDividendDate.toLocalDate())) {
        exDividendDate = exDividendDate.minusDays(1);
      }
      exDividendDate = exDividendDate.minusDays(1);
    }
    if (exDividendDays != 0 && exDividendDate.isBefore(settlementDate)) {
      return accruedInterest - coupon;
    }
    return accruedInterest;
  }

  /**
   * Calculates the accrued interest for a {@code LocalDate}.
   * 
   * @param dayCount  the day count convention, not null
   * @param settlementDate  the settlement date, not null
   * @param nominalDates  the nominalDates, not null, no null elements
   * @param coupon  the coupon value
   * @param paymentsPerYear  the number of payments per year, one, two, three, four, six or twelve
   * @param isEndOfMonthConvention  whether to use end of month rules
   * @param exDividendDays the number of ex-dividend days
   * @param calendar The working day calendar to be used in calculating ex-dividend dates, not null
   * @return the accrued interest
   */
  //TODO one where you can pass in array of coupons
  public static double getAccruedInterest(final DayCount dayCount, final LocalDate settlementDate, final LocalDate[] nominalDates, final double coupon, final double paymentsPerYear,
      final boolean isEndOfMonthConvention, final int exDividendDays, final HolidayCalendar calendar) {
    ArgChecker.notNull(dayCount, "day-count");
    ArgChecker.notNull(settlementDate, "date");
    ArgChecker.noNulls(nominalDates, "nominalDates");
    ArgChecker.notNull(calendar, "calendar");
    ArgChecker.isTrue(paymentsPerYear > 0, "payments per year must be greater than zero");
    ArgChecker.isTrue(exDividendDays >= 0, "ex dividend days must be zero or greater");
    final int i = Arrays.binarySearch(nominalDates, settlementDate);
    if (i > 0) {
      return 0;
    }
    final int index = -i - 2;
    final int length = nominalDates.length;
    if (index < 0) {
      throw new IllegalArgumentException("Settlement date is before first accrual date");
    }
    if (index == length) {
      throw new IllegalArgumentException("Settlement date is after maturity date");
    }
    final ZonedDateTime previousCouponDate = nominalDates[index].atStartOfDay(ZoneOffset.UTC);
    final ZonedDateTime date = settlementDate.atStartOfDay(ZoneOffset.UTC);
    final ZonedDateTime nextCouponDate = nominalDates[index + 1].atStartOfDay(ZoneOffset.UTC);
    final double accruedInterest = getAccruedInterest(dayCount, index, length, previousCouponDate, date, nextCouponDate, coupon, paymentsPerYear, isEndOfMonthConvention);
    LocalDate exDividendDate = nominalDates[index + 1];
    for (int j = 0; j < exDividendDays; j++) {
      while (!calendar.isBusinessDay(exDividendDate)) {
        exDividendDate = exDividendDate.minusDays(1);
      }
      exDividendDate = exDividendDate.minusDays(1);
    }
    if (exDividendDays != 0 && exDividendDate.isBefore(settlementDate)) {
      return accruedInterest - coupon;
    }
    return accruedInterest;
  }

  /**
   * Calculates the accrued interest for a {@code LocalDate}.
   * 
   * @param dayCount  the day count convention, not null
   * @param settlementDate  the settlement date, not null
   * @param nominalDates  the nominalDates, not null, no null elements
   * @param coupon  the coupon value
   * @param paymentsPerYear  the number of payments per year, one, two, three, four, six or twelve
   * @param isEndOfMonthConvention  whether to use end of month rules
   * @param exDividendDays the number of ex-dividend days
   * @param index The index of the previous coupon in the nominalDates
   * @param calendar The working day calendar to be used in calculating ex-dividend dates, not null
   * @return the accrued interest
   */
  public static double getAccruedInterest(final DayCount dayCount, final LocalDate settlementDate, final LocalDate[] nominalDates, final double coupon, final double paymentsPerYear,
      final boolean isEndOfMonthConvention, final int exDividendDays, final int index, final HolidayCalendar calendar) {
    ArgChecker.notNull(dayCount, "day-count");
    ArgChecker.notNull(settlementDate, "date");
    ArgChecker.noNulls(nominalDates, "nominalDates");
    ArgChecker.notNull(calendar, "calendar");
    ArgChecker.isTrue(paymentsPerYear > 0, "payments per year must be greater than zero");
    ArgChecker.isTrue(exDividendDays >= 0, "ex dividend days must be zero or greater");
    final int length = nominalDates.length;
    ArgChecker.isTrue(index >= 0 && index < length, "index must be valid");
    final ZonedDateTime previousCouponDate = nominalDates[index].atStartOfDay(ZoneOffset.UTC);
    final ZonedDateTime date = settlementDate.atStartOfDay(ZoneOffset.UTC);
    final ZonedDateTime nextCouponDate = nominalDates[index + 1].atStartOfDay(ZoneOffset.UTC);
    double accruedInterest;
    if (date.isAfter(nextCouponDate)) {
      accruedInterest = 0;
    } else {
      accruedInterest = getAccruedInterest(dayCount, index, length, previousCouponDate, date, nextCouponDate, coupon, paymentsPerYear, isEndOfMonthConvention);
    }
    LocalDate exDividendDate = nominalDates[index + 1];
    for (int i = 0; i < exDividendDays; i++) {
      while (!calendar.isBusinessDay(exDividendDate)) {
        exDividendDate = exDividendDate.minusDays(1);
      }
      exDividendDate = exDividendDate.minusDays(1);
    }
    if (exDividendDays != 0 && exDividendDate.isBefore(settlementDate)) {
      return accruedInterest - coupon;
    }
    return accruedInterest;
  }

  /**
   * Calculates the accrued interest for a {@code LocalDate}.
   * 
   * @param dayCount  the day count convention, not null
   * @param settlementDate  the settlement date, not null
   * @param nominalDates  the nominalDates, not null, no null elements
   * @param settlementDates  the settlement dates, not null, no null elements
   * @param coupon  the coupon value
   * @param paymentsPerYear  the number of payments per year, one, two, three, four, six or twelve
   * @param isEndOfMonthConvention  whether to use end of month rules
   * @param exDividendDays the number of ex-dividend days
   * @param index The index of the previous coupon in the nominalDates
   * @param calendar The working day calendar used to calculate the ex-dividend date, not null
   * @return the accrued interest
   */
  public static double getAccruedInterest(final DayCount dayCount, final LocalDate settlementDate, final LocalDate[] nominalDates, final LocalDate[] settlementDates, final double coupon,
      final double paymentsPerYear, final boolean isEndOfMonthConvention, final int exDividendDays, final int index, final HolidayCalendar calendar) {
    ArgChecker.notNull(dayCount, "day-count");
    ArgChecker.notNull(settlementDate, "date");
    ArgChecker.notNull(calendar, "calendar");
    ArgChecker.noNulls(nominalDates, "nominalDates");
    ArgChecker.noNulls(settlementDates, "settlementDates");
    ArgChecker.isTrue(paymentsPerYear > 0, "payments per year must be greater than zero");
    ArgChecker.isTrue(exDividendDays >= 0, "ex dividend days must be zero or greater");
    final int length = nominalDates.length;
    ArgChecker.isTrue(index >= 0 && index < length, "index must be valid");
    final LocalDate previousCouponDate = nominalDates[index];
    final LocalDate nextCouponDate = nominalDates[index + 1];
    double accruedInterest;
    if (settlementDate.isAfter(nextCouponDate)) {
      if (settlementDate.isBefore(settlementDates[index + 1])) {
        accruedInterest = coupon;
      } else {
        accruedInterest = 0;
      }
    } else {
      accruedInterest = getAccruedInterest(dayCount, index, length, previousCouponDate, settlementDate, nextCouponDate, coupon, paymentsPerYear, isEndOfMonthConvention);
    }
    LocalDate exDividendDate = nominalDates[index + 1];
    for (int i = 0; i < exDividendDays; i++) {
      while (!calendar.isBusinessDay(exDividendDate)) {
        exDividendDate = exDividendDate.minusDays(1);
      }
      exDividendDate = exDividendDate.minusDays(1);
    }
    if (exDividendDays != 0 && exDividendDate.isBefore(settlementDate)) {
      return accruedInterest - coupon;
    }
    return accruedInterest;
  }

  public static double getAccruedInterest(final DayCount dayCount, final int index, final int length, final ZonedDateTime previousCouponDate, final ZonedDateTime date,
      final ZonedDateTime nextCouponDate, final double coupon, final double paymentsPerYear, final boolean isEndOfMonthConvention) {
    if (dayCount instanceof ActualActualICMANormal) {
      if (isEndOfMonthConvention) {
        throw new IllegalArgumentException("Inconsistent definition; asked for accrual with EOM convention but are not using Actual/Actual ICMA");
      }
      final StubConvention stubType = getStubType(index, length, previousCouponDate, nextCouponDate, paymentsPerYear, isEndOfMonthConvention);
      return ((ActualActualICMANormal) dayCount).getAccruedInterest(previousCouponDate, date, nextCouponDate, coupon, paymentsPerYear, stubType);

    } else if (dayCount instanceof ActualActualICMA) {
      final StubConvention stubType = getStubType(index, length, previousCouponDate, nextCouponDate, paymentsPerYear, isEndOfMonthConvention);
      return ((ActualActualICMA) dayCount).getAccruedInterest(previousCouponDate, date, nextCouponDate, coupon, paymentsPerYear, stubType);

    } else if (dayCount instanceof ThirtyUThreeSixty) {
      return ((ThirtyUThreeSixty) dayCount).getAccruedInterest(previousCouponDate, date, coupon, isEndOfMonthConvention);
    }
    return dayCount.getAccruedInterest(previousCouponDate, date, nextCouponDate, coupon, paymentsPerYear);
  }

  public static double getAccruedInterest(final DayCount dayCount, final int index, final int length, final LocalDate previousCouponDate, final LocalDate date,
      final LocalDate nextCouponDate, final double coupon, final double paymentsPerYear, final boolean isEndOfMonthConvention) {
    if (dayCount instanceof ActualActualICMANormal) {
      if (isEndOfMonthConvention) {
        throw new IllegalArgumentException("Inconsistent definition; asked for accrual with EOM convention but are not using Actual/Actual ICMA");
      }
      final StubConvention stubType = getStubType(index, length, previousCouponDate, nextCouponDate, paymentsPerYear, isEndOfMonthConvention);
      return ((ActualActualICMANormal) dayCount).getAccruedInterest(previousCouponDate, date, nextCouponDate, coupon, paymentsPerYear, stubType);

    } else if (dayCount instanceof ActualActualICMA) {
      final StubConvention stubType = getStubType(index, length, previousCouponDate, nextCouponDate, paymentsPerYear, isEndOfMonthConvention);
      return ((ActualActualICMA) dayCount).getAccruedInterest(previousCouponDate, date, nextCouponDate, coupon, paymentsPerYear, stubType);

    } else if (dayCount instanceof ThirtyUThreeSixty) {
      return ((ThirtyUThreeSixty) dayCount).getAccruedInterest(previousCouponDate, date, coupon, isEndOfMonthConvention);
    }
    return dayCount.getAccruedInterest(previousCouponDate, date, nextCouponDate, coupon, paymentsPerYear);
  }

  private static StubConvention getStubType(final int index, final int length, final ZonedDateTime previousCouponDate, final ZonedDateTime nextCouponDate, final double paymentsPerYear,
      final boolean isEndOfMonthConvention) {
    StubConvention stubType;
    if (index == 0) {
      LocalDate[] schedule = new LocalDate[] {previousCouponDate.toLocalDate(), nextCouponDate.toLocalDate()};
      stubType = StubCalculator.getStartStubType(schedule, paymentsPerYear, isEndOfMonthConvention);

    } else if (index == length - 2) {
      LocalDate[] schedule = new LocalDate[] {previousCouponDate.toLocalDate(), nextCouponDate.toLocalDate()};
      stubType = StubCalculator.getEndStubType(schedule, paymentsPerYear, isEndOfMonthConvention);

    } else {
      stubType = StubConvention.NONE;
    }
    return stubType;
  }

  private static StubConvention getStubType(final int index, final int length, final LocalDate previousCouponDate, final LocalDate nextCouponDate, final double paymentsPerYear,
      final boolean isEndOfMonthConvention) {
    StubConvention stubType;
    if (index == 0) {
      LocalDate[] schedule = new LocalDate[] {previousCouponDate, nextCouponDate};
      stubType = StubCalculator.getStartStubType(schedule, paymentsPerYear, isEndOfMonthConvention);

    } else if (index == length - 2) {
      LocalDate[] schedule = new LocalDate[] {previousCouponDate, nextCouponDate};
      stubType = StubCalculator.getEndStubType(schedule, paymentsPerYear, isEndOfMonthConvention);

    } else {
      stubType = StubConvention.NONE;
    }
    return stubType;
  }
}
