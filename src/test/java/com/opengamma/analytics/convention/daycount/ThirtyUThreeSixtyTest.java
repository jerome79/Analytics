/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.convention.daycount;

import static org.testng.AssertJUnit.assertEquals;

import java.time.LocalDate;

import org.testng.annotations.Test;

/**
 * Test ThirtyUThreeSixty.
 */
@Test
public class ThirtyUThreeSixtyTest extends DayCountTestCase {

  private static final ThirtyUThreeSixty DC = new ThirtyUThreeSixty();

  @Override
  protected DayCount getDayCount() {
    return DC;
  }

  @Test
  public void test() {
    assertEquals(COUPON * DayCountUtils.yearFraction(DC, D1, D2), DC.getAccruedInterest(D1, D2, D3, COUPON, PAYMENTS), 0);
    assertEquals(DC.getName(), "30U/360");
    final LocalDate d1 = LocalDate.of(2012, 7, 2);
    final LocalDate d2 = LocalDate.of(2012, 7, 28);
    final LocalDate d3 = LocalDate.of(2012, 8, 2);
    final LocalDate d4 = LocalDate.of(2012, 8, 28);
    final LocalDate d5 = LocalDate.of(2013, 8, 2);
    final LocalDate d6 = LocalDate.of(2013, 8, 28);
    final LocalDate d7 = LocalDate.of(2012, 6, 29);
    final LocalDate d8 = LocalDate.of(2012, 7, 31);
    final LocalDate d9 = LocalDate.of(2012, 8, 31);
    final LocalDate d10 = LocalDate.of(2013, 8, 31);
    final LocalDate d11 = LocalDate.of(2014, 1, 31);
    assertEquals(26. / 360, DC.yearFraction(d1, d2), 0);
    assertEquals(30. / 360, DC.yearFraction(d1, d3), 0);
    assertEquals(56. / 360, DC.yearFraction(d1, d4), 0);
    assertEquals(1 + 30. / 360, DC.yearFraction(d1, d5), 0);
    assertEquals(1 + 56. / 360, DC.yearFraction(d1, d6), 0);
    assertEquals(29. / 360, DC.yearFraction(d7, d2), 0);
    assertEquals(33. / 360, DC.yearFraction(d7, d3), 0);
    assertEquals(59. / 360, DC.yearFraction(d7, d4), 0);
    assertEquals(1 + 33. / 360, DC.yearFraction(d7, d5), 0);
    assertEquals(1 + 59. / 360, DC.yearFraction(d7, d6), 0);
    assertEquals(29. / 360, DC.yearFraction(d1, d8), 0);
    assertEquals(32. / 360, DC.yearFraction(d7, d8), 0);
    assertEquals(59. / 360, DC.yearFraction(d1, d9), 0);
    assertEquals(62. / 360, DC.yearFraction(d7, d9), 0);
    assertEquals(1 + 59. / 360, DC.yearFraction(d1, d10), 0);
    assertEquals(1 + 62. / 360, DC.yearFraction(d7, d10), 0);
    assertEquals(150. / 360, DC.yearFraction(d10, d11), 0);
  }

}
