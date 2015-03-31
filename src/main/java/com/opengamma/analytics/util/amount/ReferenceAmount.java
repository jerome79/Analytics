/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.util.amount;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectBean;
import org.joda.beans.impl.direct.DirectBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.opengamma.strata.collect.ArgChecker;

/**
 * Object to represent values linked to a reference for which the values can be added or multiplied by a constant.
 * Used for different sensitivities (parallel curve sensitivity,...). The objects stored as a HashMap(T, Double).
 * @param <T> The reference object.
 */
@BeanDefinition
public class ReferenceAmount<T> extends DirectBean {

  /**
   * The data stored as a map. Not null.
   */
  @PropertyDefinition(validate = "notNull")
  private final HashMap<T, Double> _data;

  /**
   * Constructor. Create an empty map.
   */
  public ReferenceAmount() {
    _data = new HashMap<>();
  }

  /**
   * Constructor from an existing map. The map is used in the new object (no new map is created).
   * @param map The map.
   */
  private ReferenceAmount(final HashMap<T, Double> map) {
    _data = map;
  }

  /**
   * Gets the underlying map.
   * @return The map.
   */
  public HashMap<T, Double> getMap() {
    return _data;
  }

  /**
   * Add a value to the object. The existing object is modified. If the point is not in the existing points of the
   * object, it is put in the map.
   * If a point is already in the existing points of the object, the value is added to the existing value.
   * @param point The surface point.
   * @param value The associated value.
   */
  public void add(final T point, final Double value) {
    ArgChecker.notNull(point, "Point");
    if (_data.containsKey(point)) {
      _data.put(point, value + _data.get(point));
    } else {
      _data.put(point, value);
    }
  }

  /**
   * Create a new object containing the points of the initial object plus the points of another object.
   * If two points <T> are identical, the values are added.
   * @param other The other ReferenceAmount.
   * @return The total.
   */
  public ReferenceAmount<T> plus(final ReferenceAmount<T> other) {
    final HashMap<T, Double> plusMap = new HashMap<>(_data);
    final ReferenceAmount<T> plus = new ReferenceAmount<>(plusMap);
    for (final Entry<T, Double> p : other._data.entrySet()) {
      plus.add(p.getKey(), p.getValue());
    }
    return plus;
  }

  /**
   * Create a new object containing the point of the initial object with the all values multiplied by a given factor.
   * @param factor The multiplicative factor.
   * @return The multiplied surface.
   */
  public ReferenceAmount<T> multiplyBy(final double factor) {
    final HashMap<T, Double> multiplied = new HashMap<>();
    for (final T p : _data.keySet()) {
      multiplied.put(p, _data.get(p) * factor);
    }
    return new ReferenceAmount<>(multiplied);
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code ReferenceAmount}.
   * @return the meta-bean, not null
   */
  @SuppressWarnings("rawtypes")
  public static ReferenceAmount.Meta meta() {
    return ReferenceAmount.Meta.INSTANCE;
  }

  /**
   * The meta-bean for {@code ReferenceAmount}.
   * @param <R>  the bean's generic type
   * @param cls  the bean's generic type
   * @return the meta-bean, not null
   */
  @SuppressWarnings("unchecked")
  public static <R> ReferenceAmount.Meta<R> metaReferenceAmount(Class<R> cls) {
    return ReferenceAmount.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(ReferenceAmount.Meta.INSTANCE);
  }

  @SuppressWarnings("unchecked")
  @Override
  public ReferenceAmount.Meta<T> metaBean() {
    return ReferenceAmount.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the data stored as a map. Not null.
   * @return the value of the property, not null
   */
  public HashMap<T, Double> get_data() {
    return _data;
  }

  /**
   * Sets the data stored as a map. Not null.
   * @param _data  the new value of the property, not null
   */
  public void set_data(HashMap<T, Double> _data) {
    JodaBeanUtils.notNull(_data, "_data");
    this._data.clear();
    this._data.putAll(_data);
  }

  /**
   * Gets the the {@code _data} property.
   * @return the property, not null
   */
  public final Property<HashMap<T, Double>> _data() {
    return metaBean()._data().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public ReferenceAmount<T> clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      ReferenceAmount<?> other = (ReferenceAmount<?>) obj;
      return JodaBeanUtils.equal(get_data(), other.get_data());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash = hash * 31 + JodaBeanUtils.hashCode(get_data());
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(64);
    buf.append("ReferenceAmount{");
    int len = buf.length();
    toString(buf);
    if (buf.length() > len) {
      buf.setLength(buf.length() - 2);
    }
    buf.append('}');
    return buf.toString();
  }

  protected void toString(StringBuilder buf) {
    buf.append("_data").append('=').append(JodaBeanUtils.toString(get_data())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code ReferenceAmount}.
   * @param <T>  the type
   */
  public static class Meta<T> extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    @SuppressWarnings("rawtypes")
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code _data} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<HashMap<T, Double>> _data = DirectMetaProperty.ofReadWrite(
        this, "_data", ReferenceAmount.class, (Class) HashMap.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "_data");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 90810505:  // _data
          return _data;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends ReferenceAmount<T>> builder() {
      return new DirectBeanBuilder<ReferenceAmount<T>>(new ReferenceAmount<T>());
    }

    @SuppressWarnings({"unchecked", "rawtypes" })
    @Override
    public Class<? extends ReferenceAmount<T>> beanType() {
      return (Class) ReferenceAmount.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code _data} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<HashMap<T, Double>> _data() {
      return _data;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 90810505:  // _data
          return ((ReferenceAmount<?>) bean).get_data();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 90810505:  // _data
          ((ReferenceAmount<T>) bean).set_data((HashMap<T, Double>) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((ReferenceAmount<?>) bean)._data, "_data");
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
