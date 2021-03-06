/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.bond.definition;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityPaymentFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.PaymentFixed;
import com.opengamma.strata.collect.ArgChecker;

/**
 * Describes a Ibor floating coupon bond (Floating Rate Note) issue.
 */
public class BondIborSecurity extends BondSecurity<PaymentFixed, Coupon> {

  /**
   * Ibor floating bond constructor from the nominal and the coupons.
   * @param nominal The notional payments. For bullet bond, it is restricted to a single payment.
   * @param coupon The bond Ibor coupons. Can be Ibor coupons or fixed coupons (if the fixing is already known). The coupons notional should be in line with the bond nominal.
   * @param settlementTime The time (in years) to settlement date.
   */
  public BondIborSecurity(final AnnuityPaymentFixed nominal, final Annuity<Coupon> coupon, final double settlementTime) {
    super(nominal, coupon, settlementTime, "");
  }

  @Override
  public <S, T> T accept(final InstrumentDerivativeVisitor<S, T> visitor, final S data) {
    ArgChecker.notNull(visitor, "visitor");
    return visitor.visitBondIborSecurity(this, data);
  }

  @Override
  public <T> T accept(final InstrumentDerivativeVisitor<?, T> visitor) {
    ArgChecker.notNull(visitor, "visitor");
    return visitor.visitBondIborSecurity(this);
  }

}
