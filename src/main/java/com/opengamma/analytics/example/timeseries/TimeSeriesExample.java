/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.example.timeseries;

import java.io.PrintStream;
import java.time.LocalDate;

import com.opengamma.strata.collect.timeseries.LocalDateDoubleTimeSeries;
import com.opengamma.strata.collect.timeseries.LocalDateDoubleTimeSeriesBuilder;

/**
 * Example time-series code.
 */
public class TimeSeriesExample {

  public static void timeSeriesExample(final PrintStream out) {
    LocalDateDoubleTimeSeriesBuilder ts1 = LocalDateDoubleTimeSeries.builder();
    ts1.put(LocalDate.of(2010, 1, 1), 2.1d);
    ts1.put(LocalDate.of(2010, 1, 2), 2.2d);
    ts1.put(LocalDate.of(2010, 1, 3), 2.3d);
    out.println("ts1: " + ts1);

    LocalDateDoubleTimeSeries ts2 = ts1.build();
    out.println("ts2: " + ts2);

    final LocalDateDoubleTimeSeries ts3 = ts2.subSeries(LocalDate.of(2010, 1, 2), LocalDate.of(2010, 1, 3));
    out.println("ts3: " + ts3);
  }

  public static void main(final String[] args) throws Exception {  // CSIGNORE
    timeSeriesExample(System.out);
  }

}
