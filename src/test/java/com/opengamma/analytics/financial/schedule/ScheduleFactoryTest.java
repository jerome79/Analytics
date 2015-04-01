/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.schedule;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import java.time.LocalDate;
import java.time.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.analytics.util.time.DateUtils;
import com.opengamma.strata.basics.schedule.Frequency;


/**
 * Test.
 */
@Test
public class ScheduleFactoryTest {
  private static final LocalDate START1 = LocalDate.of(2000, 1, 31);
  private static final LocalDate END1 = LocalDate.of(2002, 1, 31);
  private static final ZonedDateTime START2 = DateUtils.getUTCDate(2000, 1, 31);
  private static final ZonedDateTime END2 = DateUtils.getUTCDate(2002, 1, 31);
  private static final Frequency QUARTERLY = Frequency.P3M;

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullStartDate1() {
    ScheduleFactory.getSchedule(null, END1, QUARTERLY, true, true, false);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullEndDate1() {
    ScheduleFactory.getSchedule(START1, null, QUARTERLY, true, true, false);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullFrequency1() {
    ScheduleFactory.getSchedule(START1, END1, null, true, false, false);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBadPeriodsPerYear1() {
    ScheduleFactory.getSchedule(START1, END1, 5, true, false, false);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWeeklyWithEOMAdjustment1() {
    ScheduleFactory.getSchedule(START1, END1, 52, true, true, false);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testDailyWithEOMAdjustment1() {
    ScheduleFactory.getSchedule(START1, END1, 365, true, true, false);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullStartDate2() {
    ScheduleFactory.getSchedule(null, END2, QUARTERLY, true, true, false);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullEndDate2() {
    ScheduleFactory.getSchedule(START2, null, QUARTERLY, true, true, false);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullFrequency2() {
    ScheduleFactory.getSchedule(START2, END2, null, true, false, false);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBadPeriodsPerYear2() {
    ScheduleFactory.getSchedule(START2, END2, 5, true, false, false);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWeeklyWithEOMAdjustment2() {
    ScheduleFactory.getSchedule(START2, END2, 52, true, true, false);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testDailyWithEOMAdjustment2() {
    ScheduleFactory.getSchedule(START2, END2, 365, true, true, false);
  }

  @Test
  public void testDaily() {
    final LocalDate[] schedule1 = ScheduleFactory.getSchedule(START1, END1, 365, false, true, false);
    assertArrayEquals(schedule1, ScheduleFactory.getSchedule(START1, END1, 366, false, true, false));
    assertArrayEquals(schedule1, ScheduleFactory.getSchedule(START1, END1, Frequency.P1D, false, true, false));
    assertArrayEquals(schedule1, ScheduleFactory.getSchedule(START1, END1, Frequency.P1D, false, true, false));
    assertArrayEquals(schedule1, ScheduleFactory.getSchedule(START1, END1, 365, false, true, true));
    assertArrayEquals(schedule1, ScheduleFactory.getSchedule(START1, END1, 366, false, true, true));
    assertArrayEquals(schedule1, ScheduleFactory.getSchedule(START1, END1, Frequency.P1D, false, true, true));
    assertArrayEquals(schedule1, ScheduleFactory.getSchedule(START1, END1, Frequency.P1D, false, true, true));
    assertArrayEquals(schedule1, ScheduleFactory.getSchedule(START1, END1, 365, false, false, false));
    assertArrayEquals(schedule1, ScheduleFactory.getSchedule(START1, END1, 366, false, false, false));
    assertArrayEquals(schedule1, ScheduleFactory.getSchedule(START1, END1, Frequency.P1D, false, false, false));
    assertArrayEquals(schedule1, ScheduleFactory.getSchedule(START1, END1, Frequency.P1D, false, true, false));
    assertArrayEquals(schedule1, ScheduleFactory.getSchedule(START1, END1, 365, false, false, true));
    assertArrayEquals(schedule1, ScheduleFactory.getSchedule(START1, END1, 366, false, false, true));
    assertArrayEquals(schedule1, ScheduleFactory.getSchedule(START1, END1, Frequency.P1D, false, false, true));
    assertArrayEquals(schedule1, ScheduleFactory.getSchedule(START1, END1, Frequency.P1D, false, true, true));
    final ZonedDateTime[] schedule2 = ScheduleFactory.getSchedule(START2, END2, 365, false, true, false);
    assertArrayEquals(schedule2, ScheduleFactory.getSchedule(START2, END2, 366, false, true, false));
    assertArrayEquals(schedule2, ScheduleFactory.getSchedule(START2, END2, Frequency.P1D, false, true, false));
    assertArrayEquals(schedule2, ScheduleFactory.getSchedule(START2, END2, Frequency.P1D, false, true, false));
    assertArrayEquals(schedule2, ScheduleFactory.getSchedule(START2, END2, 365, false, true, true));
    assertArrayEquals(schedule2, ScheduleFactory.getSchedule(START2, END2, 366, false, true, true));
    assertArrayEquals(schedule2, ScheduleFactory.getSchedule(START2, END2, Frequency.P1D, false, true, true));
    assertArrayEquals(schedule2, ScheduleFactory.getSchedule(START2, END2, Frequency.P1D, false, true, true));
    assertArrayEquals(schedule2, ScheduleFactory.getSchedule(START2, END2, 365, false, false, false));
    assertArrayEquals(schedule2, ScheduleFactory.getSchedule(START2, END2, 366, false, false, false));
    assertArrayEquals(schedule2, ScheduleFactory.getSchedule(START2, END2, Frequency.P1D, false, false, false));
    assertArrayEquals(schedule2, ScheduleFactory.getSchedule(START2, END2, Frequency.P1D, false, true, false));
    assertArrayEquals(schedule2, ScheduleFactory.getSchedule(START2, END2, 365, false, false, true));
    assertArrayEquals(schedule2, ScheduleFactory.getSchedule(START2, END2, 366, false, false, true));
    assertArrayEquals(schedule2, ScheduleFactory.getSchedule(START2, END2, Frequency.P1D, false, false, true));
    assertArrayEquals(schedule2, ScheduleFactory.getSchedule(START2, END2, Frequency.P1D, false, true, true));
    assertEquals(schedule1.length, schedule2.length);
    for (int i = 0; i < schedule1.length; i++) {
      assertEquals(schedule1[i], schedule2[i].toLocalDate());
    }
  }

  @Test
  public void testWeeklyBackward() {
    final LocalDate[] schedule1 = ScheduleFactory.getSchedule(START1, END1, 52, false, true, false);
    assertArrayEquals(schedule1, ScheduleFactory.getSchedule(START1, END1, Frequency.P1W, false, true, false));
    assertArrayEquals(schedule1, ScheduleFactory.getSchedule(START1, END1, Frequency.P1W, false, true, false));
    assertArrayEquals(schedule1, ScheduleFactory.getSchedule(START1, END1, 52, false, true, true));
    assertArrayEquals(schedule1, ScheduleFactory.getSchedule(START1, END1, Frequency.P1W, false, true, true));
    assertArrayEquals(schedule1, ScheduleFactory.getSchedule(START1, END1, Frequency.P1W, false, true, true));
    final LocalDate[] schedule2 = ScheduleFactory.getSchedule(START1, END1, 52, false, false, false);
    assertFalse(schedule1.equals(schedule2));
    assertArrayEquals(schedule2, ScheduleFactory.getSchedule(START1, END1, Frequency.P1W, false, false, false));
    assertArrayEquals(schedule2, ScheduleFactory.getSchedule(START1, END1, Frequency.P1W, false, false, false));
    final ZonedDateTime[] schedule3 = ScheduleFactory.getSchedule(START2, END2, 52, false, true, false);
    assertArrayEquals(schedule3, ScheduleFactory.getSchedule(START2, END2, Frequency.P1W, false, true, false));
    assertArrayEquals(schedule3, ScheduleFactory.getSchedule(START2, END2, Frequency.P1W, false, true, false));
    final ZonedDateTime[] schedule4 = ScheduleFactory.getSchedule(START2, END2, 52, false, false, false);
    assertFalse(schedule3.equals(schedule4));
    assertArrayEquals(schedule4, ScheduleFactory.getSchedule(START2, END2, 52, false, false, false));
    assertArrayEquals(schedule4, ScheduleFactory.getSchedule(START2, END2, Frequency.P1W, false, false, false));
    assertArrayEquals(schedule4, ScheduleFactory.getSchedule(START2, END2, Frequency.P1W, false, false, false));
    assertEquals(schedule1.length, schedule2.length);
    for (int i = 0; i < schedule1.length; i++) {
      assertEquals(schedule1[i], schedule3[i].toLocalDate());
    }
  }
}
