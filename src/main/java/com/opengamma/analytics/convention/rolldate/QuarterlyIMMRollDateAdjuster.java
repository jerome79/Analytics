/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.convention.rolldate;

import java.time.DayOfWeek;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;

import com.opengamma.strata.collect.ArgChecker;

/**
 * Return the next quarterly IMM roll date for a date, which is the third Wednesday in March, June,
 * September or December. If the date falls on an IMM date, it is returned unadjusted. Sample output
 * for dates is:
 * <p>
 * 2013-01-01 will return 2013-03-20<br>
 * 2013-03-01 will return 2013-03-20<br>
 * 2013-03-19 will return 2013-03-20<br>
 * 2013-03-20 will return 2013-03-20<br>
 * 2013-03-21 will return 2013-06-19<br>
 * 2013-03-31 will return 2013-06-19<br>
 * 2014-12-31 will return 2014-03-19
 */
public final class QuarterlyIMMRollDateAdjuster implements RollDateAdjuster {
  /** Adjusts a date to the third Wednesday of a month */
  private static final TemporalAdjuster THIRD_WEDNESDAY = TemporalAdjusters.dayOfWeekInMonth(3, DayOfWeek.WEDNESDAY);
  /** The single instance */
  private static final QuarterlyIMMRollDateAdjuster INSTANCE = new QuarterlyIMMRollDateAdjuster();

  /**
   * Returns the single instance of this adjuster.
   * @return The adjuster
   */
  public static RollDateAdjuster getAdjuster() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private QuarterlyIMMRollDateAdjuster() {
  }

  @Override
  public Temporal adjustInto(final Temporal temporal) {
    ArgChecker.notNull(temporal, "temporal");
    final long month = temporal.getLong(ChronoField.MONTH_OF_YEAR);
    final long offset = month % 3L == 0L ? 0 : 3L - month % 3L;
    final Temporal temp = temporal.plus(offset, ChronoUnit.MONTHS);
    final Temporal immDateInMonth = temp.with(THIRD_WEDNESDAY);
    if (offset == 0L) {
      if (temp.getLong(ChronoField.DAY_OF_MONTH) > immDateInMonth.getLong(ChronoField.DAY_OF_MONTH)) {
        return temp.with(TemporalAdjusters.firstDayOfMonth()).plus(3L, ChronoUnit.MONTHS).with(THIRD_WEDNESDAY);
      }
    }
    return immDateInMonth;
  }

  @Override
  public long getMonthsToAdjust() {
    return 3L;
  }

  @Override
  public String getName() {
    return RollDateAdjusterFactory.QUARTERLY_IMM_ROLL_STRING;
  }

}
