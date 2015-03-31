/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.daycount;

import static org.testng.AssertJUnit.assertEquals;

import java.time.ZonedDateTime;

import org.apache.commons.lang.NotImplementedException;
import org.testng.annotations.Test;

import com.opengamma.analytics.util.time.DateUtils;

/**
 * Test FlatDayCount.
 */
@Test
public class FlatDayCountTest {

  private static final FlatDayCount DC = new FlatDayCount();
  private static final ZonedDateTime D1 = DateUtils.getUTCDate(2010, 1, 1);
  private static final ZonedDateTime D2 = DateUtils.getUTCDate(2011, 1, 1);

  @Test(expectedExceptions = NotImplementedException.class)
  public void testYearFraction() {
    DC.getDayCountFraction(D1, D2);
  }

  @Test
  public void testAccruedInterest() {
    assertEquals(DC.getAccruedInterest(D1, D2, D2, 0.05, 1), 0, 0);
  }

}
