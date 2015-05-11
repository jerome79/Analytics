/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.util.time;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import org.testng.annotations.Test;

/**
 * The TimeCalculator computes the difference between two instants as 'Analytics time', 
 * which is actually a measure of years. This is used primarily for interest accrual and curve/surface interpolation
 */
@Test
public class TimeCalculatorTest {

  private static final double TOLERANCE = 1.0E-50;

  @Test
  /** Same instant must have no time between */
  public void sameInstant() {
    final ZonedDateTime now = ZonedDateTime.now();
    assertEquals(0.0, TimeCalculator.getTimeBetween(now, now));
  }

  @Test
  /** No time between instants on same date */
  public void sameDay() {
    final ZonedDateTime midday = LocalDate.now().atTime(LocalTime.NOON).atZone(ZoneOffset.UTC);
    final ZonedDateTime midnight = LocalDate.now().atTime(LocalTime.MIDNIGHT).atZone(ZoneOffset.UTC);
    final double yearFraction = TimeCalculator.getTimeBetween(midday, midnight);
    assertEquals(0.0, yearFraction, TOLERANCE);
  }

  @Test
  /** No time between instants on same date */
  public void sameDay2() {
    final ZonedDateTime midday = LocalDate.now().atTime(LocalTime.NOON).atZone(ZoneOffset.UTC);
    final ZonedDateTime midnight = LocalDate.now().atTime(LocalTime.MIDNIGHT).atZone(ZoneOffset.UTC);
    final double yearFraction = TimeCalculator.getTimeBetween(midnight, midday);
    assertEquals(0.0, yearFraction, TOLERANCE);
  }

  @Test
  /** Time between same instants but specified under time zones that fall on different days.
      This is trapped as daycount computation first converts each ZonedDateTime to LocalDate. */
  public void sameTimeDifferentLocalDates() {

    final ZonedDateTime midnightLondon = LocalDateTime.of(2012, 03, 12, 0, 0).atZone(ZoneOffset.UTC);
    final ZonedDateTime sevenNewYork = LocalDateTime.of(2012, 03, 11, 19, 0).atZone(ZoneId.of("EST", ZoneId.SHORT_IDS));
    assertTrue(midnightLondon.isEqual(sevenNewYork));
    final double yearFraction = TimeCalculator.getTimeBetween(sevenNewYork, midnightLondon);
    assertEquals(0.0, yearFraction, TOLERANCE);
  }

  @Test
  /** Time between same instants but specified under time zones that fall on different days.
      This is trapped as daycount computation first converts each ZonedDateTime to LocalDate. */
  public void sameTimeDifferentLocalDates2() {
    final ZonedDateTime date1 = LocalDateTime.of(2013, 9, 24, 0, 0).atZone(ZoneId.of("Europe/London")); // 2013-09-24T00:00+01:00[Europe/London]
    final ZonedDateTime date2 = LocalDateTime.of(2013, 9, 24, 9, 2, 45, 936000000).atZone(ZoneOffset.UTC);
    final double yearFraction = TimeCalculator.getTimeBetween(date1, date2);
    assertEquals("TimeCalculator", 0.0, yearFraction, TOLERANCE);
  }

  @Test
  /** Time between normal days (in a non leap year) */
  public void normal() {
    final ZonedDateTime date1 = DateUtils.getUTCDate(2010, 8, 18);
    final ZonedDateTime date2 = DateUtils.getUTCDate(2010, 8, 21);
    final double time = TimeCalculator.getTimeBetween(date1, date2);
    final double timeExpected = 3.0 / 365.0;
    assertEquals("TimeCalculator: normal days", timeExpected, time, TOLERANCE);
  }

  @Test
  /** Time between arrays */
  public void array() {
    final ZonedDateTime date1 = DateUtils.getUTCDate(2010, 8, 18);
    final ZonedDateTime date2 = DateUtils.getUTCDate(2010, 8, 21);
    final ZonedDateTime[] dateArray1 = new ZonedDateTime[] {date1, date2 };
    final ZonedDateTime[] dateArray2 = new ZonedDateTime[] {date2, date1 };
    final double[] timeCalculated = TimeCalculator.getTimeBetween(dateArray1, dateArray2);
    final double timeExpected = 3.0 / 365.0;
    assertArrayEquals("TimeCalculator: normal days array", new double[] {timeExpected, -timeExpected }, timeCalculated, TOLERANCE);
    final double[] timeCalculated2 = TimeCalculator.getTimeBetween(date1, dateArray2);
    assertArrayEquals("TimeCalculator: normal days array", new double[] {timeExpected, 0.0 }, timeCalculated2, TOLERANCE);
  }

}
