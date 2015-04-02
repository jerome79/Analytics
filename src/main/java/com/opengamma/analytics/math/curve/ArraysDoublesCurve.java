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

import org.apache.commons.lang.ArrayUtils;
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
 * It is possible to construct a curve using either unsorted (in <i>x</i>) data or sorted (ascending in <i>x</i>). 
 * Note that if the constructor is told that unsorted data are sorted then no sorting will take place, which will give unpredictable results.
 */
@BeanDefinition
public abstract class ArraysDoublesCurve extends DoublesCurve {

  /**
   * The size of the data points.
   */
  @PropertyDefinition(get = "private", set = "private")
  private int _n;
  /**
   * The <i>x</i> values.
   */
  @PropertyDefinition(validate = "notNull", get = "manual", set = "private")
  private double[] xData;
  /**
   * The <i>y</i> values.
   */
  @PropertyDefinition(validate = "notNull", get = "manual", set = "private")
  private double[] yData;
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
    _n = xData.length;
    this.xData = Arrays.copyOf(xData, _n);
    this.yData = Arrays.copyOf(yData, _n);
    if (!isSorted) {
      ParallelArrayBinarySort.parallelBinarySort(this.xData, this.yData);
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
    _n = xData.length;
    ArgChecker.isTrue(xData.length == yData.length, "x data size {} must be equal to y data size {}", xData.length, yData.length);
    this.xData = new double[_n];
    this.yData = new double[_n];
    for (int i = 0; i < _n; i++) {
      Double x = xData[i];
      Double y = yData[i];
      ArgChecker.notNull(x, "x");
      ArgChecker.notNull(y, "y");
      this.xData[i] = x;
      this.yData[i] = y;
    }
    if (!isSorted) {
      ParallelArrayBinarySort.parallelBinarySort(this.xData, this.yData);
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
    _n = data.size();
    xData = new double[_n];
    yData = new double[_n];
    int i = 0;
    for (final Map.Entry<Double, Double> entry : data.entrySet()) {
      Double x = entry.getKey();
      Double y = entry.getValue();
      ArgChecker.notNull(x, "x");
      ArgChecker.notNull(y, "y");
      xData[i] = x;
      yData[i++] = y;
    }
    if (!isSorted) {
      ParallelArrayBinarySort.parallelBinarySort(xData, yData);
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
    _n = data.length;
    xData = new double[_n];
    yData = new double[_n];
    for (int i = 0; i < _n; i++) {
      DoublesPair pair = data[i];
      ArgChecker.notNull(pair, "pair");
      xData[i] = pair.getFirst();
      yData[i] = pair.getSecond();
    }
    if (!isSorted) {
      ParallelArrayBinarySort.parallelBinarySort(xData, yData);
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
    _n = data.size();
    xData = new double[_n];
    yData = new double[_n];
    int i = 0;
    for (final DoublesPair entry : data) {
      ArgChecker.notNull(entry, "entry");
      xData[i] = entry.getFirst();
      yData[i++] = entry.getSecond();
    }
    if (!isSorted) {
      ParallelArrayBinarySort.parallelBinarySort(xData, yData);
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
    _n = xData.size();
    this.xData = new double[_n];
    this.yData = new double[_n];
    for (int i = 0; i < _n; i++) {
      Double x = xData.get(i);
      Double y = yData.get(i);
      ArgChecker.notNull(x, "x");
      ArgChecker.notNull(y, "y");
      this.xData[i] = x;
      this.yData[i] = y;
    }
    if (!isSorted) {
      ParallelArrayBinarySort.parallelBinarySort(this.xData, this.yData);
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
    _n = data.size();
    xData = new double[_n];
    yData = new double[_n];
    int i = 0;
    for (final DoublesPair pair : data) {
      xData[i] = pair.getFirst();
      yData[i++] = pair.getSecond();
    }
    if (!isSorted) {
      ParallelArrayBinarySort.parallelBinarySort(xData, yData);
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
    _n = xData.length;
    this.xData = Arrays.copyOf(xData, _n);
    this.yData = Arrays.copyOf(yData, _n);
    if (!isSorted) {
      ParallelArrayBinarySort.parallelBinarySort(this.xData, this.yData);
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
    _n = xData.length;
    ArgChecker.notNull(yData, "y data");
    ArgChecker.isTrue(xData.length == yData.length, "x data size {} must be equal to y data size {}", xData.length, yData.length);
    this.xData = new double[_n];
    this.yData = new double[_n];
    for (int i = 0; i < _n; i++) {
      ArgChecker.notNull(xData[i], "x");
      ArgChecker.notNull(yData[i], "y");
      this.xData[i] = xData[i];
      this.yData[i] = yData[i];
    }
    if (!isSorted) {
      ParallelArrayBinarySort.parallelBinarySort(this.xData, this.yData);
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
    _n = data.size();
    xData = new double[_n];
    yData = new double[_n];
    int i = 0;
    for (final Map.Entry<Double, Double> entry : data.entrySet()) {
      ArgChecker.notNull(entry.getKey(), "x");
      ArgChecker.notNull(entry.getValue(), "y");
      xData[i] = entry.getKey();
      yData[i++] = entry.getValue();
    }
    if (!isSorted) {
      ParallelArrayBinarySort.parallelBinarySort(xData, yData);
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
    _n = data.length;
    xData = new double[_n];
    yData = new double[_n];
    for (int i = 0; i < _n; i++) {
      ArgChecker.notNull(data[i], "entry");
      xData[i] = data[i].getFirst();
      yData[i] = data[i].getSecond();
    }
    if (!isSorted) {
      ParallelArrayBinarySort.parallelBinarySort(xData, yData);
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
    _n = data.size();
    xData = new double[_n];
    yData = new double[_n];
    int i = 0;
    for (final DoublesPair entry : data) {
      ArgChecker.notNull(entry, "entry");
      xData[i] = entry.getFirst();
      yData[i++] = entry.getSecond();
    }
    if (!isSorted) {
      ParallelArrayBinarySort.parallelBinarySort(xData, yData);
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
    _n = xData.size();
    this.xData = new double[_n];
    this.yData = new double[_n];
    for (int i = 0; i < _n; i++) {
      ArgChecker.notNull(xData.get(i), "x");
      ArgChecker.notNull(yData.get(i), "y");
      this.xData[i] = xData.get(i);
      this.yData[i] = yData.get(i);
    }
    if (!isSorted) {
      ParallelArrayBinarySort.parallelBinarySort(this.xData, this.yData);
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
    _n = data.size();
    xData = new double[_n];
    yData = new double[_n];
    int i = 0;
    for (final DoublesPair pair : data) {
      xData[i] = pair.getFirst();
      yData[i++] = pair.getSecond();
    }
    if (!isSorted) {
      ParallelArrayBinarySort.parallelBinarySort(xData, yData);
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public Double[] getXData() {
    if (xDataObject != null) {
      return xDataObject;
    }
    xDataObject = new Double[_n];
    for (int i = 0; i < _n; i++) {
      xDataObject[i] = xData[i];
    }
    return xDataObject;
  }

  @Override
  public Double[] getYData() {
    if (yDataObject != null) {
      return yDataObject;
    }
    yDataObject = new Double[_n];
    for (int i = 0; i < _n; i++) {
      yDataObject[i] = yData[i];
    }
    return yDataObject;
  }

  /**
    * Returns the <i>x</i> data points as a primitive array.
    * 
    * @return the <i>x</i> data, not null
    */
  public double[] getXDataAsPrimitive() {
    return xData;
  }

  /**
    * Returns the <i>y</i> data points as a primitive array.
    * 
    * @return the <i>y</i> data, not null
    */
  public double[] getYDataAsPrimitive() {
    return yData;
  }

  @Override
  public int size() {
    return _n;
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
    return ArrayUtils.isEquals(xData, other.xData) && ArrayUtils.isEquals(yData, other.yData);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + Arrays.hashCode(xData);
    result = prime * result + Arrays.hashCode(yData);
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
    return _n;
  }

  /**
   * Sets the size of the data points.
   * @param n  the new value of the property
   */
  private void setN(int n) {
    this._n = n;
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
   * Sets the <i>x</i> values.
   * @param xData  the new value of the property, not null
   */
  private void setXData(double[] xData) {
    JodaBeanUtils.notNull(xData, "xData");
    this.xData = xData;
  }

  /**
   * Gets the the {@code xData} property.
   * @return the property, not null
   */
  public final Property<double[]> xData() {
    return metaBean().xData().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Sets the <i>y</i> values.
   * @param yData  the new value of the property, not null
   */
  private void setYData(double[] yData) {
    JodaBeanUtils.notNull(yData, "yData");
    this.yData = yData;
  }

  /**
   * Gets the the {@code yData} property.
   * @return the property, not null
   */
  public final Property<double[]> yData() {
    return metaBean().yData().createProperty(this);
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
    buf.append("xData").append('=').append(JodaBeanUtils.toString(getXData())).append(',').append(' ');
    buf.append("yData").append('=').append(JodaBeanUtils.toString(getYData())).append(',').append(' ');
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
     * The meta-property for the {@code xData} property.
     */
    private final MetaProperty<double[]> _xData = DirectMetaProperty.ofReadWrite(
        this, "xData", ArraysDoublesCurve.class, double[].class);
    /**
     * The meta-property for the {@code yData} property.
     */
    private final MetaProperty<double[]> _yData = DirectMetaProperty.ofReadWrite(
        this, "yData", ArraysDoublesCurve.class, double[].class);
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
        "xData",
        "yData",
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
        case 112945218:  // xData
          return _xData;
        case 113868739:  // yData
          return _yData;
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
     * The meta-property for the {@code xData} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<double[]> xData() {
      return _xData;
    }

    /**
     * The meta-property for the {@code yData} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<double[]> yData() {
      return _yData;
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
        case 112945218:  // xData
          return ((ArraysDoublesCurve) bean).getXData();
        case 113868739:  // yData
          return ((ArraysDoublesCurve) bean).getYData();
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
        case 112945218:  // xData
          ((ArraysDoublesCurve) bean).setXData((double[]) newValue);
          return;
        case 113868739:  // yData
          ((ArraysDoublesCurve) bean).setYData((double[]) newValue);
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
      JodaBeanUtils.notNull(((ArraysDoublesCurve) bean).xData, "xData");
      JodaBeanUtils.notNull(((ArraysDoublesCurve) bean).yData, "yData");
      super.validate(bean);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
