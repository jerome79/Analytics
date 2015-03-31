/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.commodity.multicurvecommodity.definition;

import java.time.ZonedDateTime;

import com.opengamma.analytics.financial.commodity.multicurvecommodity.derivative.CouponCommodityCashSettle;
import com.opengamma.analytics.financial.commodity.multicurvecommodity.underlying.CommodityUnderlying;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.payments.derivative.PaymentFixed;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.analytics.util.timeseries.DoubleTimeSeries;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.strata.collect.ArgChecker;

/**
 * Class describing a Coupon Commodity Cash Settle.
 */
public class CouponCommodityCashSettleDefinition extends CouponCommodityDefinition {

  /**
   * The fixing date.
   */
  private final ZonedDateTime _fixingDate;

  /**
   * Constructor with all details.
   * @param paymentYearFraction payment year fraction, positive
   * @param underlying The commodity underlying, not null
   * @param unitName name of the unit of the commodity delivered, not null
   * @param notional notional
   * @param settlementDate The settlement date, not null
   * @param calendar The holiday calendar, not null
   * @param fixingDate the fixing date
   */
  public CouponCommodityCashSettleDefinition(final double paymentYearFraction, final CommodityUnderlying underlying, final String unitName, final double notional, final ZonedDateTime settlementDate,
      final Calendar calendar, final ZonedDateTime fixingDate) {
    super(paymentYearFraction, underlying, unitName, notional, settlementDate, calendar);
    ArgChecker.notNull(fixingDate, "fixing date");
    ArgChecker.isTrue(settlementDate.isAfter(fixingDate), "settlement date must be after the fixing date");
    _fixingDate = fixingDate;

  }

  /**
   * Constructor with all details. With a payment year fraction of 1.
   * @param underlying The commodity underlying, not null
   * @param notional notional
   * @param settlementDate The settlement date, not null
   * @param calendar The holiday calendar, not null
   * @param fixingDate the fixing date
   */
  public CouponCommodityCashSettleDefinition(final CommodityUnderlying underlying, final double notional, final ZonedDateTime settlementDate,
      final Calendar calendar, final ZonedDateTime fixingDate) {
    super(1.0, underlying, underlying.getName(), notional, settlementDate, calendar);
    ArgChecker.notNull(fixingDate, "fixing date");
    ArgChecker.isTrue(settlementDate.isAfter(fixingDate), "settlement date must be after the fixing date");
    _fixingDate = fixingDate;

  }

  /**
   * Gets the payment date.
   * @return The payment date.
   */
  public ZonedDateTime getFixingDate() {
    return _fixingDate;
  }

  @Override
  public double getReferenceAmount() {
    return getNotional();
  }

  @Override
  public Payment toDerivative(final ZonedDateTime date) {
    ArgChecker.notNull(date, "date");
    ArgChecker.inOrderOrEqual(date, getFixingDate(), "date", "expiry date");
    final double settlementTime = TimeCalculator.getTimeBetween(date, getSettlementDate());
    return new CouponCommodityCashSettle(getPaymentYearFractione(), getUnderlying(), getUnitName(), getNotional(), settlementTime, getCalendar());
  }

  public Payment toDerivative(final ZonedDateTime date, final DoubleTimeSeries<ZonedDateTime> priceIndexTimeSeries, final String... yieldCurveNames) {
    return toDerivative(date, priceIndexTimeSeries);
  }

  public Payment toDerivative(final ZonedDateTime date, final DoubleTimeSeries<ZonedDateTime> commoIndexTimeSeries) {
    ArgChecker.notNull(date, "date");
    ArgChecker.notNull(commoIndexTimeSeries, "Index fixing time series");
    final double settlementTime = TimeCalculator.getTimeBetween(date, getSettlementDate());
    if (date.equals(getFixingDate())) {
      final Double commodityFixing = commoIndexTimeSeries.getValue(getFixingDate());
      if (commodityFixing != null) {
        return new PaymentFixed(getCurrency(), settlementTime, commodityFixing * getNotional());
      }
    }

    if (date.isAfter(getFixingDate())) {
      final Double commodityFixing = commoIndexTimeSeries.getValue(getFixingDate());
      if (commodityFixing == null) {
        throw new IllegalStateException("Could not get fixing value for date " + getFixingDate());
      }
      return new PaymentFixed(getCurrency(), settlementTime, commodityFixing * getNotional());
    }
    return new CouponCommodityCashSettle(getPaymentYearFractione(), getUnderlying(), getUnitName(), getNotional(), settlementTime, getCalendar());
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    ArgChecker.notNull(visitor, "visitor");
    return visitor.visitCouponCommodityCashSettleDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    ArgChecker.notNull(visitor, "visitor");
    return visitor.visitCouponCommodityCashSettleDefinition(this);
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "CouponCommodityCashSettleDefinition [_fixingDate=" + _fixingDate + "]";
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((_fixingDate == null) ? 0 : _fixingDate.hashCode());
    return result;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
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
    final CouponCommodityCashSettleDefinition other = (CouponCommodityCashSettleDefinition) obj;
    if (_fixingDate == null) {
      if (other._fixingDate != null) {
        return false;
      }
    } else if (!_fixingDate.equals(other._fixingDate)) {
      return false;
    }
    return true;
  }

}
