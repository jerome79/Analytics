/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.timeseries.util;

import static com.opengamma.analytics.financial.timeseries.util.LocalDateDoubleTimeSeriesTestUtils.compareTimeseries;

import java.time.LocalDate;

import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;
import com.opengamma.strata.collect.timeseries.LocalDateDoubleTimeSeries;


/**
 * Test.
 */
@Test
public class TimeSeriesOperatorTest {

  private static final LocalDateDoubleTimeSeries TEST_PRICE_SERIES =
      LocalDateDoubleTimeSeries.builder()
        .putAll(
            ImmutableList.of(ld(1), ld(2), ld(3), ld(4), ld(5), ld(6), ld(7), ld(8), ld(9), ld(10)),
            ImmutableList.of(738d, 730d, 750d, 750d, 746d, 754d, 769d, 750d, 750d, 764d))
        .build();

  @Test
  public void testWeightedVol() {
    LocalDateDoubleTimeSeries priceSeries = TEST_PRICE_SERIES;
    TimeSeriesWeightedVolatilityOperator weightedVolOperator = TimeSeriesWeightedVolatilityOperator.relative(0.94);
    LocalDateDoubleTimeSeries weightedVolSeries = weightedVolOperator.apply(priceSeries);
    LocalDateDoubleTimeSeries expectedWeightedVolSeries =
        LocalDateDoubleTimeSeries.builder()
            .putAll(
                ImmutableList.of(ld(2), ld(3), ld(4), ld(5), ld(6), ld(7), ld(8), ld(9), ld(10)),
                ImmutableList.of(0.010840108, 0.012469726, 0.012089848, 0.011794118, 0.011732656, 0.012375053, 0.013438035, 0.013028659, 0.013433833))
            .build();
    compareTimeseries(expectedWeightedVolSeries, weightedVolSeries, 0.000000001);
  }
  
  @Test
  public void testRelativeVolatilityWeighting() {
    LocalDateDoubleTimeSeries priceSeries = TEST_PRICE_SERIES;
    TimeSeriesWeightedVolatilityOperator weightedVolOperator = TimeSeriesWeightedVolatilityOperator.relative(0.94);
    LocalDateDoubleTimeSeries weightedVolSeries = weightedVolOperator.apply(priceSeries);
    TimeSeriesRelativeWeightedDifferenceOperator relativeWeightedDifferenceOperator = new TimeSeriesRelativeWeightedDifferenceOperator();
    LocalDateDoubleTimeSeries relativeWeightedDifferenceSeries = relativeWeightedDifferenceOperator.apply(priceSeries, weightedVolSeries);
    LocalDateDoubleTimeSeries expectedRelativeWeightedDifferenceSeries =
        LocalDateDoubleTimeSeries.builder()
            .putAll(
                ImmutableList.of(ld(2), ld(3), ld(4), ld(5), ld(6), ld(7), ld(8), ld(9), ld(10)),
                ImmutableList.of(-9.914168489, 21.546315757, 0d, -4.556112623, 9.159959999, 16.283363544, -18.994057616, 0d, 14d))
            .build();
    compareTimeseries(expectedRelativeWeightedDifferenceSeries, relativeWeightedDifferenceSeries, 0.000000001);
  }

  private static LocalDate ld(long day) {
    return LocalDate.ofEpochDay(day);
  }
  
}
