/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.var;

import static java.time.ZoneOffset.UTC;
import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.util.timeseries.DoubleTimeSeries;
import com.opengamma.analytics.util.timeseries.zdt.ImmutableZonedDateTimeDoubleTimeSeries;


/**
 * Test.
 */
@Test
public class EmpiricalDistributionVaRCalculatorTest {
  private static final double HORIZON = 10;
  private static final double PERIODS = 250;
  private static final double QUANTILE = 0.9;
  private static final EmpiricalDistributionVaRCalculator CALCULATOR = new EmpiricalDistributionVaRCalculator();
  private static final EmpiricalDistributionVaRParameters PARAMETERS = new EmpiricalDistributionVaRParameters(HORIZON, PERIODS, QUANTILE);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullTS1() {
    CALCULATOR.evaluate(PARAMETERS, (DoubleTimeSeries<?>[]) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullParameters() {
    CALCULATOR.evaluate(null, ImmutableZonedDateTimeDoubleTimeSeries.of(new long[]{1, 2}, new double[]{0.06, 0.07}, UTC));
  }
  
  @Test
  public void test() {
    final int n = 10;
    final long[] t = new long[n];
    final double[] pnl = new double[n];
    for (int i = 0; i < 10; i++) {
      t[i] = i;
      pnl[i] = i / 10. - 0.5;
    }
    final DoubleTimeSeries<?> ts = ImmutableZonedDateTimeDoubleTimeSeries.of(t, pnl, UTC);
    assertEquals(CALCULATOR.evaluate(PARAMETERS, ts).getVaRValue(), 0.082, 1e-7);
  }
}
