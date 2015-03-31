/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.covariance;

import static java.time.ZoneOffset.UTC;

import org.testng.annotations.Test;

import com.opengamma.analytics.util.timeseries.DoubleTimeSeries;
import com.opengamma.analytics.util.timeseries.zdt.ImmutableZonedDateTimeDoubleTimeSeries;


/**
 * Test.
 */
@Test
public class CovarianceCalculatorTest {
  private static final CovarianceCalculator CALCULATOR = new CovarianceCalculator() {

    @Override
    public Double apply(DoubleTimeSeries<?> ts1, DoubleTimeSeries<?> ts2) {
      return null;
    }

  };
  private static final DoubleTimeSeries<?> TS1 = ImmutableZonedDateTimeDoubleTimeSeries.of(new long[]{2}, new double[]{1}, UTC);
  private static final DoubleTimeSeries<?> TS2 = ImmutableZonedDateTimeDoubleTimeSeries.of(new long[] {2, 3}, new double[] {1, 2}, UTC);
  private static final DoubleTimeSeries<?> TS3 = ImmutableZonedDateTimeDoubleTimeSeries.of(new long[] {4, 5}, new double[] {1, 2}, UTC);
  private static final DoubleTimeSeries<?> TS4 = ImmutableZonedDateTimeDoubleTimeSeries.of(new long[] {4, 5, 6}, new double[] {1, 2, 3}, UTC);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullTS1() {
    CALCULATOR.testTimeSeries(null, TS1);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullTS2() {
    CALCULATOR.testTimeSeries(TS2, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmptyTS1() {
    CALCULATOR.testTimeSeries(ImmutableZonedDateTimeDoubleTimeSeries.ofEmptyUTC(), TS1);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmptyTS2() {
    CALCULATOR.testTimeSeries(TS2, ImmutableZonedDateTimeDoubleTimeSeries.ofEmptyUTC());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testSmallTS1() {
    CALCULATOR.testTimeSeries(TS1, TS2);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testSmallTS2() {
    CALCULATOR.testTimeSeries(TS2, TS1);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testDifferentLength() {
    CALCULATOR.testTimeSeries(TS2, TS4);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testDifferentDates() {
    CALCULATOR.testTimeSeries(TS2, TS3);
  }
}
