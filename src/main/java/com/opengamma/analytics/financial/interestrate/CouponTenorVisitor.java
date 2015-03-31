/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import java.util.Collections;
import java.util.Set;

import com.google.common.collect.Sets;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitorAdapter;
import com.opengamma.analytics.financial.instrument.fra.ForwardRateAgreementDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedAccruedCompoundingDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedCompoundingDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborAverageFixingDatesCompoundingDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborAverageFixingDatesCompoundingFlatSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborAverageFixingDatesDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborAverageIndexDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborCompoundingDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborCompoundingFlatSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborCompoundingSimpleSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborCompoundingSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborGearingDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborRatchetDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponONArithmeticAverageDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponONArithmeticAverageSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponONArithmeticAverageSpreadSimplifiedDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponONCompoundedDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponONDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponONSimplifiedDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponONSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponONSpreadSimplifiedDefinition;
import com.opengamma.analytics.util.time.ComparableTenor;

/**
 * Gets the index tenor of each floating coupon.
 */
public final class CouponTenorVisitor extends InstrumentDefinitionVisitorAdapter<Void, Set<ComparableTenor>> {
  /** The singleton instance */
  private static final InstrumentDefinitionVisitor<Void, Set<ComparableTenor>> INSTANCE = new CouponTenorVisitor();

  /**
   * Gets the singleton instance.
   * @return The instance
   */
  public static InstrumentDefinitionVisitor<Void, Set<ComparableTenor>> getInstance() {
    return INSTANCE;
  }

  @Override
  public Set<ComparableTenor> visitCouponIborDefinition(CouponIborDefinition definition) {
    return Sets.newHashSet(ComparableTenor.of(definition.getIndex().getTenor()));
  }

  @Override
  public Set<ComparableTenor> visitCouponIborSpreadDefinition(CouponIborSpreadDefinition definition) {
    return Sets.newHashSet(ComparableTenor.of(definition.getIndex().getTenor()));
  }

  @Override
  public Set<ComparableTenor> visitCouponIborGearingDefinition(CouponIborGearingDefinition definition) {
    return Sets.newHashSet(ComparableTenor.of(definition.getIndex().getTenor()));
  }

  @Override
  public Set<ComparableTenor> visitCouponIborCompoundingDefinition(CouponIborCompoundingDefinition definition) {
    return Sets.newHashSet(ComparableTenor.of(definition.getIndex().getTenor()));
  }

  @Override
  public Set<ComparableTenor> visitCouponIborCompoundingFlatSpreadDefinition(
      CouponIborCompoundingFlatSpreadDefinition definition) {
    return Sets.newHashSet(ComparableTenor.of(definition.getIndex().getTenor()));
  }
  
  @Override
  public Set<ComparableTenor> visitCouponIborCompoundingSimpleSpreadDefinition(
      CouponIborCompoundingSimpleSpreadDefinition payment) {
    return Sets.newHashSet(ComparableTenor.of(payment.getIndex().getTenor()));
  }
  
  @Override
  public Set<ComparableTenor> visitCouponIborRatchetDefinition(CouponIborRatchetDefinition payment) {
    return Sets.newHashSet(ComparableTenor.of(payment.getIndex().getTenor()));
  }
  
  @Override
  public Set<ComparableTenor> visitCouponIborAverageDefinition(CouponIborAverageIndexDefinition payment) {
    return Sets.newHashSet(ComparableTenor.of(payment.getIndex1().getTenor()), ComparableTenor.of(payment.getIndex2().getTenor()));
  }
  
  @Override
  public Set<ComparableTenor> visitCouponIborAverageCompoundingDefinition(
        CouponIborAverageFixingDatesCompoundingDefinition payment) {
    return Sets.newHashSet(ComparableTenor.of(payment.getIndex().getTenor()));
  }
  
  @Override
  public Set<ComparableTenor> visitCouponIborAverageFixingDatesDefinition(CouponIborAverageFixingDatesDefinition payment) {
    return Sets.newHashSet(ComparableTenor.of(payment.getIndex().getTenor()));
  }
  
  @Override
  public Set<ComparableTenor> visitCouponIborAverageFlatCompoundingSpreadDefinition(
        CouponIborAverageFixingDatesCompoundingFlatSpreadDefinition payment) {
    return Sets.newHashSet(ComparableTenor.of(payment.getIndex().getTenor()));
  }

  @Override
  public Set<ComparableTenor> visitCouponOISDefinition(CouponONDefinition definition) {
    return Sets.newHashSet(ComparableTenor.ON);
  }
  
  @Override
  public Set<ComparableTenor> visitCouponOISSimplifiedDefinition(CouponONSimplifiedDefinition payment) {
    return Sets.newHashSet(ComparableTenor.ON);
  }
  
  @Override
  public Set<ComparableTenor> visitCouponArithmeticAverageONDefinition(CouponONArithmeticAverageDefinition payment) {
    return Sets.newHashSet(ComparableTenor.ON);
  }

  @Override
  public Set<ComparableTenor> visitCouponArithmeticAverageONSpreadDefinition(
      CouponONArithmeticAverageSpreadDefinition definition) {
    return Sets.newHashSet(ComparableTenor.ON);
  }
  
  @Override
  public Set<ComparableTenor> visitCouponArithmeticAverageONSpreadSimplifiedDefinition(
      CouponONArithmeticAverageSpreadSimplifiedDefinition payment) {
    return Sets.newHashSet(ComparableTenor.ON);
  }

  @Override
  public Set<ComparableTenor> visitCouponONSpreadDefinition(final CouponONSpreadDefinition definition) {
    return Sets.newHashSet(ComparableTenor.ON);
  }
  
  @Override
  public Set<ComparableTenor> visitCouponONSpreadSimplifiedDefinition(CouponONSpreadSimplifiedDefinition payment) {
    return Sets.newHashSet(ComparableTenor.ON);
  }
  
  @Override
  public Set<ComparableTenor> visitCouponONCompoundedDefinition(CouponONCompoundedDefinition payment) {
    return Sets.newHashSet(ComparableTenor.ON);
  }

  @Override
  public Set<ComparableTenor> visitCouponFixedDefinition(final CouponFixedDefinition definition) {
    return Collections.emptySet();
  }
  
  @Override
  public Set<ComparableTenor> visitCouponIborCompoundingSpreadDefinition(CouponIborCompoundingSpreadDefinition payment) {
    return Sets.newHashSet(ComparableTenor.of(payment.getIndex().getTenor()));
  }
  
  @Override
  public Set<ComparableTenor> visitCouponFixedAccruedCompoundingDefinition(CouponFixedAccruedCompoundingDefinition payment) {
    return Collections.emptySet();
  }
  
  @Override
  public Set<ComparableTenor> visitCouponFixedCompoundingDefinition(CouponFixedCompoundingDefinition payment) {
    return Collections.emptySet();
  }

  @Override
  public Set<ComparableTenor> visitForwardRateAgreementDefinition(ForwardRateAgreementDefinition fra) {
    return Sets.newHashSet(ComparableTenor.of(fra.getIndex().getTenor()));
  }
}
