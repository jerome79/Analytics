/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.schedule;

import static com.opengamma.analytics.convention.businessday.BusinessDayDateUtils.applyConvention;

import java.time.LocalDate;
import java.time.Period;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

import com.opengamma.analytics.convention.daycount.DayCount;
import com.opengamma.analytics.convention.daycount.DayCountUtils;
import com.opengamma.analytics.convention.rolldate.EndOfMonthRollDateAdjuster;
import com.opengamma.analytics.convention.rolldate.RollDateAdjuster;
import com.opengamma.analytics.financial.instrument.index.GeneratorDeposit;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.util.time.DateUtils;
import com.opengamma.strata.basics.date.BusinessDayConvention;
import com.opengamma.strata.basics.date.BusinessDayConventions;
import com.opengamma.strata.basics.date.HolidayCalendar;
import com.opengamma.strata.basics.schedule.Frequency;
import com.opengamma.strata.basics.schedule.StubConvention;
import com.opengamma.strata.collect.ArgChecker;

/**
 * Utility to calculate schedules.
 */
public final class ScheduleCalculator {

  /**
   * A singleton empty array.
   */
  private static final ZonedDateTime[] EMPTY_ARRAY = new ZonedDateTime[0];

  /**
   * Restricted constructor.
   */
  private ScheduleCalculator() {
  }

  // Already reviewed

  /**
   * Return a good business date computed from a given date and shifted by a certain number of business days.
   * If the number of shift days is 0, the return date is the next business day.
   * If the number of shift days is non-zero (positive or negative), a 0 shift is first applied and then a
   * one business day shift is applied as many time as the absolute value of the shift.
   * If the shift is positive, the one business day is to the future.
   * If the shift is negative, the one business day is to the past.
   * @param date The initial date.
   * @param shiftDays The number of days of the adjustment. Can be negative or positive.
   * @param calendar The calendar representing the good business days.
   * @return The adjusted date.
   */
  public static ZonedDateTime getAdjustedDate(ZonedDateTime date, int shiftDays, HolidayCalendar calendar) {
    ArgChecker.notNull(date, "date");
    ArgChecker.notNull(calendar, "calendar");
    ZonedDateTime result = date;
    while (!calendar.isBusinessDay(result.toLocalDate())) {
      result = result.plusDays(1);
    }
    if (shiftDays > 0) {
      for (int loopday = 0; loopday < shiftDays; loopday++) {
        result = result.plusDays(1);
        while (!calendar.isBusinessDay(result.toLocalDate())) {
          result = result.plusDays(1);
        }
      }
    } else {
      for (int loopday = 0; loopday < -shiftDays; loopday++) {
        result = result.minusDays(1);
        while (!calendar.isBusinessDay(result.toLocalDate())) {
          result = result.minusDays(1);
        }
      }
    }
    return result;
  }

  /**
   * Return a good business dates computed from given array of date and shifted by a certain number of
   * business days (one return date for each input date).
   * If the number of shift days is 0, the return date is the next business day.
   * If the number of shift days is non-zero (positive or negative), a 0 shift is first applied and then a
   * one business day shift is applied as many time as the absolute value of the shift.
   * If the shift is positive, the one business day is to the future.
   * If the shift is negative, the one business day is to the past.
   * @param dates The initial dates.
   * @param shiftDays The number of days of the adjustment. Can be negative or positive.
   * @param calendar The calendar representing the good business days.
   * @return The adjusted dates.
   */
  public static ZonedDateTime[] getAdjustedDate(ZonedDateTime[] dates, int shiftDays, HolidayCalendar calendar) {
    int nbDates = dates.length;
    ZonedDateTime[] result = new ZonedDateTime[nbDates];
    for (int loopdate = 0; loopdate < nbDates; loopdate++) {
      result[loopdate] = getAdjustedDate(dates[loopdate], shiftDays, calendar);
    }
    return result;
  }

  /**
   * Return a good business date computed from a given date and shifted by a certain number of business days.
   * The number of business days is given by the getDays part of a peeriod.
   * If the number of shift days is 0, the return date is the next business day.
   * If the number of shift days is non-zero (positive or negative), a 0 shift is first applied and then a
   * one business day shift is applied as many time as the absolute value of the shift.
   * If the shift is positive, the one business day is to the future.
   * If the shift is negative, the one business day is to the past.
   * @param date The initial date.
   * @param shiftDays The number of days of the adjustment as a period.
   * @param calendar The calendar representing the good business days.
   * @return The adjusted dates.
   */
  public static ZonedDateTime getAdjustedDate(ZonedDateTime date, Period shiftDays, HolidayCalendar calendar) {
    ArgChecker.notNull(shiftDays, "shift days");
    return getAdjustedDate(date, shiftDays.getDays(), calendar);
  }

  /**
   * Return a good business date computed from a given date and shifted by a certain number of business days.
   * This version uses LocalDate.
   * If the number of shift days is 0, the return date is the next business day.
   * If the number of shift days is non-zero (positive or negative), a 0 shift is first applied and then a
   * one business day shift is applied as many time as the absolute value of the shift.
   * If the shift is positive, the one business day is to the future.
   * If the shift is negative, the one business day is to the past.
   * @param date The initial date.
   * @param shiftDays The number of days of the adjustment. Can be negative or positive.
   * @param calendar The calendar representing the good business days.
   * @return The adjusted dates.
   */
  public static LocalDate getAdjustedDate(LocalDate date, int shiftDays, HolidayCalendar calendar) {
    ArgChecker.notNull(date, "date");
    ArgChecker.notNull(calendar, "calendar");
    LocalDate result = date;
    while (!calendar.isBusinessDay(result)) {
      result = result.plusDays(1);
    }
    if (shiftDays > 0) {
      for (int loopday = 0; loopday < shiftDays; loopday++) {
        result = result.plusDays(1);
        while (!calendar.isBusinessDay(result)) {
          result = result.plusDays(1);
        }
      }
    } else {
      for (int loopday = 0; loopday < -shiftDays; loopday++) {
        result = result.minusDays(1);
        while (!calendar.isBusinessDay(result)) {
          result = result.minusDays(1);
        }
      }
    }
    return result;
  }

  /**
   * Compute the end date of a period from the start date, the tenor and the conventions without end-of-month convention.
   * @param startDate The period start date.
   * @param tenor The period tenor.
   * @param convention The business day convention.
   * @param calendar The calendar.
   * @return The end date.
   */
  public static ZonedDateTime getAdjustedDate(
      ZonedDateTime startDate,
      Period tenor,
      BusinessDayConvention convention,
      HolidayCalendar calendar) {

    ArgChecker.notNull(startDate, "start date");
    ArgChecker.notNull(convention, "convention");
    ArgChecker.notNull(calendar, "calendar");
    ArgChecker.notNull(tenor, "tenor");
    ZonedDateTime endDate = startDate.plus(tenor); // Unadjusted date.
    return applyConvention(convention, endDate, calendar); // Adjusted by Business day convention
  }

  /**
   * Compute the end date of a period from the start date, the tenor and the conventions.
   * @param startDate The period start date.
   * @param tenor The period tenor.
   * @param convention The business day convention.
   * @param calendar The calendar.
   * @param endOfMonthRule True if end-of-month rule applies, false if it does not.
   * The rule applies when the start date is the last business day of the month and the period is a number of months or years, not days or weeks.
   * When the rule applies, the end date is the last business day of the month.
   * @return The end date.
   */
  public static ZonedDateTime getAdjustedDate(
      ZonedDateTime startDate,
      Period tenor,
      BusinessDayConvention convention,
      HolidayCalendar calendar,
      boolean endOfMonthRule) {

    ArgChecker.notNull(startDate, "Start date");
    ArgChecker.notNull(convention, "Convention");
    ArgChecker.notNull(calendar, "HolidayCalendar");
    ArgChecker.notNull(tenor, "Tenor");
    ZonedDateTime endDate = startDate.plus(tenor); // Unadjusted date.
    // Adjusted to month-end: when start date is last business day of the month, the end date is the last business day of the month.
    boolean isStartDateEOM = (startDate.getMonth() != getAdjustedDate(startDate, 1, calendar).getMonth());
    if ((tenor.getDays() == 0) & (endOfMonthRule) & (isStartDateEOM)) {
      BusinessDayConvention preceding = BusinessDayConventions.PRECEDING;
      return applyConvention(preceding, endDate.with(TemporalAdjusters.lastDayOfMonth()), calendar);
    }
    return applyConvention(convention, endDate, calendar); // Adjusted by Business day convention
  }

  /**
   * Compute the end date of a period from the start date, period, conventions and roll date adjuster. If the roll date
   * adjuster is end of month, then only apply when the start date is last business day of the month and the period is a
   * number of months or years, not days or weeks.
   *
   * @param startDate the start date
   * @param period the period between the start and end date.
   * @param convention the business day convention used to adjust the end date.
   * @param calendar the calendar used to adjust the end date.
   * @param rollDateAdjuster the roll date adjuster used to adjust the end date, before the conventions are applied.
   * @return The end date.
   */
  public static ZonedDateTime getAdjustedDate(
      ZonedDateTime startDate,
      Period period,
      BusinessDayConvention convention,
      HolidayCalendar calendar,
      RollDateAdjuster rollDateAdjuster) {

    ArgChecker.notNull(startDate, "Start date");
    ArgChecker.notNull(convention, "Convention");
    ArgChecker.notNull(calendar, "HolidayCalendar");
    ArgChecker.notNull(period, "Tenor");
    ZonedDateTime endDate = startDate.plus(period); // Unadjusted date.
    // Adjusted to month-end: when start date is last business day of the month, the end date is the last business day of the month.
    if (rollDateAdjuster instanceof EndOfMonthRollDateAdjuster) {
      boolean isStartDateEOM = (startDate.getMonth() != getAdjustedDate(startDate, 1, calendar).getMonth());
      if ((period.getDays() == 0) && isStartDateEOM) {
        BusinessDayConvention preceding = BusinessDayConventions.PRECEDING;
        return applyConvention(preceding, endDate.with(TemporalAdjusters.lastDayOfMonth()), calendar);
      }
    } else if (rollDateAdjuster != null) {
      /*
       * If we are rolling forward with a positive period and we have a day of month adjuster, we don't want to roll
       * backwards.
       */
      ZonedDateTime rolledEndDate = endDate.with(rollDateAdjuster);
      if (!period.isNegative() && rolledEndDate.isAfter(endDate)) {
        endDate = rolledEndDate;
      }
    }
    return applyConvention(convention, endDate, calendar); // Adjusted by Business day convention
  }

  /**
   * Compute the end date of a period from the start date, the tenor and the conventions.
   * @param startDate The period start date.
   * @param tenor The period tenor.
   * @param generator The deposit generator with the required conventions.
   * @return The end date.
   */
  public static ZonedDateTime getAdjustedDate(ZonedDateTime startDate, Period tenor, GeneratorDeposit generator) {
    ArgChecker.notNull(generator, "Generator");
    return getAdjustedDate(
        startDate,
        tenor,
        generator.getBusinessDayConvention(),
        generator.getCalendar(),
        generator.isEndOfMonth());
  }

  /**
   * Compute the end date of a period from the start date, a period and a Ibor index. The index is used for the conventions.
   * @param startDate The period start date.
   * @param tenor The period tenor.
   * @param index The Ibor index.
   * @param calendar The holiday calendar.
   * @return The end date.
   */
  public static ZonedDateTime getAdjustedDate(
      ZonedDateTime startDate,
      Period tenor,
      IborIndex index,
      HolidayCalendar calendar) {

    ArgChecker.notNull(index, "Index");
    return getAdjustedDate(startDate, tenor, index.getBusinessDayConvention(), calendar, index.isEndOfMonth());
  }

  /**
   * Compute the end date of a period from the start date and a Ibor index.
   * The period between the start date and the end date is the index tenor.
   * @param startDate The period start date.
   * @param index The Ibor index.
   * @param calendar The holiday calendar.
   * @return The end date.
   */
  public static ZonedDateTime getAdjustedDate(ZonedDateTime startDate, IborIndex index, HolidayCalendar calendar) {
    ArgChecker.notNull(index, "Index");
    return getAdjustedDate(startDate, index.getTenor(), index.getBusinessDayConvention(), calendar, index.isEndOfMonth());
  }

  /**
   * Compute the end dates of periods from the start dates and a Ibor index.
   * The period between the start date and the end date is the index tenor.
   *  There is one return date for each input date.
   * @param startDates The period start dates.
   * @param index The Ibor index.
   * @param calendar The holiday calendar.
   * @return The end dates.
   */
  public static ZonedDateTime[] getAdjustedDate(ZonedDateTime[] startDates, IborIndex index, HolidayCalendar calendar) {
    int nbDates = startDates.length;
    ZonedDateTime[] result = new ZonedDateTime[nbDates];
    for (int loopdate = 0; loopdate < nbDates; loopdate++) {
      result[loopdate] = getAdjustedDate(startDates[loopdate], index, calendar);
    }
    return result;
  }

  /**
   * Compute a schedule of unadjusted dates from a start date, an end date and the period between dates.
   * @param startDate The start date.
   * @param endDate The end date.
   * @param tenorPeriod The period between each date.
   * @param stub The stub type.
   * @return The date schedule (not including the start date).
   */
  public static ZonedDateTime[] getUnadjustedDateSchedule(
      ZonedDateTime startDate,
      ZonedDateTime endDate,
      Period tenorPeriod,
      StubConvention stub) {

    ArgChecker.notNull(startDate, "Start date");
    ArgChecker.notNull(endDate, "End date");
    ArgChecker.notNull(tenorPeriod, "Period tenor");
    ArgChecker.isTrue(startDate.isBefore(endDate), "Start date should be strictly before end date");
    boolean stubShort = stub.equals(StubConvention.SHORT_FINAL) || stub.equals(StubConvention.SHORT_INITIAL) ||
        stub.equals(StubConvention.NONE) || stub.equals(StubConvention.BOTH);
    boolean fromEnd = isGenerateFromEnd(stub); //  || stub.equals(StubConvention.NONE); // Implementation note: dates computed from the end.
    List<ZonedDateTime> dates = new ArrayList<>();
    int nbPeriod = 0;
    if (!fromEnd) { // Add the periods from the start date
      ZonedDateTime date = startDate.plus(tenorPeriod);
      while (date.isBefore(endDate)) { // date is strictly before endDate
        dates.add(date);
        nbPeriod++;
        date = startDate.plus(tenorPeriod.multipliedBy(nbPeriod + 1));
      }
      if (!stubShort && !date.equals(endDate) && nbPeriod >= 1) { // For long stub the last date before end date, if any, is removed.
        dates.remove(nbPeriod - 1);
      }
      dates.add(endDate);
      return dates.toArray(EMPTY_ARRAY);
    }
    // From end - Subtract the periods from the end date
    ZonedDateTime date = endDate;
    while (date.isAfter(startDate)) { // date is strictly after startDate
      dates.add(date);
      nbPeriod++;
      date = endDate.minus(tenorPeriod.multipliedBy(nbPeriod));
    }
    if (!stubShort && !date.equals(startDate) && nbPeriod > 1) { // For long stub the last date before end date, if any, is removed.
      dates.remove(nbPeriod - 1);
    }
    Collections.sort(dates); // To obtain the dates in chronological order.
    return dates.toArray(EMPTY_ARRAY);
  }

  /**
   * Compute a schedule of unadjusted dates from a start date, an end date and the period between dates.
   * @param startDate The start date.
   * @param endDate The end date.
   * @param tenorPeriod The period between each date.
   * @param stubShort In case the the periods do not fit exactly between start and end date,
   * is the remaining interval shorter (true) or longer (false) than the requested period.
   * @param fromEnd The dates in the schedule can be computed from the end date (true) or from the start date (false).
   * @return The date schedule (not including the start date).
   */
  public static ZonedDateTime[] getUnadjustedDateSchedule(
      ZonedDateTime startDate,
      ZonedDateTime endDate,
      Period tenorPeriod,
      boolean stubShort,
      boolean fromEnd) {

    ArgChecker.notNull(startDate, "Start date");
    ArgChecker.notNull(endDate, "End date");
    ArgChecker.notNull(tenorPeriod, "Period tenor");
    ArgChecker.isTrue(startDate.isBefore(endDate), "Start date {} should be strictly before end date {}", startDate, endDate);
    List<ZonedDateTime> dates = new ArrayList<>();
    int nbPeriod = 0;
    if (!fromEnd) { // Add the periods from the start date
      ZonedDateTime date = startDate.plus(tenorPeriod);
      while (date.isBefore(endDate)) { // date is strictly before endDate
        dates.add(date);
        nbPeriod++;
        date = startDate.plus(tenorPeriod.multipliedBy(nbPeriod + 1));
      }
      if (!stubShort && !date.equals(endDate) && nbPeriod >= 1) { // For long stub the last date before end date, if any, is removed.
        dates.remove(nbPeriod - 1);
      }
      dates.add(endDate);
      return dates.toArray(EMPTY_ARRAY);
    }
    // From end - Subtract the periods from the end date
    ZonedDateTime date = endDate;
    while (date.isAfter(startDate)) { // date is strictly after startDate
      dates.add(date);
      nbPeriod++;
      date = endDate.minus(tenorPeriod.multipliedBy(nbPeriod));
    }
    if (!stubShort && !date.equals(startDate) && nbPeriod > 1) { // For long stub the last date before end date, if any, is removed.
      dates.remove(nbPeriod - 1);
    }
    Collections.sort(dates); // To obtain the dates in chronological order.
    return dates.toArray(EMPTY_ARRAY);
  }

  /**
   * Adjust an array of date with a given convention and EOM flag.
   * @param dates The array of unadjusted dates.
   * @param convention The business day convention.
   * @param calendar The calendar.
   * @param eomApply The flag indicating if the EOM apply, i.e. if the flag is true,
   *  the adjusted date is the last business day of the unadjusted date.
   * @return The adjusted dates.
   */
  public static ZonedDateTime[] getAdjustedDateSchedule(
      ZonedDateTime[] dates,
      BusinessDayConvention convention,
      HolidayCalendar calendar,
      boolean eomApply) {

    ZonedDateTime[] result = new ZonedDateTime[dates.length];
    if (eomApply) {
      BusinessDayConvention precedingDBC = BusinessDayConventions.PRECEDING; //To ensure that the date stays in the current month.
      for (int loopdate = 0; loopdate < dates.length; loopdate++) {
        result[loopdate] = applyConvention(precedingDBC, dates[loopdate].with(TemporalAdjusters.lastDayOfMonth()), calendar);
      }
      return result;
    }
    for (int loopdate = 0; loopdate < dates.length; loopdate++) {
      result[loopdate] = applyConvention(convention, dates[loopdate], calendar);
    }
    return result;
  }

  public static ZonedDateTime[] getAdjustedDateSchedule(
      ZonedDateTime[] dates,
      BusinessDayConvention convention,
      HolidayCalendar calendar,
      boolean eomApply,
      RollDateAdjuster adjuster) {

    ZonedDateTime[] result = new ZonedDateTime[dates.length];
    if (eomApply) {
      BusinessDayConvention precedingDBC = BusinessDayConventions.PRECEDING; //To ensure that the date stays in the current month.
      for (int loopdate = 0; loopdate < dates.length; loopdate++) {
        result[loopdate] = applyConvention(precedingDBC, dates[loopdate].with(TemporalAdjusters.lastDayOfMonth()), calendar);
      }
      return result;
    }
    if (adjuster != null && !(adjuster instanceof EndOfMonthRollDateAdjuster)) {
      for (int loopdate = 0; loopdate < dates.length; loopdate++) {
        result[loopdate] = applyConvention(convention, dates[loopdate].with(adjuster), calendar);
      }
    } else {
      for (int loopdate = 0; loopdate < dates.length; loopdate++) {
        result[loopdate] = applyConvention(convention, dates[loopdate], calendar);
      }
    }
    // TODO workaround for PLAT-5695
    ZonedDateTime[] treeSetResult = new TreeSet<>(Arrays.asList(result)).toArray(new ZonedDateTime[] {});
    return treeSetResult;
  }

  /**
   * Compute a schedule of adjusted dates from a start date, an end date and the period between dates.
   * @param startDate The start date.
   * @param endDate The end date.
   * @param schedulePeriod The period between each date in the schedule.
   * @param stubShort In case the the periods do not fit exactly between start and end date,
   *  is the remaining interval shorter (true) or longer (false) than the requested period.
   * @param fromEnd The dates in the schedule can be computed from the end date (true) or from the start date (false).
   * @param convention The business day convention.
   * @param calendar The calendar.
   * @param eomRule Flag indicating if the end-of-month rule should be applied.
   * @return The adjusted dates schedule.
   */
  public static ZonedDateTime[] getAdjustedDateSchedule(
      ZonedDateTime startDate,
      ZonedDateTime endDate,
      Period schedulePeriod,
      boolean stubShort,
      boolean fromEnd,
      BusinessDayConvention convention,
      HolidayCalendar calendar,
      boolean eomRule) {

    ZonedDateTime[] unadjustedDateSchedule = getUnadjustedDateSchedule(startDate, endDate, schedulePeriod, stubShort, fromEnd);
    boolean eomApply = (eomRule && eomApplies(fromEnd, startDate, endDate, calendar));
    return getAdjustedDateSchedule(unadjustedDateSchedule, convention, calendar, eomApply);
  }

  public static ZonedDateTime[] getAdjustedDateSchedule(ZonedDateTime startDate, ZonedDateTime endDate, Period schedulePeriod, boolean stubShort,
      boolean fromEnd, BusinessDayConvention convention, HolidayCalendar calendar, boolean eomRule, RollDateAdjuster adjuster) {
    ZonedDateTime[] unadjustedDateSchedule = getUnadjustedDateSchedule(startDate, endDate, schedulePeriod, stubShort, fromEnd);
    boolean eomApply = (eomRule && eomApplies(fromEnd, startDate, endDate, calendar));
    return getAdjustedDateSchedule(unadjustedDateSchedule, convention, calendar, eomApply, adjuster);
  }

  /**
   * Compute a schedule of adjusted dates from a start date, an end date and the period between dates.
   * @param startDate The start date.
   * @param endDate The end date.
   * @param schedulePeriod The period between each date in the schedule.
   * @param stub The stub type.
   * @param convention The business day convention.
   * @param calendar The calendar.
   * @param eomRule Flag indicating if the end-of-month rule should be applied.
   * @return The adjusted dates schedule.
   */
  public static ZonedDateTime[] getAdjustedDateSchedule(
      ZonedDateTime startDate,
      ZonedDateTime endDate,
      Period schedulePeriod,
      StubConvention stub,
      BusinessDayConvention convention,
      HolidayCalendar calendar,
      boolean eomRule) {

    ZonedDateTime[] unadjustedDateSchedule = getUnadjustedDateSchedule(startDate, endDate, schedulePeriod, stub);
    boolean eomApply = (eomRule && eomApplies(isGenerateFromEnd(stub), startDate, endDate, calendar));
    return getAdjustedDateSchedule(unadjustedDateSchedule, convention, calendar, eomApply);
  }

  public static ZonedDateTime[] getAdjustedDateSchedule(
      ZonedDateTime startDate,
      ZonedDateTime endDate,
      Period schedulePeriod,
      StubConvention stub,
      BusinessDayConvention convention,
      HolidayCalendar calendar,
      boolean eomRule,
      RollDateAdjuster adjuster) {

    ZonedDateTime[] unadjustedDateSchedule = getUnadjustedDateSchedule(startDate, endDate, schedulePeriod, stub);
    boolean eomApply = (eomRule && eomApplies(isGenerateFromEnd(stub), startDate, endDate, calendar));
    return getAdjustedDateSchedule(unadjustedDateSchedule, convention, calendar, eomApply, adjuster);
  }

  /**
   * Calculate a schedule of adjusted dates, but not the start date.
   * 
   * @param startDate  the start date
   * @param endDate  the end date
   * @param schedulePeriod  the periodic frequency
   * @param stub  the stub type
   * @param convention  the business day convention
   * @param calendar  the holiday calendar
   * @param adjuster  the roll convention
   * @return the schedule array, not including the start date
   */
  public static ZonedDateTime[] getAdjustedDateSchedule(
      ZonedDateTime startDate,
      ZonedDateTime endDate,
      Period schedulePeriod,
      StubConvention stub,
      BusinessDayConvention convention,
      HolidayCalendar calendar,
      RollDateAdjuster adjuster) {

    ZonedDateTime[] unadjustedDateSchedule = getUnadjustedDateSchedule(startDate, endDate, schedulePeriod, stub);
    // convert roll adjuster into end-of-month flag and apply correctly
    if (adjuster instanceof EndOfMonthRollDateAdjuster) {
      // if calculating backwards, use end date to determine if rule applies, otherwise use start date
      boolean fromEnd = isGenerateFromEnd(stub);
      boolean eomApply = eomApplies(fromEnd, startDate, endDate, calendar);
      if (fromEnd) {
        return getAdjustedDateSchedule(unadjustedDateSchedule, convention, calendar, eomApply, adjuster);
      } else {
        ZonedDateTime[] adj = getAdjustedDateSchedule(unadjustedDateSchedule, convention, calendar, eomApply, adjuster);
        // ensure date is not rolled beyond end date
        if (adj.length > 0 && adj[adj.length - 1].isAfter(endDate)) {
          adj[adj.length - 1] = applyConvention(convention, endDate, calendar);
        }
        return adj;
      }
    }
    return getAdjustedDateSchedule(unadjustedDateSchedule, convention, calendar, false, adjuster);
  }

  public static ZonedDateTime[] getAdjustedDateSchedule(
      ZonedDateTime[] startDates,
      Period schedulePeriod,
      BusinessDayConvention businessDayConvention,
      HolidayCalendar calendar,
      RollDateAdjuster adjuster) {

    ZonedDateTime[] endDates = new ZonedDateTime[startDates.length];
    for (int i = 0; i < startDates.length; i++) {
      endDates[i] = getAdjustedDate(startDates[i], schedulePeriod, businessDayConvention, calendar, adjuster);
    }
    return endDates;
  }

  /**
   * Compute a schedule of adjusted dates from a start date, an end date and the period between dates.
   * @param startDate The start date.
   * @param endDate The end date.
   * @param scheduleFrequency The frequency of dates in the schedule.
   * @param stubShort In case the the periods do not fit exactly between start and end date,
   *  is the remaining interval shorter (true) or longer (false) than the requested period.
   * @param fromEnd The dates in the schedule can be computed from the end date (true) or from the start date (false).
   * @param convention The business day convention.
   * @param calendar The calendar.
   * @param eomRule Flag indicating if the end-of-month rule should be applied.
   * @return The adjusted dates schedule.
   */
  public static ZonedDateTime[] getAdjustedDateSchedule(
      ZonedDateTime startDate,
      ZonedDateTime endDate,
      Frequency scheduleFrequency,
      boolean stubShort,
      boolean fromEnd,
      BusinessDayConvention convention,
      HolidayCalendar calendar,
      boolean eomRule) {

    ArgChecker.notNull(scheduleFrequency, "Schedule frequency");
    Period schedulePeriod = scheduleFrequency.getPeriod();
    return getAdjustedDateSchedule(startDate, endDate, schedulePeriod, stubShort, fromEnd, convention, calendar, eomRule);
  }

  /**
   * Compute a schedule of adjusted dates from a start date, total tenor and the period between dates.
   * @param startDate The start date.
   * @param tenorTotal The total tenor.
   * @param tenorPeriod The period between each date.
   * @param stubShort In case the the periods do not fit exactly between start and end date,
   *  is the remaining interval shorter (true) or longer (false) than the requested period.
   * @param fromEnd The dates in the schedule can be computed from the end date (true) or from the start date (false).
   * @param convention The business day convention.
   * @param calendar The calendar.
   * @param eomRule Flag indicating if the end-of-month rule should be applied.
   * @return The adjusted dates schedule (not including the start date).
   */
  public static ZonedDateTime[] getAdjustedDateSchedule(
      ZonedDateTime startDate,
      Period tenorTotal,
      Period tenorPeriod,
      boolean stubShort,
      boolean fromEnd,
      BusinessDayConvention convention,
      HolidayCalendar calendar,
      boolean eomRule) {

    ZonedDateTime endDate = startDate.plus(tenorTotal);
    ZonedDateTime[] unadjustedDateSchedule = getUnadjustedDateSchedule(startDate, endDate, tenorPeriod, stubShort, fromEnd);
    boolean eomApply = (eomRule && eomApplies(fromEnd, startDate, endDate, calendar) && (tenorTotal.getDays() == 0));
    // Implementation note: the "tenorTotal.getDays() == 0" condition is required as the rule does not
    // apply for period of less than 1 month (like 1 week).
    return getAdjustedDateSchedule(unadjustedDateSchedule, convention, calendar, eomApply);
  }

  /**
   * Compute a schedule of adjusted dates from a start date, total tenor and a Ibor index.
   * @param startDate The start date.
   * @param tenorTotal The total tenor.
   * @param stubShort In case the the periods do not fit exactly between start and end date,
   *  is the remaining interval shorter (true) or longer (false) than the requested period.
   * @param fromEnd The dates in the schedule can be computed from the end date (true) or from the start date (false).
   * @param index The related ibor index. The period tenor, business day convention,
   *  calendar and EOM rule of the index are used.
   * @param calendar The holiday calendar.
   * @return The adjusted dates schedule (not including the start date).
   */
  public static ZonedDateTime[] getAdjustedDateSchedule(
      ZonedDateTime startDate,
      Period tenorTotal,
      boolean stubShort,
      boolean fromEnd,
      IborIndex index,
      HolidayCalendar calendar) {

    return getAdjustedDateSchedule(
        startDate,
        tenorTotal,
        index.getTenor(),
        stubShort,
        fromEnd,
        index.getBusinessDayConvention(),
        calendar,
        index.isEndOfMonth());
  }

  // TODO: review the methods below.

  // -------------------------------------------------------------------------
  /**
   * Calculates the unadjusted date schedule.
   *
   * @param effectiveDate  the effective date, not null
   * @param maturityDate  the maturity date, not null
   * @param frequency  how many times a year dates occur, not null
   * @return the schedule, not null
   */
  public static ZonedDateTime[] getUnadjustedDateSchedule(
      ZonedDateTime effectiveDate,
      ZonedDateTime maturityDate,
      Frequency frequency) {

    ArgChecker.notNull(effectiveDate, "effective date");
    ArgChecker.notNull(maturityDate, "maturity date");
    ArgChecker.notNull(frequency, "frequency");
    if (effectiveDate.isAfter(maturityDate)) {
      throw new IllegalArgumentException("Effective date was after maturity");
    }
    return getUnadjustedDateSchedule(effectiveDate, effectiveDate, maturityDate, frequency);
  }

  /**
   * Calculates the unadjusted date schedule.
   *
   * @param effectiveDate  the effective date, not null
   * @param accrualDate  the accrual date, not null
   * @param maturityDate  the maturity date, not null
   * @param frequency  how many times a year dates occur, not null
   * @return the schedule, not null
   */
  public static ZonedDateTime[] getUnadjustedDateSchedule(
      ZonedDateTime effectiveDate,
      ZonedDateTime accrualDate,
      ZonedDateTime maturityDate,
      Frequency frequency) {

    ArgChecker.notNull(effectiveDate, "effective date");
    ArgChecker.notNull(accrualDate, "accrual date");
    ArgChecker.notNull(maturityDate, "maturity date");
    ArgChecker.notNull(frequency, "frequency");
    if (effectiveDate.isAfter(maturityDate)) {
      throw new IllegalArgumentException("Effective date was after maturity");
    }
    if (accrualDate.isAfter(maturityDate)) {
      throw new IllegalArgumentException("Accrual date was after maturity");
    }

    // TODO what if there's no valid date between accrual date and maturity date?
    Period period = frequency.getPeriod();
    List<ZonedDateTime> dates = new ArrayList<>();
    ZonedDateTime date = effectiveDate; // TODO this is only correct if effective date = accrual date
    date = date.plus(period);
    // REVIEW: could speed this up by working out how many periods between start and end date?
    while (isWithinSwapLifetime(date, maturityDate)) {
      dates.add(date);
      date = date.plus(period);
    }
    return dates.toArray(EMPTY_ARRAY);
  }

  //TODO: add doc
  public static ZonedDateTime[] getUnadjustedDateSchedule(
      ZonedDateTime effectiveDate,
      ZonedDateTime accrualDate,
      ZonedDateTime maturityDate,
      Period period) {

    ArgChecker.notNull(effectiveDate, "effective date");
    ArgChecker.notNull(accrualDate, "accrual date");
    ArgChecker.notNull(maturityDate, "maturity date");
    ArgChecker.notNull(period, "period");
    if (effectiveDate.isAfter(maturityDate)) {
      throw new IllegalArgumentException("Effective date was after maturity");
    }
    if (accrualDate.isAfter(maturityDate)) {
      throw new IllegalArgumentException("Accrual date was after maturity");
    }

    // TODO what if there's no valid date between accrual date and maturity date?
    List<ZonedDateTime> dates = new ArrayList<>();
    int nbPeriod = 1; // M 26-Aug
    ZonedDateTime date = effectiveDate; // TODO this is only correct if effective date = accrual date
    date = date.plus(period);
    // REVIEW: could speed this up by working out how many periods between start and end date?
    while (isWithinSwapLifetime(date, maturityDate)) {
      dates.add(date);
      nbPeriod++; // M 26-Aug
      date = effectiveDate.plus(period.multipliedBy(nbPeriod)); // M 26-Aug date = date.plus(period);
    }
    return dates.toArray(EMPTY_ARRAY);
  }

  // -------------------------------------------------------------------------
  /**
   * Counts back from maturityDate, filling to equally spaced dates frequency
   * times a year until the last date <b>after</b> effective date.
   *
   * @param effectiveDate  the date that terminates to back counting (i.e. the first date is after this date), not null
   * @param maturityDate  the date to count back from, not null
   * @param frequency  how many times a year dates occur, not null
   * @return the first date after effectiveDate (i.e. effectiveDate is <b>not</b> included to the maturityDate (included)
   */
  public static ZonedDateTime[] getBackwardsUnadjustedDateSchedule(
      ZonedDateTime effectiveDate,
      ZonedDateTime maturityDate,
      Frequency frequency) {

    ArgChecker.notNull(effectiveDate, "effective date");
    ArgChecker.notNull(maturityDate, "maturity date");
    ArgChecker.notNull(frequency, "frequency");
    if (effectiveDate.isAfter(maturityDate)) {
      throw new IllegalArgumentException("Effective date was after maturity");
    }

    Period period = frequency.getPeriod();
    List<ZonedDateTime> dates = new ArrayList<>();
    ZonedDateTime date = maturityDate;

    // TODO review the tolerance given
    while (date.isAfter(effectiveDate) && DateUtils.getExactDaysBetween(effectiveDate, date) > 4.0) {
      dates.add(date);
      date = date.minus(period);
    }

    Collections.sort(dates);
    return dates.toArray(EMPTY_ARRAY);
  }

  private static boolean isWithinSwapLifetime(ZonedDateTime date, ZonedDateTime maturity) {
    // TODO change me urgently
    if (date.isBefore(maturity)) {
      return true;
    }
    if (DateUtils.getDaysBetween(date, maturity) < 7) {
      return true;
    }
    return false;
  }

  public static ZonedDateTime[] getAdjustedDateSchedule(
      ZonedDateTime[] dates,
      BusinessDayConvention convention,
      HolidayCalendar calendar) {

    return getAdjustedDateSchedule(dates, convention, calendar, 0);
  }

  /**
   * Return the dates adjusted by a certain number of business days.
   * @param dates The initial dates.
   * @param convention The business day convention.
   * @param calendar The calendar.
   * @param settlementDays The number of days of the adjustment. Can be negative or positive.
   * @return The adjusted dates.
   */
  public static ZonedDateTime[] getAdjustedDateSchedule(
      ZonedDateTime[] dates,
      BusinessDayConvention convention,
      HolidayCalendar calendar,
      int settlementDays) {

    ArgChecker.notEmpty(dates, "dates");
    ArgChecker.notNull(convention, "convention");
    ArgChecker.notNull(calendar, "calendar");
    int n = dates.length;
    ZonedDateTime[] result = new ZonedDateTime[n];
    for (int i = 0; i < n; i++) {
      ZonedDateTime date = applyConvention(convention, dates[i], calendar);
      if (settlementDays > 0) {
        for (int loopday = 0; loopday < settlementDays; loopday++) {
          date = date.plusDays(1);
          while (!calendar.isBusinessDay(date.toLocalDate())) {
            date = date.plusDays(1);
          }
        }
      } else {
        for (int loopday = 0; loopday < -settlementDays; loopday++) {
          date = date.minusDays(1);
          while (!calendar.isBusinessDay(date.toLocalDate())) {
            date = date.minusDays(1);
          }
        }
      }
      result[i] = date;
    }
    return result;
  }

  public static ZonedDateTime getAdjustedDate(
      ZonedDateTime originalDate,
      BusinessDayConvention convention,
      HolidayCalendar calendar,
      int offset) {

    ArgChecker.notNull(originalDate, "date");
    ArgChecker.notNull(convention, "convention");
    ArgChecker.notNull(calendar, "calendar");

    ZonedDateTime date = applyConvention(convention, originalDate, calendar);
    if (offset > 0) {
      for (int loopday = 0; loopday < offset; loopday++) {
        date = date.plusDays(1);
        while (!calendar.isBusinessDay(date.toLocalDate())) {
          date = date.plusDays(1);
        }
      }
    } else {
      for (int loopday = 0; loopday < -offset; loopday++) {
        date = date.minusDays(1);
        while (!calendar.isBusinessDay(date.toLocalDate())) {
          date = date.minusDays(1);
        }
      }
    }
    return date;
  }

  /**
   * Construct an array of dates according the a start date, an end date, the period between dates and the conventions.
   * The start date is not included in the array. The date are constructed forward and the stub period, if any, is last.
   * The end date is always included in the schedule.
   * @param startDate The reference initial date for the construction.
   * @param endDate The end date. Usually unadjusted.
   * @param period The period between payments.
   * @param businessDayConvention The business day convention.
   * @param calendar The applicable calendar.
   * @param isEOM The end-of-month rule flag.
   * @param stubShort Flag indicating if the stub, if any, is short (true) or long (false).
   * @return The array of dates.
   */
  public static ZonedDateTime[] getAdjustedDateSchedule(
      ZonedDateTime startDate,
      ZonedDateTime endDate,
      Period period,
      BusinessDayConvention businessDayConvention,
      HolidayCalendar calendar,
      boolean isEOM,
      boolean stubShort) {

    boolean eomApply = false;
    if (isEOM) {
      BusinessDayConvention following = BusinessDayConventions.FOLLOWING;
      eomApply = (following.adjust(startDate.toLocalDate().plusDays(1), calendar).getMonth() != startDate.getMonth());
    }
    // When the end-of-month rule applies and the start date is on month-end, the dates are the last business day of the month.
    BusinessDayConvention actualBDC;
    List<ZonedDateTime> adjustedDates = new ArrayList<>();
    ZonedDateTime date = startDate;
    if (eomApply) {
      actualBDC = BusinessDayConventions.PRECEDING; //To ensure that the date stays in the current month.
      date = date.plus(period).with(TemporalAdjusters.lastDayOfMonth());
      while (date.isBefore(endDate)) { // date is strictly before endDate
        adjustedDates.add(applyConvention(actualBDC, date, calendar));
        date = date.plus(period).with(TemporalAdjusters.lastDayOfMonth());
      }
    } else {
      actualBDC = businessDayConvention;
      date = date.plus(period);
      while (date.isBefore(endDate)) { // date is strictly before endDate
        adjustedDates.add(applyConvention(businessDayConvention, date, calendar));
        date = date.plus(period);
      }
    }
    // For long stub the last date before end date, if any, is removed.
    if (!stubShort && adjustedDates.size() >= 1) {
      adjustedDates.remove(adjustedDates.size() - 1);
    }
    adjustedDates.add(applyConvention(actualBDC, endDate, calendar)); // the end date
    return adjustedDates.toArray(EMPTY_ARRAY);
  }

  public static ZonedDateTime[] getAdjustedDateSchedule(
      ZonedDateTime startDate,
      ZonedDateTime endDate,
      Frequency frequency,
      BusinessDayConvention businessDayConvention,
      HolidayCalendar calendar,
      boolean isEOM) {

    Period period = frequency.getPeriod();
    return getAdjustedDateSchedule(startDate, endDate, period, businessDayConvention, calendar, isEOM, true);
  }

  /**
   * Construct an array of dates according the a start date, an end date, the period between dates and the conventions.
   * The start date is not included in the array. The date are constructed forward and the stub period, if any, is last
   * and short. The end date is always included in the schedule.
   * @param startDate The reference initial date for the construction.
   * @param endDate The end date. Usually unadjusted.
   * @param period The period between payments.
   * @param businessDayConvention The business day convention.
   * @param calendar The applicable calendar.
   * @param isEOM The end-of-month rule flag.
   * @return The array of dates.
   */
  public static ZonedDateTime[] getAdjustedDateSchedule(
      ZonedDateTime startDate,
      ZonedDateTime endDate,
      Period period,
      BusinessDayConvention businessDayConvention,
      HolidayCalendar calendar,
      boolean isEOM) {

    return getAdjustedDateSchedule(startDate, endDate, period, businessDayConvention, calendar, isEOM, true);
  }

  /**
   * Construct an array of dates according the a start date, an end date, the period between dates and the conventions.
   * The start date is not included in the array. The date are constructed forward and the stub period, if any, is last.
   * The end date is always included in the schedule.
   * @param startDate The reference initial date for the construction.
   * @param tenor The annuity tenor.
   * @param period The period between payments.
   * @param businessDayConvention The business day convention.
   * @param calendar The applicable calendar.
   * @param isEOM The end-of-month rule flag.
   * @param shortStub Flag indicating if the stub, if any, is short (true) or long (false).
   * @return The array of dates.
   */
  public static ZonedDateTime[] getAdjustedDateSchedule(
      ZonedDateTime startDate,
      Period tenor,
      Period period,
      BusinessDayConvention businessDayConvention,
      HolidayCalendar calendar,
      boolean isEOM,
      boolean shortStub) {

    ZonedDateTime endDate = startDate.plus(tenor);
    return getAdjustedDateSchedule(startDate, endDate, period, businessDayConvention, calendar, isEOM, shortStub);
  }

  /**
   * Construct an array of dates according the a start date, an end date, the period between dates and the conventions.
   * The start date is not included in the array. The date are constructed forward and the stub period, if any, is short
   * and last. The end date is always included in the schedule.
   * @param startDate The reference initial date for the construction.
   * @param tenorAnnuity The annuity tenor.
   * @param periodPayments The period between payments.
   * @param businessDayConvention The business day convention.
   * @param calendar The applicable calendar.
   * @param isEOM The end-of-month rule flag.
   * @return The array of dates.
   */
  public static ZonedDateTime[] getAdjustedDateSchedule(
      ZonedDateTime startDate,
      Period tenorAnnuity,
      Period periodPayments,
      BusinessDayConvention businessDayConvention,
      HolidayCalendar calendar,
      boolean isEOM) {

    ZonedDateTime endDate = startDate.plus(tenorAnnuity);
    return getAdjustedDateSchedule(startDate, endDate, periodPayments, businessDayConvention, calendar, isEOM, true);
  }

  public static ZonedDateTime[] getSettlementDateSchedule(
      ZonedDateTime[] dates,
      HolidayCalendar calendar,
      BusinessDayConvention businessDayConvention,
      int settlementDays) {

    ArgChecker.notEmpty(dates, "dates");
    ArgChecker.notNull(calendar, "calendar");
    int n = dates.length;
    ZonedDateTime[] result = new ZonedDateTime[n];
    for (int i = 0; i < n; i++) {
      ZonedDateTime date = applyConvention(businessDayConvention, dates[i].plusDays(1), calendar);
      for (int j = 0; j < settlementDays; j++) {
        date = applyConvention(businessDayConvention, date.plusDays(1), calendar);
      }
      result[i] = date;
    }
    return result;
  }

  public static LocalDate[] getSettlementDateSchedule(
      LocalDate[] dates,
      HolidayCalendar calendar,
      BusinessDayConvention businessDayConvention,
      int settlementDays) {

    ArgChecker.notEmpty(dates, "dates");
    ArgChecker.notNull(calendar, "calendar");
    int n = dates.length;
    LocalDate[] result = new LocalDate[n];
    for (int i = 0; i < n; i++) {
      LocalDate date = businessDayConvention.adjust(dates[i].plusDays(1), calendar);
      for (int j = 0; j < settlementDays; j++) {
        date = businessDayConvention.adjust(date.plusDays(1), calendar);
      }
      result[i] = date;
    }
    return result;
  }

  public static ZonedDateTime[] getAdjustedResetDateSchedule(
      ZonedDateTime effectiveDate,
      ZonedDateTime[] dates,
      BusinessDayConvention convention,
      HolidayCalendar calendar,
      int settlementDays) {

    ArgChecker.notNull(effectiveDate, "effective date");
    ArgChecker.notEmpty(dates, "dates");
    ArgChecker.notNull(convention, "convention");
    ArgChecker.notNull(calendar, "calendar");

    int n = dates.length;
    ZonedDateTime[] result = new ZonedDateTime[n];
    result[0] = effectiveDate;
    for (int i = 1; i < n; i++) {
      result[i] = applyConvention(convention, dates[i - 1].minusDays(settlementDays), calendar);
    }
    return result;
  }

  public static ZonedDateTime[] getAdjustedMaturityDateSchedule(
      ZonedDateTime effectiveDate,
      ZonedDateTime[] dates,
      BusinessDayConvention convention,
      HolidayCalendar calendar,
      Frequency frequency) {

    ArgChecker.notEmpty(dates, "dates");
    ArgChecker.notNull(convention, "convention");
    ArgChecker.notNull(calendar, "calendar");
    ArgChecker.notNull(frequency, "frequency");

    Period period = frequency.getPeriod();
    int n = dates.length;
    ZonedDateTime[] results = new ZonedDateTime[n];
    results[0] = effectiveDate.plus(period);
    for (int i = 1; i < n; i++) {
      // TODO need to further shift these dates by a convention
      results[i] = applyConvention(convention, dates[i - 1].plus(period), calendar);
    }
    return results;
  }

  /**
   * Converts a set of dates into time periods in years for a specified date and using a specified day count convention.
   *
   * @param dates  a set of dates, not null
   * @param dayCount  the day count convention, not null
   * @param fromDate  the date from which to measure the time period to the dates, not null
   * @return a double array of time periods (in years) -
   *  if a date is <b>before</b> the fromDate as negative value is returned, not null
   */
  public static double[] getTimes(ZonedDateTime[] dates, DayCount dayCount, ZonedDateTime fromDate) {
    ArgChecker.notEmpty(dates, "dates");
    ArgChecker.notNull(dayCount, "day count");
    ArgChecker.notNull(fromDate, "from date");

    int n = dates.length;
    double[] result = new double[n];
    double yearFrac;
    for (int i = 0; i < (n); i++) {
      if (dates[i].isAfter(fromDate)) {
        yearFrac = DayCountUtils.yearFraction(dayCount, fromDate, dates[i]);
      } else {
        yearFrac = -DayCountUtils.yearFraction(dayCount, dates[i], fromDate);
      }
      result[i] = yearFrac;
    }

    return result;
  }

  public static int numberOfNegativeValues(double[] periods) {
    int count = 0;
    for (double period : periods) {
      if (period < 0.0) {
        count++;
      }
    }
    return count;
  }

  public static double[] removeFirstNValues(double[] data, int n) {
    return Arrays.copyOfRange(data, n, data.length);
  }

  public static double[] getYearFractions(
      ZonedDateTime[] dates,
      DayCount dayCount,
      ZonedDateTime fromDate) {

    ArgChecker.notEmpty(dates, "dates");
    ArgChecker.notNull(dayCount, "day count");
    ArgChecker.notNull(fromDate, "from date");
    int n = dates.length;
    double[] result = new double[n];
    result[0] = DayCountUtils.yearFraction(dayCount, fromDate, dates[0]);
    for (int i = 1; i < n; i++) {
      result[i] = DayCountUtils.yearFraction(dayCount, dates[i - 1], dates[i]);
    }
    return result;
  }

  /**
   * Generates the start dates from the specified start date and set of end dates.
   * @param startDate the first start date.
   * @param endDates the set of end dates to generate start dates from.
   * @return the start dates relative to the end dates.
   */
  public static ZonedDateTime[] getStartDates(ZonedDateTime startDate, ZonedDateTime[] endDates) {
    ZonedDateTime[] startDates = new ZonedDateTime[endDates.length];
    startDates[0] = startDate;
    System.arraycopy(endDates, 0, startDates, 1, endDates.length - 1);
    return startDates;
  }

  //-------------------------------------------------------------------------
  /**
   * Checks if the schedule is generated from the end.
   * 
   * @param stub  the stub type
   * @return true if generating from the end
   */
  private static boolean isGenerateFromEnd(StubConvention stub) {
    return stub.isCalculateBackwards();
  }

  /**
   * Checks if the EOM rule applies.
   * <p>
   * If generation occurs forwards, check if the start date is the last day of the month.
   * If generation occurs backwards, check if the end date is the last day of the month.
   * 
   * @param fromEnd  true if generating from the end backwards
   * @param startDate  the start date
   * @param endDate  the end date
   * @param calendar  the holiday calendar
   * @return true if the rule applies
   */
  private static boolean eomApplies(
      boolean fromEnd,
      ZonedDateTime startDate,
      ZonedDateTime endDate,
      HolidayCalendar calendar) {

    if (fromEnd) {
      // end-of-month rule applies if end date is on last day of month (last business day used here)
      return getAdjustedDate(endDate, 1, calendar).getMonth() != endDate.getMonth();
    } else {
      // end-of-month rule applies if start date is on last day of month (last business day used here)
      return getAdjustedDate(startDate, 1, calendar).getMonth() != startDate.getMonth();
    }
  }

}
