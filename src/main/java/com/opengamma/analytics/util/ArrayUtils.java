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

  /**
   * An empty {@code double} array.
   */
  public static final double[] EMPTY_DOUBLE_ARRAY = new double[0];
  /**
   * An empty {@code Double} array.
   */
  public static final Double[] EMPTY_DOUBLE_OBJECT_ARRAY = new Double[0];

  //-------------------------------------------------------------------------
  /**
   * Converts a {@code double} array to a {@code Double} array.
   * 
   * @param array  the array to convert
   * @return the converted array
   */
  public static Double[] toObject(double[] array) {
    if (array.length == 0) {
      return EMPTY_DOUBLE_OBJECT_ARRAY;
    }
    Double[] result = new Double[array.length];
    for (int i = 0; i < array.length; i++) {
      result[i] = new Double(array[i]);
    }
    return result;
  }

  /**
   * Converts a {@code Double} array to a {@code double} array.
   * <p>
   * Throws an exception if null is found.
   * 
   * @param array  the array to convert
   * @return the converted array
   */
  public static double[] toPrimitive(Double[] array) {
    if (array.length == 0) {
      return EMPTY_DOUBLE_ARRAY;
    }
    double[] result = new double[array.length];
    for (int i = 0; i < array.length; i++) {
      result[i] = array[i].doubleValue();
    }
    return result;
  }

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
