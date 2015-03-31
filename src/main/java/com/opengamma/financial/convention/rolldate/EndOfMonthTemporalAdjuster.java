/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.rolldate;

import java.time.temporal.Temporal;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;

import com.opengamma.strata.collect.ArgChecker;

/**
 *  End of month roll date adjuster
 */
public final class EndOfMonthTemporalAdjuster implements TemporalAdjuster {
  /** Adjusts a date to the last day of a month */
  private static final TemporalAdjuster LAST_DAY_OF_THE_MONTH = TemporalAdjusters.lastDayOfMonth();
  /** The single instance */
  private static final EndOfMonthTemporalAdjuster INSTANCE = new EndOfMonthTemporalAdjuster();

  /**
   * Returns the single instance of this adjuster.
   * @return The adjuster
   */
  public static TemporalAdjuster getAdjuster() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private EndOfMonthTemporalAdjuster() {
  }

  @Override
  public Temporal adjustInto(final Temporal temporal) {
    ArgChecker.notNull(temporal, "temporal");
    return temporal.with(LAST_DAY_OF_THE_MONTH);
  }

}
