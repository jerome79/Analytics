/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.curve;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Period;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.common.collect.LinkedListMultimap;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorCurve;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorYDCurve;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.cash.CashDefinition;
import com.opengamma.analytics.financial.instrument.fra.ForwardRateAgreementDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureTransactionDefinition;
import com.opengamma.analytics.financial.instrument.future.SwapFuturesPriceDeliverableTransactionDefinition;
import com.opengamma.analytics.financial.instrument.index.GeneratorAttribute;
import com.opengamma.analytics.financial.instrument.index.GeneratorInstrument;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.instrument.index.IndexPrice;
import com.opengamma.analytics.financial.instrument.inflation.CouponInflationDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedCompoundingDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponONDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedInflationZeroCouponDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedONDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.legalentity.LegalEntityFilter;
import com.opengamma.analytics.financial.provider.curve.hullwhite.HullWhiteProviderDiscountBuildingRepository;
import com.opengamma.analytics.financial.provider.curve.inflation.InflationDiscountBuildingRepository;
import com.opengamma.analytics.financial.provider.curve.inflationissuer.InflationIssuerDiscountBuildingRepository;
import com.opengamma.analytics.financial.provider.curve.issuer.IssuerDiscountBuildingRepository;
import com.opengamma.analytics.financial.provider.curve.multicurve.MulticurveDiscountBuildingRepository;
import com.opengamma.analytics.financial.provider.description.inflation.InflationIssuerProviderDiscount;
import com.opengamma.analytics.financial.provider.description.inflation.InflationProviderDiscount;
import com.opengamma.analytics.financial.provider.description.inflation.ParameterInflationIssuerProviderInterface;
import com.opengamma.analytics.financial.provider.description.inflation.ParameterInflationProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.HullWhiteOneFactorProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.HullWhiteOneFactorProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterIssuerProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.inflation.InflationSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.analytics.util.timeseries.zdt.ImmutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.analytics.util.timeseries.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.basics.date.HolidayCalendar;
import com.opengamma.strata.collect.ArgChecker;
import com.opengamma.strata.collect.tuple.Pair;

/**
 * Utilities class for curve calibration tests and tutorials.
 */
public class CurveCalibrationTestsUtils {
  
  private static final ZonedDateTimeDoubleTimeSeries TS_EMPTY = ImmutableZonedDateTimeDoubleTimeSeries.ofEmptyUTC();
  
  public static void exportInflationCurve(
      ZonedDateTime calibrationDate, ZonedDateTime startDate, InflationProviderDiscount multicurve, IndexPrice index,
      File file, int nbDates, int jumpMonths) {
    ZonedDateTime[] scheduleDate = ScheduleCalculator.getUnadjustedDateSchedule(startDate, 
        startDate.plusMonths(nbDates * jumpMonths), Period.ofMonths(jumpMonths), true, false);
    final double[] indexValue = new double[nbDates];
    final double[] time = new double[nbDates];
    try (FileWriter writer = new FileWriter(file)) {
      for (int loopdate = 0; loopdate < nbDates; loopdate++) {
        time[loopdate] = TimeCalculator.getTimeBetween(calibrationDate, scheduleDate[loopdate]);
        indexValue[loopdate] = multicurve.getPriceIndex(index, time[loopdate]);
        writer.append(0.0 + "," + time[loopdate] + "," + indexValue[loopdate] + "\n");
      }
      writer.flush();
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  public static void exportIborForwardIborCurve(
      ZonedDateTime calibrationDate, MulticurveProviderInterface multicurve, IborIndex index, HolidayCalendar cal,
      File file, int startIndex, int nbDate, int jump) {
    ZonedDateTime startDate = ScheduleCalculator.getAdjustedDate(calibrationDate, index.getSpotLag() + startIndex * jump, cal);
    final double[] rateDsc = new double[nbDate];
    final double[] startTime = new double[nbDate];
    try (FileWriter writer = new FileWriter(file)) {
      for (int loopdate = 0; loopdate < nbDate; loopdate++) {
        startTime[loopdate] = TimeCalculator.getTimeBetween(calibrationDate, startDate);
        final ZonedDateTime endDate = ScheduleCalculator.getAdjustedDate(startDate, index, cal);
        final double endTime = TimeCalculator.getTimeBetween(calibrationDate, endDate);
        final double accrualFactor = index.getDayCount().yearFraction(startDate, endDate, cal);
        rateDsc[loopdate] = multicurve.getSimplyCompoundForwardRate(index, startTime[loopdate], endTime, accrualFactor);
        startDate = ScheduleCalculator.getAdjustedDate(startDate, jump, cal);
        writer.append(0.0 + "," + startTime[loopdate] + "," + rateDsc[loopdate] + "\n");
      }
      writer.flush();
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  public static void exportONForwardONCurve(
      ZonedDateTime calibrationDate, MulticurveProviderInterface multicurve, IndexON index, HolidayCalendar cal,
      File file, int nbDate, int jump) {
    ZonedDateTime startDate = calibrationDate;
    final double[] rateDsc = new double[nbDate];
    final double[] startTime = new double[nbDate];
    try (FileWriter writer = new FileWriter(file)) {
      for (int loopdate = 0; loopdate < nbDate; loopdate++) {
        startTime[loopdate] = TimeCalculator.getTimeBetween(calibrationDate, startDate);
        final ZonedDateTime endDate = ScheduleCalculator.getAdjustedDate(startDate, 1, cal);
        final double endTime = TimeCalculator.getTimeBetween(calibrationDate, endDate);
        final double accrualFactor = index.getDayCount().yearFraction(startDate, endDate);
        rateDsc[loopdate] = multicurve.getSimplyCompoundForwardRate(index, startTime[loopdate], endTime, accrualFactor);
        startDate = ScheduleCalculator.getAdjustedDate(startDate, jump, cal);
        writer.append(0.0 + "," + startTime[loopdate] + "," + rateDsc[loopdate] + "\n");
      }
      writer.flush();
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  public static void exportONForwardIborCurve(
      ZonedDateTime calibrationDate, MulticurveProviderInterface multicurve, IborIndex index, HolidayCalendar cal,
      File file, int nbDate, int jump) {
    ZonedDateTime startDate = calibrationDate;
    final double[] rateDsc = new double[nbDate];
    final double[] startTime = new double[nbDate];
    try (FileWriter writer = new FileWriter(file)) {
      for (int loopdate = 0; loopdate < nbDate; loopdate++) {
        startTime[loopdate] = TimeCalculator.getTimeBetween(calibrationDate, startDate);
        final ZonedDateTime endDate = ScheduleCalculator.getAdjustedDate(startDate, 1, cal);
        final double endTime = TimeCalculator.getTimeBetween(calibrationDate, endDate);
        final double accrualFactor = index.getDayCount().yearFraction(startDate, endDate);
        rateDsc[loopdate] = multicurve.getSimplyCompoundForwardRate(index, startTime[loopdate], endTime, accrualFactor);
        startDate = ScheduleCalculator.getAdjustedDate(startDate, jump, cal);
        writer.append(0.0 + "," + startTime[loopdate] + "," + rateDsc[loopdate] + "\n");
      }
      writer.flush();
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  public static void exportZCRatesONCurve(
      ZonedDateTime calibrationDate, MulticurveProviderDiscount multicurve, IndexON index, HolidayCalendar cal,
      File file, int nbDate, int jump) {
    ZonedDateTime startDate = calibrationDate;
    final double[] rateZC = new double[nbDate];
    final double[] startTime = new double[nbDate];
    try (FileWriter writer = new FileWriter(file)) {
      for (int loopdate = 0; loopdate < nbDate; loopdate++) {
        startTime[loopdate] = TimeCalculator.getTimeBetween(calibrationDate, startDate);
        rateZC[loopdate] = multicurve.getCurve(index).getInterestRate(startTime[loopdate]);
        startDate = ScheduleCalculator.getAdjustedDate(startDate, jump, cal);
        writer.append(0.0 + "," + startTime[loopdate] + "," + rateZC[loopdate] + "\n");
      }
      writer.flush();
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  public static void exportZCRatesIborCurve(
      ZonedDateTime calibrationDate, MulticurveProviderDiscount multicurve, IborIndex index, HolidayCalendar cal,
      File file, int nbDate, int jump) {
    ZonedDateTime startDate = calibrationDate;
    final double[] rateZC = new double[nbDate];
    final double[] startTime = new double[nbDate];
    try (FileWriter writer = new FileWriter(file)) {
      for (int loopdate = 0; loopdate < nbDate; loopdate++) {
        startTime[loopdate] = TimeCalculator.getTimeBetween(calibrationDate, startDate);
        rateZC[loopdate] = multicurve.getCurve(index).getInterestRate(startTime[loopdate]);
        startDate = ScheduleCalculator.getAdjustedDate(startDate, jump, cal);
        writer.append(0.0 + "," + startTime[loopdate] + "," + rateZC[loopdate] + "\n");
      }
      writer.flush();
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  @SuppressWarnings("unchecked")
  public static Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> makeCurvesFromDefinitionsMulticurve(
      ZonedDateTime calibrationDate, final InstrumentDefinition<?>[][][] definitions,
      final GeneratorYDCurve[][] curveGenerators, final String[][] curveNames, final MulticurveProviderDiscount knownData,
      final InstrumentDerivativeVisitor<ParameterProviderInterface, Double> calculator,
      final InstrumentDerivativeVisitor<ParameterProviderInterface, MulticurveSensitivity> sensitivityCalculator, final boolean withToday,
      LinkedHashMap<String, Currency> dscMap, LinkedHashMap<String, IndexON[]> fwdOnMap, LinkedHashMap<String, IborIndex[]> fwdIborMap,
      MulticurveDiscountBuildingRepository repository,
      ZonedDateTimeDoubleTimeSeries[] htsFixedOisWithToday, ZonedDateTimeDoubleTimeSeries[] htsFixedOisWithoutToday,
      ZonedDateTimeDoubleTimeSeries[] htsFixedIborWithToday, ZonedDateTimeDoubleTimeSeries[] htsFixedIborWithoutToday) {
    final int nUnits = definitions.length;
    final MultiCurveBundle<GeneratorYDCurve>[] curveBundles = new MultiCurveBundle[nUnits];
    for (int i = 0; i < nUnits; i++) {
      final int nCurves = definitions[i].length;
      final SingleCurveBundle<GeneratorYDCurve>[] singleCurves = new SingleCurveBundle[nCurves];
      for (int j = 0; j < nCurves; j++) {
        final int nInstruments = definitions[i][j].length;
        final InstrumentDerivative[] derivatives = new InstrumentDerivative[nInstruments];
        final double[] rates = new double[nInstruments];
        for (int k = 0; k < nInstruments; k++) {
          derivatives[k] = convert(definitions[i][j][k], withToday, calibrationDate, htsFixedOisWithToday, htsFixedOisWithoutToday,
              htsFixedIborWithToday, htsFixedIborWithoutToday);
          rates[k] = CurveCalibrationTestsUtils.initialGuess(definitions[i][j][k]);
        }
        final GeneratorYDCurve generator = curveGenerators[i][j].finalGenerator(derivatives);
        final double[] initialGuess = generator.initialGuess(rates);
        singleCurves[j] = new SingleCurveBundle<>(curveNames[i][j], derivatives, initialGuess, generator);
      }
      curveBundles[i] = new MultiCurveBundle<>(singleCurves);
    }
    return repository.makeCurvesFromDerivatives(curveBundles, knownData, dscMap, fwdIborMap, fwdOnMap, calculator, sensitivityCalculator);
  }

  @SuppressWarnings("unchecked")
  public static Pair<IssuerProviderDiscount, CurveBuildingBlockBundle> makeCurvesFromDefinitionsIssuer(
      ZonedDateTime calibrationDate, final InstrumentDefinition<?>[][][] definitions,
      final GeneratorYDCurve[][] curveGenerators, final String[][] curveNames, final IssuerProviderDiscount knownData,
      final InstrumentDerivativeVisitor<ParameterIssuerProviderInterface, Double> calculator,
      final InstrumentDerivativeVisitor<ParameterIssuerProviderInterface, MulticurveSensitivity> sensitivityCalculator, 
      boolean withToday, LinkedHashMap<String, Currency> dscMap, LinkedHashMap<String, IndexON[]> fwdOnMap, 
      LinkedHashMap<String, IborIndex[]> fwdIborMap,
      LinkedListMultimap<String, Pair<Object, LegalEntityFilter<LegalEntity>>> issuerMap, 
      IssuerDiscountBuildingRepository repository,
      ZonedDateTimeDoubleTimeSeries[] htsFixedOisWithToday, ZonedDateTimeDoubleTimeSeries[] htsFixedOisWithoutToday,
      ZonedDateTimeDoubleTimeSeries[] htsFixedIborWithToday, ZonedDateTimeDoubleTimeSeries[] htsFixedIborWithoutToday) {
    final int nUnits = definitions.length;
    final MultiCurveBundle<GeneratorYDCurve>[] curveBundles = new MultiCurveBundle[nUnits];
    for (int i = 0; i < nUnits; i++) {
      final int nCurves = definitions[i].length;
      final SingleCurveBundle<GeneratorYDCurve>[] singleCurves = new SingleCurveBundle[nCurves];
      for (int j = 0; j < nCurves; j++) {
        final int nInstruments = definitions[i][j].length;
        final InstrumentDerivative[] derivatives = new InstrumentDerivative[nInstruments];
        final double[] rates = new double[nInstruments];
        for (int k = 0; k < nInstruments; k++) {
          derivatives[k] = convert(definitions[i][j][k], withToday, calibrationDate, htsFixedOisWithToday, htsFixedOisWithoutToday,
              htsFixedIborWithToday, htsFixedIborWithoutToday);
          rates[k] = CurveCalibrationTestsUtils.initialGuess(definitions[i][j][k]);
        }
        final GeneratorYDCurve generator = curveGenerators[i][j].finalGenerator(derivatives);
        final double[] initialGuess = generator.initialGuess(rates);
        singleCurves[j] = new SingleCurveBundle<>(curveNames[i][j], derivatives, initialGuess, generator);
      }
      curveBundles[i] = new MultiCurveBundle<>(singleCurves);
    }
    return repository.makeCurvesFromDerivatives(curveBundles, knownData, dscMap, fwdIborMap, fwdOnMap, issuerMap,
        calculator, sensitivityCalculator);
  }

  @SuppressWarnings("unchecked")
  public static Pair<HullWhiteOneFactorProviderDiscount, CurveBuildingBlockBundle> makeCurvesFromDefinitionsHullWhite(
      ZonedDateTime calibrationDate, final InstrumentDefinition<?>[][][] definitions,
      final GeneratorYDCurve[][] curveGenerators, final String[][] curveNames, final HullWhiteOneFactorProviderDiscount knownData,
      final InstrumentDerivativeVisitor<HullWhiteOneFactorProviderInterface, Double> calculator,
      final InstrumentDerivativeVisitor<HullWhiteOneFactorProviderInterface, MulticurveSensitivity> sensitivityCalculator, final boolean withToday,
      LinkedHashMap<String, Currency> dscMap, LinkedHashMap<String, IndexON[]> fwdOnMap, LinkedHashMap<String, IborIndex[]> fwdIborMap,
      HullWhiteProviderDiscountBuildingRepository repository,
      ZonedDateTimeDoubleTimeSeries[] htsFixedOisWithToday, ZonedDateTimeDoubleTimeSeries[] htsFixedOisWithoutToday,
      ZonedDateTimeDoubleTimeSeries[] htsFixedIborWithToday, ZonedDateTimeDoubleTimeSeries[] htsFixedIborWithoutToday) {
    final int nUnits = definitions.length;
    final MultiCurveBundle<GeneratorYDCurve>[] curveBundles = new MultiCurveBundle[nUnits];
    for (int i = 0; i < nUnits; i++) {
      final int nCurves = definitions[i].length;
      final SingleCurveBundle<GeneratorYDCurve>[] singleCurves = new SingleCurveBundle[nCurves];
      for (int j = 0; j < nCurves; j++) {
        final int nInstruments = definitions[i][j].length;
        final InstrumentDerivative[] derivatives = new InstrumentDerivative[nInstruments];
        final double[] rates = new double[nInstruments];
        for (int k = 0; k < nInstruments; k++) {
          derivatives[k] = convert(definitions[i][j][k], withToday, calibrationDate, htsFixedOisWithToday, htsFixedOisWithoutToday,
              htsFixedIborWithToday, htsFixedIborWithoutToday);
          rates[k] = CurveCalibrationTestsUtils.initialGuess(definitions[i][j][k]);
        }
        final GeneratorYDCurve generator = curveGenerators[i][j].finalGenerator(derivatives);
        final double[] initialGuess = generator.initialGuess(rates);
        singleCurves[j] = new SingleCurveBundle<>(curveNames[i][j], derivatives, initialGuess, generator);
      }
      curveBundles[i] = new MultiCurveBundle<>(singleCurves);
    }
    return repository.makeCurvesFromDerivatives(curveBundles, knownData, dscMap, fwdIborMap, fwdOnMap, calculator, sensitivityCalculator);
  }

  public static InstrumentDerivative convert(final InstrumentDefinition<?> instrument, final boolean withToday,
      ZonedDateTime calibrationDate,
      ZonedDateTimeDoubleTimeSeries[] htsFixedOisWithToday, ZonedDateTimeDoubleTimeSeries[] htsFixedOisWithoutToday,
      ZonedDateTimeDoubleTimeSeries[] htsFixedIborWithToday, ZonedDateTimeDoubleTimeSeries[] htsFixedIborWithoutToday) {
    if (instrument instanceof SwapFixedONDefinition) {
      return ((SwapFixedONDefinition) instrument).toDerivative(calibrationDate,
          getTSSwapFixedON(withToday, htsFixedOisWithToday, htsFixedOisWithoutToday));
    }
    if (instrument instanceof SwapFixedIborDefinition) {
      return ((SwapFixedIborDefinition) instrument).toDerivative(calibrationDate,
          getTSSwapFixedIbor(withToday, htsFixedIborWithToday, htsFixedIborWithoutToday));
    }
    if (instrument instanceof SwapDefinition) {
      SwapDefinition swap = (SwapDefinition) instrument;
      ZonedDateTimeDoubleTimeSeries[] hts = new ZonedDateTimeDoubleTimeSeries[2];
      PaymentDefinition[] payment = new PaymentDefinition[2];
      payment[0] = swap.getFirstLeg().getNthPayment(0);
      payment[1] = swap.getSecondLeg().getNthPayment(0);
      for (int loopleg = 0; loopleg < 2; loopleg++) {
        if (payment[loopleg] instanceof CouponONDefinition) {
          hts[loopleg] = withToday ? htsFixedOisWithToday[0] : htsFixedOisWithoutToday[0];
        } else {
          if ((payment[loopleg] instanceof CouponIborDefinition) || (payment[loopleg] instanceof CouponIborSpreadDefinition)) {
            hts[loopleg] = withToday ? htsFixedIborWithToday[0] : htsFixedIborWithoutToday[0];
          } else {
            hts[loopleg] = ImmutableZonedDateTimeDoubleTimeSeries.ofEmptyUTC();
          }
        }
      }
      return swap.toDerivative(calibrationDate, hts);
    }
    if (instrument instanceof InterestRateFutureTransactionDefinition) {
      return ((InterestRateFutureTransactionDefinition) instrument).toDerivative(calibrationDate, 0.0); 
    } // Trade date = today, reference price not used.
    if (instrument instanceof SwapFuturesPriceDeliverableTransactionDefinition) {
      return ((SwapFuturesPriceDeliverableTransactionDefinition) instrument).toDerivative(calibrationDate, 0.0);
    } // Trade date = today, reference price not used.
    return instrument.toDerivative(calibrationDate);
  }

  public static double initialGuess(final InstrumentDefinition<?> instrument) {
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
    if (instrument instanceof InterestRateFutureTransactionDefinition) {
      return 1 - ((InterestRateFutureTransactionDefinition) instrument).getTradePrice();
    }
    if (instrument instanceof SwapFixedInflationZeroCouponDefinition) {
      PaymentDefinition cpn = ((SwapFixedInflationZeroCouponDefinition) instrument).getFirstLeg().getNthPayment(0);
      return ((CouponFixedCompoundingDefinition) cpn).getRate(); 
    }
    return 0.01;
  }

  public static ZonedDateTimeDoubleTimeSeries[] getTSSwapFixedON(final Boolean withToday,
      ZonedDateTimeDoubleTimeSeries[] htsWithToday, ZonedDateTimeDoubleTimeSeries[] htsWithoutToday) {
    return withToday ? htsWithToday : htsWithoutToday;
  }

  public static ZonedDateTimeDoubleTimeSeries[] getTSSwapFixedIbor(final Boolean withToday,
      ZonedDateTimeDoubleTimeSeries[] htsWithToday, ZonedDateTimeDoubleTimeSeries[] htsWithoutToday) {
    return withToday ? htsWithToday : htsWithoutToday;
  }
  
  @SuppressWarnings("unchecked")
  public static Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> makeCurvesFromDefinitionsMulticurve(
      ZonedDateTime calibrationDate, final InstrumentDefinition<?>[][][] definitions,
      final GeneratorYDCurve[][] curveGenerators, final String[][] curveNames, final MulticurveProviderDiscount knownData,
      final InstrumentDerivativeVisitor<ParameterProviderInterface, Double> calculator,
      final InstrumentDerivativeVisitor<ParameterProviderInterface, MulticurveSensitivity> sensitivityCalculator,
      LinkedHashMap<String, Currency> dscMap, LinkedHashMap<String, IndexON[]> fwdOnMap, LinkedHashMap<String, IborIndex[]> fwdIborMap,
      MulticurveDiscountBuildingRepository repository,
      Map<IndexON,ZonedDateTimeDoubleTimeSeries> htsOis, Map<IborIndex,ZonedDateTimeDoubleTimeSeries> htsIbor) {
    final int nUnits = definitions.length;
    final MultiCurveBundle<GeneratorYDCurve>[] curveBundles = new MultiCurveBundle[nUnits];
    for (int i = 0; i < nUnits; i++) {
      final int nCurves = definitions[i].length;
      final SingleCurveBundle<GeneratorYDCurve>[] singleCurves = new SingleCurveBundle[nCurves];
      for (int j = 0; j < nCurves; j++) {
        final int nInstruments = definitions[i][j].length;
        final InstrumentDerivative[] derivatives = new InstrumentDerivative[nInstruments];
        final double[] rates = new double[nInstruments];
        for (int k = 0; k < nInstruments; k++) {
          derivatives[k] = convert(definitions[i][j][k], calibrationDate, htsOis, htsIbor, 
              new LinkedHashMap<IndexPrice,ZonedDateTimeDoubleTimeSeries>());
          rates[k] = CurveCalibrationTestsUtils.initialGuess(definitions[i][j][k]);
        }
        final GeneratorYDCurve generator = curveGenerators[i][j].finalGenerator(derivatives);
        final double[] initialGuess = generator.initialGuess(rates);
        singleCurves[j] = new SingleCurveBundle<>(curveNames[i][j], derivatives, initialGuess, generator);
      }
      curveBundles[i] = new MultiCurveBundle<>(singleCurves);
    }
    return repository.makeCurvesFromDerivatives(curveBundles, knownData, dscMap, fwdIborMap, fwdOnMap, calculator, sensitivityCalculator);
  }

  @SuppressWarnings("unchecked")
  public static Pair<IssuerProviderDiscount, CurveBuildingBlockBundle> makeCurvesFromDefinitionsIssuer(
      ZonedDateTime calibrationDate, final InstrumentDefinition<?>[][][] definitions,
      final GeneratorYDCurve[][] curveGenerators, final String[][] curveNames, 
      final IssuerProviderDiscount knownData, CurveBuildingBlockBundle knownBlock,
      final InstrumentDerivativeVisitor<ParameterIssuerProviderInterface, Double> calculator,
      final InstrumentDerivativeVisitor<ParameterIssuerProviderInterface, MulticurveSensitivity> sensitivityCalculator, 
      LinkedHashMap<String, Currency> dscMap, LinkedHashMap<String, IndexON[]> fwdOnMap, 
      LinkedHashMap<String, IborIndex[]> fwdIborMap,
      LinkedListMultimap<String, Pair<Object, LegalEntityFilter<LegalEntity>>> issuerMap, 
      IssuerDiscountBuildingRepository repository,
      Map<IndexON,ZonedDateTimeDoubleTimeSeries> htsOis, Map<IborIndex,ZonedDateTimeDoubleTimeSeries> htsIbor) {
    final int nUnits = definitions.length;
    final MultiCurveBundle<GeneratorYDCurve>[] curveBundles = new MultiCurveBundle[nUnits];
    for (int i = 0; i < nUnits; i++) {
      final int nCurves = definitions[i].length;
      final SingleCurveBundle<GeneratorYDCurve>[] singleCurves = new SingleCurveBundle[nCurves];
      for (int j = 0; j < nCurves; j++) {
        final int nInstruments = definitions[i][j].length;
        final InstrumentDerivative[] derivatives = new InstrumentDerivative[nInstruments];
        final double[] rates = new double[nInstruments];
        for (int k = 0; k < nInstruments; k++) {
          derivatives[k] = convert(definitions[i][j][k], calibrationDate, htsOis, htsIbor, 
              new LinkedHashMap<IndexPrice,ZonedDateTimeDoubleTimeSeries>());
          rates[k] = CurveCalibrationTestsUtils.initialGuess(definitions[i][j][k]);
        }
        final GeneratorYDCurve generator = curveGenerators[i][j].finalGenerator(derivatives);
        final double[] initialGuess = generator.initialGuess(rates);
        singleCurves[j] = new SingleCurveBundle<>(curveNames[i][j], derivatives, initialGuess, generator);
      }
      curveBundles[i] = new MultiCurveBundle<>(singleCurves);
    }
    return repository.makeCurvesFromDerivatives(curveBundles, knownData, knownBlock, dscMap, fwdIborMap, fwdOnMap, 
        issuerMap, calculator, sensitivityCalculator);
  }
  
  @SuppressWarnings("unchecked")
  public static Pair<InflationProviderDiscount, CurveBuildingBlockBundle> makeCurvesFromDefinitionsInflation(
      ZonedDateTime calibrationDate, final InstrumentDefinition<?>[][][] definitions,
      final GeneratorCurve[][] curveGenerators, final String[][] curveNames, 
      final InflationProviderDiscount knownData, CurveBuildingBlockBundle knownBlock,
      final InstrumentDerivativeVisitor<ParameterInflationProviderInterface, Double> calculator,
      final InstrumentDerivativeVisitor<ParameterInflationProviderInterface, InflationSensitivity> sensitivityCalculator,
      LinkedHashMap<String, Currency> dscMap, LinkedHashMap<String, IndexON[]> fwdOnMap, 
      LinkedHashMap<String, IborIndex[]> fwdIborMap, LinkedHashMap<String, IndexPrice[]> priceMap,
      InflationDiscountBuildingRepository repository,
      Map<IndexON,ZonedDateTimeDoubleTimeSeries> htsOis, Map<IborIndex,ZonedDateTimeDoubleTimeSeries> htsIbor,
      Map<IndexPrice,ZonedDateTimeDoubleTimeSeries> htsPrice) {
    final int nUnits = definitions.length;
    final MultiCurveBundle<GeneratorCurve>[] curveBundles = new MultiCurveBundle[nUnits];
    for (int i = 0; i < nUnits; i++) {
      final int nCurves = definitions[i].length;
      final SingleCurveBundle<GeneratorCurve>[] singleCurves = new SingleCurveBundle[nCurves];
      for (int j = 0; j < nCurves; j++) {
        final int nInstruments = definitions[i][j].length;
        final InstrumentDerivative[] derivatives = new InstrumentDerivative[nInstruments];
        final double[] rates = new double[nInstruments];
        for (int k = 0; k < nInstruments; k++) {
          derivatives[k] = convert(definitions[i][j][k], calibrationDate, htsOis, htsIbor, htsPrice);
          rates[k] = CurveCalibrationTestsUtils.initialGuess(definitions[i][j][k]);
        }
        final GeneratorCurve generator = curveGenerators[i][j].finalGenerator(derivatives);
        final double[] initialGuess = generator.initialGuess(rates);
        singleCurves[j] = new SingleCurveBundle<>(curveNames[i][j], derivatives, initialGuess, generator);
      }
      curveBundles[i] = new MultiCurveBundle<>(singleCurves);
    }
    return repository.makeCurvesFromDerivatives(curveBundles, knownData, knownBlock, dscMap, fwdOnMap, fwdIborMap, 
        priceMap, calculator, sensitivityCalculator);
  }

  @SuppressWarnings("unchecked")
  public static Pair<InflationIssuerProviderDiscount, CurveBuildingBlockBundle> makeCurvesFromDefinitionsInflationIssuer(
      ZonedDateTime calibrationDate, final InstrumentDefinition<?>[][][] definitions,
      final GeneratorCurve[][] curveGenerators, final String[][] curveNames, 
      final InflationIssuerProviderDiscount knownData, CurveBuildingBlockBundle knownBlock,
      final InstrumentDerivativeVisitor<ParameterInflationIssuerProviderInterface, Double> calculator,
      final InstrumentDerivativeVisitor<ParameterInflationIssuerProviderInterface, InflationSensitivity> sensitivityCalculator, 
      LinkedHashMap<String, Currency> dscMap, LinkedHashMap<String, IndexON[]> fwdOnMap, 
      LinkedHashMap<String, IborIndex[]> fwdIborMap, LinkedHashMap<String, IndexPrice[]> priceMap,
      LinkedListMultimap<String, Pair<Object, LegalEntityFilter<LegalEntity>>> issuerMap, 
      InflationIssuerDiscountBuildingRepository repository,
      Map<IndexON,ZonedDateTimeDoubleTimeSeries> htsOis, Map<IborIndex,ZonedDateTimeDoubleTimeSeries> htsIbor,
      Map<IndexPrice,ZonedDateTimeDoubleTimeSeries> htsPrice) {
    final int nUnits = definitions.length;
    final MultiCurveBundle<GeneratorCurve>[] curveBundles = new MultiCurveBundle[nUnits];
    for (int i = 0; i < nUnits; i++) {
      final int nCurves = definitions[i].length;
      final SingleCurveBundle<GeneratorCurve>[] singleCurves = new SingleCurveBundle[nCurves];
      for (int j = 0; j < nCurves; j++) {
        final int nInstruments = definitions[i][j].length;
        final InstrumentDerivative[] derivatives = new InstrumentDerivative[nInstruments];
        final double[] rates = new double[nInstruments];
        for (int k = 0; k < nInstruments; k++) {
          derivatives[k] = convert(definitions[i][j][k], calibrationDate, htsOis, htsIbor, htsPrice);
          rates[k] = CurveCalibrationTestsUtils.initialGuess(definitions[i][j][k]);
        }
        final GeneratorCurve generator = curveGenerators[i][j].finalGenerator(derivatives);
        final double[] initialGuess = generator.initialGuess(rates);
        singleCurves[j] = new SingleCurveBundle<>(curveNames[i][j], derivatives, initialGuess, generator);
      }
      curveBundles[i] = new MultiCurveBundle<>(singleCurves);
    }
    return repository.makeCurvesFromDerivatives(curveBundles, knownData, knownBlock, dscMap, fwdOnMap, fwdIborMap, 
        priceMap, issuerMap, calculator, sensitivityCalculator);
  }

  public static InstrumentDerivative convert(final InstrumentDefinition<?> instrument, ZonedDateTime calibrationDate,
      Map<IndexON,ZonedDateTimeDoubleTimeSeries> htsOis, Map<IborIndex,ZonedDateTimeDoubleTimeSeries> htsIbor,
      Map<IndexPrice,ZonedDateTimeDoubleTimeSeries> htsPrice) {
    if (instrument instanceof SwapFixedONDefinition) {
      IndexON index = ((SwapFixedONDefinition) instrument).getOISLeg().getOvernightIndex();
      ZonedDateTimeDoubleTimeSeries[] hts = new ZonedDateTimeDoubleTimeSeries[] {htsOis.get(index)};
      return ((SwapFixedONDefinition) instrument).toDerivative(calibrationDate, hts);
    }
    if (instrument instanceof SwapFixedIborDefinition) {
      IborIndex index = ((SwapFixedIborDefinition) instrument).getIborLeg().getIborIndex();
      ZonedDateTimeDoubleTimeSeries hts = htsIbor.get(index);
      ArgChecker.notNull(hts, "time series from " + index.toString() + " is not present in map.");
      ZonedDateTimeDoubleTimeSeries[] htsArray = new ZonedDateTimeDoubleTimeSeries[] {hts};
      return ((SwapFixedIborDefinition) instrument).toDerivative(calibrationDate, htsArray);
    }
    if (instrument instanceof SwapFixedInflationZeroCouponDefinition) {
      PaymentDefinition cpn = ((SwapFixedInflationZeroCouponDefinition) instrument).getSecondLeg().getNthPayment(0);
      IndexPrice index = ((CouponInflationDefinition) cpn).getPriceIndex();
      ZonedDateTimeDoubleTimeSeries hts = htsPrice.get(index);
      ArgChecker.notNull(hts, "time series from " + index.toString() + " is not present in map.");
      ZonedDateTimeDoubleTimeSeries[] htsArray = new ZonedDateTimeDoubleTimeSeries[] {TS_EMPTY, hts};
      return ((SwapFixedInflationZeroCouponDefinition) instrument).toDerivative(calibrationDate, htsArray);      
    }
    if (instrument instanceof SwapDefinition) {
      SwapDefinition swap = (SwapDefinition) instrument;
      ZonedDateTimeDoubleTimeSeries[] hts = new ZonedDateTimeDoubleTimeSeries[2];
      PaymentDefinition[] payment = new PaymentDefinition[2];
      payment[0] = swap.getFirstLeg().getNthPayment(0); // TODO: Improve for swaps with notional payment.
      payment[1] = swap.getSecondLeg().getNthPayment(0);
      for (int loopleg = 0; loopleg < 2; loopleg++) {
        if (payment[loopleg] instanceof CouponONDefinition) {
          hts[loopleg] = htsOis.get(payment[loopleg].getCurrency());
        } else {
          if ((payment[loopleg] instanceof CouponIborDefinition)) {
            IborIndex index = ((CouponIborDefinition) payment[loopleg]).getIndex();
            ZonedDateTimeDoubleTimeSeries htsIndex = htsIbor.get(index);
            ArgChecker.notNull(htsIndex, "time series from " + index.toString() + " is not present in map.");
            hts[loopleg] = htsIndex;
          } else { 
            if (payment[loopleg] instanceof CouponIborSpreadDefinition) {
              IborIndex index =  ((CouponIborSpreadDefinition) payment[loopleg]).getIndex();
              ZonedDateTimeDoubleTimeSeries htsIndex = htsIbor.get(index);
              ArgChecker.notNull(htsIndex, "time series from " + index.toString() + " is not present in map.");
              hts[loopleg] = htsIndex;
            } else {
            hts[loopleg] = ImmutableZonedDateTimeDoubleTimeSeries.ofEmptyUTC();}
          }
        }
      }
     return swap.toDerivative(calibrationDate, hts);
    }
    if (instrument instanceof InterestRateFutureTransactionDefinition) {
      return ((InterestRateFutureTransactionDefinition) instrument).toDerivative(calibrationDate, 0.0); 
    } // Trade date = today, reference price not used.
    if (instrument instanceof SwapFuturesPriceDeliverableTransactionDefinition) {
      return ((SwapFuturesPriceDeliverableTransactionDefinition) instrument).toDerivative(calibrationDate, 0.0);
    } // Trade date = today, reference price not used.
    return instrument.toDerivative(calibrationDate);
  }
  
  @SuppressWarnings({"rawtypes", "unchecked" })
  public static InstrumentDefinition<?>[] getDefinitions(final ZonedDateTime calibrationDate, final double notional,
      final double[] marketQuotes, final GeneratorInstrument[] generators, final GeneratorAttribute[] attribute) {
    final InstrumentDefinition<?>[] definitions = new InstrumentDefinition<?>[marketQuotes.length];
    for (int loopmv = 0; loopmv < marketQuotes.length; loopmv++) {
      definitions[loopmv] = generators[loopmv].generateInstrument(calibrationDate, marketQuotes[loopmv], notional, 
          attribute[loopmv]);
    }
    return definitions;
  }

}
