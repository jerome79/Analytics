/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.bond;

import java.time.ZonedDateTime;

import com.opengamma.analytics.convention.businessday.BusinessDayDateUtils;
import com.opengamma.analytics.convention.daycount.DayCount;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionWithData;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponIborDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityPaymentFixedDefinition;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.payment.CouponIborDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentFixedDefinition;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityPaymentFixed;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondIborSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondSecurity;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.util.time.DateUtils;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.analytics.util.timeseries.DoubleTimeSeries;
import com.opengamma.analytics.util.timeseries.zdt.ImmutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.strata.basics.date.BusinessDayConvention;
import com.opengamma.strata.basics.date.HolidayCalendar;
import com.opengamma.strata.collect.ArgChecker;

/**
 * Describes a floating coupon bond (or Floating Rate Note) issue with Ibor-like coupon.
 */
public class BondIborSecurityDefinition extends BondSecurityDefinition<PaymentFixedDefinition, CouponIborDefinition>
    implements InstrumentDefinitionWithData<BondSecurity<? extends Payment, ? extends Coupon>, DoubleTimeSeries<ZonedDateTime>> {

  /**
   * The default notional for the security.
   */
  private static final double DEFAULT_NOTIONAL = 1.0;
  /**
   * The default ex-coupon number of days.
   */
  private static final int DEFAULT_EX_COUPON_DAYS = 0;
  /**
   * The coupon day count convention.
   */
  private final DayCount _dayCount;

  /**
   * Fixed coupon bond constructor from all the bond details. This constructor creates a legal entity
   * that contains only the short name of the issuer.
   * @param nominal The notional payments. For bullet bond, it is restricted to a single payment.
   * @param coupon The bond Ibor coupons. The coupons notional and currency should be in line with the bond nominal.
   * @param exCouponDays Number of days before the payment of the coupon is detached from the bond (and paid to the then owner).
   * @param settlementDays Standard number of days between trade date and trade settlement. Used for clean price and yield computation.
   * @param calendar The calendar used to compute the standard settlement date.
   * @param dayCount The coupon day count convention.
   * @param issuer The issuer name.
   */
  public BondIborSecurityDefinition(final AnnuityPaymentFixedDefinition nominal, final AnnuityCouponIborDefinition coupon, final int exCouponDays, final int settlementDays,
      final HolidayCalendar calendar,
      final DayCount dayCount, final String issuer) {
    this(nominal, coupon, exCouponDays, settlementDays, calendar, dayCount, new LegalEntity(null, issuer, null, null, null));
  }

  /**
   * Fixed coupon bond constructor from all the bond details.
   * @param nominal The notional payments, not null. For bullet bond, it is restricted to a single payment.
   * @param coupon The bond Ibor coupons. The coupons notional and currency should be in line with the bond nominal, not null.
   * @param exCouponDays Number of days before the payment of the coupon is detached from the bond (and paid to the then owner), not null.
   * @param settlementDays Standard number of days between trade date and trade settlement, not null. Used for clean price and yield computation.
   * @param calendar The calendar used to compute the standard settlement date, not null
   * @param dayCount The coupon day count convention, not null
   * @param issuer The issuer name.
   */
  public BondIborSecurityDefinition(final AnnuityPaymentFixedDefinition nominal, final AnnuityCouponIborDefinition coupon, final int exCouponDays, final int settlementDays,
      final HolidayCalendar calendar,
      final DayCount dayCount, final LegalEntity issuer) {
    super(nominal, coupon, exCouponDays, settlementDays, calendar, issuer);
    ArgChecker.notNull(dayCount, "day count");
    _dayCount = dayCount;
  }

  /**
   * Fixed coupon bond builder from standard financial details. The accrual dates are unadjusted; the payment dates are adjusted according to the business day convention.
   * @param maturityDate The maturity date.
   * @param firstAccrualDate The first accrual date (bond start date).
   * @param index The coupon Ibor index.
   * @param settlementDays Standard number of days between trade date and trade settlement. Used for clean price and yield computation.
   * @param dayCount The coupon day count convention.
   * @param businessDay The business day convention for the payments.
   * @param isEOM The end-of-month flag.
   * @param issuer The issuer name.
   * @param calendar The holiday calendar for the ibor leg.
   * @return The fixed coupon bond.
   */
  public static BondIborSecurityDefinition from(final ZonedDateTime maturityDate, final ZonedDateTime firstAccrualDate, final IborIndex index, final int settlementDays, final DayCount dayCount,
      final BusinessDayConvention businessDay, final boolean isEOM, final String issuer, final HolidayCalendar calendar) {
    return from(maturityDate, firstAccrualDate, index, settlementDays, dayCount, businessDay, isEOM, new LegalEntity(null, issuer, null, null, null), calendar);
  }

  /**
   * Fixed coupon bond builder from standard financial details. The accrual dates are unadjusted; the payment dates are adjusted according to the business day convention.
   * @param maturityDate The maturity date.
   * @param firstAccrualDate The first accrual date (bond start date).
   * @param index The coupon Ibor index.
   * @param settlementDays Standard number of days between trade date and trade settlement. Used for clean price and yield computation.
   * @param dayCount The coupon day count convention.
   * @param businessDay The business day convention for the payments.
   * @param isEOM The end-of-month flag.
   * @param issuer The issuer name.
   * @param calendar The holiday calendar for the ibor leg.
   * @return The fixed coupon bond.
   */
  public static BondIborSecurityDefinition from(final ZonedDateTime maturityDate, final ZonedDateTime firstAccrualDate, final IborIndex index, final int settlementDays, final DayCount dayCount,
      final BusinessDayConvention businessDay, final boolean isEOM, final LegalEntity issuer, final HolidayCalendar calendar) {
    ArgChecker.notNull(maturityDate, "Maturity date");
    ArgChecker.notNull(firstAccrualDate, "First accrual date");
    ArgChecker.notNull(index, "Ibor index");
    ArgChecker.notNull(dayCount, "Day count");
    ArgChecker.notNull(businessDay, "Business day convention");
    final AnnuityCouponIborDefinition coupon = AnnuityCouponIborDefinition.fromAccrualUnadjusted(firstAccrualDate, maturityDate, DEFAULT_NOTIONAL, index, false, calendar);
    ZonedDateTime adjusted = BusinessDayDateUtils.applyConvention(businessDay, maturityDate, calendar);
    final PaymentFixedDefinition[] nominalPayment = new PaymentFixedDefinition[] {
      new PaymentFixedDefinition(index.getCurrency(), adjusted, DEFAULT_NOTIONAL) };
    final AnnuityPaymentFixedDefinition nominal = new AnnuityPaymentFixedDefinition(nominalPayment, calendar);
    return new BondIborSecurityDefinition(nominal, coupon, DEFAULT_EX_COUPON_DAYS, settlementDays, calendar, dayCount, issuer);
  }

  /**
   * Gets the coupon day count.
   * @return The day count.
   */
  public DayCount getDayCount() {
    return _dayCount;
  }

  /**
   * @param date The valuation date, not null
   * @param indexFixingTS The index fixing time series, not null
   * @param settlementDate The settlement date, not null
   * @param yieldCurveNames The yield curve names, not null, must have at least two entries
   * @return The security
   * @deprecated Use the version that does not take curve names
   */
  @Deprecated
  public BondIborSecurity toDerivative(final ZonedDateTime date, final DoubleTimeSeries<ZonedDateTime> indexFixingTS, final ZonedDateTime settlementDate,
      final String... yieldCurveNames) {
    throw new UnsupportedOperationException();
  }

  @Override
  public BondIborSecurity toDerivative(final ZonedDateTime date) {
    ArgChecker.notNull(date, "date");
    final ZonedDateTime spot = ScheduleCalculator.getAdjustedDate(date, getSettlementDays(), getCalendar());
    return toDerivative(date, ImmutableZonedDateTimeDoubleTimeSeries.of(DateUtils.getUTCDate(1800, 1, 1), 0.0), spot);
  }

  @Override
  public BondIborSecurity toDerivative(final ZonedDateTime date, final DoubleTimeSeries<ZonedDateTime> indexFixingTS) {
    ArgChecker.notNull(date, "date");
    final ZonedDateTime spot = ScheduleCalculator.getAdjustedDate(date, getSettlementDays(), getCalendar());
    return toDerivative(date, indexFixingTS, spot);
  }

  /**
   * @param date The valuation date, not null
   * @param indexFixingTS The index fixing time series, not null
   * @param settlementDate The settlement date, not null
   * @return The security
   */
  public BondIborSecurity toDerivative(final ZonedDateTime date, final DoubleTimeSeries<ZonedDateTime> indexFixingTS, final ZonedDateTime settlementDate) {
    ArgChecker.notNull(date, "date");
    ArgChecker.notNull(indexFixingTS, "fixing time series");
    ArgChecker.notNull(settlementDate, "settlement date");
    double settlementTime;
    if (settlementDate.isBefore(date)) {
      settlementTime = 0.0;
    } else {
      settlementTime = TimeCalculator.getTimeBetween(date, settlementDate);
    }
    final AnnuityPaymentFixed nominal = (AnnuityPaymentFixed) getNominal().toDerivative(date);
    final Annuity<Coupon> coupon = (Annuity<Coupon>) getCoupons().toDerivative(date, indexFixingTS);
    return new BondIborSecurity(nominal.trimBefore(settlementTime), coupon.trimBefore(settlementTime), settlementTime);
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    ArgChecker.notNull(visitor, "visitor");
    return visitor.visitBondIborSecurityDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    ArgChecker.notNull(visitor, "visitor");
    return visitor.visitBondIborSecurityDefinition(this);
  }

}
