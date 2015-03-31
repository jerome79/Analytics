/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.legalentity;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;

import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.impl.direct.DirectBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.opengamma.strata.collect.ArgChecker;

/**
 * Gets the short name of an {@link LegalEntity}.
 */
@BeanDefinition
public class LegalEntityShortName implements LegalEntityFilter<LegalEntity>, Bean {

  /** Serialization version */
  private static final long serialVersionUID = 1L;

  /**
   * For the builder.
   */
  public LegalEntityShortName() {
  }

  @Override
  public Object getFilteredData(final LegalEntity legalEntity) {
    ArgChecker.notNull(legalEntity, "obligor");
    return legalEntity.getShortName();
  }

  @Override
  public Type getFilteredDataType() {
    return LegalEntity.meta().shortName().propertyGenericType();
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code LegalEntityShortName}.
   * @return the meta-bean, not null
   */
  public static LegalEntityShortName.Meta meta() {
    return LegalEntityShortName.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(LegalEntityShortName.Meta.INSTANCE);
  }

  @Override
  public LegalEntityShortName.Meta metaBean() {
    return LegalEntityShortName.Meta.INSTANCE;
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
  public LegalEntityShortName clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      return true;
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(32);
    buf.append("LegalEntityShortName{");
    int len = buf.length();
    toString(buf);
    if (buf.length() > len) {
      buf.setLength(buf.length() - 2);
    }
    buf.append('}');
    return buf.toString();
  }

  protected void toString(StringBuilder buf) {
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code LegalEntityShortName}.
   */
  public static class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null);

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    public BeanBuilder<? extends LegalEntityShortName> builder() {
      return new DirectBeanBuilder<LegalEntityShortName>(new LegalEntityShortName());
    }

    @Override
    public Class<? extends LegalEntityShortName> beanType() {
      return LegalEntityShortName.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
