/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.tutorial.analysis.swap;

import java.time.LocalDate;
import java.time.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.analytics.convention.rolldate.RollConvention;
import com.opengamma.analytics.financial.instrument.NotionalProvider;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.annuity.CompoundingMethod;
import com.opengamma.analytics.financial.instrument.annuity.FixedAnnuityDefinitionBuilder;
import com.opengamma.analytics.financial.instrument.annuity.FloatingAnnuityDefinitionBuilder;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedON;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedONMaster;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.instrument.payment.CouponDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapCouponFixedCouponDefinition;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParRateDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.generic.MarketQuoteSensitivityBlockCalculator;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.parameter.ParameterSensitivityParameterCalculator;
import com.opengamma.analytics.tutorial.datasets.GbpDatasetJuly16;
import com.opengamma.analytics.util.export.ExportUtils;
import com.opengamma.analytics.util.time.DateUtils;
import com.opengamma.analytics.util.timeseries.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.basics.currency.MultiCurrencyAmount;
import com.opengamma.strata.basics.date.BusinessDayAdjustment;
import com.opengamma.strata.basics.date.BusinessDayConventions;
import com.opengamma.strata.basics.date.DaysAdjustment;
import com.opengamma.strata.basics.date.HolidayCalendar;
import com.opengamma.strata.basics.date.HolidayCalendars;
import com.opengamma.strata.collect.tuple.Pair;

/**
 * Examples of risk analysis for different swaps in GBP.
 * Those examples can be used for tutorials. 
 */
@Test(enabled = false)
public class SwapRiskAnalysisJuly16Gbp {

  private static final ZonedDateTime VALUATION_DATE = DateUtils.getUTCDate(2014, 7, 16);

  private static final HolidayCalendar LON = HolidayCalendars.GBLO;
  private static final GeneratorSwapFixedONMaster GENERATOR_OIS_MASTER = GeneratorSwapFixedONMaster.getInstance();
  private static final GeneratorSwapFixedON GENERATOR_OIS_GBP = GENERATOR_OIS_MASTER.getGenerator("GBP1YSONIA", LON);
  private static final IndexON GBPSONIA = GENERATOR_OIS_GBP.getIndex();
  private static final Currency GBP = Currency.GBP;
  private static final BusinessDayAdjustment ADJUSTED_DATE_SONIA =
      BusinessDayAdjustment.of(GENERATOR_OIS_GBP.getBusinessDayConvention(), LON);
  private static final DaysAdjustment OFFSET_PAY_SONIA =
      DaysAdjustment.ofBusinessDays(GENERATOR_OIS_GBP.getPaymentLag(), LON,
          BusinessDayAdjustment.of(BusinessDayConventions.FOLLOWING, LON));
  private static final DaysAdjustment OFFSET_FIX_SONIA =
      DaysAdjustment.ofBusinessDays(0, LON, BusinessDayAdjustment.of(BusinessDayConventions.FOLLOWING, LON));

  /** GBP Fixed v SINOA */
  private static final LocalDate EFFECTIVE_DATE_1 = LocalDate.of(2014, 11, 6);
  private static final LocalDate MATURITY_DATE_1 = LocalDate.of(2014, 12, 4);
  private static final double FIXED_RATE_1 = 0.0051875;
  private static final boolean PAYER_1 = false;
  private static final double NOTIONAL_1 = 1000000; // 1m
  private static final NotionalProvider NOTIONAL_PROV_1 = new NotionalProvider() {
    @Override
    public double getAmount(final LocalDate date) {
      return NOTIONAL_1;
    }
  };

  /** OIS GBP 1 **/
  /** Fixed leg */
  private static final PaymentDefinition[] PAYMENT_LEG_1_DEFINITION = new FixedAnnuityDefinitionBuilder().
      payer(PAYER_1).
      currency(GBPSONIA.getCurrency()).
      notional(NOTIONAL_PROV_1).
      startDate(EFFECTIVE_DATE_1).
      endDate(MATURITY_DATE_1).
      dayCount(GENERATOR_OIS_GBP.getFixedLegDayCount()).
      accrualPeriodFrequency(GENERATOR_OIS_GBP.getLegsPeriod()).
      rate(FIXED_RATE_1).
      accrualPeriodParameters(ADJUSTED_DATE_SONIA).
      paymentDateAdjustmentParameters(OFFSET_PAY_SONIA).
      build().getPayments();
  private static final CouponFixedDefinition[] CPN_FIXED_1_DEFINITION =
      new CouponFixedDefinition[PAYMENT_LEG_1_DEFINITION.length];
  static {
    for (int loopcpn = 0; loopcpn < PAYMENT_LEG_1_DEFINITION.length; loopcpn++) {
      CPN_FIXED_1_DEFINITION[loopcpn] = (CouponFixedDefinition) PAYMENT_LEG_1_DEFINITION[loopcpn];
    }
  }
  private static final AnnuityCouponFixedDefinition FIXED_LEG_1_DEFINITION =
      new AnnuityCouponFixedDefinition(CPN_FIXED_1_DEFINITION, LON);
  /** ON leg */
  @SuppressWarnings("unchecked")
  private static final AnnuityDefinition<? extends CouponDefinition> ON_LEG_1_DEFINITION =
      (AnnuityDefinition<? extends CouponDefinition>) new FloatingAnnuityDefinitionBuilder().
          payer(!PAYER_1).
          notional(NOTIONAL_PROV_1).
          startDate(EFFECTIVE_DATE_1).
          endDate(MATURITY_DATE_1).
          index(GBPSONIA).
          accrualPeriodFrequency(GENERATOR_OIS_GBP.getLegsPeriod()).
          rollDateAdjuster(RollConvention.NONE.getRollDateAdjuster(0)).
          resetDateAdjustmentParameters(ADJUSTED_DATE_SONIA).
          accrualPeriodParameters(ADJUSTED_DATE_SONIA).
          dayCount(GBPSONIA.getDayCount()).
          fixingDateAdjustmentParameters(OFFSET_FIX_SONIA).
          currency(GBP).
          compoundingMethod(CompoundingMethod.FLAT).
          build();
  private static final SwapCouponFixedCouponDefinition SWAP_1_DEFINITION =
      new SwapCouponFixedCouponDefinition(FIXED_LEG_1_DEFINITION, ON_LEG_1_DEFINITION);

  /** Curves and fixing */
  private static final ZonedDateTimeDoubleTimeSeries TS_FIXED_SONIA_WITHOUT_TODAY =
      GbpDatasetJuly16.fixingGbpSoniaWithoutLast();

  private static final Swap<? extends Payment, ? extends Payment> SWAP_1 =
      SWAP_1_DEFINITION.toDerivative(VALUATION_DATE,
          new ZonedDateTimeDoubleTimeSeries[] {TS_FIXED_SONIA_WITHOUT_TODAY, TS_FIXED_SONIA_WITHOUT_TODAY });

  /** Calculators **/
  private static final PresentValueDiscountingCalculator PVDC = PresentValueDiscountingCalculator.getInstance();
  private static final ParRateDiscountingCalculator PRDC = ParRateDiscountingCalculator.getInstance();
  private static final PresentValueCurveSensitivityDiscountingCalculator PVCSDC =
      PresentValueCurveSensitivityDiscountingCalculator.getInstance();
  private static final ParameterSensitivityParameterCalculator<ParameterProviderInterface> PSC =
      new ParameterSensitivityParameterCalculator<>(PVCSDC);
  private static final MarketQuoteSensitivityBlockCalculator<ParameterProviderInterface> MQSBC =
      new MarketQuoteSensitivityBlockCalculator<>(PSC);

  private static final double BP1 = 1.0E-4;

  @Test(enabled = false)
  public void compareCurves() {
    final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> stdCurveAndBundle =
        GbpDatasetJuly16.getStandardCurve(VALUATION_DATE);
    final MulticurveProviderDiscount stdCurve = stdCurveAndBundle.getFirst();
    final CurveBuildingBlockBundle stdBundle = stdCurveAndBundle.getSecond();

    final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> boeCurveAndBundle =
        GbpDatasetJuly16.getBoeCurve(VALUATION_DATE);
    final MulticurveProviderDiscount boeCurve = boeCurveAndBundle.getFirst();
    final CurveBuildingBlockBundle boeBundle = boeCurveAndBundle.getSecond();

    MultiCurrencyAmount pvStd = SWAP_1.accept(PVDC, stdCurve);
    MultiCurrencyAmount pvBoe = SWAP_1.accept(PVDC, boeCurve);
    double parStd = SWAP_1.accept(PRDC, stdCurve);
    double parBoe = SWAP_1.accept(PRDC, boeCurve);

    System.out.println("Swap name,PV STD,PV BOE,PAR STD,PAR BOE");
    System.out.println("SWAP_1,"
        + String.valueOf(pvStd.getAmount(GBP)) + ","
        + String.valueOf(pvBoe.getAmount(GBP)) + ","
        + String.valueOf(parStd) + ","
        + String.valueOf(parBoe));

    System.out.println("--- STD curve ---");
    MultipleCurrencyParameterSensitivity stdSensitivities =
        MQSBC.fromInstrument(SWAP_1, stdCurve, stdBundle).multipliedBy(BP1);
    ExportUtils.consolePrint(stdSensitivities, stdCurve);

    System.out.println("--- BOE curve ---");
    MultipleCurrencyParameterSensitivity boeSensitivities =
        MQSBC.fromInstrument(SWAP_1, boeCurve, boeBundle).multipliedBy(BP1);
    ExportUtils.consolePrint(boeSensitivities, boeCurve);

  }

}
