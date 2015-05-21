/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.curve;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.opengamma.analytics.util.ParallelArrayBinarySort;
import com.opengamma.strata.collect.ArgChecker;
import com.opengamma.strata.collect.tuple.DoublesPair;

/** 
 * Parent class for a family of curves where the data is stored as arrays.
 * <p>
 * It is possible to construct a curve using either unsorted (in <i>x</i>) data or sorted (ascending in <i>x</i>). 
 * Note that if the constructor is told that unsorted data are sorted then
 * no sorting will take place, which will give unpredictable results.
 */
@BeanDefinition
public abstract class ArraysDoublesCurve extends DoublesCurve {

  /**
   * The size of the data points.
   */
  @PropertyDefinition(get = "private", set = "private")
  private int n;
  /**
   * The <i>x</i> values.
   */
  @PropertyDefinition(validate = "notNull", get = "private", set = "private")
  private double[] xDataPrimitive;
  /**
   * The <i>y</i> values.
   */
  @PropertyDefinition(validate = "notNull", get = "private", set = "private")
  private double[] yDataPrimitive;
  /**
   * The <i>x</i> values.
   */
  @PropertyDefinition(get = "private", set = "private")
  private Double[] xDataObject;
  /**
   * The <i>y</i> values.
   */
  @PropertyDefinition(get = "private", set = "private")
  private Double[] yDataObject;

  /**
   * Constructor for Joda-Beans.
   */
  protected ArraysDoublesCurve() {
  }

  /**
   * Creates an instance.
   * 
   * @param xData  the array of <i>x</i> data, not null
   * @param yData  the array of <i>y</i> data, contains same number of entries as <i>x</i>, not null
   * @param isSorted  whether the <i>x</i>-data is sorted ascending
   */
  public ArraysDoublesCurve(final double[] xData, final double[] yData, final boolean isSorted) {
    ArgChecker.notNull(xData, "x data");
    ArgChecker.notNull(yData, "y data");
    ArgChecker.isTrue(xData.length == yData.length, "x data size {} must be equal to y data size {}", xData.length, yData.length);
    n = xData.length;
    this.xDataPrimitive = Arrays.copyOf(xData, n);
    this.yDataPrimitive = Arrays.copyOf(yData, n);
    if (!isSorted) {
      ParallelArrayBinarySort.parallelBinarySort(this.xDataPrimitive, this.yDataPrimitive);
    }
  }

  /**
   * Creates an instance.
   * 
   * @param xData  the array of <i>x</i> data, not null
   * @param yData  the array of <i>y</i> data, contains same number of entries as <i>x</i>, not null
   * @param isSorted  whether the <i>x</i>-data is sorted ascending
   */
  public ArraysDoublesCurve(final Double[] xData, final Double[] yData, final boolean isSorted) {
    super();
    ArgChecker.notNull(xData, "x data");
    ArgChecker.notNull(yData, "y data");
    n = xData.length;
    ArgChecker.isTrue(xData.length == yData.length, "x data size {} must be equal to y data size {}", xData.length, yData.length);
    this.xDataPrimitive = new double[n];
    this.yDataPrimitive = new double[n];
    for (int i = 0; i < n; i++) {
      Double x = xData[i];
      Double y = yData[i];
      ArgChecker.notNull(x, "x");
      ArgChecker.notNull(y, "y");
      this.xDataPrimitive[i] = x;
      this.yDataPrimitive[i] = y;
    }
    if (!isSorted) {
      ParallelArrayBinarySort.parallelBinarySort(this.xDataPrimitive, this.yDataPrimitive);
    }
  }

  /**
   * Creates an instance.
   * 
   * @param data  the map of <i>x-y</i> data, not null
   * @param isSorted  whether the <i>x</i>-data is sorted ascending
   */
  public ArraysDoublesCurve(final Map<Double, Double> data, final boolean isSorted) {
    super();
    ArgChecker.notNull(data, "data");
    n = data.size();
    xDataPrimitive = new double[n];
    yDataPrimitive = new double[n];
    int i = 0;
    for (final Map.Entry<Double, Double> entry : data.entrySet()) {
      Double x = entry.getKey();
      Double y = entry.getValue();
      ArgChecker.notNull(x, "x");
      ArgChecker.notNull(y, "y");
      xDataPrimitive[i] = x;
      yDataPrimitive[i++] = y;
    }
    if (!isSorted) {
      ParallelArrayBinarySort.parallelBinarySort(xDataPrimitive, yDataPrimitive);
    }
  }

  /**
   * Creates an instance.
   * 
   * @param data  the array of pairs of <i>x-y</i> data, not null
   * @param isSorted  whether the <i>x</i>-data is sorted ascending
   */
  public ArraysDoublesCurve(final DoublesPair[] data, final boolean isSorted) {
    super();
    ArgChecker.notNull(data, "data");
    n = data.length;
    xDataPrimitive = new double[n];
    yDataPrimitive = new double[n];
    for (int i = 0; i < n; i++) {
      DoublesPair pair = data[i];
      ArgChecker.notNull(pair, "pair");
      xDataPrimitive[i] = pair.getFirst();
      yDataPrimitive[i] = pair.getSecond();
    }
    if (!isSorted) {
      ParallelArrayBinarySort.parallelBinarySort(xDataPrimitive, yDataPrimitive);
    }
  }

  /**
   * Creates an instance.
   * 
   * @param data  the set of pairs of <i>x-y</i> data, not null
   * @param isSorted  whether the <i>x</i>-data is sorted ascending
   */
  public ArraysDoublesCurve(final Set<DoublesPair> data, final boolean isSorted) {
    super();
    ArgChecker.notNull(data, "data");
    n = data.size();
    xDataPrimitive = new double[n];
    yDataPrimitive = new double[n];
    int i = 0;
    for (final DoublesPair entry : data) {
      ArgChecker.notNull(entry, "entry");
      xDataPrimitive[i] = entry.getFirst();
      yDataPrimitive[i++] = entry.getSecond();
    }
    if (!isSorted) {
      ParallelArrayBinarySort.parallelBinarySort(xDataPrimitive, yDataPrimitive);
    }
  }

  /**
   * Creates an instance.
   * 
   * @param xData  the list of <i>x</i> data, not null
   * @param yData  the list of <i>y</i> data, contains same number of entries as <i>x</i>, not null
   * @param isSorted  whether the <i>x</i>-data is sorted ascending
   */
  public ArraysDoublesCurve(final List<Double> xData, final List<Double> yData, final boolean isSorted) {
    super();
    ArgChecker.notNull(xData, "x data");
    ArgChecker.notNull(yData, "y data");
    ArgChecker.isTrue(xData.size() == yData.size(), "x data size {} must be equal to y data size {}", xData.size(), yData.size());
    n = xData.size();
    this.xDataPrimitive = new double[n];
    this.yDataPrimitive = new double[n];
    for (int i = 0; i < n; i++) {
      Double x = xData.get(i);
      Double y = yData.get(i);
      ArgChecker.notNull(x, "x");
      ArgChecker.notNull(y, "y");
      this.xDataPrimitive[i] = x;
      this.yDataPrimitive[i] = y;
    }
    if (!isSorted) {
      ParallelArrayBinarySort.parallelBinarySort(this.xDataPrimitive, this.yDataPrimitive);
    }
  }

  /**
   * Creates an instance.
   * 
   * @param data  the list of pairs of <i>x-y</i> data, not null
   * @param isSorted  whether the <i>x</i>-data is sorted ascending
   */
  public ArraysDoublesCurve(final List<DoublesPair> data, final boolean isSorted) {
    super();
    ArgChecker.notNull(data, "data");
    ArgChecker.noNulls(data, "data");
    n = data.size();
    xDataPrimitive = new double[n];
    yDataPrimitive = new double[n];
    int i = 0;
    for (final DoublesPair pair : data) {
      xDataPrimitive[i] = pair.getFirst();
      yDataPrimitive[i++] = pair.getSecond();
    }
    if (!isSorted) {
      ParallelArrayBinarySort.parallelBinarySort(xDataPrimitive, yDataPrimitive);
    }
  }

  /**
   * Creates an instance.
   * 
   * @param xData  the array of <i>x</i> data, not null
   * @param yData  the array of <i>y</i> data, contains same number of entries as <i>x</i>, not null
   * @param isSorted  whether the <i>x</i>-data is sorted ascending
   * @param name  the name of the curve, not null
   */
  public ArraysDoublesCurve(final double[] xData, final double[] yData, final boolean isSorted, final String name) {
    super(name);
    ArgChecker.notNull(xData, "x data");
    ArgChecker.notNull(yData, "y data");
    ArgChecker.isTrue(xData.length == yData.length, "x data size {} must be equal to y data size {}", xData.length, yData.length);
    n = xData.length;
    this.xDataPrimitive = Arrays.copyOf(xData, n);
    this.yDataPrimitive = Arrays.copyOf(yData, n);
    if (!isSorted) {
      ParallelArrayBinarySort.parallelBinarySort(this.xDataPrimitive, this.yDataPrimitive);
    }
  }

  /**
   * Creates an instance.
   * 
   * @param xData  the array of <i>x</i> data, not null
   * @param yData  the array of <i>y</i> data, contains same number of entries as <i>x</i>, not null
   * @param isSorted  whether the <i>x</i>-data is sorted ascending
   * @param name  the name of the curve, not null
   */
  public ArraysDoublesCurve(final Double[] xData, final Double[] yData, final boolean isSorted, final String name) {
    super(name);
    ArgChecker.notNull(xData, "x data");
    n = xData.length;
    ArgChecker.notNull(yData, "y data");
    ArgChecker.isTrue(xData.length == yData.length, "x data size {} must be equal to y data size {}", xData.length, yData.length);
    this.xDataPrimitive = new double[n];
    this.yDataPrimitive = new double[n];
    for (int i = 0; i < n; i++) {
      ArgChecker.notNull(xData[i], "x");
      ArgChecker.notNull(yData[i], "y");
      this.xDataPrimitive[i] = xData[i];
      this.yDataPrimitive[i] = yData[i];
    }
    if (!isSorted) {
      ParallelArrayBinarySort.parallelBinarySort(this.xDataPrimitive, this.yDataPrimitive);
    }
  }

  /**
   * Creates an instance.
   * 
   * @param data  the map of <i>x-y</i> data, not null
   * @param isSorted  whether the <i>x</i>-data is sorted ascending
   * @param name  the name of the curve, not null
   */
  public ArraysDoublesCurve(final Map<Double, Double> data, final boolean isSorted, final String name) {
    super(name);
    ArgChecker.notNull(data, "data");
    n = data.size();
    xDataPrimitive = new double[n];
    yDataPrimitive = new double[n];
    int i = 0;
    for (final Map.Entry<Double, Double> entry : data.entrySet()) {
      ArgChecker.notNull(entry.getKey(), "x");
      ArgChecker.notNull(entry.getValue(), "y");
      xDataPrimitive[i] = entry.getKey();
      yDataPrimitive[i++] = entry.getValue();
    }
    if (!isSorted) {
      ParallelArrayBinarySort.parallelBinarySort(xDataPrimitive, yDataPrimitive);
    }
  }

  /**
   * Creates an instance.
   * 
   * @param data  the array of pairs of <i>x-y</i> data, not null
   * @param isSorted  whether the <i>x</i>-data is sorted ascending
   * @param name  the name of the curve, not null
   */
  public ArraysDoublesCurve(final DoublesPair[] data, final boolean isSorted, final String name) {
    super(name);
    ArgChecker.notNull(data, "data");
    n = data.length;
    xDataPrimitive = new double[n];
    yDataPrimitive = new double[n];
    for (int i = 0; i < n; i++) {
      ArgChecker.notNull(data[i], "entry");
      xDataPrimitive[i] = data[i].getFirst();
      yDataPrimitive[i] = data[i].getSecond();
    }
    if (!isSorted) {
      ParallelArrayBinarySort.parallelBinarySort(xDataPrimitive, yDataPrimitive);
    }
  }

  /**
   * Creates an instance.
   * 
   * @param data  the set of pairs of <i>x-y</i> data, not null
   * @param isSorted  whether the <i>x</i>-data is sorted ascending
   * @param name  the name of the curve, not null
   */
  public ArraysDoublesCurve(final Set<DoublesPair> data, final boolean isSorted, final String name) {
    super(name);
    ArgChecker.notNull(data, "data");
    n = data.size();
    xDataPrimitive = new double[n];
    yDataPrimitive = new double[n];
    int i = 0;
    for (final DoublesPair entry : data) {
      ArgChecker.notNull(entry, "entry");
      xDataPrimitive[i] = entry.getFirst();
      yDataPrimitive[i++] = entry.getSecond();
    }
    if (!isSorted) {
      ParallelArrayBinarySort.parallelBinarySort(xDataPrimitive, yDataPrimitive);
    }
  }

  /**
   * Creates an instance.
   * 
   * @param xData  the list of <i>x</i> data, not null
   * @param yData  the list of <i>y</i> data, contains same number of entries as <i>x</i>, not null
   * @param isSorted  whether the <i>x</i>-data is sorted ascending
   * @param name  the name of the curve, not null
   */
  public ArraysDoublesCurve(final List<Double> xData, final List<Double> yData, final boolean isSorted, final String name) {
    super(name);
    ArgChecker.notNull(xData, "x data");
    ArgChecker.notNull(yData, "y data");
    ArgChecker.isTrue(xData.size() == yData.size(), "x data size {} must be equal to y data size {}", xData.size(), yData.size());
    n = xData.size();
    this.xDataPrimitive = new double[n];
    this.yDataPrimitive = new double[n];
    for (int i = 0; i < n; i++) {
      ArgChecker.notNull(xData.get(i), "x");
      ArgChecker.notNull(yData.get(i), "y");
      this.xDataPrimitive[i] = xData.get(i);
      this.yDataPrimitive[i] = yData.get(i);
    }
    if (!isSorted) {
      ParallelArrayBinarySort.parallelBinarySort(this.xDataPrimitive, this.yDataPrimitive);
    }
  }

  /**
   * Creates an instance.
   * 
   * @param data  the list of pairs of <i>x-y</i> data, not null
   * @param isSorted  whether the <i>x</i>-data is sorted ascending
   * @param name  the name of the curve, not null
   */
  public ArraysDoublesCurve(final List<DoublesPair> data, final boolean isSorted, final String name) {
    super(name);
    ArgChecker.notNull(data, "data");
    ArgChecker.noNulls(data, "data");
    n = data.size();
    xDataPrimitive = new double[n];
    yDataPrimitive = new double[n];
    int i = 0;
    for (final DoublesPair pair : data) {
      xDataPrimitive[i] = pair.getFirst();
      yDataPrimitive[i++] = pair.getSecond();
    }
    if (!isSorted) {
      ParallelArrayBinarySort.parallelBinarySort(xDataPrimitive, yDataPrimitive);
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public Double[] getXData() {
    if (xDataObject != null) {
      return xDataObject;
    }
    xDataObject = new Double[n];
    for (int i = 0; i < n; i++) {
      xDataObject[i] = xDataPrimitive[i];
    }
    return xDataObject;
  }

  @Override
  public Double[] getYData() {
    if (yDataObject != null) {
      return yDataObject;
    }
    yDataObject = new Double[n];
    for (int i = 0; i < n; i++) {
      yDataObject[i] = yDataPrimitive[i];
    }
    return yDataObject;
  }

  /**
    * Returns the <i>x</i> data points as a primitive array.
    * 
    * @return the <i>x</i> data, not null
    */
  public double[] getXDataAsPrimitive() {
    return xDataPrimitive;
  }

  /**
    * Returns the <i>y</i> data points as a primitive array.
    * 
    * @return the <i>y</i> data, not null
    */
  public double[] getYDataAsPrimitive() {
    return yDataPrimitive;
  }

  @Override
  public int size() {
    return n;
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final ArraysDoublesCurve other = (ArraysDoublesCurve) obj;
    return Arrays.equals(xDataPrimitive, other.xDataPrimitive) && Arrays.equals(yDataPrimitive, other.yDataPrimitive);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + Arrays.hashCode(xDataPrimitive);
    result = prime * result + Arrays.hashCode(yDataPrimitive);
    return result;
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code ArraysDoublesCurve}.
   * @return the meta-bean, not null
   */
  public static ArraysDoublesCurve.Meta meta() {
    return ArraysDoublesCurve.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(ArraysDoublesCurve.Meta.INSTANCE);
  }

  @Override
  public ArraysDoublesCurve.Meta metaBean() {
    return ArraysDoublesCurve.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the size of the data points.
   * @return the value of the property
   */
  private int getN() {
    return n;
  }

  /**
   * Sets the size of the data points.
   * @param n  the new value of the property
   */
  private void setN(int n) {
    this.n = n;
  }

  /**
   * Gets the the {@code n} property.
   * @return the property, not null
   */
  public final Property<Integer> n() {
    return metaBean().n().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the <i>x</i> values.
   * @return the value of the property, not null
   */
  private double[] getXDataPrimitive() {
    return xDataPrimitive;
  }

  /**
   * Sets the <i>x</i> values.
   * @param xDataPrimitive  the new value of the property, not null
   */
  private void setXDataPrimitive(double[] xDataPrimitive) {
    JodaBeanUtils.notNull(xDataPrimitive, "xDataPrimitive");
    this.xDataPrimitive = xDataPrimitive;
  }

  /**
   * Gets the the {@code xDataPrimitive} property.
   * @return the property, not null
   */
  public final Property<double[]> xDataPrimitive() {
    return metaBean().xDataPrimitive().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the <i>y</i> values.
   * @return the value of the property, not null
   */
  private double[] getYDataPrimitive() {
    return yDataPrimitive;
  }

  /**
   * Sets the <i>y</i> values.
   * @param yDataPrimitive  the new value of the property, not null
   */
  private void setYDataPrimitive(double[] yDataPrimitive) {
    JodaBeanUtils.notNull(yDataPrimitive, "yDataPrimitive");
    this.yDataPrimitive = yDataPrimitive;
  }

  /**
   * Gets the the {@code yDataPrimitive} property.
   * @return the property, not null
   */
  public final Property<double[]> yDataPrimitive() {
    return metaBean().yDataPrimitive().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the <i>x</i> values.
   * @return the value of the property
   */
  private Double[] getXDataObject() {
    return xDataObject;
  }

  /**
   * Sets the <i>x</i> values.
   * @param xDataObject  the new value of the property
   */
  private void setXDataObject(Double[] xDataObject) {
    this.xDataObject = xDataObject;
  }

  /**
   * Gets the the {@code xDataObject} property.
   * @return the property, not null
   */
  public final Property<Double[]> xDataObject() {
    return metaBean().xDataObject().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the <i>y</i> values.
   * @return the value of the property
   */
  private Double[] getYDataObject() {
    return yDataObject;
  }

  /**
   * Sets the <i>y</i> values.
   * @param yDataObject  the new value of the property
   */
  private void setYDataObject(Double[] yDataObject) {
    this.yDataObject = yDataObject;
  }

  /**
   * Gets the the {@code yDataObject} property.
   * @return the property, not null
   */
  public final Property<Double[]> yDataObject() {
    return metaBean().yDataObject().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(192);
    buf.append("ArraysDoublesCurve{");
    int len = buf.length();
    toString(buf);
    if (buf.length() > len) {
      buf.setLength(buf.length() - 2);
    }
    buf.append('}');
    return buf.toString();
  }

  @Override
  protected void toString(StringBuilder buf) {
    super.toString(buf);
    buf.append("n").append('=').append(JodaBeanUtils.toString(getN())).append(',').append(' ');
    buf.append("xDataPrimitive").append('=').append(JodaBeanUtils.toString(getXDataPrimitive())).append(',').append(' ');
    buf.append("yDataPrimitive").append('=').append(JodaBeanUtils.toString(getYDataPrimitive())).append(',').append(' ');
    buf.append("xDataObject").append('=').append(JodaBeanUtils.toString(getXDataObject())).append(',').append(' ');
    buf.append("yDataObject").append('=').append(JodaBeanUtils.toString(getYDataObject())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code ArraysDoublesCurve}.
   */
  public static class Meta extends DoublesCurve.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code n} property.
     */
    private final MetaProperty<Integer> _n = DirectMetaProperty.ofReadWrite(
        this, "n", ArraysDoublesCurve.class, Integer.TYPE);
    /**
     * The meta-property for the {@code xDataPrimitive} property.
     */
    private final MetaProperty<double[]> _xDataPrimitive = DirectMetaProperty.ofReadWrite(
        this, "xDataPrimitive", ArraysDoublesCurve.class, double[].class);
    /**
     * The meta-property for the {@code yDataPrimitive} property.
     */
    private final MetaProperty<double[]> _yDataPrimitive = DirectMetaProperty.ofReadWrite(
        this, "yDataPrimitive", ArraysDoublesCurve.class, double[].class);
    /**
     * The meta-property for the {@code xDataObject} property.
     */
    private final MetaProperty<Double[]> _xDataObject = DirectMetaProperty.ofReadWrite(
        this, "xDataObject", ArraysDoublesCurve.class, Double[].class);
    /**
     * The meta-property for the {@code yDataObject} property.
     */
    private final MetaProperty<Double[]> _yDataObject = DirectMetaProperty.ofReadWrite(
        this, "yDataObject", ArraysDoublesCurve.class, Double[].class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "n",
        "xDataPrimitive",
        "yDataPrimitive",
        "xDataObject",
        "yDataObject");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 110:  // n
          return _n;
        case 916319941:  // xDataPrimitive
          return _xDataPrimitive;
        case 410761316:  // yDataPrimitive
          return _yDataPrimitive;
        case -2041692639:  // xDataObject
          return _xDataObject;
        case 456323298:  // yDataObject
          return _yDataObject;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends ArraysDoublesCurve> builder() {
      throw new UnsupportedOperationException("ArraysDoublesCurve is an abstract class");
    }

    @Override
    public Class<? extends ArraysDoublesCurve> beanType() {
      return ArraysDoublesCurve.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code n} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Integer> n() {
      return _n;
    }

    /**
     * The meta-property for the {@code xDataPrimitive} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<double[]> xDataPrimitive() {
      return _xDataPrimitive;
    }

    /**
     * The meta-property for the {@code yDataPrimitive} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<double[]> yDataPrimitive() {
      return _yDataPrimitive;
    }

    /**
     * The meta-property for the {@code xDataObject} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Double[]> xDataObject() {
      return _xDataObject;
    }

    /**
     * The meta-property for the {@code yDataObject} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Double[]> yDataObject() {
      return _yDataObject;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 110:  // n
          return ((ArraysDoublesCurve) bean).getN();
        case 916319941:  // xDataPrimitive
          return ((ArraysDoublesCurve) bean).getXDataPrimitive();
        case 410761316:  // yDataPrimitive
          return ((ArraysDoublesCurve) bean).getYDataPrimitive();
        case -2041692639:  // xDataObject
          return ((ArraysDoublesCurve) bean).getXDataObject();
        case 456323298:  // yDataObject
          return ((ArraysDoublesCurve) bean).getYDataObject();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 110:  // n
          ((ArraysDoublesCurve) bean).setN((Integer) newValue);
          return;
        case 916319941:  // xDataPrimitive
          ((ArraysDoublesCurve) bean).setXDataPrimitive((double[]) newValue);
          return;
        case 410761316:  // yDataPrimitive
          ((ArraysDoublesCurve) bean).setYDataPrimitive((double[]) newValue);
          return;
        case -2041692639:  // xDataObject
          ((ArraysDoublesCurve) bean).setXDataObject((Double[]) newValue);
          return;
        case 456323298:  // yDataObject
          ((ArraysDoublesCurve) bean).setYDataObject((Double[]) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((ArraysDoublesCurve) bean).xDataPrimitive, "xDataPrimitive");
      JodaBeanUtils.notNull(((ArraysDoublesCurve) bean).yDataPrimitive, "yDataPrimitive");
      super.validate(bean);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
