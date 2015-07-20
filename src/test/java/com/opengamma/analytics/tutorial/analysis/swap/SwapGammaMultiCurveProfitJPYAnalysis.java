/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.tutorial.analysis.swap;

import java.io.FileWriter;
import java.io.IOException;
import java.time.Period;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.instrument.index.GeneratorAttributeIR;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIborMaster;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapIborIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapIborIborMaster;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexIborMaster;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapIborIborDefinition;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.provider.calculator.discounting.CrossGammaMultiCurveCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.parameter.ParameterSensitivityParameterCalculator;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.matrix.OGMatrixAlgebra;
import com.opengamma.analytics.tutorial.datasets.AnalysisMarketDataJPYSets;
import com.opengamma.analytics.util.time.DateUtils;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.basics.date.HolidayCalendar;
import com.opengamma.strata.basics.date.HolidayCalendars;
import com.opengamma.strata.collect.tuple.Pair;

/**
 * Analysis of cross-gamma to zero-coupon rate - Multi-curve settings.
 */
public class SwapGammaMultiCurveProfitJPYAnalysis {

  private static final HolidayCalendar TYO = HolidayCalendars.SAT_SUN;

  private static final ZonedDateTime CALIBRATION_DATE = DateUtils.getUTCDate(2014, 8, 2);
  private static final ZonedDateTime SPOT_DATE = ScheduleCalculator.getAdjustedDate(CALIBRATION_DATE, 2, TYO);

  private static final GeneratorSwapFixedIborMaster MASTER_IRS = GeneratorSwapFixedIborMaster.getInstance();
  private static final GeneratorSwapIborIborMaster MASTER_BS = GeneratorSwapIborIborMaster.getInstance();
  private static final GeneratorSwapFixedIbor JPY6MLIBOR6M = MASTER_IRS.getGenerator("JPY6MLIBOR6M", TYO);
  private static final GeneratorSwapIborIbor JPYLIBOR3MLIBOR6M = MASTER_BS.getGenerator("JPYLIBOR3MLIBOR6M", TYO);
  private static final IndexIborMaster MASTER_IBOR_INDEX = IndexIborMaster.getInstance();
  private static final IborIndex JPYLIBOR6M = MASTER_IBOR_INDEX.getIndex("JPYLIBOR6M");
  private static final Currency JPY = JPYLIBOR6M.getCurrency();

  private static final Period TENOR_START = Period.ofMonths(30);
  private static final Period TENOR_SWAP = Period.ofYears(15);
  private static final boolean IS_PAYER = true;

  private static final double NOTIONAL = 1.0E6; // 1m
  private static final ZonedDateTime START_DATE = ScheduleCalculator.getAdjustedDate(SPOT_DATE, TENOR_START, JPYLIBOR6M, TYO);
  private static final SwapFixedIborDefinition IRS_JPY_DEFINITION = SwapFixedIborDefinition.from(START_DATE, TENOR_SWAP, JPY6MLIBOR6M, NOTIONAL, 0.02, IS_PAYER);
  private static final SwapFixedCoupon<Coupon> IRS_JPY = IRS_JPY_DEFINITION.toDerivative(CALIBRATION_DATE);
  private static final double SPREAD_BS = 0.0005;
  private static final SwapIborIborDefinition BS_JPY_DEFINITION =
      JPYLIBOR3MLIBOR6M.generateInstrument(CALIBRATION_DATE, SPREAD_BS, NOTIONAL, new GeneratorAttributeIR(TENOR_START, TENOR_SWAP));
  private static final Swap<?, ?> BS_JPY = BS_JPY_DEFINITION.toDerivative(CALIBRATION_DATE);

  private static final double BP1 = 1.0E-4;

  private static final PresentValueDiscountingCalculator PVDC = PresentValueDiscountingCalculator.getInstance();
  private static final PresentValueCurveSensitivityDiscountingCalculator PVCSDC = PresentValueCurveSensitivityDiscountingCalculator.getInstance();
  private static final CrossGammaMultiCurveCalculator CGMCC = new CrossGammaMultiCurveCalculator(BP1, PVCSDC);
  private static final ParameterSensitivityParameterCalculator<ParameterProviderInterface> PSC = new ParameterSensitivityParameterCalculator<>(PVCSDC);

  private static final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> MULTICURVE_PAIR_0 =
      AnalysisMarketDataJPYSets.getMulticurveJPY();
  private static final MulticurveProviderDiscount MULTICURVE = MULTICURVE_PAIR_0.getFirst();

  private static final String[] CURVE_NAME = new String[2];
  static {
    CURVE_NAME[0] = MULTICURVE_PAIR_0.getFirst().getName(JPY);
    CURVE_NAME[1] = MULTICURVE_PAIR_0.getFirst().getName(JPYLIBOR6M);
  }

  private static final OGMatrixAlgebra ALGEBRA = new OGMatrixAlgebra();

  private static final DoubleMatrix2D GAMMA_CROSS = CGMCC.calculateCrossGammaCrossCurve(IRS_JPY, MULTICURVE);
  private static final double[] GAMMA_SUM_COL = new double[GAMMA_CROSS.getNumberOfColumns()];
  static {
    for (int loopcol = 0; loopcol < GAMMA_CROSS.getNumberOfColumns(); loopcol++) {
      for (int looprow = 0; looprow < GAMMA_CROSS.getNumberOfRows(); looprow++) {
        GAMMA_SUM_COL[loopcol] += GAMMA_CROSS.getEntry(looprow, loopcol);
      }
    }
  }
  private static final int NB_NODE = GAMMA_CROSS.getNumberOfColumns();

  private static final HashMap<String, DoubleMatrix2D> GAMMA_INTRA = CGMCC.calculateCrossGammaIntraCurve(IRS_JPY, MULTICURVE);
  private static final Set<String> CURVE_NAME_SET = GAMMA_INTRA.keySet();
  private static final String[] CURVE_NAME_ARRAY = CURVE_NAME_SET.toArray(new String[0]);
  private static final int[] NB_NODE_JPY = new int[3];
  static {
    int loopcurve = 0;
    for (String curve : CURVE_NAME_SET) {
      NB_NODE_JPY[loopcurve] = GAMMA_INTRA.get(curve).getNumberOfColumns();
      loopcurve++;
    }
  }
  private static final MultipleCurrencyParameterSensitivity DELTA = PSC.calculateSensitivity(IRS_JPY, MULTICURVE);

  @Test(enabled = false)
  public void crossGammaMulticurveIntraCurve() {
    HashMap<String, DoubleMatrix2D> crossGammaIntraIrs = CGMCC.calculateCrossGammaIntraCurve(IRS_JPY, MULTICURVE);
    HashMap<String, DoubleMatrix2D> crossGammaIntraBs = CGMCC.calculateCrossGammaIntraCurve(BS_JPY, MULTICURVE);
    for (String name : crossGammaIntraIrs.keySet()) {
      exportMatrix(crossGammaIntraIrs.get(name).getData(), "cross-gamma-jpy-irs-" + name + ".csv");
    }
    for (String name : crossGammaIntraBs.keySet()) {
      exportMatrix(crossGammaIntraBs.get(name).getData(), "cross-gamma-jpy-bs-" + name + ".csv");
    }
  }

  private void exportMatrix(double[][] matrix, String fileName) {
    try (FileWriter writer = new FileWriter(fileName)) {
      for (int loop1 = 0; loop1 < matrix.length; loop1++) {
        String line = "";
        for (int loop2 = 0; loop2 < matrix[loop1].length; loop2++) {
          line = line + "," + matrix[loop1][loop2];
        }
        writer.append(line + "0 \n");
      }
      writer.flush();
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Uses historical data to estimate the difference between intra-curve cross-gamma and full cross-gamma.
   * The result file is exported in the root directory of OG-Analytics.
   * @throws IOException if an error occurs
   */
  @Test(enabled = false)
  public void crossGammaCompJpyHts() throws IOException {
    long startTime, endTime;
    startTime = System.currentTimeMillis();
    final String sheetFilePath = "src/test/resources/analysis/historical_time_series/curve-changes-jpy-multicurve.csv";
    int nbCurves = CURVE_NAME_SET.size();
    int nbPoints = 0;
    double[][] x = new double[nbCurves][];
    double[][] y = new double[nbCurves][];
    for (int loopcurve = 0; loopcurve < nbCurves; loopcurve++) {
      YieldAndDiscountCurve curve = MULTICURVE.getCurve(CURVE_NAME_ARRAY[loopcurve]);
      InterpolatedDoublesCurve interpolatedCurve = (InterpolatedDoublesCurve) ((YieldCurve) curve).getCurve();
      x[loopcurve] = interpolatedCurve.getXDataAsPrimitive();
      y[loopcurve] = interpolatedCurve.getYDataAsPrimitive();
      nbPoints += x[loopcurve].length;
    }
    double[][] shiftRelative = GammaAnalysisUtils.parseShifts(sheetFilePath, 1.0);
    int nbScenarios = shiftRelative.length;
    double[][] shiftAbsolute = new double[nbScenarios][nbPoints];

    for (int loopsc = 0; loopsc < nbScenarios; loopsc++) {
      int loopnode1 = 0;
      for (int loopcurve = 0; loopcurve < nbCurves; loopcurve++) {
        for (int loopnode2 = 0; loopnode2 < x[loopcurve].length; loopnode2++) {
          shiftAbsolute[loopsc][loopnode1] = y[loopcurve][loopnode2] * shiftRelative[loopsc][loopnode1];
          loopnode1++;
        }
      }
    }
    double[] plFullR = new double[nbScenarios];
    double[] plDelta = new double[nbScenarios];
    double[] plGammaCross = new double[nbScenarios];
    double[] plGammaIntra = new double[nbScenarios];
    double[] plGammaCol = new double[nbScenarios];
    double pv0 = IRS_JPY.accept(PVDC, MULTICURVE).getAmount(JPY).getAmount();
    for (int loopsc = 0; loopsc < nbScenarios; loopsc++) {
      double[][] marketMvtIntra = new double[nbCurves][];
      int start = 0;
      for (int loopcurve = 0; loopcurve < nbCurves; loopcurve++) {
        marketMvtIntra[loopcurve] = Arrays.copyOfRange(shiftAbsolute[loopsc], start, start + NB_NODE_JPY[loopcurve]);
        start += NB_NODE_JPY[loopcurve];
      }
      // Full reval
      MulticurveProviderDiscount multicurveScenario = shiftedProvider(MULTICURVE, CURVE_NAME_ARRAY, x, y, marketMvtIntra);
      double pv = IRS_JPY.accept(PVDC, multicurveScenario).getAmount(JPY).getAmount();
      plFullR[loopsc] = pv - pv0;
      // Delta 
      for (int loopcurve = 0; loopcurve < nbCurves; loopcurve++) {
        DoubleMatrix1D sensi = DELTA.getSensitivity(CURVE_NAME_ARRAY[loopcurve], JPY);
        double[] deltaCurve;
        if (sensi == null) { // no sensitivity
          deltaCurve = new double[marketMvtIntra[loopcurve].length];
        } else {
          deltaCurve = DELTA.getSensitivity(CURVE_NAME_ARRAY[loopcurve], JPY).getData();
        }
        for (int i = 0; i < deltaCurve.length; i++) {
          plDelta[loopsc] += deltaCurve[i] * marketMvtIntra[loopcurve][i];
        }
      }
      // Intra curve cross-gamma
      int loopcurve = 0;
      start = 0;
      for (String curve : CURVE_NAME_SET) {
        DoubleMatrix2D marketMvtIntraMat = new DoubleMatrix2D(new double[][] {marketMvtIntra[loopcurve] });
        start += NB_NODE_JPY[loopcurve];
        loopcurve++;
        plGammaIntra[loopsc] += (Double) ALGEBRA.multiply(ALGEBRA.multiply(marketMvtIntraMat, GAMMA_INTRA.get(curve)), ALGEBRA.getTranspose(marketMvtIntraMat)).getEntry(0, 0) * 0.5;
      }
      // Full curve cross-gamma
      DoubleMatrix2D marketMvtMat = new DoubleMatrix2D(new double[][] {shiftAbsolute[loopsc] });
      plGammaCross[loopsc] = (Double) ALGEBRA.multiply(ALGEBRA.multiply(marketMvtMat, GAMMA_CROSS),
          ALGEBRA.getTranspose(marketMvtMat)).getEntry(0, 0) * 0.5;
      // Sum of column
      for (int loopcol = 0; loopcol < NB_NODE; loopcol++) {
        plGammaCol[loopsc] += GAMMA_SUM_COL[loopcol] * shiftAbsolute[loopsc][loopcol] * shiftAbsolute[loopsc][loopcol] * 0.5;
      }
    }
    String fileName = "swap-multicurve-delta-gamma-pl-" + TENOR_START.toString() + "x" + TENOR_SWAP.toString() + ".csv";
    try (FileWriter writer = new FileWriter(fileName)) {
      for (int loopsc = 0; loopsc < nbScenarios; loopsc++) {
        String line = plFullR[loopsc] + "," + plDelta[loopsc] + "," + plGammaCross[loopsc] + "," + plGammaIntra[loopsc] + "," + plGammaCol[loopsc] + " \n";
        writer.append(line);
      }
      writer.flush();
    } catch (final IOException e) {
      e.printStackTrace();
    }
    endTime = System.currentTimeMillis();
    System.out.println("SwapGammaMultiCurveProfitJPYAnalysis - p/L - " + (endTime - startTime) + " ms");
  }

  private MulticurveProviderDiscount shiftedProvider(MulticurveProviderDiscount multicurve, String[] curveNames, double[][] x, double[][] y, double[][] shifts) {
    MulticurveProviderDiscount multicurveBumped = new MulticurveProviderDiscount();
    multicurveBumped.setForexMatrix(multicurve.getFxRates());
    for (int loopcurve = 0; loopcurve < curveNames.length; loopcurve++) {
      double[] yShift = new double[y[loopcurve].length];
      for (int i = 0; i < y[loopcurve].length; i++) {
        yShift[i] = y[loopcurve][i] + shifts[loopcurve][i];
      }
      YieldCurve curve = (YieldCurve) multicurve.getCurve(curveNames[loopcurve]);
      InterpolatedDoublesCurve interpolatedCurve = (InterpolatedDoublesCurve) curve.getCurve();
      final YieldAndDiscountCurve curveBumped = new YieldCurve(curveNames[loopcurve],
          new InterpolatedDoublesCurve(x[loopcurve], yShift, interpolatedCurve.getInterpolator(), true));
      for (Currency loopccy : multicurve.getCurrencies()) {
        if (loopccy.equals(multicurve.getCurrencyForName(curveNames[loopcurve]))) {
          multicurveBumped.setCurve(loopccy, curveBumped);
        }
        for (IborIndex loopibor : multicurve.getIndexesIbor()) {
          if (loopibor.equals(multicurve.getIborIndexForName(curveNames[loopcurve]))) { // REQS-427
            multicurveBumped.setCurve(loopibor, curveBumped);
          }
        }
        for (IndexON loopon : multicurve.getIndexesON()) {
          if (loopon.equals(multicurve.getOvernightIndexForName(curveNames[loopcurve]))) { // REQS-427
            multicurveBumped.setCurve(loopon, curveBumped);
          }
        }
        loopcurve++;
      }
    }
    return multicurveBumped;
  }

}
