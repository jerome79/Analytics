/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.util.time;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.MONTHS;

import java.io.Serializable;
import java.time.Duration;
import java.time.Period;
import java.time.format.DateTimeParseException;

import org.joda.convert.FromString;
import org.joda.convert.ToString;

import com.opengamma.strata.basics.date.Tenor;
import com.opengamma.strata.collect.ArgChecker;

/**
 * A tenor.
 */
public class ComparableTenor implements Comparable<ComparableTenor>, Serializable {

  /** Serialization version. */
  private static final long serialVersionUID = -6312355131513714559L;

  /**
   * A tenor of one day.
   */
  public static final ComparableTenor DAY = new ComparableTenor(Period.ofDays(1));
  /**
   * A tenor of one day.
   */
  public static final ComparableTenor ONE_DAY = new ComparableTenor(Period.ofDays(1));
  /**
   * A tenor of two days.
   */
  public static final ComparableTenor TWO_DAYS = new ComparableTenor(Period.ofDays(2));
  /**
   * A tenor of two days.
   */
  public static final ComparableTenor THREE_DAYS = new ComparableTenor(Period.ofDays(3));
  /**
   * A tenor of 1 week.
   */
  public static final ComparableTenor ONE_WEEK = new ComparableTenor(Period.ofDays(7));
  /**
   * A tenor of 2 weeks.
   */
  public static final ComparableTenor TWO_WEEKS = new ComparableTenor(Period.ofDays(14));
  /**
   * A tenor of 3 weeks.
   */
  public static final ComparableTenor THREE_WEEKS = new ComparableTenor(Period.ofDays(21));
  /**
   * A tenor of 6 weeks.
   */
  public static final ComparableTenor SIX_WEEKS = new ComparableTenor(Period.ofDays(42));
  /**
   * A tenor of 1 month.
   */
  public static final ComparableTenor ONE_MONTH = new ComparableTenor(Period.ofMonths(1));
  /**
   * A tenor of 2 months.
   */
  public static final ComparableTenor TWO_MONTHS = new ComparableTenor(Period.ofMonths(2));
  /**
   * A tenor of 3 months.
   */
  public static final ComparableTenor THREE_MONTHS = new ComparableTenor(Period.ofMonths(3));
  /**
   * A tenor of 4 months.
   */
  public static final ComparableTenor FOUR_MONTHS = new ComparableTenor(Period.ofMonths(4));
  /**
   * A tenor of 5 months.
   */
  public static final ComparableTenor FIVE_MONTHS = new ComparableTenor(Period.ofMonths(5));
  /**
   * A tenor of 6 months.
   */
  public static final ComparableTenor SIX_MONTHS = new ComparableTenor(Period.ofMonths(6));
  /**
   * A tenor of 7 months.
   */
  public static final ComparableTenor SEVEN_MONTHS = new ComparableTenor(Period.ofMonths(7));
  /**
   * A tenor of 8 months.
   */
  public static final ComparableTenor EIGHT_MONTHS = new ComparableTenor(Period.ofMonths(8));
  /**
   * A tenor of 9 months.
   */
  public static final ComparableTenor NINE_MONTHS = new ComparableTenor(Period.ofMonths(9));
  /**
   * A tenor of 10 months.
   */
  public static final ComparableTenor TEN_MONTHS = new ComparableTenor(Period.ofMonths(10));
  /**
   * A tenor of 11 months.
   */
  public static final ComparableTenor ELEVEN_MONTHS = new ComparableTenor(Period.ofMonths(11));
  /**
   * A tenor of 12 months.
   */
  public static final ComparableTenor TWELVE_MONTHS = new ComparableTenor(Period.ofMonths(12));
  /**
   * A tenor of 18 months.
   */
  public static final ComparableTenor EIGHTEEN_MONTHS = new ComparableTenor(Period.ofMonths(18));
  /**
   * A tenor of 1 year.
   */
  public static final ComparableTenor ONE_YEAR = new ComparableTenor(Period.ofYears(1));
  /**
   * A tenor of 2 years.
   */
  public static final ComparableTenor TWO_YEARS = new ComparableTenor(Period.ofYears(2));
  /**
   * A tenor of 3 years.
   */
  public static final ComparableTenor THREE_YEARS = new ComparableTenor(Period.ofYears(3));
  /**
   * A tenor of 4 years.
   */
  public static final ComparableTenor FOUR_YEARS = new ComparableTenor(Period.ofYears(4));
  /**
   * A tenor of 5 years.
   */
  public static final ComparableTenor FIVE_YEARS = new ComparableTenor(Period.ofYears(5));
  /**
   * A tenor of 6 years.
   */
  public static final ComparableTenor SIX_YEARS = new ComparableTenor(Period.ofYears(6));
  /**
   * A tenor of 7 years.
   */
  public static final ComparableTenor SEVEN_YEARS = new ComparableTenor(Period.ofYears(7));
  /**
   * A tenor of 8 years.
   */
  public static final ComparableTenor EIGHT_YEARS = new ComparableTenor(Period.ofYears(8));
  /**
   * A tenor of 9 years.
   */
  public static final ComparableTenor NINE_YEARS = new ComparableTenor(Period.ofYears(9));
  /**
   * A tenor of 10 years.
   */
  public static final ComparableTenor TEN_YEARS = new ComparableTenor(Period.ofYears(10));
  /**
   * A tenor of one working week (5 days).
   */
  public static final ComparableTenor WORKING_WEEK = new ComparableTenor(Period.ofDays(5));
  /**
   * A tenor of the days in a standard year (365 days).
   */
  public static final ComparableTenor YEAR = new ComparableTenor(Period.ofDays(365));
  /**
   * A tenor of the days in a leap year (366 days).
   */
  public static final ComparableTenor LEAP_YEAR = new ComparableTenor(Period.ofDays(366));
  /**
   * An overnight / next (O/N) tenor.
   */
  public static final ComparableTenor ON = new ComparableTenor(BusinessDayTenor.OVERNIGHT);
  /**
   * A spot / next (S/N) tenor.
   */
  public static final ComparableTenor SN = new ComparableTenor(BusinessDayTenor.SPOT_NEXT);
  /**
   * A tomorrow / next (a.k.a. tom next, T/N) tenor.
   */
  public static final ComparableTenor TN = new ComparableTenor(BusinessDayTenor.TOM_NEXT);

  //-------------------------------------------------------------------------
  /**
   * Business day tenor.
   */
  public enum BusinessDayTenor {
    /**
     * Overnight.
     */
    OVERNIGHT(Period.ofDays(1)),
    /**
     * Tomorrow / next.
     */
    TOM_NEXT(Period.ofDays(2)),
    /**
     * Spot / next.
     */
    SPOT_NEXT(Period.ofDays(3));

    /** The approximate duration of a business day tenor */
    private final Duration _approximateDuration;

    /**
     * @param approximateDuration The approximate duration of a business day tenor. It is not
     * exact because there could be holidays in the period.
     */
    private BusinessDayTenor(final Period approximateDuration) {
      _approximateDuration = DAYS.getDuration().multipliedBy(approximateDuration.getDays());
    }

    /**
     * Gets the approximate duration.
     * @return The approximate duration
     */
    public Duration getApproximateDuration() {
      return _approximateDuration;
    }
  }

  //-------------------------------------------------------------------------
  /**
   * The period of the tenor.
   */
  private final Period _period;
  /**
   * The business day tenor.
   */
  private final BusinessDayTenor _businessDayTenor;

  //-------------------------------------------------------------------------
  /**
   * Obtains a {@code ComparableTenor} from a {@code Tenor}.
   *
   * @param tenor  the tenor to convert to a comparable tenor, not null
   * @return the tenor, not null
   */
  public static ComparableTenor of(Tenor tenor) {
    ArgChecker.notNull(tenor, "tenor");
    return new ComparableTenor(tenor.getPeriod());
  }

  /**
   * Obtains a {@code ComparableTenor} from a {@code Period}.
   *
   * @param period  the period to convert to a tenor, not null
   * @return the tenor, not null
   */
  public static ComparableTenor of(Period period) {
    ArgChecker.notNull(period, "period");
    return new ComparableTenor(period);
  }

  /**
   * Obtains a {@code ComparableTenor} from a {@code BusinessDayComparableTenor}.
   *
   * @param businessDayTenor  the tenor to convert, not null
   * @return the tenor, not null
   */
  public static ComparableTenor of(BusinessDayTenor businessDayTenor) {
    ArgChecker.notNull(businessDayTenor, "businessDayComparableTenor");
    return new ComparableTenor(businessDayTenor);
  }

  /**
   * Returns a tenor backed by a period of days.
   * @param days The number of days
   * @return The tenor
   */
  public static ComparableTenor ofDays(int days) {
    return new ComparableTenor(Period.ofDays(days));
  }

  /**
   * Returns a tenor backed by a period of weeks.
   * @param weeks The number of weeks
   * @return The tenor
   */
  public static ComparableTenor ofWeeks(int weeks) {
    return new ComparableTenor(Period.ofDays(weeks * 7));
  }

  /**
   * Returns a tenor backed by a period of months.
   * @param months The number of months
   * @return The tenor
   */
  public static ComparableTenor ofMonths(int months) {
    return new ComparableTenor(Period.ofMonths(months)); // TODO: what do we do here
  }

  /**
   * Returns a tenor backed by a period of years.
   * @param years The number of years
   * @return The tenor
   */
  public static ComparableTenor ofYears(int years) {
    return new ComparableTenor(Period.ofYears(years)); // TODO: what do we do here
  }

  /**
   * Returns a tenor of business days.
   * @param businessDayTenor The business day
   * @return The tenor
   */
  public static ComparableTenor ofBusinessDay(BusinessDayTenor businessDayTenor) {
    return new ComparableTenor(businessDayTenor);
  }

  /**
   * Returns a tenor of business days.
   * @param businessDayComparableTenor The business days name
   * @return The tenor
   */
  public static ComparableTenor ofBusinessDay(String businessDayComparableTenor) {
    return new ComparableTenor(BusinessDayTenor.valueOf(businessDayComparableTenor));
  }

  /**
   * Parses a formatted string representing the tenor.
   * <p>
   * The format is based on ISO-8601, such as 'P3M'.
   *
   * @param tenorStr  the string representing the tenor, not null
   * @return the tenor, not null
   */
  @FromString
  @SuppressWarnings("deprecation")
  public static ComparableTenor parse(String tenorStr) {
    ArgChecker.notNull(tenorStr, "tenorStr");
    try {
      return new ComparableTenor(Period.parse(tenorStr));
    } catch (DateTimeParseException e) {
      return new ComparableTenor(BusinessDayTenor.valueOf(tenorStr));
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Creates a tenor.
   * @param period  the period to represent
   */
  private ComparableTenor(Period period) {
    ArgChecker.notNull(period, "period");
    _period = period;
    _businessDayTenor = null;
  }

  /**
   * Creates a tenor without a period. This is used for overnight,
   * spot next and tomorrow next tenors.
   */
  private ComparableTenor(BusinessDayTenor businessDayTenor) {
    ArgChecker.notNull(businessDayTenor, "business day tenor");
    _period = null;
    _businessDayTenor = businessDayTenor;
  }
  
  /**
   * Gets the tenor period.
   * @return the period
   * @throws IllegalStateException If the tenor is not backed by a {@link Period}
   */
  public Period getPeriod() {
    if (_period == null) {
      throw new IllegalStateException("Could not get period for " + toString());
    }
    return _period;
  }

  /**
   * Gets the business day tenor if the tenor is of appropriate type.
   * @return The business day tenor
   * @throws IllegalStateException If the tenor is backed by a period
   */
  public BusinessDayTenor getBusinessDayTenor() {
    if (_businessDayTenor == null) {
      throw new IllegalStateException("Could not get business day tenor for " + toString());
    }
    return _businessDayTenor;
  }
  
  /**
   * Returns true if the tenor is a business day tenor.
   * @return True if the tenor is a business day tenor
   */
  public boolean isBusinessDayTenor() {
    return _period == null;
  }
  
  //-------------------------------------------------------------------------
  /**
   * Returns a formatted string representing the tenor.
   * <p>
   * The format is based on ISO-8601, such as 'P3M'.
   * 
   * @return the formatted tenor, not null
   */
  @ToString
  public String toFormattedString() {
    if (_period != null) {
      return getPeriod().toString();
    } 
    return getBusinessDayTenor().toString();
  }

  //-------------------------------------------------------------------------
  @Override
  public int compareTo(ComparableTenor other) {
    Duration thisDur, otherDur;
    if (_period == null) {
      thisDur = _businessDayTenor.getApproximateDuration();
    } else {
      thisDur = estimatedDuration(_period);
    }
    if (other._period == null) {
      otherDur = other._businessDayTenor.getApproximateDuration();
    } else {
      otherDur = estimatedDuration(other._period);
    }
    return thisDur.compareTo(otherDur);
  }

  /**
   * Gets the estimated duration of the period.
   * 
   * @param period the period to estimate the duration of, not null
   * @return the estimated duration, not null
   */
  private static Duration estimatedDuration(Period period) {
    Duration monthsDuration = MONTHS.getDuration().multipliedBy(period.toTotalMonths());
    Duration daysDuration = DAYS.getDuration().multipliedBy(period.getDays());
    return monthsDuration.plus(daysDuration);
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean equals(Object o) {
    if (o == null) {
      return false;
    }
    if (!(o instanceof ComparableTenor)) {
      return false;
    }
    ComparableTenor other = (ComparableTenor) o;
    if (_period == null) {
      if (other._period == null) {
        return _businessDayTenor == other._businessDayTenor;
      } 
      return false;
    }
    if (other._period == null) {
      return false;
    }
    return _period.equals(other._period);
  }

  @Override
  public int hashCode() {
    if (_period == null) {
      return getBusinessDayTenor().hashCode();
    }
    return getPeriod().hashCode();
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("ComparableTenor[");
    if (_period == null) {
      sb.append(getBusinessDayTenor().toString());
    } else {
      sb.append(getPeriod().toString());
    }
    sb.append("]");
    return sb.toString();
  }

}
