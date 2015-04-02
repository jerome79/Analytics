/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.tutorial.analysis.swap;

import static org.testng.AssertJUnit.assertEquals;

import java.time.LocalDate;
import java.time.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.analytics.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.analytics.convention.calendar.Calendar;
import com.opengamma.analytics.convention.rolldate.RollConvention;
import com.opengamma.analytics.financial.datasets.CalendarTarget;
import com.opengamma.analytics.financial.datasets.CalendarUSD;
import com.opengamma.analytics.financial.instrument.NotionalProvider;
import com.opengamma.analytics.financial.instrument.annuity.AdjustedDateParameters;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.annuity.CompoundingMethod;
import com.opengamma.analytics.financial.instrument.annuity.FixedAnnuityDefinitionBuilder;
import com.opengamma.analytics.financial.instrument.annuity.FloatingAnnuityDefinitionBuilder;
import com.opengamma.analytics.financial.instrument.annuity.OffsetAdjustedDateParameters;
import com.opengamma.analytics.financial.instrument.annuity.OffsetType;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIborMaster;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedON;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedONMaster;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexIborMaster;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.instrument.payment.CouponDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapCouponFixedCouponDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapDefinition;
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
import com.opengamma.analytics.tutorial.datasets.RecentDataSetsMulticurveXCcyUsdEur;
import com.opengamma.analytics.util.export.ExportUtils;
import com.opengamma.analytics.util.time.DateUtils;
import com.opengamma.analytics.util.timeseries.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.basics.currency.MultiCurrencyAmount;
import com.opengamma.strata.collect.tuple.Pair;

/**
 * Examples of risk analysis for different swaps in USD.
 * Those examples can be used for tutorials. 
 */
public class SwapRiskUsdEurAnalysis {

  private static final ZonedDateTime VALUATION_DATE = DateUtils.getUTCDate(2014, 7, 16);

  private static final Calendar NYC = new CalendarUSD("NYC");
  private static final Calendar TARGET = new CalendarTarget("TARGET");
  private static final IndexIborMaster IBOR_MASTER = IndexIborMaster.getInstance();
  private static final GeneratorSwapFixedONMaster GENERATOR_OIS_MASTER = GeneratorSwapFixedONMaster.getInstance();
  private static final GeneratorSwapFixedON GENERATOR_OIS_USD = GENERATOR_OIS_MASTER.getGenerator("USD1YFEDFUND", NYC);
  private static final GeneratorSwapFixedON GENERATOR_OIS_EUR = GENERATOR_OIS_MASTER.getGenerator("EUR1YEONIA", TARGET);
  private static final IndexON USDFEDFUND = GENERATOR_OIS_USD.getIndex();
  private static final IndexON EUREONIA = GENERATOR_OIS_EUR.getIndex();
  private static final GeneratorSwapFixedIborMaster GENERATOR_IRS_MASTER = GeneratorSwapFixedIborMaster.getInstance();
  private static final GeneratorSwapFixedIbor USD6MLIBOR3M = GENERATOR_IRS_MASTER.getGenerator("USD6MLIBOR3M", NYC);
  private static final GeneratorSwapFixedIbor EUR1YEURIBOR3M = GENERATOR_IRS_MASTER.getGenerator("EUR1YEURIBOR3M", TARGET);
  private static final GeneratorSwapFixedIbor EUR1YEURIBOR6M = GENERATOR_IRS_MASTER.getGenerator("EUR1YEURIBOR6M", TARGET);
  private static final IborIndex USDLIBOR3M = USD6MLIBOR3M.getIborIndex();
  private static final IborIndex EURIBOR3M = IBOR_MASTER.getIndex("EURIBOR3M");
  private static final IborIndex EURIBOR6M = IBOR_MASTER.getIndex("EURIBOR6M");
  //  private static final GeneratorSwapXCcyIborIbor EURIBOR3MUSDLIBOR3M = new GeneratorSwapXCcyIborIbor("EURIBOR3MUSDLIBOR3M", EURIBOR3M, USDLIBOR3M, TARGET, NYC);
  private static final Currency USD = USDLIBOR3M.getCurrency();
  private static final Currency EUR = EURIBOR3M.getCurrency();
  private static final AdjustedDateParameters ADJUSTED_DATE_USDLIBOR = new AdjustedDateParameters(NYC, USD6MLIBOR3M.getBusinessDayConvention());
  private static final AdjustedDateParameters ADJUSTED_DATE_EUREURIBOR = new AdjustedDateParameters(TARGET, EUR1YEURIBOR3M.getBusinessDayConvention());
  private static final OffsetAdjustedDateParameters OFFSET_ADJ_USDLIBOR =
      new OffsetAdjustedDateParameters(-2, OffsetType.BUSINESS, NYC, USD6MLIBOR3M.getBusinessDayConvention());
  private static final OffsetAdjustedDateParameters OFFSET_ADJ_EUREURIBOR =
      new OffsetAdjustedDateParameters(-2, OffsetType.BUSINESS, TARGET, EUR1YEURIBOR3M.getBusinessDayConvention());
  private static final AdjustedDateParameters ADJUSTED_DATE_FEDFUND = 
      new AdjustedDateParameters(NYC, GENERATOR_OIS_USD.getBusinessDayConvention());
  private static final AdjustedDateParameters ADJUSTED_DATE_EONIA = 
      new AdjustedDateParameters(TARGET, GENERATOR_OIS_EUR.getBusinessDayConvention());
  private static final OffsetAdjustedDateParameters OFFSET_PAY_FEDFUND =
      new OffsetAdjustedDateParameters(GENERATOR_OIS_USD.getPaymentLag(), OffsetType.BUSINESS, NYC, BusinessDayConventionFactory.of("Following"));
  private static final OffsetAdjustedDateParameters OFFSET_FIX_FEDFUND =
      new OffsetAdjustedDateParameters(0, OffsetType.BUSINESS, NYC, BusinessDayConventionFactory.of("Following"));
  private static final OffsetAdjustedDateParameters OFFSET_PAY_EONIA =
      new OffsetAdjustedDateParameters(2, OffsetType.BUSINESS, TARGET, BusinessDayConventionFactory.of("Following"));
  private static final OffsetAdjustedDateParameters OFFSET_FIX_EONIA =
      new OffsetAdjustedDateParameters(0, OffsetType.BUSINESS, TARGET, BusinessDayConventionFactory.of("Following"));

  private static final double NOTIONAL_1 = 1000000; // 1m
  private static final NotionalProvider NOTIONAL_PROV_1 = new NotionalProvider() {
    @Override
    public double getAmount(final LocalDate date) {
      return NOTIONAL_1;
    }
  };
  /** USD Fixed v USDLIBOR3M */
  private static final LocalDate EFFECTIVE_DATE_1 = LocalDate.of(2015, 7, 18);
  private static final LocalDate MATURITY_DATE_1 = LocalDate.of(2018, 7, 18);
  private static final double FIXED_RATE_1 = 0.02655;
  private static final boolean PAYER_1 = false;
  /** EUR Fixed v EUREURIBOR6M */
  private static final LocalDate EFFECTIVE_DATE_2 = LocalDate.of(2015, 7, 18);
  private static final LocalDate MATURITY_DATE_2 = LocalDate.of(2018, 7, 18);
  private static final double FIXED_RATE_2 = 0.0250;
  private static final boolean PAYER_2 = false;
  /** EUREURIBOR3M + Spread v USDLIBOR3M */
  private static final LocalDate EFFECTIVE_DATE_3 = LocalDate.of(2015, 7, 18);
  private static final LocalDate MATURITY_DATE_3 = LocalDate.of(2018, 7, 18);
  private static final double SPREAD_3 = 0.0010;
  private static final boolean PAYER_3 = false;
  /** EUR Fixed v EONIA 1Y */
  private static final LocalDate EFFECTIVE_DATE_4 = LocalDate.of(2015, 7, 18);
  private static final LocalDate MATURITY_DATE_4 = LocalDate.of(2017, 7, 18);
  private static final double FIXED_RATE_4 = 0.0050;
  private static final boolean PAYER_4 = false;

  /** IRS 1 - USD - Fixed v LIBOR3M **/
  /** Fixed leg */
  private static final PaymentDefinition[] PAYMENT_LEG_1_DEFINITION = new FixedAnnuityDefinitionBuilder().
      payer(PAYER_1).currency(USD6MLIBOR3M.getCurrency()).notional(NOTIONAL_PROV_1).startDate(EFFECTIVE_DATE_1).
      endDate(MATURITY_DATE_1).dayCount(USD6MLIBOR3M.getFixedLegDayCount()).accrualPeriodFrequency(USD6MLIBOR3M.getFixedLegPeriod()).
      rate(FIXED_RATE_1).accrualPeriodParameters(ADJUSTED_DATE_USDLIBOR).build().getPayments();
  private static final CouponFixedDefinition[] CPN_FIXED_1_DEFINITION = new CouponFixedDefinition[PAYMENT_LEG_1_DEFINITION.length];
  static {
    for (int loopcpn = 0; loopcpn < PAYMENT_LEG_1_DEFINITION.length; loopcpn++) {
      CPN_FIXED_1_DEFINITION[loopcpn] = (CouponFixedDefinition) PAYMENT_LEG_1_DEFINITION[loopcpn];
    }
  }
  private static final AnnuityCouponFixedDefinition FIXED_LEG_1_DEFINITION = new AnnuityCouponFixedDefinition(CPN_FIXED_1_DEFINITION, NYC);
  /** Ibor leg */
  private static final AnnuityDefinition<? extends CouponDefinition> IBOR_LEG_1_DEFINITION = (AnnuityDefinition<? extends CouponDefinition>)
      new FloatingAnnuityDefinitionBuilder().payer(!PAYER_1).notional(NOTIONAL_PROV_1).startDate(EFFECTIVE_DATE_1).
          endDate(MATURITY_DATE_1).index(USDLIBOR3M).accrualPeriodFrequency(USDLIBOR3M.getTenor()).
          rollDateAdjuster(RollConvention.NONE.getRollDateAdjuster(0)).resetDateAdjustmentParameters(ADJUSTED_DATE_USDLIBOR).
          accrualPeriodParameters(ADJUSTED_DATE_USDLIBOR).dayCount(USDLIBOR3M.getDayCount()).fixingDateAdjustmentParameters(OFFSET_ADJ_USDLIBOR).
          currency(USDLIBOR3M.getCurrency()).build();
  private static final SwapCouponFixedCouponDefinition IRS_1_DEFINITION = new SwapCouponFixedCouponDefinition(FIXED_LEG_1_DEFINITION, IBOR_LEG_1_DEFINITION);

  /** IRS 2 - EUR - Fixed v EURIBOR6M **/
  /** Fixed leg */
  private static final PaymentDefinition[] PAYMENT_LEG_2_DEFINITION = new FixedAnnuityDefinitionBuilder().
      payer(PAYER_2).currency(EUR1YEURIBOR6M.getCurrency()).notional(NOTIONAL_PROV_1).startDate(EFFECTIVE_DATE_2).
      endDate(MATURITY_DATE_2).dayCount(EUR1YEURIBOR6M.getFixedLegDayCount()).accrualPeriodFrequency(EUR1YEURIBOR6M.getFixedLegPeriod()).
      rate(FIXED_RATE_2).accrualPeriodParameters(ADJUSTED_DATE_EUREURIBOR).build().getPayments();
  private static final CouponFixedDefinition[] CPN_FIXED_2_DEFINITION = new CouponFixedDefinition[PAYMENT_LEG_2_DEFINITION.length];
  static {
    for (int loopcpn = 0; loopcpn < PAYMENT_LEG_2_DEFINITION.length; loopcpn++) {
      CPN_FIXED_2_DEFINITION[loopcpn] = (CouponFixedDefinition) PAYMENT_LEG_2_DEFINITION[loopcpn];
    }
  }
  private static final AnnuityCouponFixedDefinition FIXED_LEG_2_DEFINITION = new AnnuityCouponFixedDefinition(CPN_FIXED_2_DEFINITION, NYC);
  /** Euribor leg */
  private static final AnnuityDefinition<? extends CouponDefinition> IBOR_LEG_2_DEFINITION = (AnnuityDefinition<? extends CouponDefinition>)
      new FloatingAnnuityDefinitionBuilder().payer(!PAYER_2).notional(NOTIONAL_PROV_1).startDate(EFFECTIVE_DATE_2).
          endDate(MATURITY_DATE_2).index(EURIBOR6M).accrualPeriodFrequency(EURIBOR6M.getTenor()).
          rollDateAdjuster(RollConvention.NONE.getRollDateAdjuster(0)).resetDateAdjustmentParameters(ADJUSTED_DATE_EUREURIBOR).
          accrualPeriodParameters(ADJUSTED_DATE_EUREURIBOR).dayCount(EURIBOR6M.getDayCount()).fixingDateAdjustmentParameters(OFFSET_ADJ_EUREURIBOR).
          currency(EURIBOR6M.getCurrency()).build();
  private static final SwapCouponFixedCouponDefinition IRS_2_DEFINITION = new SwapCouponFixedCouponDefinition(FIXED_LEG_2_DEFINITION, IBOR_LEG_2_DEFINITION);
  
  /** BS 2 - EUR EURIBOR3M v USD LIBOR3M **/
  /** EUR Euribor leg */
  private static final AnnuityDefinition<? extends CouponDefinition> IBOR_LEG_3_1_DEFINITION = (AnnuityDefinition<? extends CouponDefinition>)
      new FloatingAnnuityDefinitionBuilder().payer(PAYER_3).notional(NOTIONAL_PROV_1).startDate(EFFECTIVE_DATE_3).
          endDate(MATURITY_DATE_3).index(EURIBOR3M).accrualPeriodFrequency(EURIBOR3M.getTenor()).
          rollDateAdjuster(RollConvention.NONE.getRollDateAdjuster(0)).resetDateAdjustmentParameters(ADJUSTED_DATE_EUREURIBOR).
          accrualPeriodParameters(ADJUSTED_DATE_EUREURIBOR).dayCount(EURIBOR3M.getDayCount()).fixingDateAdjustmentParameters(OFFSET_ADJ_EUREURIBOR).
          currency(EURIBOR3M.getCurrency()).exchangeInitialNotional(true).exchangeFinalNotional(true).
          startDateAdjustmentParameters(ADJUSTED_DATE_EUREURIBOR).endDateAdjustmentParameters(ADJUSTED_DATE_EUREURIBOR).
          spread(SPREAD_3).build();
  /** ISD LIBOR leg */
  private static final AnnuityDefinition<? extends CouponDefinition> IBOR_LEG_3_2_DEFINITION = (AnnuityDefinition<? extends CouponDefinition>)
      new FloatingAnnuityDefinitionBuilder().payer(!PAYER_3).notional(NOTIONAL_PROV_1).startDate(EFFECTIVE_DATE_3).
          endDate(MATURITY_DATE_3).index(USDLIBOR3M).accrualPeriodFrequency(USDLIBOR3M.getTenor()).
          rollDateAdjuster(RollConvention.NONE.getRollDateAdjuster(0)).resetDateAdjustmentParameters(ADJUSTED_DATE_USDLIBOR).
          accrualPeriodParameters(ADJUSTED_DATE_USDLIBOR).dayCount(USDLIBOR3M.getDayCount()).fixingDateAdjustmentParameters(OFFSET_ADJ_USDLIBOR).
          currency(USDLIBOR3M.getCurrency()).exchangeInitialNotional(true).exchangeFinalNotional(true).
          startDateAdjustmentParameters(ADJUSTED_DATE_USDLIBOR).endDateAdjustmentParameters(ADJUSTED_DATE_USDLIBOR).build();
  private static final SwapDefinition XCCY_1_DEFINITION = new SwapDefinition(IBOR_LEG_3_1_DEFINITION, IBOR_LEG_3_2_DEFINITION);


  
  /** OIS 1 - EUR - Fixed vs EONIA 1Y**/
  private static final PaymentDefinition[] PAYMENT_OIS_LEG_1_DEFINITION = new FixedAnnuityDefinitionBuilder().
      payer(PAYER_4).currency(EUR).notional(NOTIONAL_PROV_1).startDate(EFFECTIVE_DATE_4).endDate(MATURITY_DATE_4).
      dayCount(GENERATOR_OIS_EUR.getFixedLegDayCount()).accrualPeriodFrequency(GENERATOR_OIS_EUR.getLegsPeriod()).
      rate(FIXED_RATE_4).accrualPeriodParameters(ADJUSTED_DATE_EONIA).paymentDateAdjustmentParameters(OFFSET_PAY_EONIA).
      build().getPayments();
  private static final CouponFixedDefinition[] CPN_FIXED_OIS_1_DEFINITION = new CouponFixedDefinition[PAYMENT_OIS_LEG_1_DEFINITION.length];
  static {
    for (int loopcpn = 0; loopcpn < PAYMENT_OIS_LEG_1_DEFINITION.length; loopcpn++) {
      CPN_FIXED_OIS_1_DEFINITION[loopcpn] = (CouponFixedDefinition) PAYMENT_OIS_LEG_1_DEFINITION[loopcpn];
    }
  }
  private static final AnnuityCouponFixedDefinition FIXED_OIS_LEG_1_DEFINITION = 
      new AnnuityCouponFixedDefinition(CPN_FIXED_OIS_1_DEFINITION, TARGET);
  /** ON leg */
  private static final AnnuityDefinition<? extends CouponDefinition> ON_LEG_1_DEFINITION = 
      (AnnuityDefinition<? extends CouponDefinition>) new FloatingAnnuityDefinitionBuilder().
      payer(!PAYER_4).notional(NOTIONAL_PROV_1).startDate(EFFECTIVE_DATE_4).endDate(MATURITY_DATE_4).index(EUREONIA).
      accrualPeriodFrequency(GENERATOR_OIS_EUR.getLegsPeriod()).rollDateAdjuster(RollConvention.NONE.getRollDateAdjuster(0)).
      resetDateAdjustmentParameters(ADJUSTED_DATE_EONIA).accrualPeriodParameters(ADJUSTED_DATE_EONIA).
      dayCount(EUREONIA.getDayCount()).fixingDateAdjustmentParameters(OFFSET_FIX_EONIA).currency(EUR).
      compoundingMethod(CompoundingMethod.FLAT).build();
  private static final SwapCouponFixedCouponDefinition OIS_1_DEFINITION = 
      new SwapCouponFixedCouponDefinition(FIXED_OIS_LEG_1_DEFINITION, ON_LEG_1_DEFINITION);

  
  
  /** Curves and fixing */
  private static final ZonedDateTimeDoubleTimeSeries TS_FIXED_IBOR_USD3M_WITHOUT_TODAY =
      RecentDataSetsMulticurveXCcyUsdEur.fixingUsdLibor3MWithoutLast();
  private static final ZonedDateTimeDoubleTimeSeries TS_FIXED_ON_USD_WITHOUT_TODAY = 
      RecentDataSetsMulticurveXCcyUsdEur.fixingUsdOnWithoutLast();
  private static final ZonedDateTimeDoubleTimeSeries TS_FIXED_IBOR_EUR3M_WITHOUT_TODAY = 
      RecentDataSetsMulticurveXCcyUsdEur.fixingEurEuribor3MWithoutLast();

  private static final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> MULTICURVE_FF_EO_PAIR =
      RecentDataSetsMulticurveXCcyUsdEur.getCurvesUsdOisL3EurOisE3E6(VALUATION_DATE);
  private static final MulticurveProviderDiscount MULTICURVE_FF_EO = MULTICURVE_FF_EO_PAIR.getFirst();
  private static final CurveBuildingBlockBundle BLOCK_FF_EO = MULTICURVE_FF_EO_PAIR.getSecond();

  private static final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> MULTICURVE_FF_1_PAIR =
      RecentDataSetsMulticurveXCcyUsdEur.getCurvesUsdOisL3EurFxXCcy3Bs6(VALUATION_DATE);
  private static final MulticurveProviderDiscount MULTICURVE_FF_1 = MULTICURVE_FF_1_PAIR.getFirst();
  private static final CurveBuildingBlockBundle BLOCK_FF_1 = MULTICURVE_FF_1_PAIR.getSecond();

  private static final Swap<? extends Payment, ? extends Payment> IRS_1 = IRS_1_DEFINITION.toDerivative(VALUATION_DATE,
      new ZonedDateTimeDoubleTimeSeries[] {TS_FIXED_IBOR_USD3M_WITHOUT_TODAY, TS_FIXED_IBOR_USD3M_WITHOUT_TODAY });
  private static final Swap<? extends Payment, ? extends Payment> IRS_2 = IRS_2_DEFINITION.toDerivative(VALUATION_DATE,
      new ZonedDateTimeDoubleTimeSeries[] {TS_FIXED_IBOR_USD3M_WITHOUT_TODAY, TS_FIXED_IBOR_USD3M_WITHOUT_TODAY });
  private static final Swap<? extends Payment, ? extends Payment> XCCY_1 = XCCY_1_DEFINITION.toDerivative(VALUATION_DATE,
      new ZonedDateTimeDoubleTimeSeries[] {TS_FIXED_IBOR_EUR3M_WITHOUT_TODAY, TS_FIXED_IBOR_USD3M_WITHOUT_TODAY });
  private static final Swap<? extends Payment, ? extends Payment> OIS_1 = OIS_1_DEFINITION.toDerivative(VALUATION_DATE,
      new ZonedDateTimeDoubleTimeSeries[] {TS_FIXED_IBOR_EUR3M_WITHOUT_TODAY, TS_FIXED_IBOR_USD3M_WITHOUT_TODAY });

  /** Calculators **/
  private static final PresentValueDiscountingCalculator PVDC = PresentValueDiscountingCalculator.getInstance();
  private static final ParRateDiscountingCalculator PRDC = ParRateDiscountingCalculator.getInstance();
  private static final PresentValueCurveSensitivityDiscountingCalculator PVCSDC = 
      PresentValueCurveSensitivityDiscountingCalculator.getInstance();
  private static final ParameterSensitivityParameterCalculator<ParameterProviderInterface> PSC = 
      new ParameterSensitivityParameterCalculator<>(PVCSDC);
  private static final MarketQuoteSensitivityBlockCalculator<ParameterProviderInterface> MQSBC = 
      new MarketQuoteSensitivityBlockCalculator<>(PSC);

  private static final double TOLERANCE_PV = 1.0E-2;
  private static final double BP1 = 1.0E-4;

  @SuppressWarnings("unused")
  @Test(enabled = true)
  public void presentValue() {
    // USD instrument: Same PV
    MultiCurrencyAmount pvIrs1FfEo = IRS_1.accept(PVDC, MULTICURVE_FF_EO);
    MultiCurrencyAmount pvIrs1Ff1 = IRS_1.accept(PVDC, MULTICURVE_FF_1);
    assertEquals("Tutorial - Change of collateral", pvIrs1FfEo.getAmount(USD).getAmount(), pvIrs1Ff1.getAmount(USD).getAmount(), TOLERANCE_PV);
    // EUR instrument
    MultiCurrencyAmount pvIrs2FfEo = IRS_2.accept(PVDC, MULTICURVE_FF_EO);
    MultiCurrencyAmount pvIrs2Ff1 = IRS_2.accept(PVDC, MULTICURVE_FF_1);

    MultiCurrencyAmount pvXCcy1FfEo = XCCY_1.accept(PVDC, MULTICURVE_FF_EO);
    MultiCurrencyAmount pvXCcy1Ff1 = XCCY_1.accept(PVDC, MULTICURVE_FF_1);
    
    MultiCurrencyAmount pvOis1FfEo = OIS_1.accept(PVDC, MULTICURVE_FF_EO);
    MultiCurrencyAmount pvOis1Ff1 = OIS_1.accept(PVDC, MULTICURVE_FF_1);
    
    int t = 0;
  }

  @Test(enabled = false)
  public void bucketedPv01() {
    MultipleCurrencyParameterSensitivity pvmqsIrs1FfEo = MQSBC.fromInstrument(IRS_1, MULTICURVE_FF_EO, BLOCK_FF_EO).multipliedBy(BP1);
    MultipleCurrencyParameterSensitivity pvmqsIrs2FfEo = MQSBC.fromInstrument(IRS_2, MULTICURVE_FF_EO, BLOCK_FF_EO).multipliedBy(BP1);
    ExportUtils.exportMultipleCurrencyParameterSensitivity(pvmqsIrs1FfEo, "irs-usd-mqs-ff-eo.csv");
    ExportUtils.exportMultipleCurrencyParameterSensitivity(pvmqsIrs2FfEo, "irs-eur-mqs-ff-eo.csv");
    MultipleCurrencyParameterSensitivity pvmqsIrs1Ff1 = MQSBC.fromInstrument(IRS_1, MULTICURVE_FF_1, BLOCK_FF_1).multipliedBy(BP1);
    MultipleCurrencyParameterSensitivity pvmqsIrs2Ff1 = MQSBC.fromInstrument(IRS_2, MULTICURVE_FF_1, BLOCK_FF_1).multipliedBy(BP1);
    ExportUtils.exportMultipleCurrencyParameterSensitivity(pvmqsIrs1Ff1, "irs-usd-mqs-ff-fxxccy.csv");
    ExportUtils.exportMultipleCurrencyParameterSensitivity(pvmqsIrs2Ff1, "irs-eur-mqs-ff-fxxccy.csv");
  }
  

  @Test(enabled = false)
  public void exportCurves() {
    ExportUtils.exportMulticurveProviderDiscount(MULTICURVE_FF_EO, "multicurve-localcurrencycollateral.csv");
    ExportUtils.exportMulticurveProviderDiscount(MULTICURVE_FF_1, "multicurve-fedfundcollateral-1.csv");
  }

}
