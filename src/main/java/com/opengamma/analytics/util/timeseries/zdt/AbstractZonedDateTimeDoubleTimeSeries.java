/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.util.timeseries.zdt;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.function.DoubleBinaryOperator;
import java.util.function.Function;

import com.opengamma.analytics.util.timeseries.AbstractPreciseDoubleTimeSeries;
import com.opengamma.analytics.util.timeseries.DoubleTimeSeries;
import com.opengamma.analytics.util.timeseries.PreciseDoubleTimeSeries;


/**
 * Abstract implementation of {@code ZonedDateTimeDoubleTimeSeries}.
 */
abstract class AbstractZonedDateTimeDoubleTimeSeries
    extends AbstractPreciseDoubleTimeSeries<ZonedDateTime>
    implements ZonedDateTimeDoubleTimeSeries {

  /**
   * Creates an instance.
   */
  public AbstractZonedDateTimeDoubleTimeSeries() {
  }

  //-------------------------------------------------------------------------
  static long[] convertToLongArray(Collection<ZonedDateTime> instants) {
    long[] timesArray = new long[instants.size()];
    int i = 0;
    for (ZonedDateTime time : instants) {
      timesArray[i++] = ZonedDateTimeToLongConverter.convertToLong(time);
    }
    return timesArray;
  }

  static long[] convertToLongArray(ZonedDateTime[] instants) {
    long[] timesArray = new long[instants.length];
    for (int i = 0; i < timesArray.length; i++) {
      timesArray[i] = ZonedDateTimeToLongConverter.convertToLong(instants[i]);
    }
    return timesArray;
  }

  static double[] convertToDoubleArray(Collection<Double> values) {
    double[] valuesArray = new double[values.size()];
    int i = 0;
    for (Double value : values) {
      valuesArray[i++] = value;
    }
    return valuesArray;
  }

  static double[] convertToDoubleArray(Double[] values) {
    double[] valuesArray = new double[values.length];
    for (int i = 0; i < valuesArray.length; i++) {
      valuesArray[i] = values[i];
    }
    return valuesArray;
  }

  static Entry<ZonedDateTime, Double> makeMapEntry(ZonedDateTime key, Double value) {
    return new SimpleImmutableEntry<ZonedDateTime, Double>(key, value);
  }

  //-------------------------------------------------------------------------
  @Override
  protected long convertToLong(ZonedDateTime instant) {
    return ZonedDateTimeToLongConverter.convertToLong(instant);
  }

  @Override
  protected ZonedDateTime convertFromLong(long instant) {
    return ZonedDateTimeToLongConverter.convertToZonedDateTime(instant, getZone());
  }

  @Override
  protected ZonedDateTime[] createArray(int size) {
    return new ZonedDateTime[size];
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the internal storage array without cloning.
   * 
   * @return the array, not null
   */
  abstract long[] timesArrayFast0();

  /**
   * Gets the internal storage array without cloning.
   * 
   * @return the array, not null
   */
  abstract double[] valuesArrayFast0();

  /**
   * Creates a new instance without cloning.
   * 
   * @param instant  the times array, not null
   * @param values  the values array, not null
   * @return the new instance, not null
   */
  abstract ZonedDateTimeDoubleTimeSeries newInstanceFast(long[] instant, double[] values);

  //-------------------------------------------------------------------------
  @Override
  public ZonedDateTimeDoubleEntryIterator iterator() {
    return new ZonedDateTimeDoubleEntryIterator() {
      private int _index = -1;

      @Override
      public boolean hasNext() {
        return (_index + 1) < size();
      }

      @Override
      public Entry<ZonedDateTime, Double> next() {
        if (!hasNext()) {
          throw new NoSuchElementException("No more elements in the iteration");
        }
        _index++;
        long time = AbstractZonedDateTimeDoubleTimeSeries.this.getTimeAtIndexFast(_index);
        Double value = AbstractZonedDateTimeDoubleTimeSeries.this.getValueAtIndex(_index);
        return makeMapEntry(AbstractZonedDateTimeDoubleTimeSeries.this.convertFromLong(time), value);
      }

      @Override
      public long nextTimeFast() {
        if (!hasNext()) {
          throw new NoSuchElementException("No more elements in the iteration");
        }
        _index++;
        return AbstractZonedDateTimeDoubleTimeSeries.this.getTimeAtIndexFast(_index);
      }

      @Override
      public ZonedDateTime nextTime() {
        return AbstractZonedDateTimeDoubleTimeSeries.this.convertFromLong(nextTimeFast());
      }

      @Override
      public long currentTimeFast() {
        if (_index < 0) {
          throw new IllegalStateException("Iterator has not yet been started");
        }
        return AbstractZonedDateTimeDoubleTimeSeries.this.getTimeAtIndexFast(_index);
      }

      @Override
      public ZonedDateTime currentTime() {
        return AbstractZonedDateTimeDoubleTimeSeries.this.convertFromLong(currentTimeFast());
      }

      @Override
      public Double currentValue() {
        if (_index < 0) {
          throw new IllegalStateException("Iterator has not yet been started");
        }
        return AbstractZonedDateTimeDoubleTimeSeries.this.getValueAtIndex(_index);
      }

      @Override
      public double currentValueFast() {
        if (_index < 0) {
          throw new IllegalStateException("Iterator has not yet been started");
        }
        return AbstractZonedDateTimeDoubleTimeSeries.this.getValueAtIndexFast(_index);
      }

      @Override
      public int currentIndex() {
        return _index;
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException("Immutable iterator");
      }
    };
  }

  //-------------------------------------------------------------------------
  @Override
  public ZonedDateTimeDoubleTimeSeries subSeries(ZonedDateTime startZonedDateTime, ZonedDateTime endZonedDateTime) {
    return subSeriesFast(convertToLong(startZonedDateTime), true, convertToLong(endZonedDateTime), false);
  }

  @Override
  public ZonedDateTimeDoubleTimeSeries subSeries(ZonedDateTime startZonedDateTime, boolean includeStart, ZonedDateTime endZonedDateTime, boolean includeEnd) {
    return subSeriesFast(convertToLong(startZonedDateTime), includeStart, convertToLong(endZonedDateTime), includeEnd);
  }

  @Override
  public ZonedDateTimeDoubleTimeSeries subSeriesFast(long startZonedDateTime, long endZonedDateTime) {
    return subSeriesFast(startZonedDateTime, true, endZonedDateTime, false);
  }

  //-------------------------------------------------------------------------
  @Override
  public ZonedDateTimeDoubleTimeSeries lag(int days) {
    long[] times = timesArrayFast0();
    double[] values = valuesArrayFast0();
    if (days == 0) {
      return newInstanceFast(times, values);
    } else if (days < 0) {
      if (-days < times.length) {
        long[] resultTimes = new long[times.length + days]; // remember days is -ve
        System.arraycopy(times, 0, resultTimes, 0, times.length + days);
        double[] resultValues = new double[times.length + days];
        System.arraycopy(values, -days, resultValues, 0, times.length + days);
        return newInstanceFast(resultTimes, resultValues);
      } else {
        return newInstanceFast(new long[0], new double[0]);
      }
    } else { // if (days > 0) {
      if (days < times.length) {
        long[] resultTimes = new long[times.length - days]; // remember days is +ve
        System.arraycopy(times, days, resultTimes, 0, times.length - days);
        double[] resultValues = new double[times.length - days];
        System.arraycopy(values, 0, resultValues, 0, times.length - days);
        return newInstanceFast(resultTimes, resultValues);
      } else {
        return newInstanceFast(new long[0], new double[0]);
      }
    }
  }

  //-------------------------------------------------------------------------
  private ZonedDateTimeDoubleTimeSeries operate(DoubleTimeSeries<?> other, DoubleBinaryOperator operator) {
    if (other instanceof PreciseDoubleTimeSeries) {
      return operate((PreciseDoubleTimeSeries<?>) other, operator);
    }
    throw new UnsupportedOperationException("Can only operate on a PreciseDoubleTimeSeries");
  }

  public ZonedDateTimeDoubleTimeSeries operate(PreciseDoubleTimeSeries<?> other, DoubleBinaryOperator operator) {
    long[] aTimes = timesArrayFast0();
    double[] aValues = valuesArrayFast0();
    int aCount = 0;
    long[] bTimes = other.timesArrayFast();
    double[] bValues = other.valuesArrayFast();
    int bCount = 0;
    long[] resTimes = new long[Math.max(aTimes.length, bTimes.length)];
    double[] resValues = new double[resTimes.length];
    int resCount = 0;
    while (aCount < aTimes.length && bCount < bTimes.length) {
      if (aTimes[aCount] == bTimes[bCount]) {
        resTimes[resCount] = aTimes[aCount];
        resValues[resCount] = operator.applyAsDouble(aValues[aCount], bValues[bCount]);
        resCount++;
        aCount++;
        bCount++;
      } else if (aTimes[aCount] < bTimes[bCount]) {
        aCount++;
      } else { // if (aTimes[aCount] > bTimes[bCount]) {
        bCount++;
      }
    }
    long[] trimmedTimes = new long[resCount];
    double[] trimmedValues = new double[resCount];
    System.arraycopy(resTimes, 0, trimmedTimes, 0, resCount);
    System.arraycopy(resValues, 0, trimmedValues, 0, resCount);
    return newInstanceFast(trimmedTimes, trimmedValues);
  }

  private ZonedDateTimeDoubleTimeSeries unionOperate(DoubleTimeSeries<?> other, DoubleBinaryOperator operator) {
    if (other instanceof PreciseDoubleTimeSeries) {
      return unionOperate((PreciseDoubleTimeSeries<?>) other, operator);
    }
    throw new UnsupportedOperationException("Can only operate on a PreciseDoubleTimeSeries");
  }

  public ZonedDateTimeDoubleTimeSeries unionOperate(PreciseDoubleTimeSeries<?> other, DoubleBinaryOperator operator) {
    long[] aTimes = timesArrayFast0();
    double[] aValues = valuesArrayFast0();
    int aCount = 0;
    long[] bTimes = other.timesArrayFast();
    double[] bValues = other.valuesArrayFast();
    int bCount = 0;
    long[] resTimes = new long[aTimes.length + bTimes.length];
    double[] resValues = new double[resTimes.length];
    int resCount = 0;
    while (aCount < aTimes.length || bCount < bTimes.length) {
      if (aCount >= aTimes.length) {
        int bRemaining = bTimes.length - bCount;
        System.arraycopy(bTimes, bCount, resTimes, resCount, bRemaining);
        System.arraycopy(bValues, bCount, resValues, resCount, bRemaining);
        resCount += bRemaining;
        break;
      } else if (bCount >= bTimes.length) {
        int aRemaining = aTimes.length - aCount;
        System.arraycopy(aTimes, aCount, resTimes, resCount, aRemaining);
        System.arraycopy(aValues, aCount, resValues, resCount, aRemaining);
        resCount += aRemaining;
        break;
      } else if (aTimes[aCount] == bTimes[bCount]) {
        resTimes[resCount] = aTimes[aCount];
        resValues[resCount] = operator.applyAsDouble(aValues[aCount], bValues[bCount]);
        resCount++;
        aCount++;
        bCount++;
      } else if (aTimes[aCount] < bTimes[bCount]) {
        resTimes[resCount] = aTimes[aCount];
        resValues[resCount] = aValues[aCount];
        resCount++;
        aCount++;
      } else { // if (aTimes[aCount] > bTimes[bCount]) {
        resTimes[resCount] = bTimes[bCount];
        resValues[resCount] = bValues[bCount];
        resCount++;
        bCount++;
      }
    }
    long[] trimmedTimes = new long[resCount];
    double[] trimmedValues = new double[resCount];
    System.arraycopy(resTimes, 0, trimmedTimes, 0, resCount);
    System.arraycopy(resValues, 0, trimmedValues, 0, resCount);
    return newInstanceFast(trimmedTimes, trimmedValues);
  }

  //-------------------------------------------------------------------------
  @Override
  public ZonedDateTimeDoubleTimeSeries add(double amountToAdd) {
    return operate(amountToAdd, (a, b) -> a + b);
  }

  @Override
  public ZonedDateTimeDoubleTimeSeries add(DoubleTimeSeries<?> other) {
    return operate(other, (a, b) -> a + b);
  }

  @Override
  public ZonedDateTimeDoubleTimeSeries unionAdd(DoubleTimeSeries<?> other) {
    return unionOperate(other, (a, b) -> a + b);
  }

  //-------------------------------------------------------------------------
  @Override
  public ZonedDateTimeDoubleTimeSeries subtract(double amountToSubtract) {
    return operate(amountToSubtract, (a, b) -> a - b);
  }

  @Override
  public ZonedDateTimeDoubleTimeSeries subtract(DoubleTimeSeries<?> other) {
    return operate(other, (a, b) -> a - b);
  }

  @Override
  public ZonedDateTimeDoubleTimeSeries unionSubtract(DoubleTimeSeries<?> other) {
    return unionOperate(other, (a, b) -> a - b);
  }

  //-------------------------------------------------------------------------
  @Override
  public ZonedDateTimeDoubleTimeSeries multiply(double amountToMultiplyBy) {
    return operate(amountToMultiplyBy, (a, b) -> a * b);
  }

  @Override
  public ZonedDateTimeDoubleTimeSeries multiply(DoubleTimeSeries<?> other) {
    return operate(other, (a, b) -> a * b);
  }

  @Override
  public ZonedDateTimeDoubleTimeSeries unionMultiply(DoubleTimeSeries<?> other) {
    return unionOperate(other, (a, b) -> a * b);
  }

  //-------------------------------------------------------------------------
  @Override
  public ZonedDateTimeDoubleTimeSeries divide(double amountToDivideBy) {
    return operate(amountToDivideBy, (a, b) -> a / b);
  }

  @Override
  public ZonedDateTimeDoubleTimeSeries divide(DoubleTimeSeries<?> other) {
    return operate(other, (a, b) -> a / b);
  }

  @Override
  public ZonedDateTimeDoubleTimeSeries unionDivide(DoubleTimeSeries<?> other) {
    return unionOperate(other, (a, b) -> a / b);
  }

  //-------------------------------------------------------------------------
  @Override
  public ZonedDateTimeDoubleTimeSeries power(double power) {
    return operate(power, Math::pow);
  }

  @Override
  public ZonedDateTimeDoubleTimeSeries power(DoubleTimeSeries<?> other) {
    return operate(other, Math::pow);
  }

  @Override
  public ZonedDateTimeDoubleTimeSeries unionPower(DoubleTimeSeries<?> other) {
    return unionOperate(other, Math::pow);
  }

  //-------------------------------------------------------------------------
  @Override
  public ZonedDateTimeDoubleTimeSeries minimum(double minValue) {
    return operate(minValue, Math::min);
  }

  @Override
  public ZonedDateTimeDoubleTimeSeries minimum(DoubleTimeSeries<?> other) {
    return operate(other, Math::min);
  }

  @Override
  public ZonedDateTimeDoubleTimeSeries unionMinimum(DoubleTimeSeries<?> other) {
    return unionOperate(other, Math::min);
  }

  //-------------------------------------------------------------------------
  @Override
  public ZonedDateTimeDoubleTimeSeries maximum(double maxValue) {
    return operate(maxValue, Math::max);
  }

  @Override
  public ZonedDateTimeDoubleTimeSeries maximum(DoubleTimeSeries<?> other) {
    return operate(other, Math::max);
  }

  @Override
  public ZonedDateTimeDoubleTimeSeries unionMaximum(DoubleTimeSeries<?> other) {
    return unionOperate(other, Math::max);
  }

  //-------------------------------------------------------------------------
  @Override
  public ZonedDateTimeDoubleTimeSeries average(double value) {
    return operate(value, (a, b) -> (a + b) / 2);
  }

  @Override
  public ZonedDateTimeDoubleTimeSeries average(DoubleTimeSeries<?> other) {
    return operate(other, (a, b) -> (a + b) / 2);
  }

  @Override
  public ZonedDateTimeDoubleTimeSeries unionAverage(DoubleTimeSeries<?> other) {
    return unionOperate(other, (a, b) -> (a + b) / 2);
  }

  //-------------------------------------------------------------------------
  @Override
  public ZonedDateTimeDoubleTimeSeries intersectionFirstValue(DoubleTimeSeries<?> other) {
    // optimize PLAT-1590
    if (other instanceof AbstractZonedDateTimeDoubleTimeSeries) {
      long[] aTimes = timesArrayFast0();
      double[] aValues = valuesArrayFast0();
      int aCount = 0;
      long[] bTimes = ((AbstractZonedDateTimeDoubleTimeSeries) other).timesArrayFast0();
      int bCount = 0;
      long[] resTimes = new long[Math.min(aTimes.length, bTimes.length)];
      double[] resValues = new double[resTimes.length];
      int resCount = 0;
      while (aCount < aTimes.length && bCount < bTimes.length) {
        if (aTimes[aCount] == bTimes[bCount]) {
          resTimes[resCount] = aTimes[aCount];
          resValues[resCount] = aValues[aCount];
          resCount++;
          aCount++;
          bCount++;
        } else if (aTimes[aCount] < bTimes[bCount]) {
          aCount++;
        } else { // if (aTimes[aCount] > bTimes[bCount]) {
          bCount++;
        }
      }
      long[] trimmedTimes = new long[resCount];
      double[] trimmedValues = new double[resCount];
      System.arraycopy(resTimes, 0, trimmedTimes, 0, resCount);
      System.arraycopy(resValues, 0, trimmedValues, 0, resCount);
      return newInstanceFast(trimmedTimes, trimmedValues);
    }
    return operate(other, (a, b) -> a);
  }

  @Override
  public ZonedDateTimeDoubleTimeSeries intersectionSecondValue(DoubleTimeSeries<?> other) {
    return operate(other, (a, b) -> b);
  }

  @Override
  public ZonedDateTimeDoubleTimeSeries noIntersectionOperation(DoubleTimeSeries<?> other) {
    throw new IllegalStateException("No binary operation permitted");
  }

  //-------------------------------------------------------------------------
  @Override
  public ZonedDateTimeDoubleTimeSeries negate() {
    return operate(d -> -d);
  }

  @Override
  public ZonedDateTimeDoubleTimeSeries reciprocal() {
    return operate(d -> 1 / d);
  }

  @Override
  public ZonedDateTimeDoubleTimeSeries log() {
    return operate(Math::log);
  }

  @Override
  public ZonedDateTimeDoubleTimeSeries log10() {
    return operate(Math::log10);
  }

  @Override
  public ZonedDateTimeDoubleTimeSeries abs() {
    return operate(Math::abs);
  }

  //-------------------------------------------------------------------------
  @Override
  public double maxValue() {
    if (isEmpty()) {
      throw new NoSuchElementException("Time-series is empty");
    }
    double max = Double.MIN_VALUE;
    for (double value : valuesArrayFast0()) {
      max = Math.max(max, value);
    }
    return max;
  }

  @Override
  public double minValue() throws NoSuchElementException {
    if (isEmpty()) {
      throw new NoSuchElementException("Time-series is empty");
    }
    double min = Double.MAX_VALUE;
    for (double value : valuesArrayFast0()) {
      min = Math.min(min, value);
    }
    return min;
  }

  @Override
  public Function<ZonedDateTime, LocalDate> localDateConverter() {
    return ZonedDateTime::toLocalDate;
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj instanceof AbstractZonedDateTimeDoubleTimeSeries) {
      AbstractZonedDateTimeDoubleTimeSeries other = (AbstractZonedDateTimeDoubleTimeSeries) obj;
      return Arrays.equals(timesArrayFast0(), other.timesArrayFast0()) &&
              Arrays.equals(valuesArrayFast0(), other.valuesArrayFast0());
    }
    if (obj instanceof PreciseDoubleTimeSeries) {
      PreciseDoubleTimeSeries<?> other = (PreciseDoubleTimeSeries<?>) obj;
      return Arrays.equals(timesArrayFast0(), other.timesArrayFast()) &&
              Arrays.equals(valuesArrayFast0(), other.valuesArrayFast());
    }
    return false;
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(timesArrayFast0()) ^ Arrays.hashCode(valuesArrayFast0());
  }

}
