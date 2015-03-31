/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
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

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionWithData;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponONSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.analytics.util.timeseries.DoubleTimeSeries;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.collect.ArgChecker;
import com.opengamma.strata.collect.timeseries.LocalDateDoubleTimeSeries;

/**
 * Class describing a OIS-like floating coupon.
 */
public class CouponONSpreadDefinition extends CouponDefinition 
  implements InstrumentDefinitionWithData<Payment, DoubleTimeSeries<ZonedDateTime>> {

  /**
   * The OIS-like index on which the coupon fixes. The index currency should be the same as the coupon currency.
   */
  private final IndexON _index;
  /**
   * The dates of the fixing periods. The length is one greater than the number of periods, as it includes accrual start and end.
   */
  private final ZonedDateTime[] _fixingPeriodDate;
  /**
   * The accrual factors (or year fractions) associated to the fixing periods in the Index day count convention.
   */
  private final Double[] _fixingPeriodAccrualFactor;
  /**
   * The spread
   */
  private final double _spread;

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
   * @param spread The spread
   */
  public CouponONSpreadDefinition(final Currency currency, final ZonedDateTime paymentDate, final ZonedDateTime accrualStartDate, 
      final ZonedDateTime accrualEndDate, final double paymentYearFraction, final double notional, final IndexON index, 
      final ZonedDateTime fixingPeriodStartDate, final ZonedDateTime fixingPeriodEndDate, final Calendar calendar, 
      final double spread) {
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
      fixingAccrualFactorList.add(index.getDayCount().getDayCountFraction(currentDate, nextDate, calendar));
      currentDate = nextDate;
    }
    _fixingPeriodDate = fixingDateList.toArray(new ZonedDateTime[fixingDateList.size()]);
    _fixingPeriodAccrualFactor = fixingAccrualFactorList.toArray(new Double[fixingAccrualFactorList.size()]);
    _spread = spread;
  }

  /**
   * Builder from financial details. The accrual and fixing start and end dates are the same. 
   * The day count for the payment is the same as the one for the index.
   * The payment date is adjusted by the publication lag and the settlement days.
   * @param index The OIS index.
   * @param settlementDate The coupon settlement date.
   * @param tenor The coupon tenor.
   * @param notional The notional.
   * @param settlementDays The number of days between last fixing and the payment (also called spot lag).
   * @param businessDayConvention The business day convention to compute the end date of the coupon.
   * @param isEOM The end-of-month convention to compute the end date of the coupon.
   * @param calendar The holiday calendar for the overnight index.
   * @param spread The spread
   * @return The OIS coupon.
   */
  public static CouponONSpreadDefinition from(final IndexON index, final ZonedDateTime settlementDate, final Period tenor,
      final double notional, final int settlementDays, final BusinessDayConvention businessDayConvention, 
      final boolean isEOM, final Calendar calendar, final double spread) {
    final ZonedDateTime fixingPeriodEndDate = ScheduleCalculator.getAdjustedDate(settlementDate, tenor, 
        businessDayConvention, calendar, isEOM);
    return from(index, settlementDate, fixingPeriodEndDate, notional, settlementDays, calendar, spread);
  }

  /**
   * Builder from financial details. The accrual and fixing start and end dates are the same. 
   * The day count for the payment is the same as the one for the index.
   * The payment date is adjusted by the publication lag and the settlement days.
   * @param index The OIS index.
   * @param settlementDate The coupon settlement date.
   * @param fixingPeriodEndDate The last date of the fixing period. Interest accrues up to this date. 
   *   If publicationLag==0, 1 day following publication. If lag==1, the publication date.
   * @param notional The notional.
   * @param settlementDays The number of days between last fixing date and the payment fate (also called payment lag).
   * @param calendar The holiday calendar for the overnight index.
   * @param spread The spread
   * @return The OIS coupon.
   */
  public static CouponONSpreadDefinition from(final IndexON index, final ZonedDateTime settlementDate, 
      final ZonedDateTime fixingPeriodEndDate, final double notional, final int settlementDays, final Calendar calendar, 
      final double spread) {
    final ZonedDateTime paymentDate = ScheduleCalculator.getAdjustedDate(fixingPeriodEndDate, 
        -1 + index.getPublicationLag() + settlementDays, calendar);
    final double paymentYearFraction = index.getDayCount().getDayCountFraction(settlementDate, fixingPeriodEndDate, calendar);
    return new CouponONSpreadDefinition(index.getCurrency(), paymentDate, settlementDate, fixingPeriodEndDate, 
        paymentYearFraction, notional, index, settlementDate, fixingPeriodEndDate, calendar, spread);
  }

  /**
   * Gets the OIS index of the instrument.
   * @return The index.
   */
  public IndexON getIndex() {
    return _index;
  }

  /**
   * Gets the dates of the fixing periods (start and end). There is one date more than period.
   * @return The dates of the fixing periods.
   */
  public ZonedDateTime[] getFixingPeriodDate() {
    return _fixingPeriodDate;
  }

  /**
   * Gets the accrual factors (or year fractions) associated to the fixing periods in the Index day count convention.
   * @return The accrual factors.
   */
  public Double[] getFixingPeriodAccrualFactor() {
    return _fixingPeriodAccrualFactor;
  }

  /**
   * Gets the spread.
   * @return The spread
   */
  public double getSpread() {
    return _spread;
  }

  @Override
  public Coupon toDerivative(final ZonedDateTime date) {
    ArgChecker.notNull(date, "date");
    final LocalDate firstPublicationDate = _fixingPeriodDate[_index.getPublicationLag()].toLocalDate(); // This is often one business day following the first fixing date
    ArgChecker.isTrue(date.toLocalDate().isBefore(firstPublicationDate),
        "toDerivative method without time series as argument is only valid at dates where the first fixing has not yet been published.");
    final double paymentTime = TimeCalculator.getTimeBetween(date, getPaymentDate());
    final double fixingPeriodStartTime = TimeCalculator.getTimeBetween(date, _fixingPeriodDate[0]);
    final double fixingPeriodEndTime = TimeCalculator.getTimeBetween(date, _fixingPeriodDate[_fixingPeriodDate.length - 1]);
    double fixingAccrualFactorTotal = 0.0;
    for (final Double element : _fixingPeriodAccrualFactor) {
      fixingAccrualFactorTotal += element;
    }
    double spreadAmount = _spread * getNotional() * getPaymentYearFraction();
    final CouponONSpread cpn = new CouponONSpread(getCurrency(), paymentTime, getPaymentYearFraction(), getNotional(), 
        _index, fixingPeriodStartTime, fixingPeriodEndTime, fixingAccrualFactorTotal, getNotional(), spreadAmount);
    return cpn;
  }

  @Override
  public Coupon toDerivative(final ZonedDateTime valZdt, final DoubleTimeSeries<ZonedDateTime> indexFixingTimeSeries) {
    ArgChecker.notNull(valZdt, "valZdt - valuation date as ZonedDateTime");
    final LocalDate valDate = valZdt.toLocalDate();
    ArgChecker.isTrue(!valDate.isAfter(getPaymentDate().toLocalDate()), "valuation date is after payment date");
    final LocalDate firstPublicationDate = _fixingPeriodDate[_index.getPublicationLag()].toLocalDate(); // This is often one business day following the first fixing date
    if (valDate.isBefore(firstPublicationDate)) {
      return toDerivative(valZdt);
    }

    // FIXME Historical time series do not have time information to begin with.

    final LocalDateDoubleTimeSeries indexFixingDateSeries = indexFixingTimeSeries.toLocalDateDoubleTimeSeries();

    // Accrue notional for fixings before today; up to and including yesterday
    int fixedPeriod = 0;
    double accruedNotional = getNotional();
    while (valDate.isAfter(_fixingPeriodDate[fixedPeriod + _index.getPublicationLag()].toLocalDate()) && 
        (fixedPeriod < _fixingPeriodDate.length - 1)) {

      final LocalDate currentDate = _fixingPeriodDate[fixedPeriod].toLocalDate();
      OptionalDouble fixedRate = indexFixingDateSeries.get(currentDate);

      if (!fixedRate.isPresent()) {
        final LocalDate latestDate = indexFixingDateSeries.getLatestDate();
        if (currentDate.isAfter(latestDate)) {
          throw new IllegalStateException("Could not get fixing value of index " + _index.getName() + " for date "
            + currentDate + ". The last data is available on " + latestDate);
        }
        // Don't remove this until we've worked out what's going on with INR calendars
        for (int i = 0; i < 7; i++) {
          final LocalDate previousDate = currentDate.minusDays(1);
          fixedRate = indexFixingDateSeries.get(previousDate);
        }
        if (!fixedRate.isPresent()) {
          throw new IllegalStateException("Could not get fixing value of index " + _index.getName() + " for date " + currentDate);
        }
      }
      accruedNotional *= 1 + _fixingPeriodAccrualFactor[fixedPeriod] * fixedRate.getAsDouble();
      fixedPeriod++;
    }

    final double paymentTime = TimeCalculator.getTimeBetween(valZdt, getPaymentDate());
    if (fixedPeriod < _fixingPeriodDate.length - 1) { // Some OIS period left
      // Check to see if a fixing is available on current date
      OptionalDouble fixedRate = indexFixingDateSeries.get(_fixingPeriodDate[fixedPeriod + _index.getPublicationLag()].toLocalDate());
      if (fixedRate.isPresent()) { // There is!
        accruedNotional *= 1 + _fixingPeriodAccrualFactor[fixedPeriod] * (fixedRate.getAsDouble() + _spread);
        fixedPeriod++;
      }
      if (fixedPeriod < _fixingPeriodDate.length - 1) { // More OIS period left
        final double fixingPeriodStartTime = TimeCalculator.getTimeBetween(valZdt, _fixingPeriodDate[fixedPeriod + _index.getPublicationLag()]);
        final double fixingPeriodEndTime = TimeCalculator.getTimeBetween(valZdt, _fixingPeriodDate[_fixingPeriodDate.length - 1]);
        double fixingAccrualFactorLeft = 0.0;
        for (int loopperiod = fixedPeriod; loopperiod < _fixingPeriodAccrualFactor.length; loopperiod++) {
          fixingAccrualFactorLeft += _fixingPeriodAccrualFactor[loopperiod];
        }
        double spreadAmount = _spread * getNotional() * getPaymentYearFraction();
        return new CouponONSpread(getCurrency(), paymentTime, getPaymentYearFraction(), getNotional(),
            _index, fixingPeriodStartTime, fixingPeriodEndTime, fixingAccrualFactorLeft, accruedNotional, spreadAmount);
      }
      return new CouponFixed(getCurrency(), paymentTime, getPaymentYearFraction(), getNotional(), 
          (accruedNotional / getNotional() - 1.0) / getPaymentYearFraction());
    }
    // All fixed already
    return new CouponFixed(getCurrency(), paymentTime, getPaymentYearFraction(), getNotional(), 
        (accruedNotional / getNotional() - 1.0) / getPaymentYearFraction());
  }


  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    ArgChecker.notNull(visitor, "visitor");
    return visitor.visitCouponONSpreadDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    ArgChecker.notNull(visitor, "visitor");
    return visitor.visitCouponONSpreadDefinition(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + Arrays.hashCode(_fixingPeriodAccrualFactor);
    result = prime * result + Arrays.hashCode(_fixingPeriodDate);
    result = prime * result + _index.hashCode();
    final long temp = Double.doubleToLongBits(_spread);
    result = prime * result + (int) (temp ^ (temp >>> 32));
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
    final CouponONSpreadDefinition other = (CouponONSpreadDefinition) obj;
    if (Double.compare(_spread, other._spread) != 0) {
      return false;
    }
    if (!Arrays.deepEquals(_fixingPeriodAccrualFactor, other._fixingPeriodAccrualFactor)) {
      return false;
    }
    if (!Arrays.deepEquals(_fixingPeriodDate, other._fixingPeriodDate)) {
      return false;
    }
    if (!ObjectUtils.equals(_index, other._index)) {
      return false;
    }
    return true;
  }

}
