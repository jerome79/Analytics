/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.derivative;

import java.util.Objects;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.payment.CapFloor;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.collect.ArgChecker;

/**
 * Class describing a cap/floor on Ibor - aka caplet/floorlet, which can be view as a call/put on an Ibor rate.
 */
public class CapFloorIbor extends CouponFloating implements CapFloor {

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
   * The cap/floor strike.
   */
  private final double _strike;
  /**
   * The cap (true) / floor (false) flag.
   */
  private final boolean _isCap;

  /**
   * Constructor from all the cap/floor details.
   * @param currency The payment currency.
   * @param paymentTime Time (in years) up to the payment.
   * @param paymentYearFraction The year fraction (or accrual factor) for the coupon payment.
   * @param notional Coupon notional.
   * @param fixingTime Time (in years) up to fixing.
   * @param index The Ibor-like index on which the coupon fixes.
   * @param fixingPeriodStartTime Time (in years) up to the start of the fixing period.
   * @param fixingPeriodEndTime Time (in years) up to the end of the fixing period.
   * @param fixingYearFraction The year fraction (or accrual factor) for the fixing period.
   * @param strike The strike
   * @param isCap The cap/floor flag.
   */
  public CapFloorIbor(final Currency currency, final double paymentTime, final double paymentYearFraction, final double notional, final double fixingTime, final IborIndex index,
      final double fixingPeriodStartTime, final double fixingPeriodEndTime, final double fixingYearFraction, final double strike, final boolean isCap) {
    super(currency, paymentTime, paymentYearFraction, notional, fixingTime);
    ArgChecker.isTrue(fixingPeriodStartTime >= fixingTime, "fixing period start < fixing time");
    _fixingPeriodStartTime = fixingPeriodStartTime;
    ArgChecker.isTrue(fixingPeriodEndTime >= fixingPeriodStartTime, "fixing period end < fixing period start");
    _fixingPeriodEndTime = fixingPeriodEndTime;
    ArgChecker.isTrue(fixingYearFraction >= 0, "forward year fraction < 0");
    _fixingAccrualFactor = fixingYearFraction;
    _index = index;
    _strike = strike;
    _isCap = isCap;
  }

  /**
   * Create a new cap/floor with the same characteristics except the strike.
   * @param strike The new strike.
   * @return The cap/floor.
   */
  public CapFloorIbor withStrike(final double strike) {
    return new CapFloorIbor(
        getCurrency(), getPaymentTime(), getPaymentYearFraction(), getNotional(), getFixingTime(), getIndex(),
        getFixingPeriodStartTime(), getFixingPeriodEndTime(), getFixingAccrualFactor(), strike, _isCap);
  }

  /**
   * Builder from a Ibor coupon, the strike and the cap/floor flag.
   * @param coupon An Ibor coupon.
   * @param strike The strike.
   * @param isCap The cap/floor flag.
   * @return The cap/floor.
   */
  @SuppressWarnings("deprecation")
  public static CapFloorIbor from(final CouponIbor coupon, final double strike, final boolean isCap) {
    return new CapFloorIbor(
        coupon.getCurrency(), coupon.getPaymentTime(), coupon.getPaymentYearFraction(), coupon.getNotional(),
        coupon.getFixingTime(), coupon.getIndex(), coupon.getFixingPeriodStartTime(), coupon.getFixingPeriodEndTime(),
        coupon.getFixingAccrualFactor(), strike, isCap);
  }

  /**
   * Gets the Ibor-like index.
   * @return The index.
   */
  public IborIndex getIndex() {
    return _index;
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

  @Override
  public double getStrike() {
    return _strike;
  }

  @Override
  public boolean isCap() {
    return _isCap;
  }

  @Override
  public double payOff(final double fixing) {
    final double omega = (_isCap) ? 1.0 : -1.0;
    return Math.max(omega * (fixing - _strike), 0);
  }

  @Override
  public Coupon withNotional(final double notional) {
    return new CapFloorIbor(
        getCurrency(), getPaymentTime(), getPaymentYearFraction(), notional, getFixingTime(), _index,
        _fixingPeriodStartTime, _fixingPeriodEndTime, _fixingAccrualFactor, _strike, _isCap);
  }

  @SuppressWarnings("deprecation")
  public CouponIborSpread toCoupon() {
    return new CouponIborSpread(
        getCurrency(), getPaymentTime(), getFixingAccrualFactor(), getNotional(), getFixingTime(), _index,
        _fixingPeriodStartTime, _fixingPeriodEndTime, _fixingAccrualFactor);
  }

  @Override
  public <S, T> T accept(final InstrumentDerivativeVisitor<S, T> visitor, final S data) {
    return visitor.visitCapFloorIbor(this, data);
  }

  @Override
  public <T> T accept(final InstrumentDerivativeVisitor<?, T> visitor) {
    return visitor.visitCapFloorIbor(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_fixingAccrualFactor);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_fixingPeriodEndTime);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_fixingPeriodStartTime);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + _index.hashCode();
    result = prime * result + (_isCap ? 1231 : 1237);
    temp = Double.doubleToLongBits(_strike);
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
    if (!(obj instanceof CapFloorIbor)) {
      return false;
    }
    final CapFloorIbor other = (CapFloorIbor) obj;
    if (_isCap != other._isCap) {
      return false;
    }
    if (Double.compare(_strike, other._strike) != 0) {
      return false;
    }
    if (Double.compare(_fixingPeriodEndTime, other._fixingPeriodEndTime) != 0) {
      return false;
    }
    if (Double.compare(_fixingPeriodStartTime, other._fixingPeriodStartTime) != 0) {
      return false;
    }
    if (Double.compare(_fixingAccrualFactor, other._fixingAccrualFactor) != 0) {
      return false;
    }
    if (!Objects.equals(_index, other._index)) {
      return false;
    }
    return true;
  }

}
