/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.cash.derivative;

import java.util.Objects;

import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.InterestRate;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.collect.ArgChecker;

/**
 * Class describing a deposit where the rate is expressed in a specific composition convention.
 * Used mainly for curve construction with rates directly provided (not calibrated).
 */
public class DepositZero implements InstrumentDerivative {

  /**
   * The deposit currency.
   */
  private final Currency _currency;
  /**
   * The deposit start time.
   */
  private final double _startTime;
  /**
   * The deposit end (or maturity) time.
   */
  private final double _endTime;
  /**
   * The initial amount. Usually is equal to the notional or 0 if the amount has been paid in the past.
   */
  private final double _initialAmount;
  /**
   * The deposit notional.
   */
  private final double _notional;
  /**
   * The accrual factor (or year fraction).
   */
  private final double _paymentAccrualFactor;
  /**
   * The interest rate and its composition type.
   */
  private final InterestRate _rate;
  /**
   * The interest amount to be paid at end date.
   */
  private final double _interestAmount;

  /**
   * Constructor from all details.
   * @param currency The currency.
   * @param startTime The start time.
   * @param endTime The end time.
   * @param initialAmount The initial amount. Usually is equal to the notional or 0 if the amount has been paid in the past. Should be of the same sign as notional.
   * @param notional The notional.
   * @param paymentAccrualFactor The accrual factor (or year fraction).
   * @param rate The interest rate and its composition type.
   * @param interestAmount  The interest amount to be paid at end date.
   */
  public DepositZero(final Currency currency, final double startTime, final double endTime, final double initialAmount, final double notional, final double paymentAccrualFactor,
      final InterestRate rate, final double interestAmount) {
    ArgChecker.notNull(currency, "Currency");
    ArgChecker.notNull(rate, "Rate");
    _currency = currency;
    _startTime = startTime;
    _endTime = endTime;
    _initialAmount = initialAmount;
    _notional = notional;
    _paymentAccrualFactor = paymentAccrualFactor;
    _rate = rate;
    _interestAmount = interestAmount;
  }

  /**
   * Gets the deposit currency.
   * @return The currency.
   */
  public Currency getCurrency() {
    return _currency;
  }

  /**
   * Gets the deposit start time.
   * @return The time.
   */
  public double getStartTime() {
    return _startTime;
  }

  /**
   * Gets the deposit end time.
   * @return The time.
   */
  public double getEndTime() {
    return _endTime;
  }

  /**
   * Gets the initial amount.
   * @return The amount.
   */
  public double getInitialAmount() {
    return _initialAmount;
  }

  /**
   * Gets the deposit notional.
   * @return The notional.
   */
  public double getNotional() {
    return _notional;
  }

  /**
   * Gets the accrual factor (or year fraction).
   * @return The factor.
   */
  public double getPaymentAccrualFactor() {
    return _paymentAccrualFactor;
  }

  /**
   * Gets the rate (figure and composition rule).
   * @return The rate.
   */
  public InterestRate getRate() {
    return _rate;
  }

  /**
   * Gets the interest rate amount.
   * @return The amount.
   */
  public double getInterestAmount() {
    return _interestAmount;
  }

  @Override
  public <S, T> T accept(final InstrumentDerivativeVisitor<S, T> visitor, final S data) {
    ArgChecker.notNull(visitor, "visitor");
    return visitor.visitDepositZero(this, data);
  }

  @Override
  public <T> T accept(final InstrumentDerivativeVisitor<?, T> visitor) {
    ArgChecker.notNull(visitor, "visitor");
    return visitor.visitDepositZero(this);
  }

  @Override
  public String toString() {
    return "DepositZero " + _currency + " [" + _startTime + ", " + _endTime + "], notional: " + _notional + ", rate: " + _rate;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _currency.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_endTime);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_initialAmount);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_interestAmount);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_notional);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_paymentAccrualFactor);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + ((_rate == null) ? 0 : _rate.hashCode());
    temp = Double.doubleToLongBits(_startTime);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final DepositZero other = (DepositZero) obj;
    if (!Objects.equals(_currency, other._currency)) {
      return false;
    }
    if (Double.doubleToLongBits(_endTime) != Double.doubleToLongBits(other._endTime)) {
      return false;
    }
    if (Double.doubleToLongBits(_initialAmount) != Double.doubleToLongBits(other._initialAmount)) {
      return false;
    }
    if (Double.doubleToLongBits(_interestAmount) != Double.doubleToLongBits(other._interestAmount)) {
      return false;
    }
    if (Double.doubleToLongBits(_notional) != Double.doubleToLongBits(other._notional)) {
      return false;
    }
    if (Double.doubleToLongBits(_paymentAccrualFactor) != Double.doubleToLongBits(other._paymentAccrualFactor)) {
      return false;
    }
    if (!Objects.equals(_rate, other._rate)) {
      return false;
    }
    if (Double.doubleToLongBits(_startTime) != Double.doubleToLongBits(other._startTime)) {
      return false;
    }
    return true;
  }

}
