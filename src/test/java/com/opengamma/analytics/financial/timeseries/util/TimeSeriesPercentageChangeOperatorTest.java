/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.timeseries.util;

import static com.opengamma.analytics.financial.timeseries.util.LocalDateDoubleTimeSeriesTestUtils.compareTimeseries;

import java.time.LocalDate;

import org.testng.annotations.Test;

import com.opengamma.strata.collect.timeseries.LocalDateDoubleTimeSeries;

/**
 * Tests {@link TimeSeriesPercentageChangeOperator}
 */
public class TimeSeriesPercentageChangeOperatorTest {

  private static final TimeSeriesPercentageChangeOperator OP_REL_1 = new TimeSeriesPercentageChangeOperator();
  private static final TimeSeriesPercentageChangeOperator OP_REL_2 = new TimeSeriesPercentageChangeOperator(2);

  private static final double TOLERANCE_DIFF = 1.0E-10;

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullTsException() {
    OP_REL_1.apply(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void tooShortTimeSeriesException() {
    LocalDateDoubleTimeSeries tooShortTs =
        LocalDateDoubleTimeSeries.builder()
            .put(LocalDate.of(2014, 1, 2), 1d)
            .build();
    OP_REL_1.apply(tooShortTs);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void zeroValue() {
    LocalDateDoubleTimeSeries zeroValueTs =
        LocalDateDoubleTimeSeries.builder()
            .put(LocalDate.of(2014, 1, 2), 1d)
            .put(LocalDate.of(2014, 1, 3), 0d)
            .put(LocalDate.of(2014, 1, 4), 1d)
            .build();
    OP_REL_1.apply(zeroValueTs);
  }

  /** Test the relative change operator for a standard lag of 1 element. */
  @Test
  public void relative1() {

    LocalDateDoubleTimeSeries ts =
        LocalDateDoubleTimeSeries.builder()
            .put(LocalDate.of(2014, 1, 2), 1d)
            .put(LocalDate.of(2014, 1, 3), 2d)
            .put(LocalDate.of(2014, 1, 4), 5d)
            .put(LocalDate.of(2014, 1, 5), 4d)
            .put(LocalDate.of(2014, 1, 6), 8d)
            .put(LocalDate.of(2014, 1, 7), 2d)
            .build();

    LocalDateDoubleTimeSeries expected =
        LocalDateDoubleTimeSeries.builder()
            .put(LocalDate.of(2014, 1, 3), 1d)
            .put(LocalDate.of(2014, 1, 4), 1.5)
            .put(LocalDate.of(2014, 1, 5), -0.2)
            .put(LocalDate.of(2014, 1, 6), 1d)
            .put(LocalDate.of(2014, 1, 7), -0.75)
            .build();

    LocalDateDoubleTimeSeries returned = OP_REL_1.apply(ts);

    compareTimeseries(returned, expected, TOLERANCE_DIFF);
  }

  /** Tests the relative change operator for a lag of 2 elements. */
  @Test
  public void relative2() {

    LocalDateDoubleTimeSeries ts =
        LocalDateDoubleTimeSeries.builder()
            .put(LocalDate.of(2014, 1, 2), 1d)
            .put(LocalDate.of(2014, 1, 3), 2d)
            .put(LocalDate.of(2014, 1, 4), 5d)
            .put(LocalDate.of(2014, 1, 5), 4d)
            .put(LocalDate.of(2014, 1, 6), 8d)
            .put(LocalDate.of(2014, 1, 7), 2d)
            .build();

    LocalDateDoubleTimeSeries expected =
        LocalDateDoubleTimeSeries.builder()
            .put(LocalDate.of(2014, 1, 4), 4d)
            .put(LocalDate.of(2014, 1, 5), 1d)
            .put(LocalDate.of(2014, 1, 6), 0.6)
            .put(LocalDate.of(2014, 1, 7), -0.5)
            .build();

    LocalDateDoubleTimeSeries returned = OP_REL_2.apply(ts);

    compareTimeseries(returned, expected, TOLERANCE_DIFF);
  }
  
}
