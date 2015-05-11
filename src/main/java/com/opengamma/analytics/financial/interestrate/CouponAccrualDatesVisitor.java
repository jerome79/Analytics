/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import java.time.LocalDate;

import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitorAdapter;
import com.opengamma.analytics.financial.instrument.fra.ForwardRateAgreementDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponCMSDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedAccruedCompoundingDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedCompoundingDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborAverageFixingDatesCompoundingDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborAverageFixingDatesCompoundingFlatSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborAverageFixingDatesDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborAverageIndexDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborCompoundingDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborCompoundingFlatSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborCompoundingSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborGearingDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborRatchetDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponONArithmeticAverageDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponONArithmeticAverageSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponONCompoundedDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponONDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponONSpreadDefinition;
import com.opengamma.strata.collect.tuple.Pair;

/**
 *
 */
public class CouponAccrualDatesVisitor extends InstrumentDefinitionVisitorAdapter<Void, Pair<LocalDate, LocalDate>> {

  @Override
  public Pair<LocalDate, LocalDate> visitCouponFixedDefinition(final CouponFixedDefinition payment) {
    return Pair.of(payment.getAccrualStartDate().toLocalDate(), payment.getAccrualEndDate().toLocalDate());
  }

  @Override
  public Pair<LocalDate, LocalDate> visitCouponFixedAccruedCompoundingDefinition(
      CouponFixedAccruedCompoundingDefinition payment) {
    return Pair.of(payment.getAccrualStartDate().toLocalDate(), payment.getAccrualEndDate().toLocalDate());
  }

  @Override
  public Pair<LocalDate, LocalDate> visitCouponIborDefinition(CouponIborDefinition payment) {
    return Pair.of(payment.getAccrualStartDate().toLocalDate(), payment.getAccrualEndDate().toLocalDate());
  }

  @Override
  public Pair<LocalDate, LocalDate> visitCouponIborAverageDefinition(CouponIborAverageIndexDefinition payment) {
    return Pair.of(payment.getAccrualStartDate().toLocalDate(), payment.getAccrualEndDate().toLocalDate());
  }

  @Override
  public Pair<LocalDate, LocalDate> visitCouponIborSpreadDefinition(CouponIborSpreadDefinition payment) {
    return Pair.of(payment.getAccrualStartDate().toLocalDate(), payment.getAccrualEndDate().toLocalDate());
  }

  @Override
  public Pair<LocalDate, LocalDate> visitCouponIborGearingDefinition(CouponIborGearingDefinition payment) {
    return Pair.of(payment.getAccrualStartDate().toLocalDate(), payment.getAccrualEndDate().toLocalDate());
  }

  @Override
  public Pair<LocalDate, LocalDate> visitCouponIborCompoundingDefinition(CouponIborCompoundingDefinition payment) {
    return Pair.of(payment.getAccrualStartDate().toLocalDate(), payment.getAccrualEndDate().toLocalDate());
  }

  @Override
  public Pair<LocalDate, LocalDate> visitCouponIborCompoundingSpreadDefinition(
      CouponIborCompoundingSpreadDefinition payment) {
    return Pair.of(payment.getAccrualStartDate().toLocalDate(), payment.getAccrualEndDate().toLocalDate());
  }

  @Override
  public Pair<LocalDate, LocalDate> visitCouponIborCompoundingFlatSpreadDefinition(
      CouponIborCompoundingFlatSpreadDefinition payment) {
    return Pair.of(payment.getAccrualStartDate().toLocalDate(), payment.getAccrualEndDate().toLocalDate());
  }

  @Override
  public Pair<LocalDate, LocalDate> visitCouponIborRatchetDefinition(CouponIborRatchetDefinition payment) {
    return Pair.of(payment.getAccrualStartDate().toLocalDate(), payment.getAccrualEndDate().toLocalDate());
  }

  @Override
  public Pair<LocalDate, LocalDate> visitCouponOISDefinition(CouponONDefinition payment) {
    return Pair.of(payment.getAccrualStartDate().toLocalDate(), payment.getAccrualEndDate().toLocalDate());
  }

  @Override
  public Pair<LocalDate, LocalDate> visitCouponONCompoundedDefinition(CouponONCompoundedDefinition payment) {
    return Pair.of(payment.getAccrualStartDate().toLocalDate(), payment.getAccrualEndDate().toLocalDate());
  }

  @Override
  public Pair<LocalDate, LocalDate> visitCouponCMSDefinition(CouponCMSDefinition payment) {
    return Pair.of(payment.getAccrualStartDate().toLocalDate(), payment.getAccrualEndDate().toLocalDate());
  }

  @Override
  public Pair<LocalDate, LocalDate> visitCouponFixedCompoundingDefinition(CouponFixedCompoundingDefinition payment) {
    return Pair.of(payment.getAccrualStartDate().toLocalDate(), payment.getAccrualEndDate().toLocalDate());
  }

  @Override
  public Pair<LocalDate, LocalDate> visitCouponArithmeticAverageONSpreadDefinition(
      CouponONArithmeticAverageSpreadDefinition payment) {
    return Pair.of(payment.getAccrualStartDate().toLocalDate(), payment.getAccrualEndDate().toLocalDate());
  }

  @Override
  public Pair<LocalDate, LocalDate> visitCouponONSpreadDefinition(
      CouponONSpreadDefinition payment, Void data) {
    return Pair.of(payment.getAccrualStartDate().toLocalDate(), payment.getAccrualEndDate().toLocalDate());
  }

  @Override
  public Pair<LocalDate, LocalDate> visitCouponONSpreadDefinition(
      CouponONSpreadDefinition payment) {
    return Pair.of(payment.getAccrualStartDate().toLocalDate(), payment.getAccrualEndDate().toLocalDate());
  }

  @Override
  public Pair<LocalDate, LocalDate> visitCouponArithmeticAverageONDefinition(
      CouponONArithmeticAverageDefinition payment) {
    return Pair.of(payment.getAccrualStartDate().toLocalDate(), payment.getAccrualEndDate().toLocalDate());
  }

  @Override
  public Pair<LocalDate, LocalDate> visitCouponIborAverageFixingDatesDefinition(
      CouponIborAverageFixingDatesDefinition payment, Void data) {
    return Pair.of(payment.getAccrualStartDate().toLocalDate(), payment.getAccrualEndDate().toLocalDate());
  }

  @Override
  public Pair<LocalDate, LocalDate> visitCouponIborAverageFixingDatesDefinition(
      CouponIborAverageFixingDatesDefinition payment) {
    return Pair.of(payment.getAccrualStartDate().toLocalDate(), payment.getAccrualEndDate().toLocalDate());
  }

  @Override
  public Pair<LocalDate, LocalDate> visitCouponIborAverageCompoundingDefinition(
      CouponIborAverageFixingDatesCompoundingDefinition payment, Void data) {
    return Pair.of(payment.getAccrualStartDate().toLocalDate(), payment.getAccrualEndDate().toLocalDate());
  }

  @Override
  public Pair<LocalDate, LocalDate> visitCouponIborAverageCompoundingDefinition(
      CouponIborAverageFixingDatesCompoundingDefinition payment) {
    return Pair.of(payment.getAccrualStartDate().toLocalDate(), payment.getAccrualEndDate().toLocalDate());
  }

  @Override
  public Pair<LocalDate, LocalDate> visitCouponIborAverageFlatCompoundingSpreadDefinition(
      CouponIborAverageFixingDatesCompoundingFlatSpreadDefinition payment, Void data) {
    return Pair.of(payment.getAccrualStartDate().toLocalDate(), payment.getAccrualEndDate().toLocalDate());
  }

  @Override
  public Pair<LocalDate, LocalDate> visitCouponIborAverageFlatCompoundingSpreadDefinition(
      CouponIborAverageFixingDatesCompoundingFlatSpreadDefinition payment) {
    return Pair.of(payment.getAccrualStartDate().toLocalDate(), payment.getAccrualEndDate().toLocalDate());
  }

  @Override
  public Pair<LocalDate, LocalDate> visitForwardRateAgreementDefinition(ForwardRateAgreementDefinition fra) {
    return Pair.of(fra.getAccrualStartDate().toLocalDate(), fra.getAccrualEndDate().toLocalDate());
  }
}
