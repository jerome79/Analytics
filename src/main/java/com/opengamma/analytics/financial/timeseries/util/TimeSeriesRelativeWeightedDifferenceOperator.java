/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.timeseries.util;

import static com.opengamma.analytics.util.StreamUtils.zip;

import java.time.LocalDate;
import java.util.function.BinaryOperator;

import com.opengamma.strata.collect.ArgChecker;
import com.opengamma.strata.collect.timeseries.LocalDateDoublePoint;
import com.opengamma.strata.collect.timeseries.LocalDateDoubleTimeSeries;
import com.opengamma.strata.collect.tuple.Pair;

/**
 * Function taking a timeseries and a series of weightings, producing
 * a new timeseries where the entries in the timeseries are
 * weighted according to the values in the weighting series.
 */
public class TimeSeriesRelativeWeightedDifferenceOperator implements BinaryOperator<LocalDateDoubleTimeSeries> {

  /**
   * Operator to compute difference between the entries in a timeseries.
   */
  private static final TimeSeriesDifferenceOperator DIFFERENCE = new TimeSeriesDifferenceOperator();

  /**
   * Function taking a timeseries and a series of weightings, producing
   * a new timeseries where the entries in the timeseries are
   * weighted according to the values in the weighting series.
   * <p>
   * The steps are as follows:
   * <ul>
   * <li>create a new series ds, containing the difference between each
   *    element in the timeseries</li>
   * <li>validate that ds is the same size as the weights series</li>
   * <li>create a new series where each element is the corresponding
   *    element in ds, divided by the corresponding weighting and
   *    multiplied by the final weighting. If any weighting is zero
   *    then a zero entry will be written (see PLT-426).
   * </ul>
   *
   * @param ts  the timeseries to calculate weighted difference for
   * @param weights  the series of weights to be used. This series
   *     must be 1 element shorter than the timeseries.
   * @return a new weighted timeseries, equal in length to the weights
   *     used
   */
  @Override
  public LocalDateDoubleTimeSeries apply(LocalDateDoubleTimeSeries ts, LocalDateDoubleTimeSeries weights) {

    LocalDateDoubleTimeSeries differenceSeries = DIFFERENCE.apply(ts);
    ArgChecker.isTrue(
        differenceSeries.size() == weights.size(),
        "Difference series has {} points but weighting series has {}",
        differenceSeries.size(),
        weights.size());

    double endWeight = weights.getLatestValue();

    return zip(differenceSeries.stream(), weights.stream())
        .map(pair -> calculatePoint(pair, endWeight))
        .collect(LocalDateDoubleTimeSeries.collector());
  }

  private LocalDateDoublePoint calculatePoint(
      Pair<LocalDateDoublePoint, LocalDateDoublePoint> pair,
      double endWeight) {

    LocalDate date = pair.getFirst().getDate();
    double difference = pair.getFirst().getValue();
    double weight = pair.getSecond().getValue();
    double weightedDifference = weight == 0 ? 0 : difference * endWeight / weight;
    return LocalDateDoublePoint.of(date, weightedDifference);
  }

}
