/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.legalentity;

import java.io.Serializable;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.regex.Pattern;

import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.ImmutableBean;
import org.joda.beans.ImmutableConstructor;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectFieldsBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.opengamma.strata.collect.ArgChecker;

/**
 * Class representing an Industry Classification Benchmark (ICB).
 * ICB codes are four-digit numbers used to classify the industry of a
 * company.
 * <p>
 * The code represents:
 * <ul>
 * <li>Industry
 * <li>Super-sector
 * <li>Sector
 * <li>Sub-sector
 * </ul>
 * <p>
 * This class is thread-safe and immutable.
 * <p>
 * See <a href="http://www.icbenchmark.com/ICBDocs/Structure_Defs_English.pdf">http://www.icbenchmark.com/ICBDocs/Structure_Defs_English.pdf</a>
 */
@BeanDefinition(builderScope = "private")
public final class ICBCode implements ImmutableBean, Serializable {
  /** Pattern for the code. */
  private static final Pattern FORMAT = Pattern.compile("[0-9]{4}");

  /** Serialization version */
  private static final long serialVersionUID = 1L;

  /**
   * The ICB classification name.
   */
  public static final String NAME = "ICB";
  /**
   * The code.
   */
  @PropertyDefinition(get = "manual", validate = "notNull")
  private final String code;

  /**
   * Constructs an ICB code from an integer.
   * @param code The code, greater than 1000 or less than 10000
   * @return The ICB code
   */
  public static ICBCode of(final int code) {
    if ((code < 1000) || (code > 9999)) {
      throw new IllegalArgumentException("Code out of range: " + code);
    }
    return ICBCode.of(Integer.toString(code));
  }

  /**
   * Constructs an ICB code from a string
   * @param code The code, not null
   * @return The ICB code
   * @throws IllegalArgumentException If the code is not a four-digit number
   */
  public static ICBCode of(final String code) {
    ArgChecker.notNull(code, "code");
    if (FORMAT.matcher(code).matches() == false) {
      throw new IllegalArgumentException("Invalid code : " + code);
    }
    return new ICBCode(code);
  }

  /**
   * @param code The code, not null
   */
  @ImmutableConstructor
  private ICBCode(final String code) {
    ArgChecker.notNull(code, "code");
    this.code = code;
  }

  /**
   * Gets the name of this classification type.
   * @return The name
   */
  public String getClassificationName() {
    return NAME;
  }

  /**
   * Gets the ICB code
   * @return The ICB code
   */
  public String getCode() {
    return code;
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code ICBCode}.
   * @return the meta-bean, not null
   */
  public static ICBCode.Meta meta() {
    return ICBCode.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(ICBCode.Meta.INSTANCE);
  }

  @Override
  public ICBCode.Meta metaBean() {
    return ICBCode.Meta.INSTANCE;
  }

  @Override
  public <R> Property<R> property(String propertyName) {
    return metaBean().<R>metaProperty(propertyName).createProperty(this);
  }

  @Override
  public Set<String> propertyNames() {
    return metaBean().metaPropertyMap().keySet();
  }

  //-----------------------------------------------------------------------
  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      ICBCode other = (ICBCode) obj;
      return JodaBeanUtils.equal(getCode(), other.getCode());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash = hash * 31 + JodaBeanUtils.hashCode(getCode());
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(64);
    buf.append("ICBCode{");
    buf.append("code").append('=').append(JodaBeanUtils.toString(getCode()));
    buf.append('}');
    return buf.toString();
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code ICBCode}.
   */
  public static final class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code code} property.
     */
    private final MetaProperty<String> _code = DirectMetaProperty.ofImmutable(
        this, "code", ICBCode.class, String.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "code");

    /**
     * Restricted constructor.
     */
    private Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 3059181:  // code
          return _code;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends ICBCode> builder() {
      return new ICBCode.Builder();
    }

    @Override
    public Class<? extends ICBCode> beanType() {
      return ICBCode.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code code} property.
     * @return the meta-property, not null
     */
    public MetaProperty<String> code() {
      return _code;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 3059181:  // code
          return ((ICBCode) bean).getCode();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      metaProperty(propertyName);
      if (quiet) {
        return;
      }
      throw new UnsupportedOperationException("Property cannot be written: " + propertyName);
    }

  }

  //-----------------------------------------------------------------------
  /**
   * The bean-builder for {@code ICBCode}.
   */
  private static final class Builder extends DirectFieldsBeanBuilder<ICBCode> {

    private String code;

    /**
     * Restricted constructor.
     */
    private Builder() {
    }

    //-----------------------------------------------------------------------
    @Override
    public Object get(String propertyName) {
      switch (propertyName.hashCode()) {
        case 3059181:  // code
          return code;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
    }

    @Override
    public Builder set(String propertyName, Object newValue) {
      switch (propertyName.hashCode()) {
        case 3059181:  // code
          this.code = (String) newValue;
          break;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
      return this;
    }

    @Override
    public Builder set(MetaProperty<?> property, Object value) {
      super.set(property, value);
      return this;
    }

    @Override
    public Builder setString(String propertyName, String value) {
      setString(meta().metaProperty(propertyName), value);
      return this;
    }

    @Override
    public Builder setString(MetaProperty<?> property, String value) {
      super.setString(property, value);
      return this;
    }

    @Override
    public Builder setAll(Map<String, ? extends Object> propertyValueMap) {
      super.setAll(propertyValueMap);
      return this;
    }

    @Override
    public ICBCode build() {
      return new ICBCode(
          code);
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString() {
      StringBuilder buf = new StringBuilder(64);
      buf.append("ICBCode.Builder{");
      buf.append("code").append('=').append(JodaBeanUtils.toString(code));
      buf.append('}');
      return buf.toString();
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
