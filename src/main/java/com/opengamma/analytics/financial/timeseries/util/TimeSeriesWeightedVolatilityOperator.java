/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.timeseries.util;

import static java.util.stream.Collectors.toList;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

import com.google.common.util.concurrent.AtomicDouble;
import com.opengamma.strata.collect.ArgChecker;
import com.opengamma.strata.collect.timeseries.LocalDateDoubleTimeSeries;

/**
 * Calculates a weighted volatility series from a series of absolute or relative returns.
 * A seed period can be defined. In that case, the non-weighted variance/volatility is computed for the 
 * number of returns indicated in the seed length. That volatility is used as the starting point of the volatility
 * computation. The length of the output is reduced by the number of seed.
 * The length of the output time series is (Return TS length) - (seed length) + 1.
 */
public final class TimeSeriesWeightedVolatilityOperator implements UnaryOperator<LocalDateDoubleTimeSeries> {

  /** Default seed length: 0*/
  private static final int DEFAULT_SEED_LENGTH = 0;
  /** Return operator as relative return on one period. */
  private static final TimeSeriesPercentageChangeOperator RELATIVE_CHANGE = new TimeSeriesPercentageChangeOperator();
  /** Return operator as absolute return on one period. */
  private static final TimeSeriesDifferenceOperator ABSOLUTE_CHANGE = new TimeSeriesDifferenceOperator();

  private final UnaryOperator<LocalDateDoubleTimeSeries> changeOperator;
  /** The weight used for the Exponentially Weighted Moving Average computation. */
  private final double lambda;
  /** The length of the seed period. In the seed period, the variance is computed with equal weight. */
  private final int seedLength;

  /**
   * Constructor with a generic return operator and the weight.
   * @param returnOperator The return operator for time series.
   * @param lambda The weight of the exponentially weighted moving average.
   * @param seedLength The length of the seed period. In the seed period, the variance is computed with equal weight.
   */
  public TimeSeriesWeightedVolatilityOperator(
      UnaryOperator<LocalDateDoubleTimeSeries> returnOperator,
      double lambda,
      int seedLength) {
    changeOperator = returnOperator;
    this.lambda = ArgChecker.inRangeExclusive(lambda, 0d, 1d, "lambda");
    this.seedLength = seedLength;
  }

  @Override
  public LocalDateDoubleTimeSeries apply(LocalDateDoubleTimeSeries ts) {
    ArgChecker.notNull(ts, "time series");
    ArgChecker.isTrue(ts.size() > seedLength, "time series length must be > ", seedLength);
    LocalDateDoubleTimeSeries returnSeries = changeOperator.apply(ts);

    int seedLengthAdjusted = Math.max(seedLength, 1);

    // When seed part is 0, the first variance is computed as the square of the first return. This is the same
    // as a seed part of length 1.
    double seedVariance = returnSeries.headSeries(seedLengthAdjusted)
        .values()
        .map(d -> d * d)
        .average()
        .getAsDouble();

    List<LocalDate> volatilityTimes =
        returnSeries.dates()
            .skip(seedLengthAdjusted - 1)
            .collect(toList());

    // Create vols list and create the first element
    List<Double> weightedVolatilities = new ArrayList<>();
    weightedVolatilities.add(Math.sqrt(seedVariance));

    AtomicDouble ewmaVariance = new AtomicDouble(seedVariance);

    // Would be nice if this could be done with a fold/reduce
    // but it's not straightforward with Java 8
    returnSeries.values()
        .skip(seedLengthAdjusted)
        .forEach(d -> {
          double newEwma = lambda * ewmaVariance.get() + (1 - lambda) * d * d;
          weightedVolatilities.add(Math.sqrt(newEwma));
          ewmaVariance.set(newEwma);
        });

    return LocalDateDoubleTimeSeries.builder()
        .putAll(volatilityTimes, weightedVolatilities)
        .build();
  }

  /**
   * Calculates weighted volatilities using the relative difference series and the default lag of 1 period in the 
   * return computation and no seed period (seed length = 0).
   * @param lambda lambda value to apply
   * @return a TimeSeriesWeightedVolatilityOperator instance
   */
  public static TimeSeriesWeightedVolatilityOperator relative(double lambda) {
    return new TimeSeriesWeightedVolatilityOperator(RELATIVE_CHANGE, lambda, DEFAULT_SEED_LENGTH);
  }

  /**
   * Calculates weighted volatilities using the absolute difference series and the default lag of 1 period in the 
   * return computation and no seed period (seed length = 0).
   * @param lambda lambda value to apply
   * @return a TimeSeriesWeightedVolatilityOperator instance
   */
  public static TimeSeriesWeightedVolatilityOperator absolute(double lambda) {
    return new TimeSeriesWeightedVolatilityOperator(ABSOLUTE_CHANGE, lambda, DEFAULT_SEED_LENGTH);
  }

}
