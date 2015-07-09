/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 * <p>
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.timeseries.util;

import static com.opengamma.analytics.util.StreamUtils.zip;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

import org.assertj.core.data.Offset;

import com.opengamma.strata.collect.timeseries.LocalDateDoublePoint;
import com.opengamma.strata.collect.timeseries.LocalDateDoubleTimeSeries;

public class LocalDateDoubleTimeSeriesTestUtils {

  public static void compareTimeseries(LocalDateDoubleTimeSeries result, LocalDateDoubleTimeSeries expected, Double tol) {

    assertThat(result.size()).isEqualTo(expected.size());

    Offset<Double> offset = offset(tol);
    zip(result.stream(), expected.stream())
        .forEach(pair -> {
          LocalDateDoublePoint res = pair.getFirst();
          LocalDateDoublePoint exp = pair.getSecond();
          assertThat(res.getDate()).isEqualTo(exp.getDate());
          assertThat(res.getValue()).isEqualTo(exp.getValue(), offset);
        });
  }

  private LocalDateDoubleTimeSeriesTestUtils() {
  }
}
