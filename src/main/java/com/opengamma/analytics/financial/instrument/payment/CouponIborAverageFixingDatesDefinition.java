/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.payment;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Arrays;

import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionWithData;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborAverageFixingDates;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.analytics.util.timeseries.DoubleTimeSeries;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.basics.date.HolidayCalendar;
import com.opengamma.strata.collect.ArgChecker;

/**
 * Class describing an average coupon by weighted mean of index values with difference fixing dates.
 */
public class CouponIborAverageFixingDatesDefinition extends CouponDefinition implements InstrumentDefinitionWithData<Payment, DoubleTimeSeries<ZonedDateTime>> {

  /** The index on which the fixing is done. The same index is used for all the fixings. */
  private final IborIndex _index;
  /** The fixing dates of the index. The dates are in increasing order. */
  private final ZonedDateTime[] _fixingDate;
  /** The weights or quantity used for each fixing. The total weight is not necessarily 1. Same size as _fixingDate. */
  private final double[] _weight;
  /** The start dates of the underlying index period. Same size as _fixingDate. */
  private final ZonedDateTime[] _fixingPeriodStartDate;
  /** The end dates of the underlying index period. Same size as _fixingDate. */
  private final ZonedDateTime[] _fixingPeriodEndDate;
  /** The index periods accrual factors. Same size as _fixingDate. */
  private final double[] _fixingPeriodAccrualFactor;

  /**
   * Constructor. 
   * The start dates and end dates of fixing period are deduced from the index conventions.
   * @param currency The coupon currency
   * @param paymentDate The coupon payment date.
   * @param accrualStartDate The start date of the accrual period
   * @param accrualEndDate The end date of the accrual period
   * @param paymentAccrualFactor The accrual factor of the accrual period
   * @param notional The coupon notional
   * @param index The coupon Ibor index
   * @param fixingDate The coupon fixing dates
   * @param weight The weights for the index
   * @param iborCalendar The holiday calendar for the index
   */
  public CouponIborAverageFixingDatesDefinition(final Currency currency, final ZonedDateTime paymentDate, final ZonedDateTime accrualStartDate, final ZonedDateTime accrualEndDate,
      final double paymentAccrualFactor, final double notional, final IborIndex index, final ZonedDateTime[] fixingDate, final double[] weight, final HolidayCalendar iborCalendar) {
    super(currency, paymentDate, accrualStartDate, accrualEndDate, paymentAccrualFactor, notional);
    ArgChecker.notNull(fixingDate, "fixingDate");
    ArgChecker.notNull(index, "index");
    ArgChecker.notNull(weight, "weight");
    ArgChecker.notNull(iborCalendar, "iborCalendar");
    final int nDates = fixingDate.length;
    ArgChecker.isTrue(nDates == weight.length, "weight length different from fixingDate length");
    ArgChecker.isTrue(currency.equals(index.getCurrency()), "index currency different from payment currency");

    for (int i = 0; i < nDates; ++i) {
      ArgChecker.notNull(fixingDate[i], "element of fixingDate");
    }

    _fixingDate = Arrays.copyOf(fixingDate, nDates);
    _index = index;
    _weight = Arrays.copyOf(weight, nDates);

    _fixingPeriodStartDate = new ZonedDateTime[nDates];
    _fixingPeriodEndDate = new ZonedDateTime[nDates];
    _fixingPeriodAccrualFactor = new double[nDates];

    for (int i = 0; i < nDates; ++i) {
      _fixingPeriodStartDate[i] = ScheduleCalculator.getAdjustedDate(fixingDate[i], index.getSpotLag(), iborCalendar);
      _fixingPeriodEndDate[i] = ScheduleCalculator.getAdjustedDate(_fixingPeriodStartDate[i], index.getTenor(), index.getBusinessDayConvention(), iborCalendar, index.isEndOfMonth());
      _fixingPeriodAccrualFactor[i] = index.getDayCount().yearFraction(_fixingPeriodStartDate[i], _fixingPeriodEndDate[i], iborCalendar);
    }
  }

  /**
   * Constructor with start dates and end dates fixing period 
   * @param currency The coupon currency
   * @param paymentDate The coupon payment date.
   * @param accrualStartDate The start date of the accrual period
   * @param accrualEndDate The end date of the accrual period
   * @param paymentAccrualFactor The accrual factor of the accrual period
   * @param notional The coupon notional
   * @param fixingDate The coupon fixing dates
   * @param index The coupon Ibor indices
   * @param weight The weights for the indices
   * @param fixingPeriodStartDate The start date of the fixing periods
   * @param fixingPeriodEndDate The end date of the fixing periods
   * @param fixingPeriodAccrualFactor The accrual factor of the fixing periods
   */
  public CouponIborAverageFixingDatesDefinition(final Currency currency, final ZonedDateTime paymentDate, final ZonedDateTime accrualStartDate, final ZonedDateTime accrualEndDate,
      final double paymentAccrualFactor, final double notional, final IborIndex index, final ZonedDateTime[] fixingDate, final double[] weight, final ZonedDateTime[] fixingPeriodStartDate,
      final ZonedDateTime[] fixingPeriodEndDate, final double[] fixingPeriodAccrualFactor) {
    super(currency, paymentDate, accrualStartDate, accrualEndDate, paymentAccrualFactor, notional);

    ArgChecker.notNull(fixingDate, "fixingDate");
    ArgChecker.notNull(index, "index");
    ArgChecker.notNull(weight, "weight");
    ArgChecker.notNull(fixingPeriodStartDate, "fixingPeriodStartDate");
    ArgChecker.notNull(fixingPeriodEndDate, "fixingPeriodEndDate");

    final int nRates = fixingDate.length;
    ArgChecker.isTrue(nRates == weight.length, "weight length different from fixingDate length");
    ArgChecker.isTrue(nRates == fixingPeriodStartDate.length, "fixingPeriodStartDate length different from fixingDate length");
    ArgChecker.isTrue(nRates == fixingPeriodEndDate.length, "fixingPeriodEndDate length different from fixingDate length");
    ArgChecker.isTrue(nRates == fixingPeriodAccrualFactor.length, "fixingPeriodAccrualFactor length different from fixingDate length");
    ArgChecker.isTrue(currency.equals(index.getCurrency()), "index currency different from payment currency");

    for (int i = 0; i < nRates; ++i) {
      ArgChecker.notNull(fixingDate[i], "element of fixingDate");
      ArgChecker.notNull(fixingPeriodStartDate[i], "element of fixingPeriodStartDate");
      ArgChecker.notNull(fixingPeriodEndDate[i], "element of fixingPeriodEndDate");
    }

    _fixingDate = Arrays.copyOf(fixingDate, nRates);
    _index = index;
    _weight = Arrays.copyOf(weight, nRates);
    _fixingPeriodStartDate = Arrays.copyOf(fixingPeriodStartDate, nRates);
    _fixingPeriodEndDate = Arrays.copyOf(fixingPeriodEndDate, nRates);
    _fixingPeriodAccrualFactor = Arrays.copyOf(fixingPeriodAccrualFactor, nRates);
  }

  /**
   * Construct a coupon without start dates and end dates of fixing period
   * @param currency The coupon currency
   * @param paymentDate The coupon payment date.
   * @param accrualStartDate The start date of the accrual period
   * @param accrualEndDate The end date of the accrual period
   * @param paymentAccrualFactor The accrual factor of the accrual period
   * @param notional The coupon notional
   * @param fixingDate The coupon fixing dates
   * @param index The coupon Ibor indices
   * @param weight The weights for the indices
   * @param iborCalendar The holiday calendars for the indices
   * @return The coupon
   */
  public static CouponIborAverageFixingDatesDefinition from(final Currency currency, final ZonedDateTime paymentDate, final ZonedDateTime accrualStartDate, final ZonedDateTime accrualEndDate,
      final double paymentAccrualFactor, final double notional, final IborIndex index, final ZonedDateTime[] fixingDate, final double[] weight, final HolidayCalendar iborCalendar) {
    return new CouponIborAverageFixingDatesDefinition(currency, paymentDate, accrualStartDate, accrualEndDate, paymentAccrualFactor, notional, index, fixingDate, weight, iborCalendar);
  }

  /**
   * Construct a coupon with start dates and end dates fixing period 
   * @param currency The coupon currency
   * @param paymentDate The coupon payment date.
   * @param accrualStartDate The start date of the accrual period
   * @param accrualEndDate The end date of the accrual period
   * @param paymentAccrualFactor The accrual factor of the accrual period
   * @param notional The coupon notional
   * @param fixingDate The coupon fixing dates
   * @param index The coupon Ibor indices
   * @param weight The weights for the indices
   * @param fixingPeriodStartDate The start date of the fixing periods
   * @param fixingPeriodEndDate The end date of the fixing periods
   * @param fixingPeriodAccrualFactor The accrual factor of the fixing periods
   * @return The coupon
   */
  public static CouponIborAverageFixingDatesDefinition from(final Currency currency, final ZonedDateTime paymentDate, final ZonedDateTime accrualStartDate, final ZonedDateTime accrualEndDate,
      final double paymentAccrualFactor, final double notional, final IborIndex index, final ZonedDateTime[] fixingDate, final double[] weight, final ZonedDateTime[] fixingPeriodStartDate,
      final ZonedDateTime[] fixingPeriodEndDate, final double[] fixingPeriodAccrualFactor) {
    return new CouponIborAverageFixingDatesDefinition(currency, paymentDate, accrualStartDate, accrualEndDate, paymentAccrualFactor, notional, index, fixingDate, weight, fixingPeriodStartDate,
        fixingPeriodEndDate, fixingPeriodAccrualFactor);
  }

  /**
   * Construct a new coupon with the same detail except notional
   * @param notional The notional
   * @return The coupon
   */
  public CouponIborAverageFixingDatesDefinition withNotional(final double notional) {
    return new CouponIborAverageFixingDatesDefinition(getCurrency(), getPaymentDate(), getAccrualStartDate(), getAccrualEndDate(), getPaymentYearFraction(), notional, getIndex(), getFixingDate(),
        getWeight(), getFixingPeriodStartDate(), getFixingPeriodEndDate(), getFixingPeriodAccrualFactor());
  }

  @Override
  public CouponIborAverageFixingDates toDerivative(final ZonedDateTime date) {
    ArgChecker.notNull(date, "date");
    final int nDates = _weight.length;
    final LocalDate dayConversion = date.toLocalDate();
    ArgChecker.isTrue(!dayConversion.isAfter(getPaymentDate().toLocalDate()), "date is after payment date");
    ArgChecker.isTrue(!dayConversion.isAfter(getFixingDate()[0].toLocalDate()), "Do not have any fixing data but are asking for a derivative at " + date
        + " which is after fixing date " + getFixingDate()[0]);
    // Fixing dates are in increasing order; only the first one need to be checked.
    final double paymentTime = TimeCalculator.getTimeBetween(date, getPaymentDate());
    final double[] fixingTime = new double[nDates];
    final double[] fixingPeriodStartTime = new double[nDates];
    final double[] fixingPeriodEndTime = new double[nDates];
    for (int i = 0; i < nDates; ++i) {
      fixingTime[i] = TimeCalculator.getTimeBetween(date, getFixingDate()[i]);
      fixingPeriodStartTime[i] = TimeCalculator.getTimeBetween(date, getFixingPeriodStartDate()[i]);
      fixingPeriodEndTime[i] = TimeCalculator.getTimeBetween(date, getFixingPeriodEndDate()[i]);
    }
    return new CouponIborAverageFixingDates(getCurrency(), paymentTime, getPaymentYearFraction(), getNotional(),
        getIndex(), fixingTime, getWeight(), fixingPeriodStartTime, fixingPeriodEndTime, getFixingPeriodAccrualFactor(), 0);
  }

  @Override
  public Coupon toDerivative(ZonedDateTime dateTime, DoubleTimeSeries<ZonedDateTime> indexFixingTimeSeries) {
    ArgChecker.notNull(dateTime, "date");
    final LocalDate dayConversion = dateTime.toLocalDate();
    ArgChecker.notNull(indexFixingTimeSeries, "Index fixing time series");
    ArgChecker.isTrue(!dayConversion.isAfter(getPaymentDate().toLocalDate()), "date is after payment date");
    final double paymentTime = TimeCalculator.getTimeBetween(dateTime, getPaymentDate());
    final int nDates = getFixingDate().length;
    if (dayConversion.isBefore(getFixingDate()[0].toLocalDate())) {
      return toDerivative(dateTime);
    }
    int position = 0;
    double amountAccrued = 0.;
    while (position < nDates && dayConversion.isAfter(getFixingDate()[position].toLocalDate())) { // Strictly after fixing date: fixing value should be available
      final Double fixedRate = indexFixingTimeSeries.getValue(getFixingDate()[position]);
      if (fixedRate == null) {
        throw new IllegalStateException("Could not get fixing value for date " + getFixingDate()[position]);
      }
      amountAccrued += getWeight()[position] * fixedRate;
      ++position;
    }
    if (position < nDates && dayConversion.equals(getFixingDate()[position].toLocalDate())) { // On Fixing date: use fixing value if available
      final Double fixedRate = indexFixingTimeSeries.getValue(getFixingDate()[position]);
      if (fixedRate != null) {
        amountAccrued += getWeight()[position] * fixedRate;
        ++position;
      }
    }
    if (position == nDates) { // If all fixing are known, return a couponFixed.
      return new CouponFixed(getCurrency(), paymentTime, getPaymentYearFraction(), getNotional(), amountAccrued, getAccrualStartDate(), getAccrualEndDate());
    }
    final int nDatesLeft = nDates - position; // If not all fixing are known, create a average coupon on the remaining period.
    final double[] fixingTime = new double[nDatesLeft];
    final double[] fixingPeriodStartTime = new double[nDatesLeft];
    final double[] fixingPeriodEndTime = new double[nDatesLeft];
    for (int i = 0; i < nDatesLeft; ++i) {
      fixingTime[i] = TimeCalculator.getTimeBetween(dateTime, getFixingDate()[position + i]);
      fixingPeriodStartTime[i] = TimeCalculator.getTimeBetween(dateTime, getFixingPeriodStartDate()[position + i]);
      fixingPeriodEndTime[i] = TimeCalculator.getTimeBetween(dateTime, getFixingPeriodEndDate()[position + i]);
    }
    final double[] weightLeft = new double[nDatesLeft];
    final double[] fixingPeriodAccrualFactorLeft = new double[nDatesLeft];
    System.arraycopy(getWeight(), position, weightLeft, 0, nDatesLeft);
    System.arraycopy(getFixingPeriodAccrualFactor(), position, fixingPeriodAccrualFactorLeft, 0, nDatesLeft);
    return new CouponIborAverageFixingDates(getCurrency(), paymentTime, getPaymentYearFraction(), getNotional(),
        getIndex(), fixingTime, weightLeft, fixingPeriodStartTime, fixingPeriodEndTime, fixingPeriodAccrualFactorLeft, amountAccrued);
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    ArgChecker.notNull(visitor, "visitor");
    return visitor.visitCouponIborAverageFixingDatesDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    ArgChecker.notNull(visitor, "visitor");
    return visitor.visitCouponIborAverageFixingDatesDefinition(this);
  }

  /**
   * Gets the fixingDate.
   * @return the fixingDate
   */
  public ZonedDateTime[] getFixingDate() {
    return _fixingDate;
  }

  /**
   * Gets the index.
   * @return the index
   */
  public IborIndex getIndex() {
    return _index;
  }

  /**
   * Gets the weight.
   * @return the weight
   */
  public double[] getWeight() {
    return _weight;
  }

  /**
   * Gets the fixingPeriodStartDate.
   * @return the fixingPeriodStartDate
   */
  public ZonedDateTime[] getFixingPeriodStartDate() {
    return _fixingPeriodStartDate;
  }

  /**
   * Gets the fixingPeriodEndDate.
   * @return the fixingPeriodEndDate
   */
  public ZonedDateTime[] getFixingPeriodEndDate() {
    return _fixingPeriodEndDate;
  }

  /**
   * Gets the fixingPeriodAccrualFactor.
   * @return the fixingPeriodAccrualFactor
   */
  public double[] getFixingPeriodAccrualFactor() {
    return _fixingPeriodAccrualFactor;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + Arrays.hashCode(_fixingDate);
    result = prime * result + Arrays.hashCode(_fixingPeriodAccrualFactor);
    result = prime * result + Arrays.hashCode(_fixingPeriodEndDate);
    result = prime * result + Arrays.hashCode(_fixingPeriodStartDate);
    result = prime * result + ((_index == null) ? 0 : _index.hashCode());
    result = prime * result + Arrays.hashCode(_weight);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (!(obj instanceof CouponIborAverageFixingDatesDefinition)) {
      return false;
    }
    CouponIborAverageFixingDatesDefinition other = (CouponIborAverageFixingDatesDefinition) obj;
    if (!Arrays.equals(_fixingDate, other._fixingDate)) {
      return false;
    }
    if (!Arrays.equals(_fixingPeriodAccrualFactor, other._fixingPeriodAccrualFactor)) {
      return false;
    }
    if (!Arrays.equals(_fixingPeriodEndDate, other._fixingPeriodEndDate)) {
      return false;
    }
    if (!Arrays.equals(_fixingPeriodStartDate, other._fixingPeriodStartDate)) {
      return false;
    }
    if (_index == null) {
      if (other._index != null) {
        return false;
      }
    } else if (!_index.equals(other._index)) {
      return false;
    }
    if (!Arrays.equals(_weight, other._weight)) {
      return false;
    }
    return true;
  }

}
