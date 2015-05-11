package com.opengamma.analytics.financial.timeseries.util;

import static com.opengamma.analytics.financial.timeseries.util.LocalDateDoubleTimeSeriesTestUtils.compareTimeseries;

import java.time.LocalDate;
import java.util.List;

import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;
import com.opengamma.strata.collect.timeseries.LocalDateDoubleTimeSeries;

/**
 * Tests for TimeSeriesRelativeWeightedDifferenceOperator.
 */
public class TimeSeriesRelativeWeightedDifferenceOperatorTest {

  private TimeSeriesRelativeWeightedDifferenceOperator _operator = new TimeSeriesRelativeWeightedDifferenceOperator();

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullSeriesCausesException() {
    _operator.apply(null, LocalDateDoubleTimeSeries.empty());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullWeightsCausesException() {
    _operator.apply(LocalDateDoubleTimeSeries.empty(), null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void seriesMustHaveOneMoreElementThanWeights() {
    List<LocalDate> dates = ImmutableList.of(LocalDate.of(2014, 1, 1), LocalDate.of(2014, 1, 2), LocalDate.of(2014, 1, 3));
    List<Double> values = ImmutableList.of(1.23, 2.34, 3.45);
    LocalDateDoubleTimeSeries series = LocalDateDoubleTimeSeries.builder().putAll(dates, values).build();
    LocalDateDoubleTimeSeries weights = series.tailSeries(1);
    _operator.apply(series, weights);
  }

  @Test
  public void constantWeightsJustProducesSeriesDifference() {

    List<LocalDate> dates = ImmutableList.of(LocalDate.of(2014, 1, 1), LocalDate.of(2014, 1, 2), LocalDate.of(2014, 1, 3));
    List<Double> values = ImmutableList.of(1.23, 2.34, 3.45);
    LocalDateDoubleTimeSeries series = LocalDateDoubleTimeSeries.builder().putAll(dates, values).build();
    List<LocalDate> weightDates = ImmutableList.of(LocalDate.of(2014, 1, 2), LocalDate.of(2014, 1, 3));
    List<Double> weightValues = ImmutableList.of(2d, 2d);
    LocalDateDoubleTimeSeries weights = LocalDateDoubleTimeSeries.builder().putAll(weightDates, weightValues).build();

    LocalDateDoubleTimeSeries result = _operator.apply(series, weights);
    LocalDateDoubleTimeSeries expected =
        LocalDateDoubleTimeSeries.builder()
            .put(LocalDate.of(2014, 1, 2), 1.11)
            .put(LocalDate.of(2014, 1, 3), 1.11)
            .build();
    compareTimeseries(result, expected);
  }

  /**
   * This tests the issue raised in PLT-426 where a 0 weight at the start
   * of a series causes NaNs to appear in the results as the value is
   * being multiplied by wt(T) / wt(t) where T is the final weight and
   * wt(t) = 0.
   * <p>
   * For the time being the fix is to set the calculated value to zero
   * which may want to be revisited in the future as it assumes that the
   * value we are multiplying is also 0.
   */
  @Test
  public void zeroWeightsAreHandled() {

    LocalDateDoubleTimeSeries series =
        LocalDateDoubleTimeSeries.builder()
            .put(LocalDate.of(2014, 1, 1), 1.23)
            .put(LocalDate.of(2014, 1, 2), 2.34)
            .put(LocalDate.of(2014, 1, 3), 3.45)
            .build();

    LocalDateDoubleTimeSeries weights =
        LocalDateDoubleTimeSeries.builder()
            .put(LocalDate.of(2014, 1, 2), 0)
            .put(LocalDate.of(2014, 1, 3), 2)
            .build();

    LocalDateDoubleTimeSeries result = _operator.apply(series, weights);

    LocalDateDoubleTimeSeries expected =
        LocalDateDoubleTimeSeries.builder()
            .put(LocalDate.of(2014, 1, 2), 0)
            .put(LocalDate.of(2014, 1, 3), 1.11)
            .build();
    compareTimeseries(result, expected);
  }

}
