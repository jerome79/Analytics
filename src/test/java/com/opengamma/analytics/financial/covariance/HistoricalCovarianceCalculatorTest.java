/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.covariance;

import static java.time.ZoneOffset.UTC;
import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.util.timeseries.DoubleTimeSeries;
import com.opengamma.analytics.util.timeseries.zdt.ImmutableZonedDateTimeDoubleTimeSeries;

/**
 * Test.
 */
@Test
public class HistoricalCovarianceCalculatorTest {
  private static final DoubleTimeSeries<?> TS1 = ImmutableZonedDateTimeDoubleTimeSeries.of(new long[] {1, 2, 3, 4, 5 }, new double[] {1, 1, 1, 1, 1 }, UTC);
  private static final CovarianceCalculator CALCULATOR = new HistoricalCovarianceCalculator();

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullArray() {
    CALCULATOR.apply(null, TS1);
  }

  @Test
  public void test() {
    final double n = TS1.size();
    final double covariance = CALCULATOR.apply(TS1, TS1);
    assertEquals(covariance, n / (n - 1) - 1, 1e-9);
  }
}
