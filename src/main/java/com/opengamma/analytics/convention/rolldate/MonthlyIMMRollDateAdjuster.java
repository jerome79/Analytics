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
 * 2013-01-01 will return 2013-01-16<br>
 * 2013-01-15 will return 2013-01-16<br>
 * 2013-01-16 will return 2013-01-16<br>
 * 2013-03-17 will return 2013-02-20<br>
 * 2014-12-31 will return 2014-01-15
 */
public final class MonthlyIMMRollDateAdjuster implements RollDateAdjuster {
  /** Adjusts a date to the third Wednesday of a month */
  private static final TemporalAdjuster THIRD_WEDNESDAY = TemporalAdjusters.dayOfWeekInMonth(3, DayOfWeek.WEDNESDAY);
  /** The single instance */
  private static final MonthlyIMMRollDateAdjuster INSTANCE = new MonthlyIMMRollDateAdjuster();

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
  private MonthlyIMMRollDateAdjuster() {
  }

  @Override
  public Temporal adjustInto(final Temporal temporal) {
    ArgChecker.notNull(temporal, "temporal");
    final Temporal immDateInMonth = temporal.with(THIRD_WEDNESDAY);
    if (temporal.getLong(ChronoField.DAY_OF_MONTH) > immDateInMonth.getLong(ChronoField.DAY_OF_MONTH)) {
      return temporal.with(TemporalAdjusters.firstDayOfMonth()).plus(1L, ChronoUnit.MONTHS).with(THIRD_WEDNESDAY);
    }
    return immDateInMonth;
  }

  @Override
  public long getMonthsToAdjust() {
    return 1L;
  }
  
  @Override
  public String getName() {
    return RollDateAdjusterFactory.MONTHLY_IMM_ROLL_STRING;
  }

}
