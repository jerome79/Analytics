/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.derivative;

import java.util.Arrays;

import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.collect.ArgChecker;

/**
 * Class describing a Fed Fund swap-like floating coupon (arithmetic average on overnight rates).
 */
public final class CouponONArithmeticAverage extends Coupon {

  /**
   * The overnight index on which the coupon fixes. The index currency should be the same as the coupon currency. Not null.
   */
  private final IndexON _index;
  /**
   * The times of the fixing periods. The length is one greater than the number of periods, as it includes accrual start and end.
   */
  private final double[] _fixingPeriodStartTimes;

  /**
   * The times of the fixing periods. The length is one greater than the number of periods, as it includes accrual start and end.
   */
  private final double[] _fixingPeriodEndTimes;

  /**
   * The accrual factors (or year fractions) associated to the fixing periods in the Index day count convention.
   */
  private final double[] _fixingPeriodAccrualFactors;
  /**
   * The interest accrued over the periods already fixed multiplied by the accrual factors, i.e. the sum (\delta_i r_i).
   */
  private final double _rateAccrued;
  /**
   * The accrual factor (or year fraction) associated to the remaining fixing period in the Index day count convention.
   */
  private final double _fixingPeriodRemainingAccrualFactor;

  /**
   * Constructor.
   * @param currency The coupon currency.
   * @param paymentTime The coupon payment time.
   * @param paymentAccrualFactor The year fraction of the full coupon.
   * @param notional The coupon notional.
   * @param index The index associated to the coupon.
   * @param fixingPeriodTimes The times of the remaining fixing. The length is one greater than the number of periods, as it includes accrual start and end.
   * @param fixingPeriodAccrualFactors The accrual factors (or year fractions) associated to the fixing periods in the Index day count convention.
   * @param rateAccrued The interest accrued over the periods already fixed.
   * @param fixingPeriodRemainingAccrualFactor ??
   */
  private CouponONArithmeticAverage(final Currency currency, final double paymentTime, final double paymentYearFraction, final double notional, final IndexON index,
      final double[] fixingPeriodStartTimes,
      final double[] fixingPeriodEndTimes, final double[] fixingPeriodAccrualFactors, final double rateAccrued, final double fixingPeriodRemainingAccrualFactor) {
    super(currency, paymentTime, paymentYearFraction, notional);
    _index = index;
    _fixingPeriodStartTimes = fixingPeriodStartTimes;
    _fixingPeriodEndTimes = fixingPeriodEndTimes;
    _fixingPeriodAccrualFactors = fixingPeriodAccrualFactors;
    _rateAccrued = rateAccrued;
    _fixingPeriodRemainingAccrualFactor = fixingPeriodRemainingAccrualFactor;
  }

  /**
   * Builder from financial details.
   * @param paymentTime The coupon payment time.
   * @param paymentAccrualFactor The year fraction of the full coupon.
   * @param notional The coupon notional.
   * @param index The index associated to the coupon.
   * @param fixingPeriodStartTimes The start times of the remaining fixing. The length is the same as the number of periods.
   * @param fixingPeriodEndTimes The end times of the remaining fixing. The length is the same as the number of periods.
   * @param fixingPeriodAccrualFactors The accrual factors (or year fractions) associated to the fixing periods in the Index day count convention.
   * @param rateAccrued The interest accrued over the periods already fixed.
   * @return The coupon.
   */
  public static CouponONArithmeticAverage from(final double paymentTime, final double paymentAccrualFactor, final double notional, final IndexON index, final double[] fixingPeriodStartTimes,
      final double[] fixingPeriodEndTimes, final double[] fixingPeriodAccrualFactors,
      final double rateAccrued) {
    ArgChecker.notNull(index, "Index");
    ArgChecker.notNull(fixingPeriodStartTimes, "Fixing Start Times");
    ArgChecker.notNull(fixingPeriodEndTimes, "Fixing End Times");
    ArgChecker.notNull(fixingPeriodAccrualFactors, "Accrual Factors");
    double fixingPeriodRemainingAccrualFactor = 0.0;
    for (final double fixingPeriodAccrualFactor : fixingPeriodAccrualFactors) {
      fixingPeriodRemainingAccrualFactor += fixingPeriodAccrualFactor;
    }
    return new CouponONArithmeticAverage(index.getCurrency(), paymentTime, paymentAccrualFactor, notional, index, fixingPeriodStartTimes, fixingPeriodEndTimes, fixingPeriodAccrualFactors,
        rateAccrued,
        fixingPeriodRemainingAccrualFactor);
  }

  /**
   * Gets the index.
   * @return The index.
   */
  public IndexON getIndex() {
    return _index;
  }

  /**
   * Gets the start times of the fixing periods.
   * @return The times.
   */
  public double[] getFixingPeriodStartTimes() {
    return _fixingPeriodStartTimes;
  }

  /**
   * Gets the end times of the fixing periods.
   * @return The times.
   */
  public double[] getFixingPeriodEndTimes() {
    return _fixingPeriodEndTimes;
  }

  /**
   * Gets the fixingPeriodAccrualFactors field.
   * @return the fixingPeriodAccrualFactors
   */
  public double[] getFixingPeriodAccrualFactors() {
    return _fixingPeriodAccrualFactors;
  }

  /**
   * Gets the notionalAccrued field.
   * @return the notionalAccrued
   */
  public double getRateAccrued() {
    return _rateAccrued;
  }

  /**
   * Gets the fixingPeriodTotalAccrualFactor field.
   * @return the fixingPeriodTotalAccrualFactor
   */
  public double getFixingPeriodRemainingAccrualFactor() {
    return _fixingPeriodRemainingAccrualFactor;
  }

  @Override
  public Coupon withNotional(final double notional) {
    return null; // TODO
  }

  @Override
  public <S, T> T accept(final InstrumentDerivativeVisitor<S, T> visitor, final S data) {
    ArgChecker.notNull(visitor, "visitor");
    return visitor.visitCouponONArithmeticAverage(this, data);
  }

  @Override
  public <T> T accept(final InstrumentDerivativeVisitor<?, T> visitor) {
    ArgChecker.notNull(visitor, "visitor");
    return visitor.visitCouponONArithmeticAverage(this);
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + Arrays.hashCode(_fixingPeriodAccrualFactors);
    result = prime * result + Arrays.hashCode(_fixingPeriodEndTimes);
    long temp;
    temp = Double.doubleToLongBits(_fixingPeriodRemainingAccrualFactor);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + Arrays.hashCode(_fixingPeriodStartTimes);
    result = prime * result + ((_index == null) ? 0 : _index.hashCode());
    temp = Double.doubleToLongBits(_rateAccrued);
    result = prime * result + (int) (temp ^ (temp >>> 32));
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
    final CouponONArithmeticAverage other = (CouponONArithmeticAverage) obj;
    if (!Arrays.equals(_fixingPeriodAccrualFactors, other._fixingPeriodAccrualFactors)) {
      return false;
    }
    if (!Arrays.equals(_fixingPeriodEndTimes, other._fixingPeriodEndTimes)) {
      return false;
    }
    if (Double.doubleToLongBits(_fixingPeriodRemainingAccrualFactor) != Double.doubleToLongBits(other._fixingPeriodRemainingAccrualFactor)) {
      return false;
    }
    if (!Arrays.equals(_fixingPeriodStartTimes, other._fixingPeriodStartTimes)) {
      return false;
    }
    if (_index == null) {
      if (other._index != null) {
        return false;
      }
    } else if (!_index.equals(other._index)) {
      return false;
    }
    if (Double.doubleToLongBits(_rateAccrued) != Double.doubleToLongBits(other._rateAccrued)) {
      return false;
    }
    return true;
  }

}
