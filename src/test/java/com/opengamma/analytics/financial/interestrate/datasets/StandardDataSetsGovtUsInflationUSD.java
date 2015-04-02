/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.datasets;

import static com.opengamma.strata.basics.currency.Currency.USD;

import java.time.Period;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.LinkedListMultimap;
import com.opengamma.analytics.convention.calendar.Calendar;
import com.opengamma.analytics.financial.curve.inflation.generator.GeneratorPriceIndexCurve;
import com.opengamma.analytics.financial.curve.inflation.generator.GeneratorPriceIndexCurveInterpolatedAnchor;
import com.opengamma.analytics.financial.curve.inflation.generator.GeneratorPriceIndexCurveMultiplyFixedCurve;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorCurve;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorYDCurve;
import com.opengamma.analytics.financial.datasets.CalendarUSD;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.bond.BillSecurityDefinition;
import com.opengamma.analytics.financial.instrument.bond.BondDataSetsUsd;
import com.opengamma.analytics.financial.instrument.bond.BondFixedSecurityDefinition;
import com.opengamma.analytics.financial.instrument.index.GeneratorAttribute;
import com.opengamma.analytics.financial.instrument.index.GeneratorAttributeET;
import com.opengamma.analytics.financial.instrument.index.GeneratorAttributeIR;
import com.opengamma.analytics.financial.instrument.index.GeneratorBill;
import com.opengamma.analytics.financial.instrument.index.GeneratorBondFixed;
import com.opengamma.analytics.financial.instrument.index.GeneratorInstrument;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedON;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedONMaster;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.instrument.index.IndexPrice;
import com.opengamma.analytics.financial.instrument.index.IndexPriceMaster;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.legalentity.LegalEntityFilter;
import com.opengamma.analytics.financial.legalentity.LegalEntityShortName;
import com.opengamma.analytics.financial.model.interestrate.curve.SeasonalCurve;
import com.opengamma.analytics.financial.provider.calculator.generic.LastFixingEndTimeCalculator;
import com.opengamma.analytics.financial.provider.calculator.inflationissuer.ParSpreadInflationMarketQuoteCurveSensitivityIssuerDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.inflationissuer.ParSpreadInflationMarketQuoteIssuerDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.issuer.ParSpreadMarketQuoteCurveSensitivityIssuerDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.issuer.ParSpreadMarketQuoteIssuerDiscountingCalculator;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.curve.CurveCalibrationConventionDataSets;
import com.opengamma.analytics.financial.provider.curve.CurveCalibrationTestsUtils;
import com.opengamma.analytics.financial.provider.curve.inflationissuer.InflationIssuerDiscountBuildingRepository;
import com.opengamma.analytics.financial.provider.curve.issuer.IssuerDiscountBuildingRepository;
import com.opengamma.analytics.financial.provider.description.inflation.InflationIssuerProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderDiscount;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.math.curve.DoublesCurve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.curve.MultiplyCurveSpreadFunction;
import com.opengamma.analytics.math.curve.SpreadDoublesCurve;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.util.time.DateUtils;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.analytics.util.timeseries.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.basics.currency.FxMatrix;
import com.opengamma.strata.collect.tuple.Pair;

/**
 * Curves calibration in USD: Dsc/ON, US government and inflation curves.
 * 1) DSCON-OIS_USD-BLBNUSGOVT. Discounting and UG Govt curves; used for comparison purposes.
 * 2) DSCON-OIS_USD-BLBNUSGOVT_HICP-ZC. Discounting, US Govt and US CPI (from ZC swaps).
 * 3) DSCON-OIS_USD-BLBNUSGOVT_HICP-ZC. Discounting, US Govt and US CPI (from ZC swaps). Seasonality and know current
 * CPI used to calibrate the inflation curve.
 * Data stored in snapshots for comparison with platform.
 */
public class StandardDataSetsGovtUsInflationUSD {

  private static final Calendar NYC = new CalendarUSD("NYC");
  private static final FxMatrix FX_MATRIX = FxMatrix.EMPTY_FX_MATRIX;
  
  private static final double NOTIONAL = 1.0;

  private static final GeneratorSwapFixedONMaster GENERATOR_OIS_MASTER = GeneratorSwapFixedONMaster.getInstance();

  private static final GeneratorSwapFixedON GENERATOR_OIS_USD = GENERATOR_OIS_MASTER.getGenerator("USD1YFEDFUND", NYC);
  private static final IndexON USDFEDFUND = GENERATOR_OIS_USD.getIndex();
  
  private static final String CURVE_NAME_OIS = "USD-OIS";
  private static final String CURVE_NAME_GVT = "USD-BLBNUSGOVT";
  private static final String CURVE_NAME_CPI = "USD-ZCHICP";
  
  private static final IndexPrice USCPI = IndexPriceMaster.getInstance().getIndex("USCPI");
  private static final GeneratorPriceIndexCurve GENERATOR_PI_FIX_LIN = 
      CurveCalibrationConventionDataSets.generatorPiFixLin();
  private static final GeneratorYDCurve GENERATOR_YD_MAT_LIN = 
      CurveCalibrationConventionDataSets.generatorYDMatLin();
  private static final Interpolator1D INTERPOLATOR_STEP_FLAT =
      CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.STEP,
          Interpolator1DFactory.FLAT_EXTRAPOLATOR, Interpolator1DFactory.FLAT_EXTRAPOLATOR);
  private static final Interpolator1D INTERPOLATOR_LINEAR = 
      CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LINEAR, 
          Interpolator1DFactory.FLAT_EXTRAPOLATOR, Interpolator1DFactory.FLAT_EXTRAPOLATOR);
  private static final LastFixingEndTimeCalculator LAST_FIXING_END_CALCULATOR = LastFixingEndTimeCalculator.getInstance();
  public static final double[] SEASONAL_FACTORS = 
    {1.005, 1.001, 1.01, .999, .998, .9997, 1.004, 1.006, .994, .993, .9991 };
  
  /** Market values for the dsc USD curve */
  private static final double[] OIS_MARKET_QUOTES = new double[] {0.0016, 0.0016,
    0.00072000, 0.00082000, 0.00093000, 0.00090000, 0.00105000,
    0.00118500, 0.00318650, 0.00704000, 0.01121500, 0.01515000,
    0.01845500, 0.02111000, 0.02332000, 0.02513500, 0.02668500 }; //17
  /** Generators for the dsc USD curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] DSC_1_USD_GENERATORS = 
      CurveCalibrationConventionDataSets.generatorUsdOnOisFfs(2, 15, 0);
  /** Tenors for the dsc USD curve */
  private static final Period[] DSC_1_USD_TENOR = new Period[] {Period.ofDays(0), Period.ofDays(1),
    Period.ofMonths(1), Period.ofMonths(2), Period.ofMonths(3), Period.ofMonths(6), Period.ofMonths(9),
    Period.ofYears(1), Period.ofYears(2), Period.ofYears(3), Period.ofYears(4), Period.ofYears(5),
    Period.ofYears(6), Period.ofYears(7), Period.ofYears(8), Period.ofYears(9), Period.ofYears(10) };
  private static final GeneratorAttributeIR[] DSC_1_USD_ATTR = new GeneratorAttributeIR[DSC_1_USD_TENOR.length];
  static {
    for (int loopins = 0; loopins < 2; loopins++) {
      DSC_1_USD_ATTR[loopins] = new GeneratorAttributeIR(DSC_1_USD_TENOR[loopins], Period.ofDays(0));
    }
    for (int loopins = 2; loopins < DSC_1_USD_TENOR.length; loopins++) {
      DSC_1_USD_ATTR[loopins] = new GeneratorAttributeIR(DSC_1_USD_TENOR[loopins]);
    }
  }
  
  /** Market quotes for the USD-USGVT curve */
  // 3M: 2015-01-08
  // 6M: 2015-04-09
  // 12M: 2015-09-17
  // 2Y: 2016-09-30 Street Convention - 0.0050 100-07 - CT2 GOVT - US912828F478
  // 5Y: 2019-09-30 0.0175 101-11 - US912828F395
  // 10Y: 2024-08-15 0.02375 101-10 - US912828D564
  private static final ZonedDateTime[] BILL_MATURITY = new ZonedDateTime[] {DateUtils.getUTCDate(2015, 1, 8),
    DateUtils.getUTCDate(2015, 4, 9), DateUtils.getUTCDate(2015, 9, 17) };
  private static final int NB_BILL = BILL_MATURITY.length;
  private static final BillSecurityDefinition[] BILL_SECURITY = new BillSecurityDefinition[NB_BILL];
  private static final GeneratorBill[] GENERATOR_BILL = new GeneratorBill[NB_BILL];
  static {
    for (int loopbill = 0; loopbill < BILL_MATURITY.length; loopbill++) {
      BILL_SECURITY[loopbill] = BondDataSetsUsd.billUS(NOTIONAL, BILL_MATURITY[loopbill]);
      GENERATOR_BILL[loopbill] = new GeneratorBill("GeneratorBill" + loopbill, BILL_SECURITY[loopbill]);
    }
  }
  private static final int NB_BOND = 3;
  private static final BondFixedSecurityDefinition[] BOND_SECURITY = new BondFixedSecurityDefinition[NB_BOND];
  private static final GeneratorBondFixed[] GENERATOR_BOND = new GeneratorBondFixed[NB_BOND];
  static {
    BOND_SECURITY[0] = BondDataSetsUsd.bondUST_20160930(NOTIONAL);
    BOND_SECURITY[1] = BondDataSetsUsd.bondUST_20190930(NOTIONAL);
    BOND_SECURITY[2] = BondDataSetsUsd.bondUST_20240815(NOTIONAL);
    for (int loopbnd = 0; loopbnd < NB_BOND; loopbnd++) {
      GENERATOR_BOND[loopbnd] = new GeneratorBondFixed("GeneratorBond" + loopbnd, BOND_SECURITY[loopbnd]);
    }
  }
  /** Market values for the US Govt curve */
  private static final double[] GOVT_MARKET_QUOTES = 
      new double[] {0.00005, 0.0003, 0.0007, 1.0+7.0/32.0/100.0, 1.01+11.0/32.0/100.0, 1.01+10.0/32.0/100.0 };
  /** Generators for the US Govt curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] GOVT_GENERATORS =
      new GeneratorInstrument<?>[] {GENERATOR_BILL[0], GENERATOR_BILL[1], GENERATOR_BILL[2],
        GENERATOR_BOND[0], GENERATOR_BOND[1], GENERATOR_BOND[2] };
  /** Attributes for the US Govt curve */
  private static final GeneratorAttributeET[] GOVT_ATTR = new GeneratorAttributeET[GOVT_MARKET_QUOTES.length];
  static {
    for (int loopins = 0; loopins < NB_BILL; loopins++) {
      GOVT_ATTR[loopins] = new GeneratorAttributeET(false);
    }
    for (int loopins = NB_BILL; loopins < GOVT_MARKET_QUOTES.length; loopins++) {
      GOVT_ATTR[loopins] = new GeneratorAttributeET(true);
    }
  }
  private static final LegalEntity US_GOVT_LEGAL_ENTITY = BondDataSetsUsd.getLegalEntityUsGovt();
  
  /** Market values for the HICP USD curve */ /** USSWITx Interpolation 3M lag */
  public static final double[] CPI_USD_MARKET_QUOTES = 
      new double[] {0.0200, 0.0200, 0.0200, 0.0200, 0.0200, 0.0200, 0.0200, 0.0200, 0.0200, 0.0200, 
    0.0200, 0.0200, 0.0200, 0.0200, 0.0200 };
  /** Generators for the HICP USD curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] HICP_USD_GENERATORS = 
      CurveCalibrationConventionDataSets.generatorUsdCpi(15);
  /** Tenors for the HICP USD curve */
  private static final Period[] HICP_USD_TENOR = new Period[] {
    Period.ofYears(1), Period.ofYears(2), Period.ofYears(3), Period.ofYears(4), Period.ofYears(5), 
    Period.ofYears(6), Period.ofYears(7), Period.ofYears(8), Period.ofYears(9), Period.ofYears(10), 
    Period.ofYears(12), Period.ofYears(15), Period.ofYears(20), Period.ofYears(25), Period.ofYears(30) };
  private static final GeneratorAttributeIR[] HICP_USD_ATTR = new GeneratorAttributeIR[HICP_USD_TENOR.length];
  static {
    for (int loopins = 0; loopins < HICP_USD_TENOR.length; loopins++) {
      HICP_USD_ATTR[loopins] = new GeneratorAttributeIR(HICP_USD_TENOR[loopins]);
    }
  }
  
  /** Map of index/curves */
  private static final LinkedHashMap<String, Currency> DSC_MAP = new LinkedHashMap<>();
  private static final LinkedHashMap<String, IndexON[]> FWD_ON_MAP = new LinkedHashMap<>();
  private static final LinkedHashMap<String, IborIndex[]> FWD_IBOR_MAP = new LinkedHashMap<>();
  private static final LinkedListMultimap<String, Pair<Object, LegalEntityFilter<LegalEntity>>> DSC_ISS_MAP = LinkedListMultimap.create();
  private static final LinkedHashMap<String, IndexPrice[]> USD_HICP_MAP = new LinkedHashMap<>();

  static {
    DSC_MAP.put(CURVE_NAME_OIS, USD);
    FWD_ON_MAP.put(CURVE_NAME_OIS, new IndexON[] {USDFEDFUND });
    USD_HICP_MAP.put(CURVE_NAME_CPI, new IndexPrice[] {USCPI });
    DSC_ISS_MAP.put(CURVE_NAME_GVT, Pair.of((Object) US_GOVT_LEGAL_ENTITY.getShortName(),
        (LegalEntityFilter<LegalEntity>) new LegalEntityShortName()));
  }
  
  /** Calculators */
  private static final ParSpreadMarketQuoteIssuerDiscountingCalculator PSMQDIC = 
      ParSpreadMarketQuoteIssuerDiscountingCalculator.getInstance();
  private static final ParSpreadMarketQuoteCurveSensitivityIssuerDiscountingCalculator PSMQCSIDC =
      ParSpreadMarketQuoteCurveSensitivityIssuerDiscountingCalculator.getInstance();
  private static final ParSpreadInflationMarketQuoteIssuerDiscountingCalculator PSMQIssuerInflationC = 
      ParSpreadInflationMarketQuoteIssuerDiscountingCalculator.getInstance();
  private static final ParSpreadInflationMarketQuoteCurveSensitivityIssuerDiscountingCalculator PSMQCSIssuerInflationC = 
      ParSpreadInflationMarketQuoteCurveSensitivityIssuerDiscountingCalculator.getInstance();

  private static final IssuerDiscountBuildingRepository CURVE_BUILDING_REPOSITORY_ISSUER = 
      CurveCalibrationConventionDataSets.curveBuildingRepositoryIssuer();
  private static final InflationIssuerDiscountBuildingRepository CURVE_BUILDING_REPOSITORY_INFLATION_ISSUER = 
      CurveCalibrationConventionDataSets.curveBuildingRepositoryInflationIssuer();
  
  /**
   * Returns a set of calibrated curve: dsc/on with OIS and US CPI with zero-coupon swaps.
   * The curves are calibrated as two units in a unique calibration.
   * @param calibrationDate The calibration date.
   * @return  The calibrated curves and Jacobians.
   */
  public static Pair<IssuerProviderDiscount, CurveBuildingBlockBundle> getCurvesUsdOisUsGovt(
      ZonedDateTime calibrationDate) {
    InstrumentDefinition<?>[] oisDefinition = CurveCalibrationTestsUtils.getDefinitions(calibrationDate, NOTIONAL,
        OIS_MARKET_QUOTES, DSC_1_USD_GENERATORS, DSC_1_USD_ATTR);
    InstrumentDefinition<?>[] govtDefinition = CurveCalibrationTestsUtils.getDefinitions(calibrationDate, NOTIONAL,
        GOVT_MARKET_QUOTES, GOVT_GENERATORS, GOVT_ATTR);
    InstrumentDefinition<?>[][][] unitDefinition = 
        new InstrumentDefinition<?>[][][] {{oisDefinition}, {govtDefinition}};
    GeneratorYDCurve[][] generator = 
        new GeneratorYDCurve[][] {{GENERATOR_YD_MAT_LIN}, {GENERATOR_YD_MAT_LIN}};
    String[][] namesCurves = new String[][] {{CURVE_NAME_OIS}, {CURVE_NAME_GVT}};
    Map<IndexON, ZonedDateTimeDoubleTimeSeries> htsOn = getOnHts(calibrationDate, false);
    IssuerProviderDiscount knownDataIssuer = new IssuerProviderDiscount(FX_MATRIX);
    Pair<IssuerProviderDiscount, CurveBuildingBlockBundle> multicurveInflation =
        CurveCalibrationTestsUtils.makeCurvesFromDefinitionsIssuer(calibrationDate, unitDefinition, 
            generator, namesCurves, knownDataIssuer, new CurveBuildingBlockBundle(), PSMQDIC, PSMQCSIDC, DSC_MAP,
            FWD_ON_MAP, FWD_IBOR_MAP, DSC_ISS_MAP, 
            CURVE_BUILDING_REPOSITORY_ISSUER, htsOn, HTS_IBOR);
    return multicurveInflation;
  }
  
  /**
   * Returns a set of calibrated curve: dsc/on with OIS and US CPI with zero-coupon swaps.
   * The curves are calibrated as two units in a unique calibration.
   * @param calibrationDate The calibration date.
   * @return  The calibrated curves and Jacobians.
   */
  public static Pair<InflationIssuerProviderDiscount, CurveBuildingBlockBundle> getCurvesUsdOisUsGovtUsCpi(
      ZonedDateTime calibrationDate) {
    InstrumentDefinition<?>[] oisDefinition = CurveCalibrationTestsUtils.getDefinitions(calibrationDate, NOTIONAL,
        OIS_MARKET_QUOTES, DSC_1_USD_GENERATORS, DSC_1_USD_ATTR);
    InstrumentDefinition<?>[] govtDefinition = CurveCalibrationTestsUtils.getDefinitions(calibrationDate, NOTIONAL,
        GOVT_MARKET_QUOTES, GOVT_GENERATORS, GOVT_ATTR);
    InstrumentDefinition<?>[] inflDefinition = CurveCalibrationTestsUtils.getDefinitions(calibrationDate, NOTIONAL,
        CPI_USD_MARKET_QUOTES, HICP_USD_GENERATORS, HICP_USD_ATTR);
    InstrumentDefinition<?>[][][] unitDefinition = 
        new InstrumentDefinition<?>[][][] {{oisDefinition}, {govtDefinition}, {inflDefinition}};
    GeneratorCurve[][] generator = 
        new GeneratorCurve[][] {{GENERATOR_YD_MAT_LIN}, {GENERATOR_YD_MAT_LIN}, {GENERATOR_PI_FIX_LIN}};
    String[][] namesCurves = new String[][] {{CURVE_NAME_OIS}, {CURVE_NAME_GVT}, {CURVE_NAME_CPI}};
    Map<IndexON, ZonedDateTimeDoubleTimeSeries> htsOn = getOnHts(calibrationDate, false);
    InflationIssuerProviderDiscount knownDataIssuer = new InflationIssuerProviderDiscount(FX_MATRIX);
    Pair<InflationIssuerProviderDiscount, CurveBuildingBlockBundle> multicurveInflation = 
        CurveCalibrationTestsUtils.makeCurvesFromDefinitionsInflationIssuer(calibrationDate, unitDefinition, 
            generator, namesCurves, knownDataIssuer, new CurveBuildingBlockBundle(), PSMQIssuerInflationC, 
            PSMQCSIssuerInflationC, DSC_MAP, FWD_ON_MAP, FWD_IBOR_MAP, USD_HICP_MAP, DSC_ISS_MAP, 
            CURVE_BUILDING_REPOSITORY_INFLATION_ISSUER, htsOn, HTS_IBOR, getCpiHts(calibrationDate));
    return multicurveInflation;
  }
  
  /**
   * Returns a set of calibrated curve: dsc/on with OIS and US CPI with zero-coupon swaps.
   * The curves are calibrated as two units in a unique calibration.
   * The inflation curve start with the known data (CPI up to calibration date) and a seasonality is used.
   * @param calibrationDate The calibration date.
   * @return  The calibrated curves and Jacobians.
   */
  public static Pair<InflationIssuerProviderDiscount, CurveBuildingBlockBundle> getCurvesUsdOisUsGovtUsCpiCurrentSeasonality(
      ZonedDateTime calibrationDate) {
    ZonedDateTimeDoubleTimeSeries htsCpi = StandardTimeSeriesInflationDataSets.timeSeriesUsCpi(
        calibrationDate.minusMonths(7).with(TemporalAdjusters.lastDayOfMonth()), calibrationDate);
    List<ZonedDateTime> timesList = htsCpi.times();
    List<Double> valuesList = htsCpi.values();
    int nbTimes = timesList.size();
    Double[] times = new Double[nbTimes];
    Double[] values = valuesList.toArray(new Double[0]);
    for(int i=0; i<nbTimes; i++) {
      times[i] = TimeCalculator.getTimeBetween(calibrationDate, timesList.get(i));
    }
    InterpolatedDoublesCurve startCurve = new InterpolatedDoublesCurve(times, values, INTERPOLATOR_STEP_FLAT, true);
    // Create seasonal adjustments
    ZonedDateTime currentDataEnd = timesList.get(timesList.size()-1);
    ZonedDateTime[] seasonalityDate = ScheduleCalculator.getUnadjustedDateSchedule(currentDataEnd, 
        currentDataEnd.plusYears(30), Period.ofMonths(1), true, false);
    double[] seasonalStep = new double[seasonalityDate.length];
    for (int loopins = 0; loopins < seasonalityDate.length; loopins++) {
        seasonalStep[loopins] = TimeCalculator.getTimeBetween(calibrationDate, seasonalityDate[loopins]);
    }
    SeasonalCurve seasonalCurve = new SeasonalCurve(seasonalStep, SEASONAL_FACTORS, false);
    // Total adjustment as multiplication between seasonal and start.
    DoublesCurve adjustmentCurve = new SpreadDoublesCurve(MultiplyCurveSpreadFunction.getInstance(), startCurve, seasonalCurve);
    GeneratorPriceIndexCurve generatorFixLinAnchor = new GeneratorPriceIndexCurveInterpolatedAnchor(
        LAST_FIXING_END_CALCULATOR, INTERPOLATOR_LINEAR, times[nbTimes-1], 1.0);
    GeneratorPriceIndexCurve genAdjustment = 
        new GeneratorPriceIndexCurveMultiplyFixedCurve(generatorFixLinAnchor, adjustmentCurve);
    InstrumentDefinition<?>[] oisDefinition = CurveCalibrationTestsUtils.getDefinitions(calibrationDate, NOTIONAL,
        OIS_MARKET_QUOTES, DSC_1_USD_GENERATORS, DSC_1_USD_ATTR);
    InstrumentDefinition<?>[] govtDefinition = CurveCalibrationTestsUtils.getDefinitions(calibrationDate, NOTIONAL,
        GOVT_MARKET_QUOTES, GOVT_GENERATORS, GOVT_ATTR);
    InstrumentDefinition<?>[] inflDefinition = CurveCalibrationTestsUtils.getDefinitions(calibrationDate, NOTIONAL,
        CPI_USD_MARKET_QUOTES, HICP_USD_GENERATORS, HICP_USD_ATTR);
    InstrumentDefinition<?>[][][] unitDefinition = 
        new InstrumentDefinition<?>[][][] {{oisDefinition}, {govtDefinition}, {inflDefinition}};
    GeneratorCurve[][] generator = 
        new GeneratorCurve[][] {{GENERATOR_YD_MAT_LIN}, {GENERATOR_YD_MAT_LIN}, {genAdjustment}};
    String[][] namesCurves = new String[][] {{CURVE_NAME_OIS}, {CURVE_NAME_GVT}, {CURVE_NAME_CPI}};
    Map<IndexON, ZonedDateTimeDoubleTimeSeries> htsOn = getOnHts(calibrationDate, false);
    InflationIssuerProviderDiscount knownDataIssuer = new InflationIssuerProviderDiscount(FX_MATRIX);
    Pair<InflationIssuerProviderDiscount, CurveBuildingBlockBundle> multicurveInflation = 
        CurveCalibrationTestsUtils.makeCurvesFromDefinitionsInflationIssuer(calibrationDate, unitDefinition, 
            generator, namesCurves, knownDataIssuer, new CurveBuildingBlockBundle(), PSMQIssuerInflationC, 
            PSMQCSIssuerInflationC, DSC_MAP, FWD_ON_MAP, FWD_IBOR_MAP, USD_HICP_MAP, DSC_ISS_MAP, 
            CURVE_BUILDING_REPOSITORY_INFLATION_ISSUER, htsOn, HTS_IBOR, getCpiHts(calibrationDate));
    return multicurveInflation;
  }
  
  private static Map<IndexON,ZonedDateTimeDoubleTimeSeries> getOnHts(ZonedDateTime calibrationDate, boolean withToday) {
    ZonedDateTime referenceDate = withToday ? calibrationDate : calibrationDate.minusDays(1);
    ZonedDateTimeDoubleTimeSeries htsOn = StandardTimeSeriesOnIborDataSets.timeSeriesUsdOn2014Jan(referenceDate);
    Map<IndexON,ZonedDateTimeDoubleTimeSeries> htsOnMap = new HashMap<>();
    htsOnMap.put(USDFEDFUND, htsOn);    
    return htsOnMap;
  }
  
  private static Map<IndexPrice,ZonedDateTimeDoubleTimeSeries> getCpiHts(ZonedDateTime calibrationDate) {
    ZonedDateTimeDoubleTimeSeries htsCpi = StandardTimeSeriesInflationDataSets.timeSeriesUsCpi(calibrationDate);
    Map<IndexPrice,ZonedDateTimeDoubleTimeSeries> htsCpiMap = new HashMap<>();
    htsCpiMap.put(USCPI, htsCpi);    
    return htsCpiMap;
  }
  
  private static final Map<IborIndex,ZonedDateTimeDoubleTimeSeries> HTS_IBOR = new HashMap<>();
  
}
