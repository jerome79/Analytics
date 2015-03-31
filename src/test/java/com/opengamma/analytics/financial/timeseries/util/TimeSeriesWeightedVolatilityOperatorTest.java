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
 * Tests {@link TimeSeriesWeightedVolatilityOperator}
 */
@Test
public class TimeSeriesWeightedVolatilityOperatorTest {

  private static final LocalDateDoubleTimeSeries TS_1 =
      LocalDateDoubleTimeSeries.builder()
          .put(LocalDate.of(2014, 1, 2), 0.0024285)
          .put(LocalDate.of(2014, 1, 3), 0.0023985)
          .put(LocalDate.of(2014, 1, 6), 0.0023935)
          .put(LocalDate.of(2014, 1, 7), 0.002421)
          .put(LocalDate.of(2014, 1, 8), 0.002404)
          .put(LocalDate.of(2014, 1, 9), 0.0024165)
          .put(LocalDate.of(2014, 1, 10), 0.0024165)
          .put(LocalDate.of(2014, 1, 13), 0.002389)
          .put(LocalDate.of(2014, 1, 14), 0.0023675)
          .put(LocalDate.of(2014, 1, 15), 0.0023785)
          .put(LocalDate.of(2014, 1, 16), 0.0023635)
          .put(LocalDate.of(2014, 1, 17), 0.002366)
          .put(LocalDate.of(2014, 1, 20), 0.002371)
          .put(LocalDate.of(2014, 1, 21), 0.002366)
          .put(LocalDate.of(2014, 1, 22), 0.002371)
          .put(LocalDate.of(2014, 1, 23), 0.002386)
          .put(LocalDate.of(2014, 1, 24), 0.0023535)
          .put(LocalDate.of(2014, 1, 27), 0.002361)
          .put(LocalDate.of(2014, 1, 28), 0.002361)
          .put(LocalDate.of(2014, 1, 29), 0.002356)
          .put(LocalDate.of(2014, 1, 30), 0.002376)
          .put(LocalDate.of(2014, 1, 31), 0.002366)
          .build();

  private static final double LAMBDA = 0.98;
  private static final TimeSeriesPercentageChangeOperator OP_REL_1 = new TimeSeriesPercentageChangeOperator();
  private static final TimeSeriesWeightedVolatilityOperator OP_EWMA_1 =
      TimeSeriesWeightedVolatilityOperator.relative(LAMBDA);
  private static final TimeSeriesPercentageChangeOperator OP_REL_2 = new TimeSeriesPercentageChangeOperator(2);
  private static final TimeSeriesWeightedVolatilityOperator OP_EWMA_2 =
      new TimeSeriesWeightedVolatilityOperator(OP_REL_2, LAMBDA, 0);

  private static final double TOLERANCE_DIFF = 1.0E-10;

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void incorrectLambda0Exception() {
    TimeSeriesWeightedVolatilityOperator.relative(0.0);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void incorrectLambda1Exception() {
    TimeSeriesWeightedVolatilityOperator.relative(1.0);
  }

  /** Test the EWMA for a relative change with 1 period lag. No seed period. */
  public void ewmaRelative1NoSeed() {

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
            .put(LocalDate.of(2014, 1, 4), 1.0124228366)
            .put(LocalDate.of(2014, 1, 5), 1.002646498)
            .put(LocalDate.of(2014, 1, 6), 1.0025936365)
            .put(LocalDate.of(2014, 1, 7), 0.9981683826)
            .build();

    LocalDateDoubleTimeSeries returned = OP_EWMA_1.apply(ts);

    compareTimeseries(returned, expected, TOLERANCE_DIFF);
  }

  /** Test the EWMA for a relative change with 2 period lag. No seed period. */
  public void ewmaRelative2NoSeed() {

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
            .put(LocalDate.of(2014, 1, 5), 3.9623225512)
            .put(LocalDate.of(2014, 1, 6), 3.9234168782)
            .put(LocalDate.of(2014, 1, 7), 3.8846281675)
            .build();

    LocalDateDoubleTimeSeries returned = OP_EWMA_2.apply(ts);

    compareTimeseries(returned, expected, TOLERANCE_DIFF);
  }

  /** Test the EWMA for a relative change with 1 period lag and seed period of length 1. Should be equal to no seed. */
  public void ewmaRelative1Seed1() {

    LocalDateDoubleTimeSeries tsEwmaNS = OP_EWMA_1.apply(TS_1);
    TimeSeriesWeightedVolatilityOperator opEwmaS1 = new TimeSeriesWeightedVolatilityOperator(OP_REL_1, LAMBDA, 1);
    LocalDateDoubleTimeSeries tsEwmaS1 = opEwmaS1.apply(TS_1);

    compareTimeseries(tsEwmaNS, tsEwmaS1, TOLERANCE_DIFF);
  }

  /** Test the EWMA for a relative change with 1 period lag and seed period of length 10. */
  public void ewmaRelative1Seed10() {

    int seedLength = 10;
    TimeSeriesWeightedVolatilityOperator opEwmaS10 =
        new TimeSeriesWeightedVolatilityOperator(OP_REL_1, LAMBDA, seedLength);

    // Values can easily be computed using Excel
    LocalDateDoubleTimeSeries expected =
        LocalDateDoubleTimeSeries.builder()
            .put(LocalDate.of(2014, 1, 16), 0.0079822264)
            .put(LocalDate.of(2014, 1, 17), 0.0079034168)
            .put(LocalDate.of(2014, 1, 20), 0.0078296894)
            .put(LocalDate.of(2014, 1, 21), 0.0077567323)
            .put(LocalDate.of(2014, 1, 22), 0.007684587)
            .put(LocalDate.of(2014, 1, 23), 0.0076597844)
            .put(LocalDate.of(2014, 1, 24), 0.0078236533)
            .put(LocalDate.of(2014, 1, 27), 0.0077581227)
            .put(LocalDate.of(2014, 1, 28), 0.0076801496)
            .put(LocalDate.of(2014, 1, 29), 0.0076088567)
            .put(LocalDate.of(2014, 1, 30), 0.0076274542)
            .put(LocalDate.of(2014, 1, 31), 0.0075742173)
            .build();

    LocalDateDoubleTimeSeries returned = opEwmaS10.apply(TS_1);

    compareTimeseries(returned, expected, TOLERANCE_DIFF);
  }

  /** Test the EWMA for a relative change with 2 period lag and seed period of length 10. */
  public void ewmaRelative2Seed10() {

    int seedLength = 10;
    TimeSeriesWeightedVolatilityOperator opEwmaS10 =
        new TimeSeriesWeightedVolatilityOperator(OP_REL_2, LAMBDA, seedLength);

    // Values can easily be computed using Excel
    LocalDateDoubleTimeSeries expected =
        LocalDateDoubleTimeSeries.builder()
            .put(LocalDate.of(2014, 1, 17), 0.0096742598)
            .put(LocalDate.of(2014, 1, 20), 0.0095875371)
            .put(LocalDate.of(2014, 1, 21), 0.0094911775)
            .put(LocalDate.of(2014, 1, 22), 0.0093957864)
            .put(LocalDate.of(2014, 1, 23), 0.0093778611)
            .put(LocalDate.of(2014, 1, 24), 0.0093421054)
            .put(LocalDate.of(2014, 1, 27), 0.0093661686)
            .put(LocalDate.of(2014, 1, 28), 0.00928298)
            .put(LocalDate.of(2014, 1, 29), 0.0091945604)
            .put(LocalDate.of(2014, 1, 30), 0.0091463881)
            .put(LocalDate.of(2014, 1, 31), 0.0090743374)
            .build();

    LocalDateDoubleTimeSeries returned = opEwmaS10.apply(TS_1);

    compareTimeseries(returned, expected, TOLERANCE_DIFF);
  }

}
