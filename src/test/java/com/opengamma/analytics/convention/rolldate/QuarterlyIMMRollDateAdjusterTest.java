/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.convention.rolldate;

import static org.testng.AssertJUnit.assertEquals;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjuster;

import org.testng.annotations.Test;

/**
 * Test.
 */
@Test
public class QuarterlyIMMRollDateAdjusterTest {
  private static final TemporalAdjuster ADJUSTER = QuarterlyIMMRollDateAdjuster.getAdjuster();

  @Test
  public void testFirstQuarter() {
    LocalDate date = LocalDate.of(2013, 1, 1);
    final LocalDate immDate = LocalDate.of(2013, 3, 20);
    while (!date.isAfter(immDate)) {
      assertEquals(immDate, ADJUSTER.adjustInto(date));
      date = date.plusDays(1);
    }
  }

  @Test
  public void testSecondQuarter() {
    LocalDate date = LocalDate.of(2013, 3, 21);
    final LocalDate immDate = LocalDate.of(2013, 6, 19);
    while (!date.isAfter(immDate)) {
      assertEquals(immDate, ADJUSTER.adjustInto(date));
      date = date.plusDays(1);
    }
  }

  @Test
  public void testThirdQuarterBefore() {
    LocalDate date = LocalDate.of(2013, 6, 20);
    final LocalDate immDate = LocalDate.of(2013, 9, 18);
    while (!date.isAfter(immDate)) {
      assertEquals(immDate, ADJUSTER.adjustInto(date));
      date = date.plusDays(1);
    }
  }

  @Test
  public void testFourthQuarter() {
    LocalDate date = LocalDate.of(2013, 9, 19);
    final LocalDate immDate = LocalDate.of(2013, 12, 18);
    while (!date.isAfter(immDate)) {
      assertEquals(immDate, ADJUSTER.adjustInto(date));
      date = date.plusDays(1);
    }
  }

  @Test
  public void testYearEnd() {
    LocalDate date = LocalDate.of(2013, 12, 19);
    final LocalDate endOfYear = LocalDate.of(2013, 12, 31);
    final LocalDate immDate = LocalDate.of(2014, 3, 19);
    while (!date.isAfter(endOfYear)) {
      assertEquals(immDate, ADJUSTER.adjustInto(date));
      date = date.plusDays(1);
    }
  }
}
