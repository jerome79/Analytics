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
import java.util.Objects;
import java.util.OptionalDouble;

import com.google.common.primitives.Doubles;
import com.opengamma.analytics.convention.daycount.DayCountUtils;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionWithData;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponONArithmeticAverageSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.analytics.util.timeseries.DoubleTimeSeries;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.basics.date.BusinessDayConvention;
import com.opengamma.strata.basics.date.HolidayCalendar;
import com.opengamma.strata.collect.ArgChecker;
import com.opengamma.strata.collect.timeseries.LocalDateDoubleTimeSeries;

/**
 * Class describing a Fed Fund swap-like floating coupon (arithmetic average on overnight rates) with a spread.
 */
public class CouponONArithmeticAverageSpreadDefinition extends CouponDefinition implements InstrumentDefinitionWithData<Payment, DoubleTimeSeries<ZonedDateTime>> {

  /**
   * The overnight index on which the coupon fixes. The index currency should be the same as the coupon currency. Not null.
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
   * The spread rate paid above the arithmetic average.
   */
  private final double _spread;
  /**
   * The fixed amount related to the spread.
   */
  private final double _spreadAmount;

  // TODO: Implement the rate cut-off mechanism (the two last periods use the same fixing)

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
   * @param spread The spread rate paid above the arithmetic average.
   * @param calendar The holiday calendar for the overnight index.
   */
  public CouponONArithmeticAverageSpreadDefinition(final Currency currency, final ZonedDateTime paymentDate, final ZonedDateTime accrualStartDate, final ZonedDateTime accrualEndDate,
      final double paymentYearFraction, final double notional, final IndexON index, final ZonedDateTime fixingPeriodStartDate, final ZonedDateTime fixingPeriodEndDate, final double spread,
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
      final double af = DayCountUtils.yearFraction(index.getDayCount(), currentDate, nextDate, calendar);
      fixingAccrualFactorList.add(af);
      currentDate = nextDate;
    }
    _fixingPeriodDates = fixingDateList.toArray(new ZonedDateTime[fixingDateList.size()]);
    _fixingPeriodAccrualFactors = Doubles.toArray(fixingAccrualFactorList);
    _spread = spread;
    _spreadAmount = spread * paymentYearFraction * notional;
  }

  /**
   * Builder from financial details. The accrual and fixing start and end dates are the same. The day count for the payment is the same as the one for the index.
   * The payment date is adjusted by the publication lag and the settlement days.
   * @param index The OIS index.
   * @param fixingPeriodStartDate The coupon settlement date and start of the fixing period.
   * @param tenor The coupon tenor.
   * @param notional The notional.
   * @param paymentLag The number of days between last fixing date and the payment date (also called payment delay).
   * @param businessDayConvention The business day convention to compute the end date of the coupon.
   * @param isEOM The end-of-month convention to compute the end date of the coupon.
   * @param spread The spread rate paid above the arithmetic average.
   * @param calendar The holiday calendar for the overnight index.
   * @return The OIS coupon.
   */
  public static CouponONArithmeticAverageSpreadDefinition from(final IndexON index, final ZonedDateTime fixingPeriodStartDate, final Period tenor, final double notional, final int paymentLag,
      final BusinessDayConvention businessDayConvention, final boolean isEOM, final double spread, final HolidayCalendar calendar) {
    final ZonedDateTime fixingPeriodEndDate = ScheduleCalculator.getAdjustedDate(fixingPeriodStartDate, tenor, businessDayConvention, calendar, isEOM);
    return from(index, fixingPeriodStartDate, fixingPeriodEndDate, notional, paymentLag, spread, calendar);
  }

  /**
   * Builder from financial details. The accrual and fixing start and end dates are the same. The day count for the payment is the same as the one for the index.
   * The payment date is adjusted by the publication lag and the settlement days.
   * @param index The OIS index.
   * @param fixingPeriodStartDate The coupon settlement date and start of the fixing period.
   * @param fixingPeriodEndDate The last date of the fixing period. Interest accrues up to this date. If publicationLag==0, 1 day following publication. If lag==1, the publication date.
   * @param notional The notional.
   * @param paymentLag The number of days between last fixing date and the payment date (also called payment delay).
   * @param spread The spread rate paid above the arithmetic average.
   * @param calendar The holiday calendar for the overnight index.
   * @return The OIS coupon.
   */
  public static CouponONArithmeticAverageSpreadDefinition from(final IndexON index, final ZonedDateTime fixingPeriodStartDate, final ZonedDateTime fixingPeriodEndDate, final double notional,
      final int paymentLag, final double spread, final HolidayCalendar calendar) {
    final ZonedDateTime paymentDate = ScheduleCalculator.getAdjustedDate(fixingPeriodEndDate, -1 + index.getPublicationLag() + paymentLag, calendar);
    final double paymentYearFraction = DayCountUtils.yearFraction(index.getDayCount(), fixingPeriodStartDate, fixingPeriodEndDate, calendar);
    return new CouponONArithmeticAverageSpreadDefinition(index.getCurrency(), paymentDate, fixingPeriodStartDate, fixingPeriodEndDate, paymentYearFraction, notional, index, fixingPeriodStartDate,
        fixingPeriodEndDate, spread, calendar);
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

  /**
   * Returns the spread rate paid above the arithmetic average.
   * @return The spread.
   */
  public double getSpread() {
    return _spread;
  }

  /**
   * Returns the fixed amount related to the spread.
   * @return The amount.
   */
  public double getSpreadAmount() {
    return _spreadAmount;
  }

  @Override
  public CouponONArithmeticAverageSpread toDerivative(final ZonedDateTime date) {
    ArgChecker.isTrue(!_fixingPeriodDates[0].plusDays(_index.getPublicationLag()).isBefore(date), "First fixing publication strictly before reference date");
    final double paymentTime = TimeCalculator.getTimeBetween(date, getPaymentDate());
    final double[] fixingPeriodTimes = TimeCalculator.getTimeBetween(date, _fixingPeriodDates);
    return CouponONArithmeticAverageSpread.from(paymentTime, getPaymentYearFraction(), getNotional(), _index, fixingPeriodTimes, _fixingPeriodAccrualFactors, 0, _spread);
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

    // Accrued rate for fixings before today; up to and including yesterday
    int fixedPeriod = 0;
    double accruedRate = 0.0;
    while ((fixedPeriod < _fixingPeriodDates.length - 1) && valDate.isAfter(_fixingPeriodDates[fixedPeriod + _index.getPublicationLag()].toLocalDate())) {

      final LocalDate currentDate = _fixingPeriodDates[fixedPeriod].toLocalDate();
      double fixedRate = indexFixingDateSeries.get(currentDate)
          .orElseThrow(() -> new IllegalStateException(
              "Could not get fixing value of index " + _index.getName() + " for date " + currentDate +
                  ". The last data is available on " + indexFixingDateSeries.getLatestDate()));

      accruedRate += _fixingPeriodAccrualFactors[fixedPeriod] * fixedRate;
      fixedPeriod++;
    }

    final double paymentTime = TimeCalculator.getTimeBetween(valZdt, getPaymentDate());
    if (fixedPeriod < _fixingPeriodDates.length - 1) { // Some OIS period left
      // Check to see if a fixing is available on current date
      final OptionalDouble fixedRate = indexFixingDateSeries.get(_fixingPeriodDates[fixedPeriod].toLocalDate());
      if (fixedRate.isPresent()) { // There is!
        accruedRate += _fixingPeriodAccrualFactors[fixedPeriod] * fixedRate.getAsDouble();
        fixedPeriod++;
      }
      if (fixedPeriod < _fixingPeriodDates.length - 1) { // More OIS period left
        final double[] fixingAccrualFactorsLeft = new double[_fixingPeriodAccrualFactors.length - fixedPeriod];
        final double[] fixingPeriodTimes = new double[_fixingPeriodDates.length - fixedPeriod];

        for (int i = 0; i < _fixingPeriodDates.length - fixedPeriod; i++) {
          fixingPeriodTimes[i] = TimeCalculator.getTimeBetween(valZdt, _fixingPeriodDates[i + fixedPeriod]);
        }

        System.arraycopy(_fixingPeriodAccrualFactors, fixedPeriod, fixingAccrualFactorsLeft, 0, _fixingPeriodAccrualFactors.length - fixedPeriod);
        return CouponONArithmeticAverageSpread.from(paymentTime, getPaymentYearFraction(), getNotional(), _index, fixingPeriodTimes,
            fixingAccrualFactorsLeft, accruedRate, _spread);
      }
      return new CouponFixed(getCurrency(), paymentTime, getPaymentYearFraction(), getNotional(), accruedRate / getPaymentYearFraction());
    }

    // All fixed already
    return new CouponFixed(getCurrency(), paymentTime, getPaymentYearFraction(), getNotional(), accruedRate / getPaymentYearFraction());
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    ArgChecker.notNull(visitor, "visitor");
    return visitor.visitCouponArithmeticAverageONSpreadDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    ArgChecker.notNull(visitor, "visitor");
    return visitor.visitCouponArithmeticAverageONSpreadDefinition(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + Arrays.hashCode(_fixingPeriodAccrualFactors);
    result = prime * result + Arrays.hashCode(_fixingPeriodDates);
    result = prime * result + _index.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_spread);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_spreadAmount);
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
    final CouponONArithmeticAverageSpreadDefinition other = (CouponONArithmeticAverageSpreadDefinition) obj;
    if (!Arrays.equals(_fixingPeriodAccrualFactors, other._fixingPeriodAccrualFactors)) {
      return false;
    }
    if (!Arrays.equals(_fixingPeriodDates, other._fixingPeriodDates)) {
      return false;
    }
    if (!Objects.equals(_index, other._index)) {
      return false;
    }
    if (Double.doubleToLongBits(_spread) != Double.doubleToLongBits(other._spread)) {
      return false;
    }
    if (Double.doubleToLongBits(_spreadAmount) != Double.doubleToLongBits(other._spreadAmount)) {
      return false;
    }
    return true;
  }

}
