/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.bond;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionWithData;
import com.opengamma.analytics.financial.instrument.inflation.CouponInflationDefinition;
import com.opengamma.analytics.financial.instrument.inflation.CouponInflationZeroCouponInterpolationGearingDefinition;
import com.opengamma.analytics.financial.instrument.inflation.CouponInflationZeroCouponMonthlyGearingDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentFixedDefinition;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondInterestIndexedSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondInterestIndexedTransaction;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondTransaction;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.payments.derivative.PaymentFixed;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.util.timeseries.DoubleTimeSeries;
import com.opengamma.analytics.util.timeseries.zdt.ImmutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.strata.collect.ArgChecker;

/**
 * Describes a interest inflation indexed bond transaction. Both the coupon and the nominal are indexed on a price index.
 * @param <N> Type of  fix payment coupon.
 * @param <C> Type of inflation coupon. Can be {@link CouponInflationZeroCouponMonthlyGearingDefinition} or {@link CouponInflationZeroCouponInterpolationGearingDefinition}.
 */
public class BondInterestIndexedTransactionDefinition<N extends PaymentFixedDefinition, C extends CouponDefinition> extends BondTransactionDefinition<N, C>
    implements InstrumentDefinitionWithData<BondTransaction<? extends BondSecurity<? extends Payment, ? extends Coupon>>, DoubleTimeSeries<ZonedDateTime>> {

  /**
   * Constructor of a interest indexed bond transaction from all the transaction details.
   * @param underlyingBond The interest indexed bond underlying the transaction.
   * @param quantity The number of bonds purchased (can be negative or positive).
   * @param settlementDate Transaction settlement date.
   * @param price The (clean quoted) price of the transaction in relative term (i.e. 0.90 if the dirty price is 90% of nominal).
   */
  public BondInterestIndexedTransactionDefinition(final BondSecurityDefinition<N, C> underlyingBond, final double quantity, final ZonedDateTime settlementDate,
      final double price) {
    super(underlyingBond, quantity, settlementDate, price);
    ArgChecker.isTrue(underlyingBond instanceof BondInterestIndexedSecurityDefinition, "interest Indexed bond");
  }

  //TODO: from clean price adjusted monthly (for UK linked-gilts pre-2005).

  @Override
  public BondInterestIndexedTransaction<PaymentFixed, Coupon> toDerivative(final ZonedDateTime date) {
    final ImmutableZonedDateTimeDoubleTimeSeries series = ImmutableZonedDateTimeDoubleTimeSeries.ofEmpty(ZoneOffset.UTC);
    return toDerivative(date, series);
  }

  @Override
  public BondInterestIndexedTransaction<PaymentFixed, Coupon> toDerivative(final ZonedDateTime date, final DoubleTimeSeries<ZonedDateTime> data) {
    ArgChecker.notNull(date, "date");
    ArgChecker.notNull(data, "Price index fixing time series");
    final BondInterestIndexedSecurity<PaymentFixed, Coupon> bondPurchase = ((BondInterestIndexedSecurityDefinition<PaymentFixedDefinition, CouponInflationDefinition>) getUnderlyingBond())
        .toDerivative(date, getSettlementDate(), data);
    final ZonedDateTime spot = ScheduleCalculator.getAdjustedDate(date, getUnderlyingBond().getSettlementDays(), getUnderlyingBond().getCalendar());
    final BondInterestIndexedSecurity<PaymentFixed, Coupon> bondStandard = ((BondInterestIndexedSecurityDefinition<PaymentFixedDefinition, CouponInflationDefinition>) getUnderlyingBond())
        .toDerivative(date, spot, data);
    final int nbCoupon = getUnderlyingBond().getCoupons().getNumberOfPayments();
    int couponIndex = 0; // The index of the coupon of the spot date.
    for (int loopcpn = 0; loopcpn < nbCoupon; loopcpn++) {
      if (getUnderlyingBond().getCoupons().getNthPayment(loopcpn).getAccrualEndDate().isAfter(spot)) {
        couponIndex = loopcpn;
        break;
      }
    }
    final double notionalStandard = getUnderlyingBond().getCoupons().getNthPayment(couponIndex).getNotional();
    final BondInterestIndexedTransaction<PaymentFixed, Coupon> result = new BondInterestIndexedTransaction<>(bondPurchase, getQuantity(), getPrice(), bondStandard, notionalStandard);
    return result;
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    ArgChecker.notNull(visitor, "visitor");
    return visitor.visitBondInterestIndexedTransaction(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    ArgChecker.notNull(visitor, "visitor");
    return visitor.visitBondInterestIndexedTransaction(this);
  }

}
