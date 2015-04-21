/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.convention.daycount;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;



/**
 * Test ActualThreeSixtyFive.
 */
@Test
public class ActualThreeSixtyFiveTest extends DayCountTestCase {

  private static final ActualThreeSixtyFive DC = new ActualThreeSixtyFive();

  @Override
  protected DayCount getDayCount() {
    return DC;
  }

  @Test
  public void test() {
    assertEquals(COUPON * DC.yearFraction(D1, D2), DC.getAccruedInterest(D1, D2, D3, COUPON, PAYMENTS), 0);
    assertEquals(DC.getName(), "Actual/365");
  }

}
