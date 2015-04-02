/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.tutorial.analysis.swap;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

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
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIborMaster;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedON;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedONMaster;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.instrument.payment.CouponDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapCouponFixedCouponDefinition;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParRateDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.generic.MarketQuoteSensitivityBlockCalculator;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.parameter.ParameterSensitivityParameterAbstractCalculator;
import com.opengamma.analytics.financial.provider.sensitivity.parameter.ParameterSensitivityParameterCalculator;
import com.opengamma.analytics.financial.provider.sensitivity.parameter.ParameterSensitivityUnderlyingParameterCalculator;
import com.opengamma.analytics.financial.util.AssertSensitivityObjects;
import com.opengamma.analytics.tutorial.datasets.RecentDataSetsMulticurveOisMeetingDatesGbp;
import com.opengamma.analytics.tutorial.datasets.RecentDataSetsMulticurveStandardGbp;
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
public class SwapRiskGbpAnalysis {

  private static final ZonedDateTime VALUATION_DATE = DateUtils.getUTCDate(2014, 7, 16);

  private static final HolidayCalendar LON = HolidayCalendars.GBLO;
  private static final GeneratorSwapFixedONMaster GENERATOR_OIS_MASTER = GeneratorSwapFixedONMaster.getInstance();
  private static final GeneratorSwapFixedIborMaster GENERATOR_IRS_MASTER = GeneratorSwapFixedIborMaster.getInstance();
  private static final GeneratorSwapFixedON GENERATOR_OIS_GBP = GENERATOR_OIS_MASTER.getGenerator("GBP1YSONIA", LON);
  private static final IndexON GBPSONIA = GENERATOR_OIS_GBP.getIndex();
  private static final Currency GBP = Currency.GBP;
  private static final GeneratorSwapFixedIbor GBP6MLIBOR6M = GENERATOR_IRS_MASTER.getGenerator("GBP6MLIBOR6M", LON);
  private static final IborIndex GBPLIBOR6M = GBP6MLIBOR6M.getIborIndex();
  private static final BusinessDayAdjustment ADJUSTED_DATE_LIBOR = 
      BusinessDayAdjustment.of(GBP6MLIBOR6M.getBusinessDayConvention(), LON);
  private static final DaysAdjustment OFFSET_ADJ_LIBOR =
      DaysAdjustment.ofBusinessDays(-2, LON, BusinessDayAdjustment.of(GBP6MLIBOR6M.getBusinessDayConvention(), LON));
  private static final BusinessDayAdjustment ADJUSTED_DATE_SONIA = 
      BusinessDayAdjustment.of(GENERATOR_OIS_GBP.getBusinessDayConvention(), LON);
  private static final DaysAdjustment OFFSET_PAY_SONIA =
      DaysAdjustment.ofBusinessDays(GENERATOR_OIS_GBP.getPaymentLag(), LON,
          BusinessDayAdjustment.of(BusinessDayConventions.FOLLOWING, LON));
  private static final DaysAdjustment OFFSET_FIX_SONIA =
      DaysAdjustment.ofBusinessDays(0, LON, BusinessDayAdjustment.of(BusinessDayConventions.FOLLOWING, LON));

  /** GBP Fixed v SONIA */
  private static final LocalDate EFFECTIVE_DATE_1 = LocalDate.of(2014, 11, 6);
  private static final LocalDate MATURITY_DATE_1 = LocalDate.of(2014, 12, 4);
  private static final double FIXED_RATE_1 = 0.0051875;
  private static final boolean PAYER_1 = false;
  /** GBP Fixed v LIBOR6M */
  private static final LocalDate EFFECTIVE_DATE_2 = LocalDate.of(2014, 11, 6);
  private static final LocalDate MATURITY_DATE_2 = LocalDate.of(2024, 11, 6);
  private static final double FIXED_RATE_2 = 0.0200;
  private static final boolean PAYER_2 = false;

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
  private static final CouponFixedDefinition[] CPN_FIXED_1_DEFINITION = new CouponFixedDefinition[PAYMENT_LEG_1_DEFINITION.length];
  static {
    for (int loopcpn = 0; loopcpn < PAYMENT_LEG_1_DEFINITION.length; loopcpn++) {
      CPN_FIXED_1_DEFINITION[loopcpn] = (CouponFixedDefinition) PAYMENT_LEG_1_DEFINITION[loopcpn];
    }
  }
  private static final AnnuityCouponFixedDefinition FIXED_LEG_1_DEFINITION = new AnnuityCouponFixedDefinition(CPN_FIXED_1_DEFINITION, LON);
  /** ON leg */
  @SuppressWarnings("unchecked")
  private static final AnnuityDefinition<? extends CouponDefinition> ON_LEG_1_DEFINITION = (AnnuityDefinition<? extends CouponDefinition>) new FloatingAnnuityDefinitionBuilder().
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
  private static final SwapCouponFixedCouponDefinition OIS_1_DEFINITION = new SwapCouponFixedCouponDefinition(FIXED_LEG_1_DEFINITION, ON_LEG_1_DEFINITION);

  /** GBP IRS 1 **/
  private static final PaymentDefinition[] PAYMENT_LEG_2_DEFINITION = new FixedAnnuityDefinitionBuilder().
      payer(PAYER_2).currency(GBP6MLIBOR6M.getCurrency()).notional(NOTIONAL_PROV_1).startDate(EFFECTIVE_DATE_2).
      endDate(MATURITY_DATE_2).dayCount(GBP6MLIBOR6M.getFixedLegDayCount()).accrualPeriodFrequency(GBP6MLIBOR6M.getFixedLegPeriod()).
      rate(FIXED_RATE_2).accrualPeriodParameters(ADJUSTED_DATE_LIBOR).build().getPayments();
  private static final CouponFixedDefinition[] CPN_FIXED_2_DEFINITION = new CouponFixedDefinition[PAYMENT_LEG_2_DEFINITION.length];
  static {
    for (int loopcpn = 0; loopcpn < PAYMENT_LEG_2_DEFINITION.length; loopcpn++) {
      CPN_FIXED_2_DEFINITION[loopcpn] = (CouponFixedDefinition) PAYMENT_LEG_2_DEFINITION[loopcpn];
    }
  }
  private static final AnnuityCouponFixedDefinition FIXED_LEG_2_DEFINITION = new AnnuityCouponFixedDefinition(CPN_FIXED_2_DEFINITION, LON);
  private static final AnnuityDefinition<? extends CouponDefinition> IBOR_LEG_2_DEFINITION = (AnnuityDefinition<? extends CouponDefinition>)
      new FloatingAnnuityDefinitionBuilder().
          payer(!PAYER_2).notional(NOTIONAL_PROV_1).startDate(EFFECTIVE_DATE_2).endDate(MATURITY_DATE_2).
          index(GBPLIBOR6M).accrualPeriodFrequency(GBPLIBOR6M.getTenor()).rollDateAdjuster(RollConvention.NONE.getRollDateAdjuster(0)).
          resetDateAdjustmentParameters(ADJUSTED_DATE_LIBOR).accrualPeriodParameters(ADJUSTED_DATE_LIBOR).dayCount(GBPLIBOR6M.getDayCount()).
          fixingDateAdjustmentParameters(OFFSET_ADJ_LIBOR).currency(GBPLIBOR6M.getCurrency()).build();
  private static final SwapCouponFixedCouponDefinition IRS_1_DEFINITION = new SwapCouponFixedCouponDefinition(FIXED_LEG_2_DEFINITION, IBOR_LEG_2_DEFINITION);

  /** Curves and fixing */
  private static final ZonedDateTimeDoubleTimeSeries TS_FIXED_SONIA_WITHOUT_TODAY = RecentDataSetsMulticurveStandardGbp.fixingGbpSoniaWithoutLast();
  private static final ZonedDateTimeDoubleTimeSeries TS_FIXED_LIBOR6M_WITHOUT_TODAY = RecentDataSetsMulticurveStandardGbp.fixingGbpLibor6MWithoutLast();

  /** Curve with standard OIS and IRS nodes. */
  private static final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> MULTICURVE_STD_PAIR =
      RecentDataSetsMulticurveStandardGbp.getCurvesGbpOisL6(VALUATION_DATE);
  private static final MulticurveProviderDiscount MULTICURVE_STD = MULTICURVE_STD_PAIR.getFirst();
  private static final CurveBuildingBlockBundle BLOCK_STD = MULTICURVE_STD_PAIR.getSecond();

  /** Curve with standard nodes and the discounting curve a spread to the LIBOR6M curve */
  private static final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> MULTICURVE_STD_SPREAD_PAIR =
      RecentDataSetsMulticurveStandardGbp.getCurvesGbpOisL6Spread(VALUATION_DATE);
  private static final MulticurveProviderDiscount MULTICURVE_STD_SPREAD = MULTICURVE_STD_SPREAD_PAIR.getFirst();
  private static final CurveBuildingBlockBundle BLOCK_STD_SPREAD = MULTICURVE_STD_SPREAD_PAIR.getSecond();

  private static final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> MULTICURVE_BOE_PAIR =
      RecentDataSetsMulticurveOisMeetingDatesGbp.getCurvesGbpOis(VALUATION_DATE);
  private static final MulticurveProviderDiscount MULTICURVE_BOE = MULTICURVE_BOE_PAIR.getFirst();
  private static final CurveBuildingBlockBundle BLOCK_BOE = MULTICURVE_BOE_PAIR.getSecond();

  private static final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> MULTICURVE_STD_BOE_PAIR =
      RecentDataSetsMulticurveOisMeetingDatesGbp.getCurvesGbpOisWithStdInstruments(VALUATION_DATE);
  private static final MulticurveProviderDiscount MULTICURVE_STD_BOE = MULTICURVE_BOE_PAIR.getFirst();
  private static final CurveBuildingBlockBundle BLOCK_STD_BOE = MULTICURVE_BOE_PAIR.getSecond();

  private static final Annuity<?> FIXED_LEG_1 = FIXED_LEG_1_DEFINITION.toDerivative(VALUATION_DATE);
  private static final Annuity<?> ON_LEG_1 = ON_LEG_1_DEFINITION.toDerivative(VALUATION_DATE, TS_FIXED_SONIA_WITHOUT_TODAY);
  private static final Swap<? extends Payment, ? extends Payment> OIS_1 = OIS_1_DEFINITION.toDerivative(VALUATION_DATE,
      new ZonedDateTimeDoubleTimeSeries[] {TS_FIXED_SONIA_WITHOUT_TODAY, TS_FIXED_SONIA_WITHOUT_TODAY });
  private static final Swap<? extends Payment, ? extends Payment> IRS_1 = IRS_1_DEFINITION.toDerivative(VALUATION_DATE,
      new ZonedDateTimeDoubleTimeSeries[] {TS_FIXED_LIBOR6M_WITHOUT_TODAY, TS_FIXED_LIBOR6M_WITHOUT_TODAY });

  /** Calculators **/
  private static final PresentValueDiscountingCalculator PVDC = PresentValueDiscountingCalculator.getInstance();
  private static final ParRateDiscountingCalculator PRDC = ParRateDiscountingCalculator.getInstance();
  private static final PresentValueCurveSensitivityDiscountingCalculator PVCSDC = PresentValueCurveSensitivityDiscountingCalculator.getInstance();
  private static final ParameterSensitivityParameterAbstractCalculator<ParameterProviderInterface> PSC = new ParameterSensitivityParameterCalculator<>(PVCSDC);
  private static final ParameterSensitivityParameterAbstractCalculator<ParameterProviderInterface> PSUC = new ParameterSensitivityUnderlyingParameterCalculator<>(PVCSDC);
  private static final MarketQuoteSensitivityBlockCalculator<ParameterProviderInterface> MQSBC = new MarketQuoteSensitivityBlockCalculator<>(PSC);
  private static final MarketQuoteSensitivityBlockCalculator<ParameterProviderInterface> MQSUBC = new MarketQuoteSensitivityBlockCalculator<>(PSUC);

  private static final double TOLERANCE_PV = 1.0E-2;
  private static final double TOLERANCE_PV_DELTA = 1.0E-1;
  private static final double BP1 = 1.0E-4;

  @Test
  public void presentValueStdCurve() {
    MultiCurrencyAmount pvFixed = FIXED_LEG_1.accept(PVDC, MULTICURVE_STD);
    MultiCurrencyAmount pvIbor = ON_LEG_1.accept(PVDC, MULTICURVE_STD);
    MultiCurrencyAmount pvOis1Std = OIS_1.accept(PVDC, MULTICURVE_STD);
    assertTrue("SwapRiskUsdAnalysis: present value", pvFixed.getAmount(GBP).getAmount() * pvIbor.getAmount(GBP).getAmount() < 0);
    assertEquals("SwapRiskUsdAnalysis: present value", pvOis1Std.getAmount(GBP).getAmount(), pvFixed.getAmount(GBP).getAmount() + pvIbor.getAmount(GBP).getAmount(), TOLERANCE_PV);
    MultiCurrencyAmount pvOis1Spread = OIS_1.accept(PVDC, MULTICURVE_STD_SPREAD);
    assertEquals("SwapRiskUsdAnalysis: present value", pvOis1Std.getAmount(GBP).getAmount(), pvOis1Spread.getAmount(GBP).getAmount(), TOLERANCE_PV);
    MultiCurrencyAmount pvIrs1Std = IRS_1.accept(PVDC, MULTICURVE_STD);
    MultiCurrencyAmount pvIrs1Spread = IRS_1.accept(PVDC, MULTICURVE_STD_SPREAD);
    assertEquals("SwapRiskUsdAnalysis: present value", pvIrs1Std.getAmount(GBP).getAmount(), pvIrs1Spread.getAmount(GBP).getAmount(), 2000); // Discounting is different due to different extrapolation mechanisms
  }

  @SuppressWarnings("unused")
  @Test
  public void parRateStdCurve() {
    double pr1 = OIS_1.accept(PRDC, MULTICURVE_STD);
  }

  @SuppressWarnings("unused")
  @Test
  public void bucketedPv01StdCurve() {
    MultipleCurrencyMulticurveSensitivity pvPointSensi1Std = OIS_1.accept(PVCSDC, MULTICURVE_STD);
    MultipleCurrencyParameterSensitivity pvParameterSensi1Std = PSC.pointToParameterSensitivity(pvPointSensi1Std, MULTICURVE_STD);
    MultipleCurrencyParameterSensitivity pvmqsOis1Std = MQSBC.fromInstrument(OIS_1, MULTICURVE_STD, BLOCK_STD).multipliedBy(BP1);
    MultipleCurrencyMulticurveSensitivity pvPointSensi1Spread = OIS_1.accept(PVCSDC, MULTICURVE_STD_SPREAD);
    MultipleCurrencyParameterSensitivity pvParameterSensi1Spread = PSUC.pointToParameterSensitivity(pvPointSensi1Spread, MULTICURVE_STD_SPREAD);
    MultipleCurrencyParameterSensitivity pvmqsOis1Spread = MQSUBC.fromInstrument(OIS_1, MULTICURVE_STD_SPREAD, BLOCK_STD_SPREAD).multipliedBy(BP1);
    AssertSensitivityObjects.assertEquals("Sensitivity with standard and spread curves", pvmqsOis1Std, pvmqsOis1Spread, TOLERANCE_PV_DELTA);
    MultipleCurrencyParameterSensitivity pvmqsIrs1Std = MQSBC.fromInstrument(IRS_1, MULTICURVE_STD, BLOCK_STD).multipliedBy(BP1);
    MultipleCurrencyParameterSensitivity pvmqsIrs1StdUn = MQSUBC.fromInstrument(IRS_1, MULTICURVE_STD, BLOCK_STD).multipliedBy(BP1);
    AssertSensitivityObjects.assertEquals("Sensitivity with and without underlying curve sensitivity", pvmqsIrs1Std, pvmqsIrs1StdUn, TOLERANCE_PV_DELTA);
    MultipleCurrencyParameterSensitivity pvmqsIrs1Spread = MQSUBC.fromInstrument(IRS_1, MULTICURVE_STD_SPREAD, BLOCK_STD_SPREAD).multipliedBy(BP1);
  }

  @Test
  public void presentValueBoeCurve() {
    MultiCurrencyAmount pvFixed = FIXED_LEG_1.accept(PVDC, MULTICURVE_BOE);
    MultiCurrencyAmount pvIbor = ON_LEG_1.accept(PVDC, MULTICURVE_BOE);
    MultiCurrencyAmount pvSwap1Std = OIS_1.accept(PVDC, MULTICURVE_BOE);
    assertTrue("SwapRiskUsdAnalysis: present value", pvFixed.getAmount(GBP).getAmount() * pvIbor.getAmount(GBP).getAmount() < 0);
    assertEquals("SwapRiskUsdAnalysis: present value", pvSwap1Std.getAmount(GBP).getAmount(),
        pvFixed.getAmount(GBP).getAmount() + pvIbor.getAmount(GBP).getAmount(), TOLERANCE_PV);
    System.out.println("--- BOE PVs ---");
    System.out.println("PV fixed-rate leg," + String.valueOf(pvFixed.getAmount(GBP).getAmount()));
    System.out.println("PV floating-rate leg," + String.valueOf(pvIbor.getAmount(GBP).getAmount()));
    System.out.println("PV swap," + String.valueOf(pvSwap1Std.getAmount(GBP).getAmount()));
  }

  @Test
  public void parRateBoeCurve() {
    double pr1 = OIS_1.accept(PRDC, MULTICURVE_BOE);
    System.out.println("--- BOE Break-even rate ---");
    System.out.println("Par rate," + String.valueOf(pr1));
  }

  @Test
  public void bucketedPv0BoeCurve() {
    MultipleCurrencyParameterSensitivity pvmqs1Boe =
        MQSBC.fromInstrument(OIS_1, MULTICURVE_BOE, BLOCK_BOE).multipliedBy(BP1);
    ExportUtils.consolePrint(pvmqs1Boe, MULTICURVE_BOE);
  }

}
