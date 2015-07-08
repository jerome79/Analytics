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
 * Class describing an Ibor-like coupon.
 */
public class CouponIborSpread extends CouponFloating implements DepositIndexCoupon<IborIndex> {

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
   * The spread paid above Ibor.
   */
  private final double _spread;
  /**
   * The fixed amount related to the spread.
   */
  private final double _spreadAmount;

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
   * @param spread The spread.
   */
  public CouponIborSpread(final Currency currency, final double paymentTime, final double paymentYearFraction, final double notional, final double fixingTime,
      final IborIndex index, final double fixingPeriodStartTime, final double fixingPeriodEndTime, final double fixingYearFraction, final double spread) {
    super(currency, paymentTime, paymentYearFraction, notional, fixingTime);
    ArgChecker.isTrue(fixingPeriodStartTime >= fixingTime, "fixing period start {} < fixing time {}", fixingPeriodStartTime, fixingTime);
    _fixingPeriodStartTime = fixingPeriodStartTime;
    ArgChecker.isTrue(fixingPeriodEndTime >= fixingPeriodStartTime, "fixing period end {} < fixing period start {}", fixingPeriodEndTime, fixingPeriodStartTime);
    _fixingPeriodEndTime = fixingPeriodEndTime;
    ArgChecker.isTrue(fixingYearFraction >= 0, "fixing year fraction {} < 0", fixingYearFraction);
    _fixingAccrualFactor = fixingYearFraction;
    _spread = spread;
    _spreadAmount = _spread * getPaymentYearFraction() * getNotional();
    _index = index;
  }

  /**
   * Constructor from details with spread defaulted to 0.
   * @param currency The payment currency.
   * @param paymentTime Time (in years) up to the payment.
   * @param paymentYearFraction The year fraction (or accrual factor) for the coupon payment.
   * @param notional Coupon notional.
   * @param fixingTime Time (in years) up to fixing.
   * @param index The Ibor-like index on which the coupon fixes.
   * @param fixingPeriodStartTime The fixing period start time (in years).
   * @param fixingPeriodEndTime Time (in years) up to the end of the fixing period.
   * @param fixingYearFraction The year fraction (or accrual factor) for the fixing period.
   */
  public CouponIborSpread(final Currency currency, final double paymentTime, final double paymentYearFraction, final double notional, final double fixingTime,
      final IborIndex index, final double fixingPeriodStartTime, final double fixingPeriodEndTime, final double fixingYearFraction) {
    this(currency, paymentTime, paymentYearFraction, notional, fixingTime, index, fixingPeriodStartTime, fixingPeriodEndTime, fixingYearFraction, 0.0);
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
   * Gets the _spread field.
   * @return the _spread
   */
  public double getSpread() {
    return _spread;
  }

  /**
   * Gets the spread amount.
   * @return The spread amount.
   */
  public double getSpreadAmount() {
    return _spreadAmount;
  }

  /**
   * Gets the Ibor-like index.
   * @return The index.
   */
  @Override
  public IborIndex getIndex() {
    return _index;
  }

  public CouponIborSpread withZeroSpread() {
    if (getSpread() == 0.0) {
      return this;
    }
    return withSpread(0.0);
  }

  @Override
  public CouponIborSpread withNotional(final double notional) {
    return new CouponIborSpread(getCurrency(), getPaymentTime(), getPaymentYearFraction(), notional, getFixingTime(),
        _index, getFixingPeriodStartTime(), getFixingPeriodEndTime(), getFixingAccrualFactor(), getSpread());
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(super.toString());
    sb.append(", fixing : [");
    sb.append(_fixingPeriodStartTime);
    sb.append(" - ");
    sb.append(_fixingPeriodEndTime);
    sb.append(" - ");
    sb.append(_fixingAccrualFactor);
    sb.append("], spread = ");
    sb.append(_spread);
    return sb.toString();
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
    temp = Double.doubleToLongBits(_spread);
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
    final CouponIborSpread other = (CouponIborSpread) obj;
    if (Double.doubleToLongBits(_fixingPeriodEndTime) != Double.doubleToLongBits(other._fixingPeriodEndTime)) {
      return false;
    }
    if (Double.doubleToLongBits(_fixingPeriodStartTime) != Double.doubleToLongBits(other._fixingPeriodStartTime)) {
      return false;
    }
    if (Double.doubleToLongBits(_fixingAccrualFactor) != Double.doubleToLongBits(other._fixingAccrualFactor)) {
      return false;
    }
    if (Double.doubleToLongBits(_spread) != Double.doubleToLongBits(other._spread)) {
      return false;
    }
    return true;
  }

  public CouponIborSpread withSpread(final double spread) {
    return new CouponIborSpread(getCurrency(), getPaymentTime(), getPaymentYearFraction(), getNotional(),
        getFixingTime(), _index, getFixingPeriodStartTime(), getFixingPeriodEndTime(), getFixingAccrualFactor(), spread);
  }

  @Override
  public <S, T> T accept(final InstrumentDerivativeVisitor<S, T> visitor, final S data) {
    ArgChecker.notNull(visitor, "visitor");
    return visitor.visitCouponIborSpread(this, data);
  }

  @Override
  public <T> T accept(final InstrumentDerivativeVisitor<?, T> visitor) {
    ArgChecker.notNull(visitor, "visitor");
    return visitor.visitCouponIborSpread(this);
  }

}
