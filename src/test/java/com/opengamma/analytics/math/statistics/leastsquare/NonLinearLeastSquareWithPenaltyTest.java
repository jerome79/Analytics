/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.statistics.leastsquare;

import static com.opengamma.analytics.math.interpolation.PenaltyMatrixGenerator.getPenaltyMatrix;
import static org.testng.AssertJUnit.assertEquals;

import java.util.List;

import org.apache.commons.math3.random.Well44497b;
import org.testng.annotations.Test;

import com.opengamma.analytics.math.curve.DoublesCurve;
import com.opengamma.analytics.math.curve.FunctionalDoublesCurve;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.interpolation.BasisFunctionAggregation;
import com.opengamma.analytics.math.interpolation.BasisFunctionGenerator;
import com.opengamma.analytics.math.interpolation.PenaltyMatrixGenerator;
import com.opengamma.analytics.math.matrix.CommonsMatrixAlgebra;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.matrix.MatrixAlgebra;

/**
 * Test.
 */
@SuppressWarnings("deprecation")
@Test
public class NonLinearLeastSquareWithPenaltyTest {

  private static final MatrixAlgebra MA = new CommonsMatrixAlgebra();

  private static BasisFunctionGenerator GEN = new BasisFunctionGenerator();
  private static NonLinearLeastSquareWithPenalty NLLSWP = new NonLinearLeastSquareWithPenalty();
  private static double[] TENORS = new double[] {1, 2, 3, 5, 7, 10, 15, 20 };
  private static int FREQ = 2;
  static int N_SWAPS = 8;
  private static Function1D<DoublesCurve, DoubleMatrix1D> swapRateFunction;

  // pSpline parameters
  private static int N_KNOTS = 20;
  private static int DEGREE = 3;
  private static int DIFFERENCE_ORDER = 2;
  private static double LAMBDA = 1e5;
  @SuppressWarnings("unused")
  private static DoubleMatrix2D PENALTY_MAT;
  private static List<Function1D<Double, Double>> B_SPLINES;
  @SuppressWarnings("unused")
  private static Function1D<DoubleMatrix1D, DoubleMatrix1D> WEIGHTS_TO_SWAP_FUNC;

  static {
    B_SPLINES = GEN.generateSet(0.0, TENORS[TENORS.length - 1], N_KNOTS, DEGREE);
    int nWeights = B_SPLINES.size();
    PENALTY_MAT = (DoubleMatrix2D) MA.scale(PenaltyMatrixGenerator.getPenaltyMatrix(nWeights, DIFFERENCE_ORDER), LAMBDA);

    // map from curve to swap rates
    swapRateFunction = new Function1D<DoublesCurve, DoubleMatrix1D>() {
      @SuppressWarnings("synthetic-access")
      @Override
      public DoubleMatrix1D evaluate(DoublesCurve curve) {
        double[] res = new double[N_SWAPS];
        double sum = 0.0;

        for (int i = 0; i < N_SWAPS; i++) {
          int start = (int) (i == 0 ? 0 : TENORS[i - 1] * FREQ);
          int end = (int) (TENORS[i] * FREQ - 1);
          for (int k = start; k < end; k++) {
            double t = (k + 1) * 1.0 / FREQ;
            sum += Math.exp(-t * curve.getYValue(t));
          }
          double last = Math.exp(-TENORS[i] * curve.getYValue(TENORS[i]));
          sum += last;
          res[i] = FREQ * (1 - last) / sum;
        }

        return new DoubleMatrix1D(res);
      }
    };

    WEIGHTS_TO_SWAP_FUNC = new Function1D<DoubleMatrix1D, DoubleMatrix1D>() {
      @SuppressWarnings("synthetic-access")
      @Override
      public DoubleMatrix1D evaluate(DoubleMatrix1D x) {
        Function1D<Double, Double> func = new BasisFunctionAggregation<>(B_SPLINES, x.getData());
        FunctionalDoublesCurve curve = FunctionalDoublesCurve.from(func);
        return swapRateFunction.evaluate(curve);
      }
    };

  }

  public void linearTest() {
    boolean print = false;
    if (print) {
      System.out.println("NonLinearLeastSquareWithPenaltyTest.linearTest");
    }
    int nWeights = 20;
    int diffOrder = 2;
    double lambda = 100.0;
    DoubleMatrix2D penalty = (DoubleMatrix2D) MA.scale(getPenaltyMatrix(nWeights, diffOrder), lambda);
    int[] onIndex = new int[] {1, 4, 11, 12, 15, 17};
    double[] obs = new double[] {0, 1.0, 1.0, 1.0, 0.0, 0.0};
    int n = onIndex.length;

    Function1D<DoubleMatrix1D, DoubleMatrix1D> func = new Function1D<DoubleMatrix1D, DoubleMatrix1D>() {

      @Override
      public DoubleMatrix1D evaluate(DoubleMatrix1D x) {
        double[] temp = new double[n];
        for (int i = 0; i < n; i++) {
          temp[i] = x.getEntry(onIndex[i]);
        }
        return new DoubleMatrix1D(temp);
      }
    };

    Function1D<DoubleMatrix1D, DoubleMatrix2D> jac = new Function1D<DoubleMatrix1D, DoubleMatrix2D>() {

      @Override
      public DoubleMatrix2D evaluate(DoubleMatrix1D x) {
        DoubleMatrix2D res = new DoubleMatrix2D(n, nWeights);
        for (int i = 0; i < n; i++) {
          res.getData()[i][onIndex[i]] = 1.0;
        }
        return res;
      }
    };

    Well44497b random = new Well44497b(0L);
    double[] temp = new double[nWeights];
    for (int i = 0; i < nWeights; i++) {
      temp[i] = random.nextDouble();
    }
    DoubleMatrix1D start = new DoubleMatrix1D(temp);

    LeastSquareWithPenaltyResults lsRes = NLLSWP.solve(new DoubleMatrix1D(obs), new DoubleMatrix1D(n, 0.01), func, jac, start,
        penalty);
    if (print) {
      System.out.println("chi2: " + lsRes.getChiSq());
      System.out.println(lsRes.getFitParameters());
    }
    for (int i = 0; i < n; i++) {
      assertEquals(obs[i], lsRes.getFitParameters().getEntry(onIndex[i]), 0.01);
    }
    double expPen = 20.87912357454752;
    assertEquals(expPen, lsRes.getPenalty(), 1e-9);
  }

}
