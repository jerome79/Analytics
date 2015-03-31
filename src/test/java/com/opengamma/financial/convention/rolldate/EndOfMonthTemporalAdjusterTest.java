/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.rolldate;

import static org.testng.AssertJUnit.assertEquals;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjuster;

import org.testng.annotations.Test;

/**
 *  Test for the end of month temporal adjuster
 */
@Test
public class EndOfMonthTemporalAdjusterTest {
  private static final TemporalAdjuster ADJUSTER = EndOfMonthTemporalAdjuster.getAdjuster();

  @Test
  public void test() {
    for (int i = 1; i < 30; i++) {
      final LocalDate date = LocalDate.of(2013, 1, i);
      assertEquals(LocalDate.of(2013, 1, 31), ADJUSTER.adjustInto(date));
    }
  }

}
