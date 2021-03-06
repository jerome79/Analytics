/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.util;

import java.util.Arrays;

/**
 * Provides utilities for array handling.
 */
public class ArrayUtils {

  //-------------------------------------------------------------------------
  /**
   * Adds one element to an array.
   * 
   * @param array  the array
   * @param index  the index to add at
   * @param value  the value to add
   * @return the resulting array
   */
  public static double[] add(double[] array, int index, double value) {
    int length = array.length;
    if (index > length || index < 0) {
      throw new IndexOutOfBoundsException("Index: " + index + ", Length: " + length);
    }
    double[] result = Arrays.copyOf(array, length + 1);
    if (index < length) {
      System.arraycopy(array, index, result, index + 1, length - index);
    }
    result[index] = value;
    return result;
  }

  //-------------------------------------------------------------------------
  /**
   * Combines two arrays into one.
   * 
   * @param array1  the first array
   * @param array2  the second array
   * @return the combined array
   */
  public static double[] addAll(double[] array1, double[] array2) {
    double[] result = Arrays.copyOf(array1, array1.length + array2.length);
    System.arraycopy(array2, 0, result, array1.length, array2.length);
    return result;
  }

  /**
   * Combines two arrays into one.
   * 
   * @param array1  the first array
   * @param array2  the second array
   * @return the combined array
   */
  public static <T> T[] addAll(T[] array1, T[] array2) {
    T[] result = Arrays.copyOf(array1, array1.length + array2.length);
    System.arraycopy(array2, 0, result, array1.length, array2.length);
    return result;
  }

}
