/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.tutorial.analysis.swap;

import java.io.FileWriter;
import java.io.IOException;
import java.time.Period;
import java.time.ZonedDateTime;
import java.util.Arrays;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIborMaster;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexIborMaster;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.provider.calculator.discounting.CrossGammaSingleCurveCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.parameter.ParameterSensitivityParameterCalculator;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.matrix.OGMatrixAlgebra;
import com.opengamma.analytics.tutorial.datasets.AnalysisMarketDataEURJun13Sets;
import com.opengamma.analytics.util.time.DateUtils;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.basics.date.HolidayCalendar;
import com.opengamma.strata.basics.date.HolidayCalendars;
import com.opengamma.strata.collect.tuple.Pair;

/**
 * Analysis of cross-gamma to zero-coupon and market rates.
 */
public class SwapGammaSingleCurveProfitAnalysis {

  private static final HolidayCalendar TARGET = HolidayCalendars.EUTA;
  private static final HolidayCalendar LON = HolidayCalendars.SAT_SUN;

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2013, 6, 13);
  private static final ZonedDateTime SPOT_DATE = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, 2, TARGET);
  private static final ZonedDateTime SPOT_DATE_GBP = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, 0, LON);

  private static final GeneratorSwapFixedIborMaster MASTER_SWAP_FIXED_IBOR = GeneratorSwapFixedIborMaster.getInstance();
  private static final GeneratorSwapFixedIbor EUR1YEURIBOR3M = MASTER_SWAP_FIXED_IBOR.getGenerator("EUR1YEURIBOR3M", TARGET);
  private static final GeneratorSwapFixedIbor GBP6MLIBOR6M = MASTER_SWAP_FIXED_IBOR.getGenerator("GBP6MLIBOR6M", TARGET);
  private static final IndexIborMaster MASTER_IBOR_INDEX = IndexIborMaster.getInstance();
  private static final IborIndex EURIBOR3M = MASTER_IBOR_INDEX.getIndex("EURIBOR3M");
  private static final Currency EUR = EURIBOR3M.getCurrency();
  private static final IborIndex GBPLIBOR6M = MASTER_IBOR_INDEX.getIndex("GBPLIBOR6M");

  private static final Period TENOR_START = Period.ofMonths(150);
  private static final Period TENOR_SWAP = Period.ofYears(5);
  private static final boolean IS_PAYER = true;

  private static final double NOTIONAL = 1.0E6; // 1m
  private static final ZonedDateTime START_DATE = ScheduleCalculator.getAdjustedDate(SPOT_DATE, TENOR_START, EURIBOR3M, TARGET);
  private static final ZonedDateTime START_DATE_GBP = ScheduleCalculator.getAdjustedDate(SPOT_DATE_GBP, TENOR_START, GBPLIBOR6M, TARGET);
  private static final SwapFixedIborDefinition SWAP_EUR_DEFINITION = SwapFixedIborDefinition.from(START_DATE, TENOR_SWAP, EUR1YEURIBOR3M, NOTIONAL, 0.02, IS_PAYER);
  private static final SwapFixedCoupon<Coupon> SWAP_EUR = SWAP_EUR_DEFINITION.toDerivative(REFERENCE_DATE);

  private static final SwapFixedIborDefinition SWAP_GBP_DEFINITION = SwapFixedIborDefinition.from(START_DATE_GBP, TENOR_SWAP, GBP6MLIBOR6M, NOTIONAL, 0.02, IS_PAYER);
  private static final SwapFixedCoupon<Coupon> SWAP_GBP = SWAP_GBP_DEFINITION.toDerivative(REFERENCE_DATE);

  private static final PresentValueCurveSensitivityDiscountingCalculator PVCSDC = PresentValueCurveSensitivityDiscountingCalculator.getInstance();
  private static final ParameterSensitivityParameterCalculator<ParameterProviderInterface> PSC = new ParameterSensitivityParameterCalculator<>(PVCSDC);

  private static final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> MULTICURVE_PAIR_0 = AnalysisMarketDataEURJun13Sets.getMulticurveEUR();
  private static final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> SINGLECURVE_PAIR_0 = AnalysisMarketDataEURJun13Sets.getSingleCurveEUR();
  private static final MulticurveProviderDiscount SINGLECURVE_GBP = MulticurveProviderDiscountDataSets.createSinglecurveGbp();

  private static final String[] CURVE_NAME = new String[2];
  static {
    CURVE_NAME[0] = MULTICURVE_PAIR_0.getFirst().getName(EUR);
    CURVE_NAME[1] = MULTICURVE_PAIR_0.getFirst().getName(EURIBOR3M);
  }
  private static final double BP1 = 1.0E-4;
  private static final double SHIFT = 1 * BP1;

  private static final OGMatrixAlgebra ALGEBRA = new OGMatrixAlgebra();

  private static final CrossGammaSingleCurveCalculator GC = new CrossGammaSingleCurveCalculator(BP1, PVCSDC);

  private static final int NB_NODE_EUR = AnalysisMarketDataEURJun13Sets.getCurveEURNumberNodeForward();
  private static final DoubleMatrix2D GAMMA_EUR = GC.calculateCrossGamma(SWAP_EUR, SINGLECURVE_PAIR_0.getFirst());
  private static final double[] GAMMA_SUM_EUR = new double[GAMMA_EUR.getNumberOfColumns()];
  static {
    for (int loopcol = 0; loopcol < GAMMA_EUR.getNumberOfColumns(); loopcol++) {
      for (int looprow = 0; looprow < GAMMA_EUR.getNumberOfRows(); looprow++) {
        GAMMA_SUM_EUR[loopcol] += GAMMA_EUR.getEntry(looprow, loopcol);
      }
    }
  }
  private static final DoubleMatrix2D GAMMA_GBP = GC.calculateCrossGamma(SWAP_GBP, SINGLECURVE_GBP);
  private static final double[] GAMMA_SUM_GBP = new double[GAMMA_GBP.getNumberOfColumns()];
  private static final double GAMMA_TOT_GBP;
  static {
    double gammaTot = 0;
    for (int loopcol = 0; loopcol < GAMMA_GBP.getNumberOfColumns(); loopcol++) {
      for (int looprow = 0; looprow < GAMMA_GBP.getNumberOfRows(); looprow++) {
        GAMMA_SUM_GBP[loopcol] += GAMMA_GBP.getEntry(looprow, loopcol);
      }
      gammaTot += GAMMA_SUM_GBP[loopcol];
    }
    GAMMA_TOT_GBP = gammaTot;
  }
  private static final int NB_NODE_GBP = GAMMA_GBP.getNumberOfColumns();

  @Test(enabled = false)
  public void crossGammaZeroSingleExport() {
    try (FileWriter writer = new FileWriter("swap-x-gamma-single.csv")) {
      for (int loopnodei = 0; loopnodei < NB_NODE_EUR; loopnodei++) {
        String line = "";
        for (int loopnode2 = 0; loopnode2 < NB_NODE_EUR; loopnode2++) {
          line = line + "," + GAMMA_EUR.getEntry(loopnodei, loopnode2);
        }
        writer.append(line + "0 \n");
      }
      writer.flush();
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  @SuppressWarnings("unused")
  @Test(enabled = false)
  public void crossGammaDiagonalComp() {
    double[] marketMvtArray = new double[NB_NODE_EUR];
    Arrays.fill(marketMvtArray, 0.0010);
    marketMvtArray[5] = -0.0020;
    marketMvtArray[7] = -0.0020;
    marketMvtArray[9] = -0.0020;
    marketMvtArray[11] = -0.0020;
    DoubleMatrix2D marketMvt = new DoubleMatrix2D(new double[][] {marketMvtArray });

    double plTotal = (Double) ALGEBRA.multiply(ALGEBRA.multiply(marketMvt, GAMMA_EUR), ALGEBRA.getTranspose(marketMvt)).getEntry(0, 0);
    double plDiag = 0;
    for (int loopdiag = 0; loopdiag < NB_NODE_EUR; loopdiag++) {
      plDiag += GAMMA_EUR.getEntry(loopdiag, loopdiag) * marketMvtArray[loopdiag] * marketMvtArray[loopdiag];
    }
    double plCol = 0;
    for (int loopcol = 0; loopcol < NB_NODE_EUR; loopcol++) {
      plCol += GAMMA_SUM_EUR[loopcol] * marketMvtArray[loopcol] * marketMvtArray[loopcol];
    }
  }

  @SuppressWarnings("unused")
  @Test(enabled = false)
  public void crossGammaDiagonalCompGBP() {
    double[] marketMvtArray = new double[NB_NODE_GBP];
    Arrays.fill(marketMvtArray, 0.0001); // 1bp
    DoubleMatrix2D marketMvt = new DoubleMatrix2D(new double[][] {marketMvtArray });

    double plTotal = (Double) ALGEBRA.multiply(ALGEBRA.multiply(marketMvt, GAMMA_GBP), ALGEBRA.getTranspose(marketMvt)).getEntry(0, 0);
    double plDiag = 0;
    for (int loopdiag = 0; loopdiag < NB_NODE_GBP; loopdiag++) {
      plDiag += GAMMA_GBP.getEntry(loopdiag, loopdiag) * marketMvtArray[loopdiag] * marketMvtArray[loopdiag];
    }

    int t = 0;
  }

  /**
   * Uses historical data to estimate the difference between diagonal Gamma, sum of column gamma,
   * parallel gamma and full cross-gamma.
   * The result file is exported in the root directory of OG-Analytics.
   * @throws IOException for file
   */
  @Test(enabled = false)
  public void crossGammaDiagonalCompGbpHts() throws IOException {
    final String sheetFilePath = "src/test/resources/analysis/historical_time_series/curve-changes-gbp-10y.csv";
    double[][] shift = GammaAnalysisUtils.parseShifts(sheetFilePath, BP1);
    int nbScenarios = shift.length;
    double[] plGammaTotal = new double[nbScenarios];
    double[] plGammaDiag = new double[nbScenarios];
    double[] plGammaCol = new double[nbScenarios];
    double[] plGammaPar = new double[nbScenarios];
    for (int loopsc = 0; loopsc < nbScenarios; loopsc++) {
      DoubleMatrix2D marketMvt = new DoubleMatrix2D(new double[][] {shift[loopsc] });
      plGammaTotal[loopsc] = (Double) ALGEBRA.multiply(ALGEBRA.multiply(marketMvt, GAMMA_GBP), ALGEBRA.getTranspose(marketMvt)).getEntry(0, 0) * 0.5;
      for (int loopdiag = 0; loopdiag < NB_NODE_GBP; loopdiag++) {
        plGammaDiag[loopsc] += GAMMA_GBP.getEntry(loopdiag, loopdiag) * shift[loopsc][loopdiag] * shift[loopsc][loopdiag] * 0.5;
      }
      for (int loopcol = 0; loopcol < NB_NODE_GBP; loopcol++) {
        plGammaCol[loopsc] += GAMMA_SUM_GBP[loopcol] * shift[loopsc][loopcol] * shift[loopsc][loopcol] * 0.5;
      }
      double shiftAverage = 0;
      for (int loopcol = 0; loopcol < NB_NODE_GBP; loopcol++) {
        shiftAverage += shift[loopsc][loopcol];
      }
      shiftAverage /= NB_NODE_GBP;
      plGammaPar[loopsc] = GAMMA_TOT_GBP * shiftAverage * shiftAverage * 0.5;
    }
    String fileName = "swap-x-gamma-pl-" + TENOR_START.toString() + "x" + TENOR_SWAP.toString() + ".csv";
    try (FileWriter writer = new FileWriter(fileName)) {
      for (int loopsc = 0; loopsc < nbScenarios; loopsc++) {
        String line = plGammaTotal[loopsc] + "," + plGammaDiag[loopsc] + "," + plGammaCol[loopsc] + "," + plGammaPar[loopsc] + " \n";
        writer.append(line);
      }
      writer.flush();
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  @Test(enabled = false)
  public void crossGammaZeroMulti() {
    MulticurveProviderDiscount multicurve = MULTICURVE_PAIR_0.getFirst();
    MultipleCurrencyParameterSensitivity ps0 = PSC.calculateSensitivity(SWAP_EUR, multicurve);
    DoubleMatrix1D[] ps0Mat = new DoubleMatrix1D[2];
    for (int i = 0; i < 2; i++) {
      ps0Mat[i] = ps0.getSensitivity(CURVE_NAME[i], EUR);
    }
    DoubleMatrix1D[][][] psShift = new DoubleMatrix1D[2][2][];
    DoubleMatrix1D[][][] gamma = new DoubleMatrix1D[2][2][]; // Curve shifted, curve impacted
    int[] nbNode = new int[2];
    nbNode[0] = AnalysisMarketDataEURJun13Sets.getCurveEURNumberNodeDiscounting();
    nbNode[1] = AnalysisMarketDataEURJun13Sets.getCurveEURNumberNodeForward();
    MultipleCurrencyParameterSensitivity[] psShiftDsc = new MultipleCurrencyParameterSensitivity[nbNode[0]];
    for (int i = 0; i < 2; i++) {
      psShift[0][i] = new DoubleMatrix1D[nbNode[0]];
      gamma[0][i] = new DoubleMatrix1D[nbNode[0]];
    }
    for (int loopdsc = 0; loopdsc < nbNode[0]; loopdsc++) {
      MulticurveProviderDiscount multicurveShift = AnalysisMarketDataEURJun13Sets.getMulticurvesEURShiftParameterPoint(SHIFT, loopdsc, true);
      psShiftDsc[loopdsc] = PSC.calculateSensitivity(SWAP_EUR, multicurveShift);
      for (int i = 0; i < 2; i++) {
        psShift[0][i][loopdsc] = psShiftDsc[loopdsc].getSensitivity(CURVE_NAME[i], EUR);
        gamma[0][i][loopdsc] = (DoubleMatrix1D) ALGEBRA.add(psShift[0][i][loopdsc], ALGEBRA.scale(ps0Mat[i], -1.0));
      }
    }
    for (int i = 0; i < 2; i++) {
      psShift[1][i] = new DoubleMatrix1D[nbNode[1]];
      gamma[1][i] = new DoubleMatrix1D[nbNode[1]];
    }
    MultipleCurrencyParameterSensitivity[] psShiftFwd = new MultipleCurrencyParameterSensitivity[nbNode[1]];
    for (int loopfwd = 0; loopfwd < nbNode[1]; loopfwd++) {
      MulticurveProviderDiscount multicurveShift = AnalysisMarketDataEURJun13Sets.getMulticurvesEURShiftParameterPoint(SHIFT, loopfwd, false);
      psShiftFwd[loopfwd] = PSC.calculateSensitivity(SWAP_EUR, multicurveShift);
      for (int i = 0; i < 2; i++) {
        psShift[1][i][loopfwd] = psShiftFwd[loopfwd].getSensitivity(CURVE_NAME[i], EUR);
        gamma[1][i][loopfwd] = (DoubleMatrix1D) ALGEBRA.add(psShift[1][i][loopfwd], ALGEBRA.scale(ps0Mat[i], -1.0));
      }
    }

    try (FileWriter writer = new FileWriter("swap-x-gamma-multicurve.csv")) {
      for (int i = 0; i < 2; i++) {
        for (int loopnodei = 0; loopnodei < nbNode[i]; loopnodei++) {
          String line = "";
          for (int j = 0; j < 2; j++) {
            for (int loopnode2 = 0; loopnode2 < nbNode[j]; loopnode2++) {
              line = line + "," + gamma[i][j][loopnodei].getEntry(loopnode2);
            }
          }
          writer.append(line + "0 \n");
        }
      }
      writer.flush();
    } catch (final IOException e) {
      e.printStackTrace();
    }

    @SuppressWarnings("unused")
    int t = 0;
  }

}
