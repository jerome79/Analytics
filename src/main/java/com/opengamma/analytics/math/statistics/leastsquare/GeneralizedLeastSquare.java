/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.statistics.leastsquare;

import static org.apache.commons.math3.util.CombinatoricsUtils.binomialCoefficient;

import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.primitives.Doubles;
import com.opengamma.analytics.math.FunctionUtils;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.linearalgebra.Decomposition;
import com.opengamma.analytics.math.linearalgebra.DecompositionResult;
import com.opengamma.analytics.math.linearalgebra.SVDecompositionCommons;
import com.opengamma.analytics.math.matrix.CommonsMatrixAlgebra;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.matrix.DoubleMatrixUtils;
import com.opengamma.analytics.math.matrix.MatrixAlgebra;
import com.opengamma.strata.collect.ArgChecker;
import com.opengamma.strata.collect.DoubleArrayMath;

/**
 * 
 */
public class GeneralizedLeastSquare {

  private final Decomposition<?> _decomposition;
  private final MatrixAlgebra _algebra;

  public GeneralizedLeastSquare() {
    _decomposition = new SVDecompositionCommons();
    _algebra = new CommonsMatrixAlgebra();
  }

  /**
   * 
   * @param <T> The type of the independent variables (e.g. Double, double[], DoubleMatrix1D etc)
   * @param x independent variables
   * @param y dependent (scalar) variables
   * @param sigma (Gaussian) measurement error on dependent variables
   * @param basisFunctions set of basis functions - the fitting function is formed by these basis functions times a set of weights
   * @return the results of the least square
   */
  public <T> GeneralizedLeastSquareResults<T> solve(
      T[] x, double[] y, double[] sigma, List<Function1D<T, Double>> basisFunctions) {
    return solve(x, y, sigma, basisFunctions, 0.0, 0);
  }

  /**
   * Generalised least square with penalty on (higher-order) finite differences of weights
   * @param <T> The type of the independent variables (e.g. Double, double[], DoubleMatrix1D etc)
   * @param x independent variables
   * @param y dependent (scalar) variables
   * @param sigma (Gaussian) measurement error on dependent variables
   * @param basisFunctions set of basis functions - the fitting function is formed by these basis functions times a set of weights
   * @param lambda strength of penalty function
   * @param differenceOrder difference order between weights used in penalty function
   * @return the results of the least square
   */
  public <T> GeneralizedLeastSquareResults<T> solve(
      T[] x, double[] y, double[] sigma, List<Function1D<T, Double>> basisFunctions,
      double lambda, int differenceOrder) {
    ArgChecker.notNull(x, "x null");
    ArgChecker.notNull(y, "y null");
    ArgChecker.notNull(sigma, "sigma null");
    ArgChecker.notEmpty(basisFunctions, "empty basisFunctions");
    int n = x.length;
    ArgChecker.isTrue(n > 0, "no data");
    ArgChecker.isTrue(y.length == n, "y wrong length");
    ArgChecker.isTrue(sigma.length == n, "sigma wrong length");

    ArgChecker.isTrue(lambda >= 0.0, "negative lambda");
    ArgChecker.isTrue(differenceOrder >= 0, "difference order");

    List<T> lx = Lists.newArrayList(x);
    List<Double> ly = Lists.newArrayList(Doubles.asList(y));
    List<Double> lsigma = Lists.newArrayList(Doubles.asList(sigma));

    return solveImp(lx, ly, lsigma, basisFunctions, lambda, differenceOrder);
  }

  GeneralizedLeastSquareResults<Double> solve(
      double[] x, double[] y, double[] sigma, List<Function1D<Double, Double>> basisFunctions,
      double lambda, int differenceOrder) {
    return solve(DoubleArrayMath.toObject(x), y, sigma, basisFunctions, lambda, differenceOrder);
  }

  /**
   * 
   * @param <T> The type of the independent variables (e.g. Double, double[], DoubleMatrix1D etc)
   * @param x independent variables
   * @param y dependent (scalar) variables
   * @param sigma (Gaussian) measurement error on dependent variables
   * @param basisFunctions set of basis functions - the fitting function is formed by these basis functions times a set of weights
   * @return the results of the least square
   */
  public <T> GeneralizedLeastSquareResults<T> solve(
      List<T> x, List<Double> y, List<Double> sigma, List<Function1D<T, Double>> basisFunctions) {
    return solve(x, y, sigma, basisFunctions, 0.0, 0);
  }

  /**
   * Generalised least square with penalty on (higher-order) finite differences of weights
   * @param <T> The type of the independent variables (e.g. Double, double[], DoubleMatrix1D etc)
   * @param x independent variables
   * @param y dependent (scalar) variables
   * @param sigma (Gaussian) measurement error on dependent variables
   * @param basisFunctions set of basis functions - the fitting function is formed by these basis functions times a set of weights
   * @param lambda strength of penalty function
   * @param differenceOrder difference order between weights used in penalty function
   * @return the results of the least square
   */
  public <T> GeneralizedLeastSquareResults<T> solve(
      List<T> x, List<Double> y, List<Double> sigma, List<Function1D<T, Double>> basisFunctions,
      double lambda, int differenceOrder) {
    ArgChecker.notEmpty(x, "empty measurement points");
    ArgChecker.notEmpty(y, "empty measurement values");
    ArgChecker.notEmpty(sigma, "empty measurement errors");
    ArgChecker.notEmpty(basisFunctions, "empty basisFunctions");
    int n = x.size();
    ArgChecker.isTrue(n > 0, "no data");
    ArgChecker.isTrue(y.size() == n, "y wrong length");
    ArgChecker.isTrue(sigma.size() == n, "sigma wrong length");

    ArgChecker.isTrue(lambda >= 0.0, "negative lambda");
    ArgChecker.isTrue(differenceOrder >= 0, "difference order");

    return solveImp(x, y, sigma, basisFunctions, lambda, differenceOrder);
  }

  /**
   * Specialist method used mainly for solving multidimensional P-spline problems where the basis functions (B-splines) span a N-dimension space, and the weights sit on an N-dimension
   *  grid and are treated as a N-order tensor rather than a vector, so k-order differencing is done for each tensor index while varying the other indices.
   * @param <T> The type of the independent variables (e.g. Double, double[], DoubleMatrix1D etc)
   * @param x independent variables
   * @param y dependent (scalar) variables
   * @param sigma (Gaussian) measurement error on dependent variables
   * @param basisFunctions set of basis functions - the fitting function is formed by these basis functions times a set of weights
   * @param sizes The size the weights tensor in each dimension (the product of this must equal the number of basis functions)
   * @param lambda strength of penalty function in each dimension
   * @param differenceOrder difference order between weights used in penalty function for each dimension
   * @return the results of the least square
   */
  public <T> GeneralizedLeastSquareResults<T> solve(
      List<T> x, List<Double> y, List<Double> sigma, List<Function1D<T, Double>> basisFunctions,
      int[] sizes, double[] lambda, int[] differenceOrder) {
    ArgChecker.notEmpty(x, "empty measurement points");
    ArgChecker.notEmpty(y, "empty measurement values");
    ArgChecker.notEmpty(sigma, "empty measurement errors");
    ArgChecker.notEmpty(basisFunctions, "empty basisFunctions");
    int n = x.size();
    ArgChecker.isTrue(n > 0, "no data");
    ArgChecker.isTrue(y.size() == n, "y wrong length");
    ArgChecker.isTrue(sigma.size() == n, "sigma wrong length");

    int dim = sizes.length;
    ArgChecker.isTrue(dim == lambda.length, "number of penalty functions {} must be equal to number of directions {}",
        lambda.length, dim);
    ArgChecker.isTrue(dim == differenceOrder.length, "number of difference order {} must be equal to number of directions {}",
        differenceOrder.length, dim);

    for (int i = 0; i < dim; i++) {
      ArgChecker.isTrue(sizes[i] > 0, "sizes must be >= 1");
      ArgChecker.isTrue(lambda[i] >= 0.0, "negative lambda");
      ArgChecker.isTrue(differenceOrder[i] >= 0, "difference order");
    }
    return solveImp(x, y, sigma, basisFunctions, sizes, lambda, differenceOrder);
  }

  private <T> GeneralizedLeastSquareResults<T> solveImp(
      List<T> x, List<Double> y, List<Double> sigma, List<Function1D<T, Double>> basisFunctions,
      double lambda, int differenceOrder) {

    int n = x.size();

    int m = basisFunctions.size();

    double[] b = new double[m];

    double[] invSigmaSqr = new double[n];
    double[][] f = new double[m][n];
    int i, j, k;

    for (i = 0; i < n; i++) {
      double temp = sigma.get(i);
      ArgChecker.isTrue(temp > 0, "sigma must be greater than zero");
      invSigmaSqr[i] = 1.0 / temp / temp;
    }

    for (i = 0; i < m; i++) {
      for (j = 0; j < n; j++) {
        f[i][j] = basisFunctions.get(i).evaluate(x.get(j));
      }
    }

    double sum;
    for (i = 0; i < m; i++) {
      sum = 0;
      for (k = 0; k < n; k++) {
        sum += y.get(k) * f[i][k] * invSigmaSqr[k];
      }
      b[i] = sum;

    }

    DoubleMatrix1D mb = new DoubleMatrix1D(b);
    DoubleMatrix2D ma = getAMatrix(f, invSigmaSqr);

    if (lambda > 0.0) {
      DoubleMatrix2D d = getDiffMatrix(m, differenceOrder);
      ma = (DoubleMatrix2D) _algebra.add(ma, _algebra.scale(d, lambda));
    }

    DecompositionResult decmp = _decomposition.evaluate(ma);
    DoubleMatrix1D w = decmp.solve(mb);
    DoubleMatrix2D covar = decmp.solve(DoubleMatrixUtils.getIdentityMatrix2D(m));

    double chiSq = 0;
    for (i = 0; i < n; i++) {
      double temp = 0;
      for (k = 0; k < m; k++) {
        temp += w.getEntry(k) * f[k][i];
      }
      chiSq += FunctionUtils.square(y.get(i) - temp) * invSigmaSqr[i];
    }

    return new GeneralizedLeastSquareResults<>(basisFunctions, chiSq, w, covar);
  }

  private <T> GeneralizedLeastSquareResults<T> solveImp(
      List<T> x, List<Double> y, List<Double> sigma, List<Function1D<T, Double>> basisFunctions,
      int[] sizes, double[] lambda, int[] differenceOrder) {

    int dim = sizes.length;

    int n = x.size();

    int m = basisFunctions.size();

    double[] b = new double[m];

    double[] invSigmaSqr = new double[n];
    double[][] f = new double[m][n];
    int i, j, k;

    for (i = 0; i < n; i++) {
      double temp = sigma.get(i);
      ArgChecker.isTrue(temp > 0, "sigma must be great than zero");
      invSigmaSqr[i] = 1.0 / temp / temp;
    }

    for (i = 0; i < m; i++) {
      for (j = 0; j < n; j++) {
        f[i][j] = basisFunctions.get(i).evaluate(x.get(j));
      }
    }

    double sum;
    for (i = 0; i < m; i++) {
      sum = 0;
      for (k = 0; k < n; k++) {
        sum += y.get(k) * f[i][k] * invSigmaSqr[k];
      }
      b[i] = sum;

    }

    DoubleMatrix1D mb = new DoubleMatrix1D(b);
    DoubleMatrix2D ma = getAMatrix(f, invSigmaSqr);

    for (i = 0; i < dim; i++) {
      if (lambda[i] > 0.0) {
        DoubleMatrix2D d = getDiffMatrix(sizes, differenceOrder[i], i);
        ma = (DoubleMatrix2D) _algebra.add(ma, _algebra.scale(d, lambda[i]));
      }
    }

    DecompositionResult decmp = _decomposition.evaluate(ma);
    DoubleMatrix1D w = decmp.solve(mb);
    DoubleMatrix2D covar = decmp.solve(DoubleMatrixUtils.getIdentityMatrix2D(m));

    double chiSq = 0;
    for (i = 0; i < n; i++) {
      double temp = 0;
      for (k = 0; k < m; k++) {
        temp += w.getEntry(k) * f[k][i];
      }
      chiSq += FunctionUtils.square(y.get(i) - temp) * invSigmaSqr[i];
    }

    return new GeneralizedLeastSquareResults<>(basisFunctions, chiSq, w, covar);
  }

  private DoubleMatrix2D getAMatrix(double[][] funcMatrix, double[] invSigmaSqr) {
    int m = funcMatrix.length;
    int n = funcMatrix[0].length;
    double[][] a = new double[m][m];
    for (int i = 0; i < m; i++) {
      double sum = 0;
      for (int k = 0; k < n; k++) {
        sum += FunctionUtils.square(funcMatrix[i][k]) * invSigmaSqr[k];
      }
      a[i][i] = sum;
      for (int j = i + 1; j < m; j++) {
        sum = 0;
        for (int k = 0; k < n; k++) {
          sum += funcMatrix[i][k] * funcMatrix[j][k] * invSigmaSqr[k];
        }
        a[i][j] = sum;
        a[j][i] = sum;
      }
    }

    return new DoubleMatrix2D(a);
  }

  private DoubleMatrix2D getDiffMatrix(int m, int k) {
    ArgChecker.isTrue(k < m, "difference order too high");

    double[][] data = new double[m][m];
    if (m == 0) {
      for (int i = 0; i < m; i++) {
        data[i][i] = 1.0;
      }
      return new DoubleMatrix2D(data);
    }

    int[] coeff = new int[k + 1];

    int sign = 1;
    for (int i = k; i >= 0; i--) {
      coeff[i] = (int) (sign * binomialCoefficient(k, i));
      sign *= -1;
    }

    for (int i = k; i < m; i++) {
      for (int j = 0; j < k + 1; j++) {
        data[i][j + i - k] = coeff[j];
      }
    }
    DoubleMatrix2D d = new DoubleMatrix2D(data);

    DoubleMatrix2D dt = _algebra.getTranspose(d);
    return (DoubleMatrix2D) _algebra.multiply(dt, d);
  }

  private DoubleMatrix2D getDiffMatrix(int[] size, int k, int indices) {
    int dim = size.length;

    DoubleMatrix2D d = getDiffMatrix(size[indices], k);

    int preProduct = 1;
    int postProduct = 1;
    for (int j = indices + 1; j < dim; j++) {
      preProduct *= size[j];
    }
    for (int j = 0; j < indices; j++) {
      postProduct *= size[j];
    }
    DoubleMatrix2D temp = d;
    if (preProduct != 1) {
      temp = (DoubleMatrix2D) _algebra.kroneckerProduct(DoubleMatrixUtils.getIdentityMatrix2D(preProduct), temp);
    }
    if (postProduct != 1) {
      temp = (DoubleMatrix2D) _algebra.kroneckerProduct(temp, DoubleMatrixUtils.getIdentityMatrix2D(postProduct));
    }

    return temp;
  }

}
