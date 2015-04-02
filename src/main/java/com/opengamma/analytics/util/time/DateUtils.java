/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.util.time;

import static java.time.temporal.ChronoField.DAY_OF_MONTH;
import static java.time.temporal.ChronoField.MONTH_OF_YEAR;
import static java.time.temporal.ChronoField.YEAR;
import static java.time.temporal.ChronoUnit.DAYS;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.SignStyle;
import java.time.temporal.Temporal;

/**
 * Utility class for dates.
 * <p>
 * This is a thread-safe static utility class.
 */
public final class DateUtils {

  /**
   * The number of seconds in one day.
   */
  public static final long SECONDS_PER_DAY = 86400L;
  /**
   * The number of days in one year (estimated as 365.25).
   */
  //TODO change this to 365.2425 to be consistent with JSR-310
  public static final double DAYS_PER_YEAR = 365.25;
  /**
   * The number of milliseconds in one day.
   */
  public static final long MILLISECONDS_PER_DAY = SECONDS_PER_DAY * 1000;
  /**
   * The number of seconds in one year.
   */
  public static final long SECONDS_PER_YEAR = (long) (SECONDS_PER_DAY * DAYS_PER_YEAR);
  /**
   * The number of milliseconds in one year.
   */
  public static final long MILLISECONDS_PER_YEAR = SECONDS_PER_YEAR * 1000;
  /**
   * A formatter for yyyyMMdd.
   */
  private static final DateTimeFormatter YYYYMMDD_LOCAL_DATE;
  static {
    YYYYMMDD_LOCAL_DATE = new DateTimeFormatterBuilder()
        .appendValue(YEAR, 4, 10, SignStyle.EXCEEDS_PAD)
        .appendValue(MONTH_OF_YEAR, 2)
        .appendValue(DAY_OF_MONTH, 2)
        .toFormatter();
  }
  /**
   * A formatter for MM-dd
   */
  private static final DateTimeFormatter MM_DD_LOCAL_DATE;
  static {
    MM_DD_LOCAL_DATE = new DateTimeFormatterBuilder()
        .appendValue(MONTH_OF_YEAR, 2)
        .appendLiteral("-")
        .appendValue(DAY_OF_MONTH, 2)
        .toFormatter();
  }

  /**
   * Restricted constructor.
   */
  private DateUtils() {
  }

  //-------------------------------------------------------------------------
  /**
   * Returns endDate - startDate in years, where a year is defined as 365.25 days.
   * 
   * @param startDate the start date, not null
   * @param endDate the end date, not null
   * @return the difference in years
   * @throws IllegalArgumentException if either date is null
   */
  public static double getDifferenceInYears(final Instant startDate, final Instant endDate) {
    if (startDate == null) {
      throw new IllegalArgumentException("Start date was null");
    }
    if (endDate == null) {
      throw new IllegalArgumentException("End date was null");
    }
    return (double) (endDate.toEpochMilli() - startDate.toEpochMilli()) / MILLISECONDS_PER_YEAR;
  }

  /**
   * Returns endDate - startDate in years, where a year is defined as 365.25 days.
   * 
   * @param startDate the start date, not null
   * @param endDate the end date, not null
   * @return the difference in years
   * @throws IllegalArgumentException if either date is null
   */
  public static double getDifferenceInYears(final ZonedDateTime startDate, final ZonedDateTime endDate) {
    if (startDate == null) {
      throw new IllegalArgumentException("Start date was null");
    }
    if (endDate == null) {
      throw new IllegalArgumentException("End date was null");
    }
    return (double) (endDate.toInstant().toEpochMilli() - startDate.toInstant().toEpochMilli()) / MILLISECONDS_PER_YEAR;
  }

  /**
   * Returns endDate - startDate in years, where a year is defined as 365.25 days.
   * 
   * @param startDate the start date, not null
   * @param endDate the end date, not null
   * @return the difference in years
   * @throws IllegalArgumentException if either date is null
   */
  public static double getDifferenceInYears(final LocalDate startDate, final LocalDate endDate) {
    if (startDate == null) {
      throw new IllegalArgumentException("Start date was null");
    }
    if (endDate == null) {
      throw new IllegalArgumentException("End date was null");
    }
    double diff = endDate.toEpochDay() - startDate.toEpochDay();
    return diff / DAYS_PER_YEAR;
  }

  /**
   * Returns endDate - startDate in years, where a year-length is specified.
   * 
   * @param startDate the start date, not null
   * @param endDate the end date, not null
   * @param daysPerYear the number of days in the year for calculation
   * @return the difference in years
   * @throws IllegalArgumentException if either date is null
   */
  public static double getDifferenceInYears(final Instant startDate, final Instant endDate, final double daysPerYear) {
    if (startDate == null) {
      throw new IllegalArgumentException("Start date was null");
    }
    if (endDate == null) {
      throw new IllegalArgumentException("End date was null");
    }
    return (endDate.toEpochMilli() - startDate.toEpochMilli()) / MILLISECONDS_PER_DAY / daysPerYear;
  }

  //-------------------------------------------------------------------------
  /**
   * Method that allows a fraction of a year to be added to a date. If the yearFraction that is used does not give an integer number of seconds, it is rounded to the nearest nanosecond. Note that the
   * number of days in a year is defined to be 365.25.
   * 
   * @param startDate the start date, not null
   * @param yearFraction the fraction of a year
   * @return the calculated instant, not null
   * @throws IllegalArgumentException if the date is null
   */
  public static Instant getDateOffsetWithYearFraction(final Instant startDate, final double yearFraction) {
    if (startDate == null) {
      throw new IllegalArgumentException("Date was null");
    }
    final long nanos = Math.round(1e9 * SECONDS_PER_YEAR * yearFraction);
    return startDate.plusNanos(nanos);
  }

  /**
   * Method that allows a fraction of a year to be added to a date. If the yearFraction that is used does not give an integer number of seconds, it is rounded to the nearest nanosecond. Note that the
   * number of days in a year is defined to be 365.25.
   * 
   * @param startDate the start date, not null
   * @param yearFraction the fraction of a year
   * @return the calculated date-time, not null
   * @throws IllegalArgumentException if the date is null
   */
  public static ZonedDateTime getDateOffsetWithYearFraction(final ZonedDateTime startDate, final double yearFraction) {
    if (startDate == null) {
      throw new IllegalArgumentException("Date was null");
    }
    final Instant instant = startDate.toInstant();
    final Instant offsetDate = getDateOffsetWithYearFraction(instant, yearFraction);
    return ZonedDateTime.ofInstant(offsetDate, startDate.getZone());
  }

  //-------------------------------------------------------------------------
  /**
   * Returns a UTC date given year, month, day with the time set to midnight (UTC).
   * 
   * @param year the year
   * @param month the month
   * @param day the day of month
   * @return the date-time, not null
   */
  public static ZonedDateTime getUTCDate(final int year, final int month, final int day) {
    return LocalDate.of(year, month, day).atStartOfDay(ZoneOffset.UTC);
  }

  /**
   * Returns a UTC date given year, month, day, hour and minutes.
   * 
   * @param year the year
   * @param month the month
   * @param day the day of month
   * @param hour the hour
   * @param minute the minute
   * @return the date-time, not null
   */
  public static ZonedDateTime getUTCDate(final int year, final int month, final int day, final int hour, final int minute) {
    return ZonedDateTime.of(LocalDateTime.of(year, month, day, hour, minute), ZoneOffset.UTC);
  }

  //-------------------------------------------------------------------------
  /**
   * Calculates the exact number of 24 hour days in between two dates. Accounts for dates being in different time zones.
   * 
   * @param startDate the start date, not null
   * @param endDate the end date, not null
   * @return the exact fraction of days between two dates
   * @throws IllegalArgumentException if the date is null
   */
  public static double getExactDaysBetween(final ZonedDateTime startDate, final ZonedDateTime endDate) {
    // TODO: was 24-hour days intended?
    if (startDate == null) {
      throw new IllegalArgumentException("Start date was null");
    }
    if (endDate == null) {
      throw new IllegalArgumentException("End date was null");
    }
    return (endDate.toInstant().getEpochSecond() - startDate.toInstant().getEpochSecond()) / (double) SECONDS_PER_DAY;
  }

  /**
   * Calculates the number of days in between two dates.
   * 
   * @param startDate the start date, not null
   * @param endDate the end date, not null
   * @return the number of days between two dates
   * @throws IllegalArgumentException if the date is null
   */
  public static int getDaysBetween(final Temporal startDate, final Temporal endDate) {
    return getDaysBetween(startDate, true, endDate, false);
  }

  /**
   * Calculates the number of days in between two dates.
   * 
   * @param startDate the start date, not null
   * @param includeStart whether to include the start
   * @param endDate the end date, not null
   * @param includeEnd whether to include the end
   * @return the number of days between two dates
   * @throws IllegalArgumentException if the date is null
   */
  public static int getDaysBetween(final Temporal startDate, final boolean includeStart, final Temporal endDate, final boolean includeEnd) {
    if (startDate == null) {
      throw new IllegalArgumentException("Start date was null");
    }
    if (endDate == null) {
      throw new IllegalArgumentException("End date was null");
    }
    int daysBetween = (int) Math.abs(DAYS.between(startDate, endDate));
    if (includeStart && includeEnd) {
      daysBetween++;
    } else if (!includeStart && !includeEnd) {
      daysBetween--;
    }
    return daysBetween;
  }

}
