/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;

/**
 * Test.
 */
@Test
public class ArrayUtilsTest {

  public void constants() {
    assertThat(ArrayUtils.EMPTY_DOUBLE_ARRAY).isEqualTo(new double[0]);
    assertThat(ArrayUtils.EMPTY_DOUBLE_OBJECT_ARRAY).isEqualTo(new Double[0]);
  }

  public void toPrimitive() {
    assertThat(ArrayUtils.toPrimitive(new Double[] {})).isEqualTo(new double[] {});
    assertThat(ArrayUtils.toPrimitive(new Double[] {1d, 2.5d })).isEqualTo(new double[] {1d, 2.5d });
  }

  public void toObject() {
    assertThat(ArrayUtils.toObject(new double[] {})).isEqualTo(new Double[] {});
    assertThat(ArrayUtils.toObject(new double[] {1d, 2.5d })).isEqualTo(new Double[] {1d, 2.5d });
  }

  public void add() {
    double[] input = new double[] {1d, 2.5d };
    assertThat(ArrayUtils.add(input, 0, 6d)).isEqualTo(new double[] {6d, 1d, 2.5d });
    assertThat(ArrayUtils.add(input, 1, 6d)).isEqualTo(new double[] {1d, 6d, 2.5d });
    assertThat(ArrayUtils.add(input, 2, 6d)).isEqualTo(new double[] {1d, 2.5d, 6d });
  }

  public void addAll() {
    double[] input = new double[] {1d, 2.5d };
    double[] input2 = new double[] {2d, 3.5d };
    assertThat(ArrayUtils.addAll(input, new double[0])).isEqualTo(new double[] {1d, 2.5d });
    assertThat(ArrayUtils.addAll(new double[0], input2)).isEqualTo(new double[] {2d, 3.5d });
    assertThat(ArrayUtils.addAll(input, input2)).isEqualTo(new double[] {1d, 2.5d, 2d, 3.5d });
  }

}
