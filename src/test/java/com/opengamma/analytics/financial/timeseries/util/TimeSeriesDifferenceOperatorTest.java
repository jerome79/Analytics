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
 * Tests {@link TimeSeriesDifferenceOperator}
 */
public class TimeSeriesDifferenceOperatorTest {

  private static final TimeSeriesDifferenceOperator OP_DIF_1 = new TimeSeriesDifferenceOperator();
  private static final TimeSeriesDifferenceOperator OP_DIF_2 = new TimeSeriesDifferenceOperator(2);

  private static final double TOLERANCE_DIFF = 1.0E-10;

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullTsException() {
    OP_DIF_1.apply(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void tooShortTimeSeriesException() {
    LocalDateDoubleTimeSeries tooShortTs =
        LocalDateDoubleTimeSeries.builder()
            .put(LocalDate.of(2014, 1, 2), 1.0)
            .build();
    OP_DIF_1.apply(tooShortTs);
  }

  /**
   * Test the difference operator for a standard lag of 1 element.
   */
  @Test
  public void difference1() {
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
            .put(LocalDate.of(2014, 1, 3), 1)
            .put(LocalDate.of(2014, 1, 4), 3d)
            .put(LocalDate.of(2014, 1, 5), -1d)
            .put(LocalDate.of(2014, 1, 6), 4d)
            .put(LocalDate.of(2014, 1, 7), -6d)
            .build();

    LocalDateDoubleTimeSeries returned = OP_DIF_1.apply(ts);

    compareTimeseries(returned, expected, TOLERANCE_DIFF);
  }

  /** Tests the difference operator for a lag of 2 elements. */
  @Test
  public void difference2() {

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
            .put(LocalDate.of(2014, 1, 5), 2d)
            .put(LocalDate.of(2014, 1, 6), 3d)
            .put(LocalDate.of(2014, 1, 7), -2d)
            .build();

    LocalDateDoubleTimeSeries returned = OP_DIF_2.apply(ts);

    compareTimeseries(returned, expected, TOLERANCE_DIFF);
  }

}
