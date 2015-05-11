/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.timeseries.util;

import static com.opengamma.analytics.util.StreamUtils.zip;

import java.time.LocalDate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import com.opengamma.strata.collect.ArgChecker;
import com.opengamma.strata.collect.timeseries.LocalDateDoublePoint;
import com.opengamma.strata.collect.timeseries.LocalDateDoubleTimeSeries;
import com.opengamma.strata.collect.tuple.Pair;

/**
 * Operator to obtain the difference or absolute return of a time series: difference = V(end) - V(start)
 * The difference is taken between elements in the time series with a certain lag. 
 * The default lag is 1 element, which means that the difference is between consecutive entries in the series.
 * The series returned has less element than the input series by the lag.
 * The dates of the returned time series are the dates of the end of the period on which the difference is computed.
 */
public class TimeSeriesDifferenceOperator implements UnaryOperator<LocalDateDoubleTimeSeries> {

  /** The default lag: 1 time series element. */
  private static final int DEFAULT_LAG = 1;
  /** The lag between the element of the times series on which the difference is taken. */
  private final int lag;

  /**
   * Constructor with the default lag of 1 element.
   */
  public TimeSeriesDifferenceOperator() {
    this(DEFAULT_LAG);
  }

  /**
   * Constructor with a specified lag.
   * @param lag The lag between element to compute the difference.
   */
  public TimeSeriesDifferenceOperator(int lag) {
    this.lag = lag;
  }

  @Override
  public LocalDateDoubleTimeSeries apply(final LocalDateDoubleTimeSeries ts) {
    ArgChecker.notNull(ts, "time series");
    ArgChecker.isTrue(ts.size() > lag, "time series length must be > lag");

    Stream<LocalDateDoublePoint> lagged = ts.stream().skip(lag);

    return zip(lagged, ts.stream())
        .map(this::calculateNewPoint)
        .collect(LocalDateDoubleTimeSeries.collector());
  }

  private LocalDateDoublePoint calculateNewPoint(Pair<LocalDateDoublePoint, LocalDateDoublePoint> pair) {
    LocalDate offsetDate = pair.getFirst().getDate();
    double offsetValue = pair.getFirst().getValue();
    double originalValue = pair.getSecond().getValue();
    return LocalDateDoublePoint.of(offsetDate, offsetValue - originalValue);
  }

}
