/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.payment;

import java.time.LocalDate;
import java.time.Period;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.OptionalDouble;

import com.opengamma.analytics.convention.daycount.DayCountUtils;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionWithData;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedCompoundedONCompounded;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponONCompounded;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.util.time.TimeCalculatorBUS252;
import com.opengamma.analytics.util.timeseries.DoubleTimeSeries;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.basics.date.BusinessDayConvention;
import com.opengamma.strata.basics.date.HolidayCalendar;
import com.opengamma.strata.collect.ArgChecker;
import com.opengamma.strata.collect.timeseries.LocalDateDoubleTimeSeries;

/**
 * Class describing a OIS-like floating compounded coupon. The pay-off of this coupon is :
 * $$
 * \begin{equation*}
 * \left(\prod_{i=1}^n (1+ r_i)^\delta_i \right)
 * \end{equation*}
 * $$
 * r_i the overnight rate for the fixing date t_i (or between t_i and t_{i+1})
 * \delta_i is the accrued between t_i and t_{i+1} using the appropriate day-count, for example if we use business/252 as daycounter \delta_i=1/252.
 *
 *  This coupon is especially used for Brazilian swaps with the day count business/252.
 * 
 */
public class CouponONCompoundedDefinition extends CouponDefinition implements InstrumentDefinitionWithData<Payment, DoubleTimeSeries<ZonedDateTime>> {

  /**
   * The OIS-like index on which the coupon fixes. The index currency should be the same as the coupon currency.
   */
  private final IndexON _index;
  /**
   * The dates of the fixing periods. The length is one greater than the number of periods, as it includes accrual start and end.
   */
  private final ZonedDateTime[] _fixingPeriodDates;
  /**
   * The accrual factors (or year fractions) associated to the fixing periods in the Index day count convention.
   */
  private final double[] _fixingPeriodAccrualFactors;
  /**
   * The calendar.
   */
  private final HolidayCalendar _calendar;

  /**
   * Constructor from all the coupon details.
   * @param currency The payment currency.
   * @param paymentDate Coupon payment date.
   * @param accrualStartDate Start date of the accrual period.
   * @param accrualEndDate End date of the accrual period.
   * @param paymentYearFraction Accrual factor of the accrual period.
   * @param notional Coupon notional.
   * @param index The OIS-like index on which the coupon fixes.
   * @param fixingPeriodStartDate The start date of the fixing period.
   * @param fixingPeriodEndDate The end date of the fixing period.
   * @param calendar The holiday calendar for the overnight index.
   */
  public CouponONCompoundedDefinition(final Currency currency, final ZonedDateTime paymentDate, final ZonedDateTime accrualStartDate, final ZonedDateTime accrualEndDate,
      final double paymentYearFraction, final double notional, final IndexON index, final ZonedDateTime fixingPeriodStartDate, final ZonedDateTime fixingPeriodEndDate,
      final HolidayCalendar calendar) {
    super(currency, paymentDate, accrualStartDate, accrualEndDate, paymentYearFraction, notional);
    ArgChecker.notNull(index, "CouponOISDefinition: index");
    ArgChecker.notNull(fixingPeriodStartDate, "CouponOISDefinition: fixingPeriodStartDate");
    ArgChecker.notNull(fixingPeriodEndDate, "CouponOISDefinition: fixingPeriodEndDate");
    ArgChecker.isTrue(currency.equals(index.getCurrency()), "Coupon and index currencies are not compatible. Expected to be the same");
    _index = index;

    final List<ZonedDateTime> fixingDateList = new ArrayList<>();
    final List<Double> fixingAccrualFactorList = new ArrayList<>();

    ZonedDateTime currentDate = fixingPeriodStartDate;
    fixingDateList.add(currentDate);
    ZonedDateTime nextDate;
    while (currentDate.isBefore(fixingPeriodEndDate)) {
      nextDate = ScheduleCalculator.getAdjustedDate(currentDate, 1, calendar);
      fixingDateList.add(nextDate);
      fixingAccrualFactorList.add(DayCountUtils.yearFraction(index.getDayCount(), currentDate, nextDate, calendar));
      currentDate = nextDate;
    }
    _fixingPeriodDates = fixingDateList.toArray(new ZonedDateTime[fixingDateList.size()]);
    _fixingPeriodAccrualFactors = new double[fixingAccrualFactorList.size()];
    for (int i = 0; i < fixingAccrualFactorList.size(); i++) {
      _fixingPeriodAccrualFactors[i] = fixingAccrualFactorList.get(i);
    }
    _calendar = calendar;
  }

  /**
   * Builder from financial details. The accrual and fixing start and end dates are the same. The day count for the payment is the same as the one for the index.
   * The payment date is adjusted by the publication lag and the settlement days.
   * @param index The OIS index.
   * @param settlementDate The coupon settlement date.
   * @param tenor The coupon tenor.
   * @param notional The notional.
   * @param settlementDays The number of days between last fixing and the payment (also called spot lag).
   * @param businessDayConvention The business day convention to compute the end date of the coupon.
   * @param isEOM The end-of-month convention to compute the end date of the coupon.
   * @param calendar The holiday calendar for the overnight index.
   * @return The OIS coupon.
   */
  public static CouponONCompoundedDefinition from(final IndexON index, final ZonedDateTime settlementDate, final Period tenor, final double notional, final int settlementDays,
      final BusinessDayConvention businessDayConvention, final boolean isEOM, final HolidayCalendar calendar) {
    final ZonedDateTime fixingPeriodEndDate = ScheduleCalculator.getAdjustedDate(settlementDate, tenor, businessDayConvention, calendar, isEOM);
    return from(index, settlementDate, fixingPeriodEndDate, notional, settlementDays, calendar);
  }

  /**
   * Builder from financial details. The accrual and fixing start and end dates are the same. The day count for the payment is the same as the one for the index.
   * The payment date is adjusted by the publication lag and the settlement days.
   * @param index The OIS index.
   * @param settlementDate The coupon settlement date.
   * @param fixingPeriodEndDate The last date of the fixing period. Interest accrues up to this date. If publicationLag==0, 1 day following publication. If lag==1, the publication date.
   * @param notional The notional.
   * @param settlementDays The number of days between last fixing date and the payment fate (also called payment lag).
   * @param calendar The holiday calendar for the overnight index.
   * @return The OIS coupon.
   */
  public static CouponONCompoundedDefinition from(final IndexON index, final ZonedDateTime settlementDate, final ZonedDateTime fixingPeriodEndDate, final double notional,
      final int settlementDays, final HolidayCalendar calendar) {
    final ZonedDateTime paymentDate = ScheduleCalculator.getAdjustedDate(fixingPeriodEndDate, -1 + index.getPublicationLag() + settlementDays, calendar);
    final double paymentYearFraction = DayCountUtils.yearFraction(index.getDayCount(), settlementDate, fixingPeriodEndDate, calendar);
    return new CouponONCompoundedDefinition(index.getCurrency(), paymentDate, settlementDate, fixingPeriodEndDate, paymentYearFraction, notional, index, settlementDate,
        fixingPeriodEndDate, calendar);
  }

  /**
   * Builder from financial details using genrator. The accrual and fixing start and end dates are the same. The day count for the payment is the same as the one for the index.
   *  And with the same fixingAccrualFactor for each period.
   * The payment date is adjusted by the publication lag and the settlement days.
   * @param generator the generator
   * @param settlementDate The coupon settlement date.
   * @param tenor The coupon tenor.
   * @param notional The notional.
   * @return The OIS coupon.
   */
  public static CouponONCompoundedDefinition from(final GeneratorSwapFixedCompoundedONCompounded generator, final ZonedDateTime settlementDate, final Period tenor, final double notional) {
    final ZonedDateTime fixingPeriodEndDate = ScheduleCalculator.getAdjustedDate(settlementDate, tenor, generator.getBusinessDayConvention(), generator.getOvernightCalendar(),
        generator.isEndOfMonth());
    return from(generator.getIndex(), settlementDate, fixingPeriodEndDate, notional, generator.getSpotLag(), generator.getOvernightCalendar());
  }

  /**
   * Builder from financial details. The accrual and fixing start and end dates are the same. The day count for the payment is the same as the one for the index.
   * And with the same fixingAccrualFactor for each period.
   * The payment date is adjusted by the publication lag and the settlement days.
   * @param generator the generator
   * @param settlementDate The coupon settlement date.
   * @param fixingPeriodEndDate The last date of the fixing period. Interest accrues up to this date. If publicationLag==0, 1 day following publication. If lag==1, the publication date.
   * @param notional The notional.
   * @return The OIS coupon.
   */
  public static CouponONCompoundedDefinition from(final GeneratorSwapFixedCompoundedONCompounded generator, final ZonedDateTime settlementDate, final ZonedDateTime fixingPeriodEndDate,
      final double notional) {
    final ZonedDateTime paymentDate = ScheduleCalculator.getAdjustedDate(fixingPeriodEndDate, -1 + generator.getIndex().getPublicationLag() + generator.getSpotLag(), generator.getOvernightCalendar());
    final double paymentYearFraction = DayCountUtils.yearFraction(generator.getIndex().getDayCount(), settlementDate, fixingPeriodEndDate, generator.getOvernightCalendar());
    return new CouponONCompoundedDefinition(generator.getIndex().getCurrency(), paymentDate, settlementDate, fixingPeriodEndDate, paymentYearFraction, notional, generator.getIndex(), settlementDate,
        fixingPeriodEndDate, generator.getOvernightCalendar());
  }

  /**
   * Gets the ON index of the instrument.
   * @return The index.
   */
  public IndexON getIndex() {
    return _index;
  }

  /**
   * Gets the calendar.
   * @return The calendar
   */
  public HolidayCalendar getCalendar() {
    return _calendar;
  }

  /**
   * Gets the dates of the fixing periods (start and end). There is one date more than period.
   * @return The dates of the fixing periods.
   */
  public ZonedDateTime[] getFixingPeriodDates() {
    return _fixingPeriodDates;
  }

  /**
   * Gets the accrual factors (or year fractions) associated to the fixing periods in the Index day count convention.
   * @return The accrual factors.
   */
  public double[] getFixingPeriodAccrualFactors() {
    return _fixingPeriodAccrualFactors;
  }

  @Override
  public CouponONCompounded toDerivative(final ZonedDateTime date) {
    ArgChecker.notNull(date, "date");
    final LocalDate firstPublicationDate = _fixingPeriodDates[_index.getPublicationLag()].toLocalDate(); // This is often one business day following the first fixing date
    ArgChecker.isTrue(date.toLocalDate().isBefore(firstPublicationDate),
        "toDerivative method without time series as argument is only valid at dates where the first fixing has not yet been published.");
    final double paymentTime = TimeCalculatorBUS252.getTimeBetween(date, getPaymentDate(), _calendar);
    final double[] fixingPeriodStartTimes = new double[_fixingPeriodDates.length - 1];
    final double[] fixingPeriodEndTimes = new double[_fixingPeriodDates.length - 1];
    for (int i = 0; i < _fixingPeriodDates.length - 1; i++) {
      fixingPeriodStartTimes[i] = TimeCalculatorBUS252.getTimeBetween(date, _fixingPeriodDates[i], _calendar);
      fixingPeriodEndTimes[i] = TimeCalculatorBUS252.getTimeBetween(date, _fixingPeriodDates[i + 1], _calendar);
    }

    return new CouponONCompounded(getCurrency(), paymentTime, getPaymentYearFraction(), getNotional(), _index, fixingPeriodStartTimes,
        fixingPeriodEndTimes, _fixingPeriodAccrualFactors, getNotional());
  }

  @Override
  public Coupon toDerivative(final ZonedDateTime valZdt, final DoubleTimeSeries<ZonedDateTime> indexFixingTimeSeries) {
    ArgChecker.notNull(valZdt, "valZdt - valuation date as ZonedDateTime");
    final LocalDate valDate = valZdt.toLocalDate();
    ArgChecker.isTrue(!valDate.isAfter(getPaymentDate().toLocalDate()), "valuation date is after payment date");
    final LocalDate firstPublicationDate = _fixingPeriodDates[_index.getPublicationLag()].toLocalDate(); // This is often one business day following the first fixing date
    if (valDate.isBefore(firstPublicationDate)) {
      return toDerivative(valZdt);
    }

    // FIXME Historical time series do not have time information to begin with.

    final LocalDateDoubleTimeSeries indexFixingDateSeries = indexFixingTimeSeries.toLocalDateDoubleTimeSeries();

    // Accrue notional for fixings before today; up to and including yesterday
    int fixedPeriod = 0;
    double accruedNotional = getNotional();
    while ((fixedPeriod < _fixingPeriodDates.length - 1) && valDate.isAfter(_fixingPeriodDates[fixedPeriod + _index.getPublicationLag()].toLocalDate())) {
      final LocalDate currentDate = _fixingPeriodDates[fixedPeriod].toLocalDate();
      final double fixedRate = indexFixingDateSeries.get(currentDate)
          .orElseThrow(() -> new IllegalStateException(
              "Could not get fixing value of index " + _index.getName() + " for date " + currentDate +
                  ". The last data is available on " + indexFixingDateSeries.getLatestDate()));

      accruedNotional *= Math.pow(1 + fixedRate, _fixingPeriodAccrualFactors[fixedPeriod]);
      fixedPeriod++;
    }

    final double paymentTime = TimeCalculatorBUS252.getTimeBetween(valZdt, getPaymentDate(), _calendar);
    if (fixedPeriod < _fixingPeriodDates.length - 1) { // Some OIS period left
      // Check to see if a fixing is available on current date
      final OptionalDouble fixedRate = indexFixingDateSeries.get(_fixingPeriodDates[fixedPeriod].toLocalDate());
      if (fixedRate.isPresent()) { // There is!
        accruedNotional *= Math.pow(1 + fixedRate.getAsDouble(), _fixingPeriodAccrualFactors[fixedPeriod]);
        fixedPeriod++;
      }
      if (fixedPeriod < _fixingPeriodDates.length - 1) { // More OIS period left
        final double[] fixingAccrualFactorsLeft = new double[_fixingPeriodAccrualFactors.length - fixedPeriod];
        final double[] fixingPeriodStartTimes = new double[_fixingPeriodDates.length - 1 - fixedPeriod];
        final double[] fixingPeriodEndTimes = new double[_fixingPeriodDates.length - 1 - fixedPeriod];
        for (int i = 0; i < _fixingPeriodDates.length - 1 - fixedPeriod; i++) {
          fixingPeriodStartTimes[i] = TimeCalculatorBUS252.getTimeBetween(valZdt, _fixingPeriodDates[i + fixedPeriod], _calendar);
          fixingPeriodEndTimes[i] = TimeCalculatorBUS252.getTimeBetween(valZdt, _fixingPeriodDates[i + 1 + fixedPeriod], _calendar);
        }

        System.arraycopy(_fixingPeriodAccrualFactors, fixedPeriod, fixingAccrualFactorsLeft, 0, _fixingPeriodAccrualFactors.length - fixedPeriod);
        return new CouponONCompounded(getCurrency(), paymentTime, getPaymentYearFraction(), getNotional(), _index, fixingPeriodStartTimes,
            fixingPeriodEndTimes, fixingAccrualFactorsLeft, accruedNotional);
      }
      return new CouponFixed(getCurrency(), paymentTime, getPaymentYearFraction(), getNotional(), (accruedNotional / getNotional() - 1.0)
          / getPaymentYearFraction());

    }

    // All fixed already
    return new CouponFixed(getCurrency(), paymentTime, getPaymentYearFraction(), getNotional(), (accruedNotional / getNotional() - 1.0)
        / getPaymentYearFraction());
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    ArgChecker.notNull(visitor, "visitor");
    return visitor.visitCouponONCompoundedDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    ArgChecker.notNull(visitor, "visitor");
    return visitor.visitCouponONCompoundedDefinition(this);
  }

  @Override
  public String toString() {
    return "CouponONCompoundedDefinition [_fixingPeriodDate=" + Arrays.toString(_fixingPeriodDates) + ", _fixingPeriodAccrualFactor=" + Arrays.toString(_fixingPeriodAccrualFactors) + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + Arrays.hashCode(_fixingPeriodAccrualFactors);
    result = prime * result + Arrays.hashCode(_fixingPeriodDates);
    result = prime * result + ((_index == null) ? 0 : _index.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final CouponONCompoundedDefinition other = (CouponONCompoundedDefinition) obj;
    if (!Arrays.equals(_fixingPeriodAccrualFactors, other._fixingPeriodAccrualFactors)) {
      return false;
    }
    if (!Arrays.equals(_fixingPeriodDates, other._fixingPeriodDates)) {
      return false;
    }
    if (_index == null) {
      if (other._index != null) {
        return false;
      }
    } else if (!_index.equals(other._index)) {
      return false;
    }
    return true;
  }

}
