/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.convention.daycount;

import static java.time.DayOfWeek.SATURDAY;
import static java.time.DayOfWeek.SUNDAY;
import static org.testng.Assert.assertEquals;

import java.time.LocalDate;
import java.time.ZonedDateTime;

import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;
import com.opengamma.analytics.util.time.DateUtils;
import com.opengamma.strata.basics.date.HolidayCalendar;
import com.opengamma.strata.basics.date.HolidayCalendars;
import com.opengamma.strata.basics.date.ImmutableHolidayCalendar;

/**
 * Test.
 */
@Test
public class BusinessTwoFiveTwoTest {
  private static final ZonedDateTime D1 = DateUtils.getUTCDate(2010, 1, 1);
  private static final ZonedDateTime D2 = DateUtils.getUTCDate(2010, 4, 1);
  private static final ZonedDateTime D3 = DateUtils.getUTCDate(2010, 7, 1);
  private static final LocalDate D4 = LocalDate.of(2010, 1, 1);
  private static final LocalDate D5 = LocalDate.of(2010, 4, 1);
  private static final LocalDate D6 = LocalDate.of(2010, 7, 1);
  private static final double COUPON = 0.01;
  private static final int PAYMENTS = 4;
  private static final HolidayCalendar WEEKEND_CALENDAR = HolidayCalendars.SAT_SUN;
  private static final HolidayCalendar HOLIDAY_CALENDAR = ImmutableHolidayCalendar.of(
      "Holiday", ImmutableList.of(LocalDate.of(2012, 7, 19), LocalDate.of(2012, 7, 26)), SATURDAY, SUNDAY);
  private static final BusinessTwoFiveTwo DC = new BusinessTwoFiveTwo();

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullFirstDate1() {
    DC.yearFraction(null, D2, WEEKEND_CALENDAR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSecondDate1() {
    DC.yearFraction(D1, null, WEEKEND_CALENDAR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongOrder1() {
    DC.yearFraction(D2, D1, WEEKEND_CALENDAR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullFirstDate2() {
    DC.yearFraction(null, D5, WEEKEND_CALENDAR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSecondDate2() {
    DC.yearFraction(D4, null, WEEKEND_CALENDAR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongOrder2() {
    DC.yearFraction(D5, D4, WEEKEND_CALENDAR);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testNoCalendar1() {
    DC.yearFraction(D4, D5);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testNoCalendar2() {
    DC.yearFraction(D1, D3);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCalendar1() {
    DC.yearFraction(D4, D5, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCalendar2() {
    DC.yearFraction(D1, D2, null);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testAccruedInterest() {
    DC.getAccruedInterest(D4, D5, D6, COUPON, PAYMENTS);
  }

  @Test
  public void test() {
    assertEquals(DC.getName(), "Business/252");
    final LocalDate d1 = LocalDate.of(2012, 7, 16);
    final LocalDate d2 = LocalDate.of(2012, 7, 17);
    final LocalDate d3 = LocalDate.of(2012, 7, 23);
    final LocalDate d4 = LocalDate.of(2012, 7, 31);
    final LocalDate d5 = LocalDate.of(2012, 7, 29);
    final LocalDate d6 = LocalDate.of(2012, 7, 14);
    final LocalDate d7 = LocalDate.of(2012, 7, 26);
    assertEquals(1. / 252, DC.yearFraction(d1, d2, WEEKEND_CALENDAR), 0);
    assertEquals(1. / 252, DC.yearFraction(d1, d2, HOLIDAY_CALENDAR), 0);
    assertEquals(5. / 252, DC.yearFraction(d1, d3, WEEKEND_CALENDAR), 0);
    assertEquals(4. / 252, DC.yearFraction(d1, d3, HOLIDAY_CALENDAR), 0);
    assertEquals(11. / 252, DC.yearFraction(d1, d4, WEEKEND_CALENDAR), 0);
    assertEquals(9. / 252, DC.yearFraction(d1, d4, HOLIDAY_CALENDAR), 0);
    assertEquals(10. / 252, DC.yearFraction(d1, d5, WEEKEND_CALENDAR), 0);
    assertEquals(8. / 252, DC.yearFraction(d1, d5, HOLIDAY_CALENDAR), 0);
    assertEquals(1. / 252, DC.yearFraction(d6, d2, WEEKEND_CALENDAR), 0);
    assertEquals(1. / 252, DC.yearFraction(d6, d2, HOLIDAY_CALENDAR), 0);
    assertEquals(5. / 252, DC.yearFraction(d6, d3, WEEKEND_CALENDAR), 0);
    assertEquals(4. / 252, DC.yearFraction(d6, d3, HOLIDAY_CALENDAR), 0);
    assertEquals(11. / 252, DC.yearFraction(d6, d4, WEEKEND_CALENDAR), 0);
    assertEquals(9. / 252, DC.yearFraction(d6, d4, HOLIDAY_CALENDAR), 0);
    assertEquals(10. / 252, DC.yearFraction(d6, d5, WEEKEND_CALENDAR), 0);
    assertEquals(8. / 252, DC.yearFraction(d6, d5, HOLIDAY_CALENDAR), 0);
    assertEquals(8. / 252, DC.yearFraction(d1, d7, WEEKEND_CALENDAR), 0);
    assertEquals(7. / 252, DC.yearFraction(d1, d7, HOLIDAY_CALENDAR), 0);
  }

  /**
   * Any day count with a fixed denominator should obey the additivity rule DCC(d1,d2) = DCC(d1,d) + DCC(d,d2) for all
   * d1 <= d <= d2
   */
  @Test
  public void additivityTest() {
    LocalDate d1 = LocalDate.of(2014, 7, 16);
    LocalDate d2 = LocalDate.of(2014, 8, 17);

    double yf = DC.yearFraction(d1, d2, WEEKEND_CALENDAR);
    LocalDate d = d1;
    while (!d.isAfter(d2)) {
      assertEquals(yf, DC.yearFraction(d1, d, WEEKEND_CALENDAR) + DC.yearFraction(d, d2, WEEKEND_CALENDAR), 1e-15);
      d = d.plusDays(1);
    }
  }

}
