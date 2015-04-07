/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.curve;

import java.lang.reflect.Array;
import java.util.ArrayList;
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
import com.opengamma.strata.collect.tuple.Pair;

/**
 * Parent class for a family of curves that can have any time of data on the <i>x</i> and <i>y</i> axes, provided that the <i>x</i> data is {@link Comparable}.
 * It is possible to construct a curve using either unsorted (in <i>x</i>) data or sorted (ascending in <i>x</i>). Note that if the constructor
 * is told that unsorted data are sorted then no sorting will take place, which will give unpredictable results.
 * @param <T> The type of the x data
 * @param <U> The type of the y data
 */
@BeanDefinition
public abstract class ObjectsCurve<T extends Comparable<T>, U>
    extends Curve<T, U> {

  @PropertyDefinition(get = "private", set = "private")
  private int n;

  @PropertyDefinition(validate = "notNull", get = "manual", set = "private")
  private T[] xData;

  @PropertyDefinition(validate = "notNull", get = "manual", set = "private")
  private U[] yData;

  /**
   * Constructor for Joda-Beans.
   */
  protected ObjectsCurve() {
  }

  /**
   * Creates an instance.
   * 
   * @param xData  the array of <i>x</i> data, not null
   * @param yData  the array of <i>y</i> data, contains same number of entries as <i>x</i>, not null
   * @param isSorted  whether the <i>x</i>-data is sorted ascending
   */
  public ObjectsCurve(final T[] xData, final U[] yData, final boolean isSorted) {
    super();
    ArgChecker.notNull(xData, "x data");
    ArgChecker.notNull(yData, "y data");
    n = xData.length;
    ArgChecker.isTrue(n == yData.length, "size of x data {} does not match size of y data {}", n, yData.length);
    this.xData = Arrays.copyOf(xData, n);
    this.yData = Arrays.copyOf(yData, n);
    for (int i = 0; i < n; i++) {
      ArgChecker.notNull(xData[i], "element " + i + " of x data");
      ArgChecker.notNull(yData[i], "element " + i + " of y data");
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
  @SuppressWarnings("unchecked")
  public ObjectsCurve(final Map<T, U> data, final boolean isSorted) {
    super();
    ArgChecker.noNulls(data.keySet(), "x values");
    ArgChecker.noNulls(data.values(), "y values");
    n = data.size();
    final Map.Entry<T, U> firstEntry = data.entrySet().iterator().next();
    xData = data.keySet().toArray((T[]) Array.newInstance(firstEntry.getKey().getClass(), 0));
    yData = data.values().toArray((U[]) Array.newInstance(firstEntry.getValue().getClass(), 0));
    if (!isSorted) {
      ParallelArrayBinarySort.parallelBinarySort(xData, yData);
    }
  }

  /**
   * Creates an instance.
   * 
   * @param data  the set of <i>x-y</i> pairs, not null
   * @param isSorted  whether the <i>x</i>-data is sorted ascending
   */
  @SuppressWarnings("unchecked")
  public ObjectsCurve(final Set<Pair<T, U>> data, final boolean isSorted) {
    super();
    ArgChecker.notNull(data, "data");
    n = data.size();
    final List<T> xTemp = new ArrayList<>(n);
    final List<U> yTemp = new ArrayList<>(n);
    for (final Pair<T, U> entry : data) {
      ArgChecker.notNull(entry, "element of data");
      xTemp.add(entry.getFirst());
      yTemp.add(entry.getSecond());
    }
    final Pair<T, U> firstEntry = data.iterator().next();
    xData = xTemp.toArray((T[]) Array.newInstance(firstEntry.getFirst().getClass(), 0));
    yData = yTemp.toArray((U[]) Array.newInstance(firstEntry.getSecond().getClass(), 0));
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
  @SuppressWarnings("unchecked")
  public ObjectsCurve(final List<T> xData, final List<U> yData, final boolean isSorted) {
    super();
    ArgChecker.notNull(xData, "x data");
    ArgChecker.notNull(yData, "y data");
    n = xData.size();
    ArgChecker.isTrue(n == yData.size(), "size of x data {} does not match size of y data {}", n, yData.size());
    this.xData = xData.toArray((T[]) Array.newInstance(xData.get(0).getClass(), 0));
    this.yData = yData.toArray((U[]) Array.newInstance(yData.get(0).getClass(), 0));
    if (!isSorted) {
      ParallelArrayBinarySort.parallelBinarySort(this.xData, this.yData);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Creates an instance.
   * 
   * @param xData  the array of <i>x</i> data, not null
   * @param yData  the array of <i>y</i> data, not null, contains same number of entries as <i>x</i>
   * @param isSorted  whether the <i>x</i>-data is sorted ascending
   * @param name  the name of the curve, not null
   */
  public ObjectsCurve(final T[] xData, final U[] yData, final boolean isSorted, final String name) {
    super(name);
    ArgChecker.notNull(xData, "x data");
    n = xData.length;
    ArgChecker.notNull(yData, "y data");
    ArgChecker.isTrue(n == yData.length, "size of x data {} does not match size of y data {}", n, yData.length);
    this.xData = Arrays.copyOf(xData, n);
    this.yData = Arrays.copyOf(yData, n);
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
  @SuppressWarnings("unchecked")
  public ObjectsCurve(final Map<T, U> data, final boolean isSorted, final String name) {
    super(name);
    ArgChecker.noNulls(data.keySet(), "x values");
    ArgChecker.noNulls(data.values(), "y values");
    n = data.size();
    final Map.Entry<T, U> firstEntry = data.entrySet().iterator().next();
    xData = data.keySet().toArray((T[]) Array.newInstance(firstEntry.getKey().getClass(), 0));
    yData = data.values().toArray((U[]) Array.newInstance(firstEntry.getValue().getClass(), 0));
    if (!isSorted) {
      ParallelArrayBinarySort.parallelBinarySort(xData, yData);
    }

  }

  /**
   * Creates an instance.
   * 
   * @param data the set of <i>x-y</i> pairs, not null
   * @param isSorted  whether the <i>x</i>-data is sorted ascending
   * @param name  the name of the curve, not null
   */
  @SuppressWarnings("unchecked")
  public ObjectsCurve(final Set<Pair<T, U>> data, final boolean isSorted, final String name) {
    super(name);
    ArgChecker.notNull(data, "data");
    n = data.size();
    final List<T> xTemp = new ArrayList<>(n);
    final List<U> yTemp = new ArrayList<>(n);
    final int i = 0;
    for (final Pair<T, U> entry : data) {
      ArgChecker.notNull(entry, "element " + i + " of data");
      xTemp.add(entry.getFirst());
      yTemp.add(entry.getSecond());
    }
    final Pair<T, U> firstEntry = data.iterator().next();
    xData = xTemp.toArray((T[]) Array.newInstance(firstEntry.getFirst().getClass(), 0));
    yData = yTemp.toArray((U[]) Array.newInstance(firstEntry.getSecond().getClass(), 0));
    if (!isSorted) {
      ParallelArrayBinarySort.parallelBinarySort(xData, yData);
    }
  }

  /**
   * Creates an instance.
   * 
   * @param xData  the list of <i>x</i> data, not null
   * @param yData  the list of <i>y</i> data, not null, contains same number of entries as <i>x</i>
   * @param isSorted  whether the <i>x</i>-data is sorted ascending
   * @param name  the name of the curve, not null
   */
  @SuppressWarnings("unchecked")
  public ObjectsCurve(final List<T> xData, final List<U> yData, final boolean isSorted, final String name) {
    super(name);
    ArgChecker.notNull(xData, "x data");
    ArgChecker.notNull(yData, "y data");
    n = xData.size();
    ArgChecker.isTrue(n == yData.size(), "size of x data {} does not match size of y data {}", n, yData.size());
    this.xData = xData.toArray((T[]) Array.newInstance(xData.get(0).getClass(), 0));
    this.yData = yData.toArray((U[]) Array.newInstance(yData.get(0).getClass(), 0));
    if (!isSorted) {
      ParallelArrayBinarySort.parallelBinarySort(this.xData, this.yData);
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public T[] getXData() {
    return xData;
  }

  @Override
  public U[] getYData() {
    return yData;
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
    final ObjectsCurve<?, ?> other = (ObjectsCurve<?, ?>) obj;
    return Arrays.equals(xData, other.xData) && Arrays.equals(yData, other.yData);
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
   * The meta-bean for {@code ObjectsCurve}.
   * @return the meta-bean, not null
   */
  @SuppressWarnings("rawtypes")
  public static ObjectsCurve.Meta meta() {
    return ObjectsCurve.Meta.INSTANCE;
  }

  /**
   * The meta-bean for {@code ObjectsCurve}.
   * @param <R>  the first generic type
   * @param <S>  the second generic type
   * @param cls1  the first generic type
   * @param cls2  the second generic type
   * @return the meta-bean, not null
   */
  @SuppressWarnings("unchecked")
  public static <R extends Comparable<R>, S> ObjectsCurve.Meta<R, S> metaObjectsCurve(Class<R> cls1, Class<S> cls2) {
    return ObjectsCurve.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(ObjectsCurve.Meta.INSTANCE);
  }

  @SuppressWarnings("unchecked")
  @Override
  public ObjectsCurve.Meta<T, U> metaBean() {
    return ObjectsCurve.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the n.
   * @return the value of the property
   */
  private int getN() {
    return n;
  }

  /**
   * Sets the n.
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
   * Sets the xData.
   * @param xData  the new value of the property, not null
   */
  private void setXData(T[] xData) {
    JodaBeanUtils.notNull(xData, "xData");
    this.xData = xData;
  }

  /**
   * Gets the the {@code xData} property.
   * @return the property, not null
   */
  public final Property<T[]> xData() {
    return metaBean().xData().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Sets the yData.
   * @param yData  the new value of the property, not null
   */
  private void setYData(U[] yData) {
    JodaBeanUtils.notNull(yData, "yData");
    this.yData = yData;
  }

  /**
   * Gets the the {@code yData} property.
   * @return the property, not null
   */
  public final Property<U[]> yData() {
    return metaBean().yData().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(128);
    buf.append("ObjectsCurve{");
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
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code ObjectsCurve}.
   * @param <T>  the type
   * @param <U>  the type
   */
  public static class Meta<T extends Comparable<T>, U> extends Curve.Meta<T, U> {
    /**
     * The singleton instance of the meta-bean.
     */
    @SuppressWarnings("rawtypes")
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code n} property.
     */
    private final MetaProperty<Integer> _n = DirectMetaProperty.ofReadWrite(
        this, "n", ObjectsCurve.class, Integer.TYPE);
    /**
     * The meta-property for the {@code xData} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<T[]> _xData = (DirectMetaProperty) DirectMetaProperty.ofReadWrite(
        this, "xData", ObjectsCurve.class, Object[].class);
    /**
     * The meta-property for the {@code yData} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<U[]> _yData = (DirectMetaProperty) DirectMetaProperty.ofReadWrite(
        this, "yData", ObjectsCurve.class, Object[].class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "n",
        "xData",
        "yData");

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
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends ObjectsCurve<T, U>> builder() {
      throw new UnsupportedOperationException("ObjectsCurve is an abstract class");
    }

    @SuppressWarnings({"unchecked", "rawtypes" })
    @Override
    public Class<? extends ObjectsCurve<T, U>> beanType() {
      return (Class) ObjectsCurve.class;
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
    public final MetaProperty<T[]> xData() {
      return _xData;
    }

    /**
     * The meta-property for the {@code yData} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<U[]> yData() {
      return _yData;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 110:  // n
          return ((ObjectsCurve<?, ?>) bean).getN();
        case 112945218:  // xData
          return ((ObjectsCurve<?, ?>) bean).getXData();
        case 113868739:  // yData
          return ((ObjectsCurve<?, ?>) bean).getYData();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 110:  // n
          ((ObjectsCurve<T, U>) bean).setN((Integer) newValue);
          return;
        case 112945218:  // xData
          ((ObjectsCurve<T, U>) bean).setXData((T[]) newValue);
          return;
        case 113868739:  // yData
          ((ObjectsCurve<T, U>) bean).setYData((U[]) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((ObjectsCurve<?, ?>) bean).xData, "xData");
      JodaBeanUtils.notNull(((ObjectsCurve<?, ?>) bean).yData, "yData");
      super.validate(bean);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
