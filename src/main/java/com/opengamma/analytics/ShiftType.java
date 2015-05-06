/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics;

/**
 * Enum representing data shift types.
 */
public enum ShiftType {

  /**
   * A relative shift where the value is scaled by the shift amount.
   * <p>
   * The shift amount is interpreted as a percentage. For example, a shift amount of 0.1 is a
   * shift of +10% which multiplies the value by 1.1. A shift amount of -0.2 is a shift of -20%
   * which multiplies the value by 0.8
   * <p>
   * {@code shiftedValue = (value x (1 + shiftAmount))}
   */
  RELATIVE("Relative") {
    @Override
    public double applyShift(double value, double shiftAmount) {
      return value * (1 + shiftAmount);
    }
  },

  /**
   * An absolute shift where the shift amount is added to the value.
   * <p>
   * {@code shiftedValue = (value + shiftAmount)}
   */
  ABSOLUTE("Absolute") {
    @Override
    public double applyShift(double value, double shiftAmount) {
      return value + shiftAmount;
    }
  };

  /**
   * Applies the shift to the value using appropriate logic for the shift type.
   *
   * @param value the value to shift
   * @param shiftAmount the shift to apply
   * @return the shifted value
   */
  public abstract double applyShift(double value, double shiftAmount);

  /** The name of the shift type. */
  private String name;

  /**
   * Creates a new instance.
   *
   * @param name  the name of the value
   */
  ShiftType(final String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return name;
  }
}
