/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.derivative;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.collect.ArgChecker;

/**
 * Class describing a single currency Ibor-like coupon.
 */
public class CouponIbor extends CouponFloating implements DepositIndexCoupon<IborIndex> {

  /**
   * The Ibor-like index on which the coupon fixes. The index currency should be the same as the index currency.
   */
  private final IborIndex _index;
  /**
   * The fixing period start time (in years).
   */
  private final double _fixingPeriodStartTime;
  /**
   * The fixing period end time (in years).
   */
  private final double _fixingPeriodEndTime;
  /**
   * The fixing period year fraction (or accrual factor) in the fixing convention.
   */
  private final double _fixingAccrualFactor;

  /**
   * Constructor from all details.
   * @param currency The payment currency.
   * @param paymentTime Time (in years) up to the payment.
   * @param paymentYearFraction The year fraction (or accrual factor) for the coupon payment.
   * @param notional Coupon notional.
   * @param fixingTime Time (in years) up to fixing.
   * @param index The Ibor-like index on which the coupon fixes.
   * @param fixingPeriodStartTime The fixing period start time (in years).
   * @param fixingPeriodEndTime The fixing period end time (in years).
   * @param fixingYearFraction The year fraction (or accrual factor) for the fixing period.
   */
  public CouponIbor(final Currency currency, final double paymentTime, final double paymentYearFraction, final double notional, final double fixingTime,
      final IborIndex index, final double fixingPeriodStartTime, final double fixingPeriodEndTime, final double fixingYearFraction) {
    super(currency, paymentTime, paymentYearFraction, notional, fixingTime);
    ArgChecker.isTrue(fixingPeriodStartTime >= fixingTime, "fixing period start < fixing time");
    ArgChecker.isTrue(fixingPeriodEndTime >= fixingPeriodStartTime, "fixing period end < fixing period start");
    ArgChecker.isTrue(fixingYearFraction >= 0, "forward year fraction < 0");
    ArgChecker.notNull(index, "Index");
    ArgChecker.isTrue(currency.equals(index.getCurrency()), "Index currency incompatible with coupon currency");
    _fixingPeriodStartTime = fixingPeriodStartTime;
    _fixingPeriodEndTime = fixingPeriodEndTime;
    _fixingAccrualFactor = fixingYearFraction;
    _index = index;
  }

  /**
   * Gets the fixing period start time (in years).
   * @return The fixing period start time.
   */
  public double getFixingPeriodStartTime() {
    return _fixingPeriodStartTime;
  }

  /**
   * Gets the fixing period end time (in years).
   * @return The fixing period end time.
   */
  public double getFixingPeriodEndTime() {
    return _fixingPeriodEndTime;
  }

  /**
   * Gets the accrual factor for the fixing period.
   * @return The accrual factor.
   */
  public double getFixingAccrualFactor() {
    return _fixingAccrualFactor;
  }

  /**
   * Gets the Ibor-like index.
   * @return The index.
   */
  @Override
  public IborIndex getIndex() {
    return _index;
  }

  @Override
  public CouponIbor withNotional(final double notional) {
    return new CouponIbor(
        getCurrency(), getPaymentTime(), getPaymentYearFraction(), notional, getFixingTime(),
        _index, getFixingPeriodStartTime(), getFixingPeriodEndTime(), getFixingAccrualFactor());
  }

  @Override
  public String toString() {
    return "CouponIbor: " + super.toString() + ", fixing : [" + _fixingPeriodStartTime + " - " +
        _fixingPeriodEndTime + " - " + _fixingAccrualFactor + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_fixingPeriodEndTime);
    result = prime * result + (int) (temp ^ temp >>> 32);
    temp = Double.doubleToLongBits(_fixingPeriodStartTime);
    result = prime * result + (int) (temp ^ temp >>> 32);
    temp = Double.doubleToLongBits(_fixingAccrualFactor);
    result = prime * result + (int) (temp ^ temp >>> 32);
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
    final CouponIbor other = (CouponIbor) obj;
    if (Double.doubleToLongBits(_fixingPeriodEndTime) != Double.doubleToLongBits(other._fixingPeriodEndTime)) {
      return false;
    }
    if (Double.doubleToLongBits(_fixingPeriodStartTime) != Double.doubleToLongBits(other._fixingPeriodStartTime)) {
      return false;
    }
    if (Double.doubleToLongBits(_fixingAccrualFactor) != Double.doubleToLongBits(other._fixingAccrualFactor)) {
      return false;
    }
    return true;
  }

  public CouponFixed withUnitCoupon() {
    return new CouponFixed(getCurrency(), getPaymentTime(), getPaymentYearFraction(), getNotional(), 1.0);
  }

  @Override
  public <S, T> T accept(final InstrumentDerivativeVisitor<S, T> visitor, final S data) {
    return visitor.visitCouponIbor(this, data);
  }

  @Override
  public <T> T accept(final InstrumentDerivativeVisitor<?, T> visitor) {
    return visitor.visitCouponIbor(this);
  }

}
