/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation;

import java.util.Arrays;

/**
 * Utilities for arrays.
 */
public class SurfaceArrayUtils {

  /**
   * For an array of doubles in strictly ascending order, find the index of the entry in the array
   * that is largest value less than or equal to the lookUpValue. 
   * If the lookUpValue is less than the first entry, zero is return, and if the lookUpValue
   * is greater than the last entry, n-1 is returned. 
   * 
   * @param array  the array of strictly acceding doubles 
   * @param lookUpValue  the lookup value
   * @return the index 
   */
  public static int getLowerBoundIndex(final double[] array, final double lookUpValue) {
    int n = array.length;
    if (lookUpValue < array[0]) {
      return 0;
    }
    if (lookUpValue > array[n - 1]) {
      return n - 1;
    }

    int index = Arrays.binarySearch(array, lookUpValue);
    if (index >= 0) {
      // Fast break out if it's an exact match.
      return index;
    }
    if (index < 0) {
      index = -(index + 1);
      index--;
    }
    return index;
  }

}
