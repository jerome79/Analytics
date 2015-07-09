/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.fra.derivative;

import java.util.Objects;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFloating;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.collect.ArgChecker;

/**
 * Class describing a Forward Rate Agreement (FRA).
 */
public class ForwardRateAgreement extends CouponFloating {

  /**
   * Ibor-like index on which the FRA fixes. The index currency should be the same as the instrument currency.
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
  private final double _fixingYearFraction;
  /**
   * The FRA rate.
   */
  private final double _rate;

  /**
   * Constructor from all details.
   * @param currency The payment currency.
   * @param paymentTime Time (in years) up to the payment.
   * @param paymentYearFraction The year fraction (or accrual factor) for the coupon payment.
   * @param notional Coupon notional.
   * @param index The Ibor index.
   * @param fixingTime Time (in years) up to fixing.
   * @param fixingPeriodStartTime Time (in years) up to the start of the fixing period.
   * @param fixingPeriodEndTime Time (in years) up to the end of the fixing period.
   * @param fixingYearFraction The year fraction (or accrual factor) for the fixing period.
   * @param rate The FRA rate.
   */
  public ForwardRateAgreement(final Currency currency, final double paymentTime, final double paymentYearFraction, final double notional, final IborIndex index,
      final double fixingTime, final double fixingPeriodStartTime, final double fixingPeriodEndTime, final double fixingYearFraction, final double rate) {
    super(currency, paymentTime, paymentYearFraction, notional, fixingTime);
    ArgChecker.notNull(index, "index");
    ArgChecker.isTrue(fixingPeriodStartTime >= fixingTime, "fixing period start < fixing time");
    ArgChecker.isTrue(fixingPeriodEndTime >= fixingPeriodStartTime, "fixing period end < fixing period start");
    ArgChecker.isTrue(fixingYearFraction >= 0, "forward year fraction < 0");
    _index = index;
    _fixingPeriodStartTime = fixingPeriodStartTime;
    _fixingPeriodEndTime = fixingPeriodEndTime;
    _fixingYearFraction = fixingYearFraction;
    _rate = rate;
  }

  /**
   * Gets the Ibor index.
   * @return The index.
   */
  public IborIndex getIndex() {
    return _index;
  }

  /**
   * Gets the fixing period start time.
   * @return The fixing period start time.
   */
  public double getFixingPeriodStartTime() {
    return _fixingPeriodStartTime;
  }

  /**
   * Gets the fixing period end time.
   * @return The fixing period start time.
   */
  public double getFixingPeriodEndTime() {
    return _fixingPeriodEndTime;
  }

  /**
   * Gets the fixing Year Fraction.
   * @return The fixing Year Fraction.
   */
  public double getFixingYearFraction() {
    return _fixingYearFraction;
  }

  /**
   * Gets the FRA rate.
   * @return The rate.
   */
  public double getRate() {
    return _rate;
  }

  @Override
  public ForwardRateAgreement withNotional(final double notional) {
    return new ForwardRateAgreement(
        getCurrency(), getPaymentTime(), getPaymentYearFraction(), notional,
        _index, getFixingTime(), _fixingPeriodStartTime, _fixingPeriodEndTime, _fixingYearFraction, _rate);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_fixingPeriodEndTime);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_fixingPeriodStartTime);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_fixingYearFraction);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + _index.hashCode();
    temp = Double.doubleToLongBits(_rate);
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
    final ForwardRateAgreement other = (ForwardRateAgreement) obj;
    if (Double.doubleToLongBits(_fixingPeriodEndTime) != Double.doubleToLongBits(other._fixingPeriodEndTime)) {
      return false;
    }
    if (Double.doubleToLongBits(_fixingPeriodStartTime) != Double.doubleToLongBits(other._fixingPeriodStartTime)) {
      return false;
    }
    if (Double.doubleToLongBits(_fixingYearFraction) != Double.doubleToLongBits(other._fixingYearFraction)) {
      return false;
    }
    if (!Objects.equals(_index, other._index)) {
      return false;
    }
    if (Double.doubleToLongBits(_rate) != Double.doubleToLongBits(other._rate)) {
      return false;
    }
    return true;
  }

  @Override
  public <S, T> T accept(final InstrumentDerivativeVisitor<S, T> visitor, final S data) {
    return visitor.visitForwardRateAgreement(this, data);
  }

  @Override
  public <T> T accept(final InstrumentDerivativeVisitor<?, T> visitor) {
    return visitor.visitForwardRateAgreement(this);
  }

}
