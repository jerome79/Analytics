/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.surface;

import java.util.Arrays;

import com.opengamma.analytics.math.curve.CurveShiftFunctionFactory;
import com.opengamma.analytics.math.curve.DoublesCurve;
import com.opengamma.strata.collect.ArgChecker;

/**
 * Shifts an {@link InterpolatedFromCurvesSurfaceAdditiveShiftFunction}. If an <i>x</i> (<i>y</i>) shift does not coincide with the one of the <i>x</i> (<i>y</i>) values 
 * of the intersection of the curves with the axis, an exception is thrown.
 */
public class InterpolatedFromCurvesSurfaceAdditiveShiftFunction implements SurfaceShiftFunction<InterpolatedFromCurvesDoublesSurface> {

  /**
   * {@inheritDoc}
   */
  @Override
  public InterpolatedFromCurvesDoublesSurface evaluate(final InterpolatedFromCurvesDoublesSurface surface, final double shift) {
    ArgChecker.notNull(surface, "surface");
    return evaluate(surface, shift, "PARALLEL_SHIFT_" + surface.getName());
  }

  /**
   * {@inheritDoc}
   */
  @SuppressWarnings("unchecked")
  @Override
  public InterpolatedFromCurvesDoublesSurface evaluate(final InterpolatedFromCurvesDoublesSurface surface, final double shift, final String newName) {
    ArgChecker.notNull(surface, "surface");
    final boolean xzCurves = surface.isXZCurves();
    final double[] points = surface.getPoints();
    final DoublesCurve[] curves = surface.getCurves();
    final int n = curves.length;
    final DoublesCurve[] newCurves = new DoublesCurve[curves.length];
    for (int i = 0; i < n; i++) {
      newCurves[i] = CurveShiftFunctionFactory.getShiftedCurve(curves[i], shift);
    }
    return InterpolatedFromCurvesDoublesSurface.from(xzCurves, points, newCurves, surface.getInterpolator(), newName);
  }

  /**
   * {@inheritDoc}
   * @throws UnsupportedOperationException If the <i>x</i> (<i>y</i>) position of the shift does not coincide with one of the <i>x</i> (<i>y</i>) intersections 
   * of the curves with the axis
   */

  @Override
  public InterpolatedFromCurvesDoublesSurface evaluate(final InterpolatedFromCurvesDoublesSurface surface, final double x, final double y, final double shift) {
    ArgChecker.notNull(surface, "surface");
    return evaluate(surface, x, y, shift, "SINGLE_SHIFT_" + surface.getName());
  }

  /**
   * {@inheritDoc}
   * @throws UnsupportedOperationException If the <i>x</i> (<i>y</i>) position of the shift does not coincide with one of the <i>x</i> (<i>y</i>) intersections 
   * of the curves with the axis
   */
  @Override
  public InterpolatedFromCurvesDoublesSurface evaluate(final InterpolatedFromCurvesDoublesSurface surface, final double x, final double y, final double shift, final String newName) {
    ArgChecker.notNull(surface, "surface");
    final boolean xzCurves = surface.isXZCurves();
    final double[] points = surface.getPoints();
    if (xzCurves) {
      final int index = Arrays.binarySearch(points, y);
      final DoublesCurve[] curves = surface.getCurves();
      if (index >= 0) {
        final DoublesCurve[] newCurves = Arrays.copyOf(surface.getCurves(), points.length);
        newCurves[index] = CurveShiftFunctionFactory.getShiftedCurve(curves[index], x, shift);
        return InterpolatedFromCurvesDoublesSurface.fromSorted(xzCurves, points, newCurves, surface.getInterpolator(), newName);
      }
      throw new UnsupportedOperationException("Cannot get shift for y-value not in original list of curves: asked for " + y);
    }
    final int index = Arrays.binarySearch(points, x);
    final DoublesCurve[] curves = surface.getCurves();
    if (index >= 0) {
      final DoublesCurve[] newCurves = Arrays.copyOf(surface.getCurves(), points.length);
      newCurves[index] = CurveShiftFunctionFactory.getShiftedCurve(curves[index], y, shift);
      return InterpolatedFromCurvesDoublesSurface.fromSorted(xzCurves, points, newCurves, surface.getInterpolator(), newName);
    }
    throw new UnsupportedOperationException("Cannot get shift for x-value not in original list of curves: asked for " + x);
  }

  /**
   * {@inheritDoc}
   * @throws UnsupportedOperationException If the <i>x</i> (<i>y</i>) positions of the shifts do not coincide with one of the <i>x</i> (<i>y</i>) intersections 
   * of the curves with the axis
   */
  @Override
  public InterpolatedFromCurvesDoublesSurface evaluate(final InterpolatedFromCurvesDoublesSurface surface, final double[] xShift, final double[] yShift, final double[] shift) {
    ArgChecker.notNull(surface, "surface");
    return evaluate(surface, xShift, yShift, shift, "MULTIPLE_SHIFT_" + surface.getName());
  }

  /**
   * {@inheritDoc}
   * @throws UnsupportedOperationException If the <i>x</i> (<i>y</i>) positions of the shifts do not coincide with one of the <i>x</i> (<i>y</i>) intersections 
   * of the curves with the axis
   */
  @Override
  public InterpolatedFromCurvesDoublesSurface evaluate(final InterpolatedFromCurvesDoublesSurface surface, final double[] xShift, final double[] yShift, final double[] shift, final String newName) {
    ArgChecker.notNull(surface, "surface");
    ArgChecker.notNull(xShift, "x shifts");
    ArgChecker.notNull(yShift, "y shifts");
    ArgChecker.notNull(shift, "shifts");
    final int n = xShift.length;
    if (n == 0) {
      return InterpolatedFromCurvesDoublesSurface.from(surface.isXZCurves(), surface.getPoints(), surface.getCurves(), surface.getInterpolator(), newName);
    }
    ArgChecker.isTrue(n == yShift.length && n == shift.length);
    final boolean xzCurves = surface.isXZCurves();
    final double[] points = surface.getPoints();
    if (xzCurves) {
      final DoublesCurve[] newCurves = Arrays.copyOf(surface.getCurves(), points.length);
      for (int i = 0; i < n; i++) {
        final int index = Arrays.binarySearch(points, yShift[i]);
        boolean foundValue = false;
        if (index >= 0) {
          newCurves[index] = CurveShiftFunctionFactory.getShiftedCurve(newCurves[index], xShift[i], shift[i]);
          foundValue = true;
        }
        if (!foundValue) {
          throw new UnsupportedOperationException("Cannot get shift for y-value not in original list of curves: asked for " + yShift[i]);
        }
      }
      return InterpolatedFromCurvesDoublesSurface.fromSorted(xzCurves, points, newCurves, surface.getInterpolator(), newName);
    }
    final DoublesCurve[] newCurves = Arrays.copyOf(surface.getCurves(), points.length);
    for (int i = 0; i < n; i++) {
      final int index = Arrays.binarySearch(points, xShift[i]);
      boolean foundValue = false;
      if (index >= 0) {
        newCurves[index] = CurveShiftFunctionFactory.getShiftedCurve(newCurves[index], yShift[i], shift[i]);
        foundValue = true;
      }
      if (!foundValue) {
        throw new UnsupportedOperationException("Cannot get shift for x-value not in original list of curves: asked for " + xShift[i]);
      }
    }
    return InterpolatedFromCurvesDoublesSurface.fromSorted(xzCurves, points, newCurves, surface.getInterpolator(), newName);
  }
}
