/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.legalentity;

import java.util.Map;
import java.util.Set;

import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

/**
 * Represents an obligor with RED code information.
 */
//TODO this shouldn't be a separate type, as it cannot be used generically
@BeanDefinition
public class LegalEntityWithREDCode extends LegalEntity {

  /** Serialization version */
  private static final long serialVersionUID = 1L;

  /**
   * The RED code.
   */
  @PropertyDefinition(validate = "notNull")
  private String redCode;

  /**
   * For the builder.
   */
  public LegalEntityWithREDCode() {
    super();
  }

  /**
   * @param ticker The ticker, not null
   * @param shortName The short name, not null
   * @param creditRatings The set of credit ratings, not null
   * @param sector The sector, not null
   * @param region The region, not null
   * @param redCode The RED code, not null
   */
  public LegalEntityWithREDCode(final String ticker, final String shortName, final Set<CreditRating> creditRatings, final Sector sector,
      final Region region, final String redCode) {
    super(ticker, shortName, creditRatings, sector, region);
    setRedCode(redCode);
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code LegalEntityWithREDCode}.
   * @return the meta-bean, not null
   */
  public static LegalEntityWithREDCode.Meta meta() {
    return LegalEntityWithREDCode.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(LegalEntityWithREDCode.Meta.INSTANCE);
  }

  @Override
  public LegalEntityWithREDCode.Meta metaBean() {
    return LegalEntityWithREDCode.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the RED code.
   * @return the value of the property, not null
   */
  public String getRedCode() {
    return redCode;
  }

  /**
   * Sets the RED code.
   * @param redCode  the new value of the property, not null
   */
  public void setRedCode(String redCode) {
    JodaBeanUtils.notNull(redCode, "redCode");
    this.redCode = redCode;
  }

  /**
   * Gets the the {@code redCode} property.
   * @return the property, not null
   */
  public final Property<String> redCode() {
    return metaBean().redCode().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public LegalEntityWithREDCode clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      LegalEntityWithREDCode other = (LegalEntityWithREDCode) obj;
      return JodaBeanUtils.equal(getRedCode(), other.getRedCode()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = hash * 31 + JodaBeanUtils.hashCode(getRedCode());
    return hash ^ super.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(64);
    buf.append("LegalEntityWithREDCode{");
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
    buf.append("redCode").append('=').append(JodaBeanUtils.toString(getRedCode())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code LegalEntityWithREDCode}.
   */
  public static class Meta extends LegalEntity.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code redCode} property.
     */
    private final MetaProperty<String> _redCode = DirectMetaProperty.ofReadWrite(
        this, "redCode", LegalEntityWithREDCode.class, String.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "redCode");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 1082206750:  // redCode
          return _redCode;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends LegalEntityWithREDCode> builder() {
      return new DirectBeanBuilder<LegalEntityWithREDCode>(new LegalEntityWithREDCode());
    }

    @Override
    public Class<? extends LegalEntityWithREDCode> beanType() {
      return LegalEntityWithREDCode.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code redCode} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> redCode() {
      return _redCode;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 1082206750:  // redCode
          return ((LegalEntityWithREDCode) bean).getRedCode();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 1082206750:  // redCode
          ((LegalEntityWithREDCode) bean).setRedCode((String) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((LegalEntityWithREDCode) bean).redCode, "redCode");
      super.validate(bean);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
