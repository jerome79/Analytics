/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.curve;

import static com.opengamma.strata.basics.currency.Currency.BRL;
import static org.testng.AssertJUnit.assertEquals;

import java.time.Period;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorCurveYieldInterpolated;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorYDCurve;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.cash.CashDefinition;
import com.opengamma.analytics.financial.instrument.fra.ForwardRateAgreementDefinition;
import com.opengamma.analytics.financial.instrument.index.GeneratorAttribute;
import com.opengamma.analytics.financial.instrument.index.GeneratorAttributeIR;
import com.opengamma.analytics.financial.instrument.index.GeneratorInstrument;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedCompoundedONCompounded;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedCompoundedONCompoundedMaster;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedCompoundedONCompoundedDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedONDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParSpreadMarketQuoteDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.generic.LastTimeCalculator;
import com.opengamma.analytics.financial.provider.curve.multicurve.MulticurveProviderForwardBuildingRepository;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderForward;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.util.time.DateUtils;
import com.opengamma.analytics.util.timeseries.zdt.ImmutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.analytics.util.timeseries.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.basics.currency.FxMatrix;
import com.opengamma.strata.basics.date.HolidayCalendar;
import com.opengamma.strata.basics.date.HolidayCalendars;
import com.opengamma.strata.collect.tuple.Pair;

@Test
public class MulticurveBuildingDiscountingForwardBrazilianONTest {

  private static final Interpolator1D INTERPOLATOR = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LOG_NATURAL_CUBIC_MONOTONE, Interpolator1DFactory.FLAT_EXTRAPOLATOR,
      Interpolator1DFactory.FLAT_EXTRAPOLATOR);

  private static final LastTimeCalculator MATURITY_CALCULATOR = LastTimeCalculator.getInstance();
  private static final double TOLERANCE_ROOT = 1.0E-10;
  private static final int STEP_MAX = 100;

  private static final HolidayCalendar NYC = HolidayCalendars.SAT_SUN;

  private static final FxMatrix FX_MATRIX = FxMatrix.empty();

  private static final double NOTIONAL = 1.0;

  private static final GeneratorSwapFixedCompoundedONCompounded GENERATOR_OIS_BRL = GeneratorSwapFixedCompoundedONCompoundedMaster.getInstance().getGenerator("BRLCDI", NYC);
  private static final IndexON INDEX_ON_BRL = GENERATOR_OIS_BRL.getIndex();

  private static final ZonedDateTime NOW = DateUtils.getUTCDate(2013, 10, 7);

  private static final ZonedDateTimeDoubleTimeSeries TS_EMPTY = ImmutableZonedDateTimeDoubleTimeSeries.ofEmptyUTC();
  private static final ZonedDateTimeDoubleTimeSeries TS_ON_BRL_WITH_TODAY = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2013, 8, 10),
    DateUtils.getUTCDate(2013, 9, 10) }, new double[] {0.0881, 0.0881 });
  private static final ZonedDateTimeDoubleTimeSeries TS_ON_BRL_WITHOUT_TODAY = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2013, 8, 10),
    DateUtils.getUTCDate(2013, 9, 10) }, new double[] {0.0881, 0.0881 });
  private static final ZonedDateTimeDoubleTimeSeries[] TS_FIXED_CDI_BRL_WITH_TODAY = new ZonedDateTimeDoubleTimeSeries[] {TS_EMPTY, TS_ON_BRL_WITH_TODAY };
  private static final ZonedDateTimeDoubleTimeSeries[] TS_FIXED_CDI_BRL_WITHOUT_TODAY = new ZonedDateTimeDoubleTimeSeries[] {TS_EMPTY, TS_ON_BRL_WITHOUT_TODAY };

  private static final String CURVE_NAME_DSC_BRL = "BRL Dsc";

  /** Market values for the dsc BRL curve */
  private static final double[] DSC_BRL_MARKET_QUOTES = new double[] {0.092925, 0.09325, 0.09458, 0.09545, 0.09665, 0.09845, 0.1001, 0.10101, 0.10335, 0.10565, 0.10725, 0.10865, 0.1098, 0.11085,
    0.1113, 0.11165, 0.11205, 0.1127 };
  /** Generators for the dsc BRL curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] DSC_BRL_GENERATORS = new GeneratorInstrument<?>[] {GENERATOR_OIS_BRL, GENERATOR_OIS_BRL,
    GENERATOR_OIS_BRL, GENERATOR_OIS_BRL, GENERATOR_OIS_BRL, GENERATOR_OIS_BRL, GENERATOR_OIS_BRL, GENERATOR_OIS_BRL, GENERATOR_OIS_BRL, GENERATOR_OIS_BRL, GENERATOR_OIS_BRL, GENERATOR_OIS_BRL,
    GENERATOR_OIS_BRL, GENERATOR_OIS_BRL, GENERATOR_OIS_BRL, GENERATOR_OIS_BRL, GENERATOR_OIS_BRL, GENERATOR_OIS_BRL };
  /** Tenors for the dsc BRL curve */
  private static final Period[] DSC_BRL_TENOR = new Period[] {Period.ofDays(23), Period.ofDays(54), Period.ofDays(85),
    Period.ofDays(117), Period.ofDays(174), Period.ofDays(267), Period.ofDays(357), Period.ofDays(450), Period.ofDays(539), Period.ofDays(630), Period.ofDays(722), Period.ofDays(817),
    Period.ofDays(996), Period.ofDays(1090), Period.ofDays(1181), Period.ofDays(1272), Period.ofDays(1363), Period.ofDays(1454) };
  private static final GeneratorAttributeIR[] DSC_BRL_ATTR = new GeneratorAttributeIR[DSC_BRL_TENOR.length];
  static {
    for (int loopins = 0; loopins < DSC_BRL_TENOR.length; loopins++) {
      DSC_BRL_ATTR[loopins] = new GeneratorAttributeIR(DSC_BRL_TENOR[loopins]);
    }
  }

  /** Standard BRL discounting curve instrument definitions */
  private static final InstrumentDefinition<?>[] DEFINITIONS_DSC_BRL;

  /** Units of curves */
  private static final int[] NB_UNITS = new int[] {1 };
  private static final int NB_BLOCKS = NB_UNITS.length;
  private static final InstrumentDefinition<?>[][][][] DEFINITIONS_UNITS = new InstrumentDefinition<?>[NB_BLOCKS][][][];
  private static final GeneratorYDCurve[][][] GENERATORS_UNITS = new GeneratorYDCurve[NB_BLOCKS][][];
  private static final String[][][] NAMES_UNITS = new String[NB_BLOCKS][][];
  private static final MulticurveProviderForward KNOWN_DATA = new MulticurveProviderForward(FX_MATRIX);
  private static final LinkedHashMap<String, Currency> DSC_MAP = new LinkedHashMap<>();
  private static final LinkedHashMap<String, IndexON> FWD_ON_MAP = new LinkedHashMap<>();
  private static final LinkedHashMap<String, IborIndex> FWD_IBOR_MAP = new LinkedHashMap<>();

  static {
    DEFINITIONS_DSC_BRL = getDefinitions(DSC_BRL_MARKET_QUOTES, DSC_BRL_GENERATORS, DSC_BRL_ATTR);
    for (int loopblock = 0; loopblock < NB_BLOCKS; loopblock++) {
      DEFINITIONS_UNITS[loopblock] = new InstrumentDefinition<?>[NB_UNITS[loopblock]][][];
      GENERATORS_UNITS[loopblock] = new GeneratorYDCurve[NB_UNITS[loopblock]][];
      NAMES_UNITS[loopblock] = new String[NB_UNITS[loopblock]][];
    }
    DEFINITIONS_UNITS[0][0] = new InstrumentDefinition<?>[][] {DEFINITIONS_DSC_BRL };
    final GeneratorYDCurve genIntLin = new GeneratorCurveYieldInterpolated(MATURITY_CALCULATOR, INTERPOLATOR);
    GENERATORS_UNITS[0][0] = new GeneratorYDCurve[] {genIntLin };
    NAMES_UNITS[0][0] = new String[] {CURVE_NAME_DSC_BRL };
    DSC_MAP.put(CURVE_NAME_DSC_BRL, BRL);
    FWD_ON_MAP.put(CURVE_NAME_DSC_BRL, INDEX_ON_BRL);
  }

  @SuppressWarnings({"rawtypes", "unchecked" })
  public static InstrumentDefinition<?>[] getDefinitions(final double[] marketQuotes, final GeneratorInstrument[] generators, final GeneratorAttribute[] attribute) {
    final InstrumentDefinition<?>[] definitions = new InstrumentDefinition<?>[marketQuotes.length];
    for (int loopmv = 0; loopmv < marketQuotes.length; loopmv++) {
      definitions[loopmv] = generators[loopmv].generateInstrument(NOW, marketQuotes[loopmv], NOTIONAL, attribute[loopmv]);
    }
    return definitions;
  }

  private static List<Pair<MulticurveProviderForward, CurveBuildingBlockBundle>> CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK = new ArrayList<>();

  // Calculator
  private static final PresentValueDiscountingCalculator PVC = PresentValueDiscountingCalculator.getInstance();
  private static final ParSpreadMarketQuoteDiscountingCalculator PSMQC = ParSpreadMarketQuoteDiscountingCalculator.getInstance();
  private static final ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator PSMQCSC = ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator.getInstance();

  private static final MulticurveProviderForwardBuildingRepository CURVE_BUILDING_REPOSITORY = new MulticurveProviderForwardBuildingRepository(TOLERANCE_ROOT, TOLERANCE_ROOT, STEP_MAX);

  private static final double TOLERANCE_CAL = 1.0E-9;

  @BeforeSuite
  static void initClass() {
    for (int loopblock = 0; loopblock < NB_BLOCKS; loopblock++) {
      CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK.add(makeCurvesFromDefinitions(DEFINITIONS_UNITS[loopblock], GENERATORS_UNITS[loopblock], NAMES_UNITS[loopblock], KNOWN_DATA, PSMQC, PSMQCSC, false));
    }
  }

  public void curveConstructionGeneratorOtherBlocks() {
    for (int loopblock = 0; loopblock < NB_BLOCKS; loopblock++) {
      curveConstructionTest(DEFINITIONS_UNITS[loopblock], CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK.get(loopblock).getFirst(), false, loopblock);
    }
  }

  private void curveConstructionTest(final InstrumentDefinition<?>[][][] definitions, final MulticurveProviderForward curves, final boolean withToday, final int block) {
    final int nbBlocks = definitions.length;
    for (int loopblock = 0; loopblock < nbBlocks; loopblock++) {
      final InstrumentDerivative[][] instruments = convert(definitions[loopblock], loopblock, withToday);
      final double[][] pv = new double[instruments.length][];
      for (int loopcurve = 0; loopcurve < instruments.length; loopcurve++) {
        pv[loopcurve] = new double[instruments[loopcurve].length];
        for (int loopins = 0; loopins < instruments[loopcurve].length; loopins++) {
          pv[loopcurve][loopins] = curves.getFxRates().convert(instruments[loopcurve][loopins].accept(PVC, curves), BRL).getAmount();
          assertEquals("Curve construction: block " + block + ", unit " + loopblock + " - instrument " + loopins, 0, pv[loopcurve][loopins], TOLERANCE_CAL);
        }
      }
    }
  }

  private static Pair<MulticurveProviderForward, CurveBuildingBlockBundle> makeCurvesFromDefinitions(final InstrumentDefinition<?>[][][] definitions, final GeneratorYDCurve[][] curveGenerators,
      final String[][] curveNames, final MulticurveProviderForward knownData, final InstrumentDerivativeVisitor<ParameterProviderInterface, Double> calculator,
      final InstrumentDerivativeVisitor<ParameterProviderInterface, MulticurveSensitivity> sensitivityCalculator, final boolean withToday) {
    final int nUnits = definitions.length;
    final double[][] parametersGuess = new double[nUnits][];
    final GeneratorYDCurve[][] generatorFinal = new GeneratorYDCurve[nUnits][];
    final InstrumentDerivative[][][] instruments = new InstrumentDerivative[nUnits][][];
    for (int loopunit = 0; loopunit < nUnits; loopunit++) {
      generatorFinal[loopunit] = new GeneratorYDCurve[curveGenerators[loopunit].length];
      int nbInsUnit = 0;
      for (int loopcurve = 0; loopcurve < curveGenerators[loopunit].length; loopcurve++) {
        nbInsUnit += definitions[loopunit][loopcurve].length;
      }
      parametersGuess[loopunit] = new double[nbInsUnit];
      int startCurve = 0; // First parameter index of the curve in the unit.
      instruments[loopunit] = convert(definitions[loopunit], loopunit, withToday);
      for (int loopcurve = 0; loopcurve < curveGenerators[loopunit].length; loopcurve++) {
        generatorFinal[loopunit][loopcurve] = curveGenerators[loopunit][loopcurve].finalGenerator(instruments[loopunit][loopcurve]);
        final double[] guessCurve = generatorFinal[loopunit][loopcurve].initialGuess(initialGuess(definitions[loopunit][loopcurve]));
        System.arraycopy(guessCurve, 0, parametersGuess[loopunit], startCurve, instruments[loopunit][loopcurve].length);
        startCurve += instruments[loopunit][loopcurve].length;
      }
    }
    return CURVE_BUILDING_REPOSITORY.makeCurvesFromDerivatives(instruments, generatorFinal, curveNames, parametersGuess, knownData, DSC_MAP, FWD_IBOR_MAP, FWD_ON_MAP, calculator,
        sensitivityCalculator);
  }

  private static InstrumentDerivative[][] convert(final InstrumentDefinition<?>[][] definitions, final int unit, final boolean withToday) {
    final InstrumentDerivative[][] instruments = new InstrumentDerivative[definitions.length][];
    for (int loopcurve = 0; loopcurve < definitions.length; loopcurve++) {
      instruments[loopcurve] = new InstrumentDerivative[definitions[loopcurve].length];
      int loopins = 0;
      for (final InstrumentDefinition<?> instrument : definitions[loopcurve]) {
        InstrumentDerivative ird;
        if (instrument instanceof SwapFixedCompoundedONCompoundedDefinition) {
          ird = ((SwapFixedCompoundedONCompoundedDefinition) instrument).toDerivative(NOW, getTSSwapFixedON(withToday, unit));
        } else {
          ird = instrument.toDerivative(NOW);
        }
        instruments[loopcurve][loopins++] = ird;
      }
    }
    return instruments;
  }

  private static ZonedDateTimeDoubleTimeSeries[] getTSSwapFixedON(final Boolean withToday, final Integer unit) {
    switch (unit) {
      case 0:
        return withToday ? TS_FIXED_CDI_BRL_WITH_TODAY : TS_FIXED_CDI_BRL_WITHOUT_TODAY;
      default:
        throw new IllegalArgumentException(unit.toString());
    }
  }

  private static double[] initialGuess(final InstrumentDefinition<?>[] definitions) {
    final double[] result = new double[definitions.length];
    int loopr = 0;
    for (final InstrumentDefinition<?> definition : definitions) {
      result[loopr++] = initialGuess(definition);
    }
    return result;
  }

  private static double initialGuess(final InstrumentDefinition<?> instrument) {
    if (instrument instanceof SwapFixedONDefinition) {
      return ((SwapFixedONDefinition) instrument).getFixedLeg().getNthPayment(0).getRate();
    }
    if (instrument instanceof SwapFixedIborDefinition) {
      return ((SwapFixedIborDefinition) instrument).getFixedLeg().getNthPayment(0).getRate();
    }
    if (instrument instanceof ForwardRateAgreementDefinition) {
      return ((ForwardRateAgreementDefinition) instrument).getRate();
    }
    if (instrument instanceof CashDefinition) {
      return ((CashDefinition) instrument).getRate();
    }
    return 0.1;
  }

}
