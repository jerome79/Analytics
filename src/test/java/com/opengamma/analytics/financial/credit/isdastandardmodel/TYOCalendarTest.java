/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.isdastandardmodel;

import static java.time.Month.MARCH;
import static java.time.Month.SEPTEMBER;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.time.LocalDate;

import org.testng.annotations.Test;


/**
 * Test.
 */
@Test
public class TYOCalendarTest {

  @Test
  public void test() {
    final TYOCalendar tyo = new TYOCalendar("TYO");

    assertFalse("" + LocalDate.of(2009, MARCH, 20).getDayOfWeek(), tyo.isBusinessDay(LocalDate.of(2009, MARCH, 20)));
    assertFalse(tyo.isBusinessDay(LocalDate.of(2009, MARCH, 21)));
    assertTrue(tyo.isBusinessDay(LocalDate.of(2009, MARCH, 23)));
    assertFalse(tyo.isBusinessDay(LocalDate.of(2032, SEPTEMBER, 20)));
  }

}
