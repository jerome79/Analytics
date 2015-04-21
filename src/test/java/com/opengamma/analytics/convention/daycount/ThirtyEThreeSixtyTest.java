/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.convention.daycount;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;



/**
 * Test ThirtyEThreeSixty.
 */
@Test
public class ThirtyEThreeSixtyTest extends DayCountTestCase {

  private static final ThirtyEThreeSixty DC = new ThirtyEThreeSixty();

  @Override
  protected DayCount getDayCount() {
    return DC;
  }

  @Test
  public void test() {
    assertEquals(COUPON * DC.yearFraction(D1, D2), DC.getAccruedInterest(D1, D2, D3, COUPON, PAYMENTS), 0);
    assertEquals(DC.getName(), "30E/360");
  }

}
