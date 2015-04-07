/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.derivative;

import java.util.Objects;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.collect.ArgChecker;

/**
 * Class describing a fixed payment coupon with FX reset.
 * The currency is the currency of the payment. 
 * The notional is expressed in the reference currency, from which the FX reset will be computed.
 * The payment is (getNotional() * FX(at FX reset date) * _rate * getPaymentYearFraction())
 * For exact description of the instrument, see reference.
 * <P>
 * Reference: Coupon with FX Reset Notional, OpenGamma Documentation 26, September 2014.
 */
public class CouponFixedFxReset extends Coupon {
  
  // TODO: nominal exchange?

  /** The fixed rate of the fixed coupon. */
  private final double _rate;
  /** The reference currency. */
  private final Currency _referenceCurrency;
  /** The FX fixing time. The notional used for the payment is the FX rate between the reference currency (RC) and the 
   *  payment currency (PC): 1 RC = X . PC. */
  private final double _fxFixingTime;
  /** The spot (delivery) time for the FX transaction underlying the FX fixing. */
  private final double _fxDeliveryTime;

  /**
   * Constructor of the coupon.
   * @param currency The payment currency.
   * @param paymentTime Time (in years) up to the payment.
   * @param paymentAccrualFactor Accrual factor of the accrual period.
   * @param notional Coupon notional in the reference currency.
   * @param rate Fixed rate.
   * @param referenceCurrency The reference currency for the FX reset. Not null.
   * @param fxFixingTime The FX fixing date. The notional used for the payment is the FX rate between the reference 
   * currency (RC) and the payment currency (PC): 1 RC = X . PC.
   * @param fxDeliveryTime The spot or delivery date for the FX transaction underlying the FX fixing.
   */
  public CouponFixedFxReset(Currency currency, double paymentTime, double paymentAccrualFactor, double notional,
      double rate, Currency referenceCurrency, double fxFixingTime, double fxDeliveryTime) {
    super(currency, paymentTime, paymentAccrualFactor, notional);
    ArgChecker.notNull(referenceCurrency, "reference currency");
    _rate = rate;
    _referenceCurrency = referenceCurrency;
    _fxFixingTime = fxFixingTime;
    _fxDeliveryTime = fxDeliveryTime;
  }
  
  /**
   * Returns the amount paid for a given FX reset rate.
   * @param fxRate The exchange rate between the reference currency (RC) and the payment currency (PC): 1 RC = X . PC.
   * @return The amount.
   */
  public double paymentAmount(double fxRate) {
    return getNotional() * fxRate * _rate * getPaymentYearFraction();
  }

  /**
   * Returns the fixed rate.
   * @return The rate.
   */
  public double getRate() {
    return _rate;
  }

  /**
   * Returns the reference currency.
   * @return The currency.
   */
  public Currency getReferenceCurrency() {
    return _referenceCurrency;
  }

  /**
   * Returns the FX fixing time.
   * @return The time.
   */
  public double getFxFixingTime() {
    return _fxFixingTime;
  }

  /**
   * Returns the FX delivery time.
   * @return The time.
   */
  public double getFxDeliveryTime() {
    return _fxDeliveryTime;
  }

  @Override
  public <S, T> T accept(InstrumentDerivativeVisitor<S, T> visitor, S data) {
    ArgChecker.notNull(visitor, "visitor");
    return visitor.visitCouponFixedFxReset(this, data);
  }

  @Override
  public <T> T accept(InstrumentDerivativeVisitor<?, T> visitor) {
    ArgChecker.notNull(visitor, "visitor");
    return visitor.visitCouponFixedFxReset(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_fxFixingTime);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_fxDeliveryTime);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_rate);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + _referenceCurrency.hashCode();
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    CouponFixedFxReset other = (CouponFixedFxReset) obj;
    if (Double.doubleToLongBits(_fxFixingTime) != Double.doubleToLongBits(other._fxFixingTime)) {
      return false;
    }
    if (Double.doubleToLongBits(_fxDeliveryTime) != Double.doubleToLongBits(other._fxDeliveryTime)) {
      return false;
    }
    if (Double.doubleToLongBits(_rate) != Double.doubleToLongBits(other._rate)) {
      return false;
    }
    if (!Objects.equals(_referenceCurrency, other._referenceCurrency)) {
      return false;
    }
    return true;
  }

  @Override
  public Coupon withNotional(double notional) {
    throw new UnsupportedOperationException("CouponFixedFXReset does not support withNotional method.");
  }  

}
