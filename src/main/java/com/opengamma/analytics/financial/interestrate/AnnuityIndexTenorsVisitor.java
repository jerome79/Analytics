/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitorAdapter;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentDefinition;
import com.opengamma.analytics.util.time.ComparableTenor;

/**
 * Gets the index tenors for the coupons in an annuity.
 */
public final class AnnuityIndexTenorsVisitor
    extends InstrumentDefinitionVisitorAdapter<ZonedDateTime, List<Set<ComparableTenor>>> {

  /** The coupon accrual year fraction visitor */
  private static final InstrumentDefinitionVisitor<Void, Set<ComparableTenor>> COUPON_VISITOR = CouponTenorVisitor.getInstance();
  /** A singleton instance */
  private static final InstrumentDefinitionVisitor<ZonedDateTime, List<Set<ComparableTenor>>> INSTANCE =
      new AnnuityIndexTenorsVisitor();

  /**
   * Gets the singleton instance.
   * @return The instance
   */
  public static InstrumentDefinitionVisitor<ZonedDateTime, List<Set<ComparableTenor>>> getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private AnnuityIndexTenorsVisitor() {
  }

  @Override
  public List<Set<ComparableTenor>> visitAnnuityDefinition(
      AnnuityDefinition<? extends PaymentDefinition> annuity,
      ZonedDateTime date) {
    final int n = annuity.getNumberOfPayments();
    final List<Set<ComparableTenor>> tenors = new ArrayList<>();
    for (int i = 0; i < n; i++) {
      final PaymentDefinition payment = annuity.getNthPayment(i);
      if (!date.isAfter(payment.getPaymentDate())) {
        tenors.add(payment.accept(COUPON_VISITOR));
      }
    }
    return tenors;
  }

}
