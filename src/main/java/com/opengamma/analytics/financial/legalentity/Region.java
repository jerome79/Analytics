/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.legalentity;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.ImmutableBean;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectFieldsBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.google.common.collect.ImmutableSet;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.basics.location.Country;
import com.opengamma.strata.collect.ArgChecker;

/**
 *
 */
@BeanDefinition(builderScope = "private")
public final class Region implements ImmutableBean, Serializable {

  /** The serialization version */
  private static final long serialVersionUID = 1L;

  /**
   * The name of the region.
   */
  @PropertyDefinition(validate = "notNull")
  private final String name;

  /**
   * The set of countries comprising the region, if appropriate.
   */
  @PropertyDefinition(validate = "notNull")
  private final Set<Country> countries;

  /**
   * The set of currencies applicable to the region if appropriate.
   */
  @PropertyDefinition(validate = "notNull")
  private final Set<Currency> currencies;

  /**
   * Creates a region.
   * @param name The name, not null
   * @return The region
   */
  public static Region of(final String name) {
    return new Region(name, Collections.<Country>emptySet(), Collections.<Currency>emptySet());
  }

  /**
   * Creates a region.
   * @param name The name, not null
   * @param country The country
   * @param currency The currency
   * @return The region
   */
  public static Region of(final String name, final Country country, final Currency currency) {
    return new Region(name, country == null ? Collections.<Country>emptySet() : Collections.singleton(country),
        currency == null ? Collections.<Currency>emptySet() : Collections.singleton(currency));
  }

  /**
   * Creates a region.
   * @param name The name, not null
   * @param countries The countries, not null
   * @param currencies The currencies, not null
   * @return The region
   */
  public static Region of(final String name, final Set<Country> countries, final Set<Currency> currencies) {
    ArgChecker.notNull(countries, "countries");
    ArgChecker.notNull(currencies, "currencies");
    return new Region(name, countries, currencies);
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code Region}.
   * @return the meta-bean, not null
   */
  public static Region.Meta meta() {
    return Region.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(Region.Meta.INSTANCE);
  }

  private Region(
      String name,
      Set<Country> countries,
      Set<Currency> currencies) {
    JodaBeanUtils.notNull(name, "name");
    JodaBeanUtils.notNull(countries, "countries");
    JodaBeanUtils.notNull(currencies, "currencies");
    this.name = name;
    this.countries = ImmutableSet.copyOf(countries);
    this.currencies = ImmutableSet.copyOf(currencies);
  }

  @Override
  public Region.Meta metaBean() {
    return Region.Meta.INSTANCE;
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
  /**
   * Gets the name of the region.
   * @return the value of the property, not null
   */
  public String getName() {
    return name;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the set of countries comprising the region, if appropriate.
   * @return the value of the property, not null
   */
  public Set<Country> getCountries() {
    return countries;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the set of currencies applicable to the region if appropriate.
   * @return the value of the property, not null
   */
  public Set<Currency> getCurrencies() {
    return currencies;
  }

  //-----------------------------------------------------------------------
  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      Region other = (Region) obj;
      return JodaBeanUtils.equal(getName(), other.getName()) &&
          JodaBeanUtils.equal(getCountries(), other.getCountries()) &&
          JodaBeanUtils.equal(getCurrencies(), other.getCurrencies());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash = hash * 31 + JodaBeanUtils.hashCode(getName());
    hash = hash * 31 + JodaBeanUtils.hashCode(getCountries());
    hash = hash * 31 + JodaBeanUtils.hashCode(getCurrencies());
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(128);
    buf.append("Region{");
    buf.append("name").append('=').append(getName()).append(',').append(' ');
    buf.append("countries").append('=').append(getCountries()).append(',').append(' ');
    buf.append("currencies").append('=').append(JodaBeanUtils.toString(getCurrencies()));
    buf.append('}');
    return buf.toString();
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code Region}.
   */
  public static final class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code name} property.
     */
    private final MetaProperty<String> _name = DirectMetaProperty.ofImmutable(
        this, "name", Region.class, String.class);
    /**
     * The meta-property for the {@code countries} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Set<Country>> _countries = DirectMetaProperty.ofImmutable(
        this, "countries", Region.class, (Class) Set.class);
    /**
     * The meta-property for the {@code currencies} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Set<Currency>> _currencies = DirectMetaProperty.ofImmutable(
        this, "currencies", Region.class, (Class) Set.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "name",
        "countries",
        "currencies");

    /**
     * Restricted constructor.
     */
    private Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 3373707:  // name
          return _name;
        case 1352637108:  // countries
          return _countries;
        case -1089470353:  // currencies
          return _currencies;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends Region> builder() {
      return new Region.Builder();
    }

    @Override
    public Class<? extends Region> beanType() {
      return Region.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code name} property.
     * @return the meta-property, not null
     */
    public MetaProperty<String> name() {
      return _name;
    }

    /**
     * The meta-property for the {@code countries} property.
     * @return the meta-property, not null
     */
    public MetaProperty<Set<Country>> countries() {
      return _countries;
    }

    /**
     * The meta-property for the {@code currencies} property.
     * @return the meta-property, not null
     */
    public MetaProperty<Set<Currency>> currencies() {
      return _currencies;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 3373707:  // name
          return ((Region) bean).getName();
        case 1352637108:  // countries
          return ((Region) bean).getCountries();
        case -1089470353:  // currencies
          return ((Region) bean).getCurrencies();
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
   * The bean-builder for {@code Region}.
   */
  private static final class Builder extends DirectFieldsBeanBuilder<Region> {

    private String name;
    private Set<Country> countries = ImmutableSet.of();
    private Set<Currency> currencies = ImmutableSet.of();

    /**
     * Restricted constructor.
     */
    private Builder() {
    }

    //-----------------------------------------------------------------------
    @Override
    public Object get(String propertyName) {
      switch (propertyName.hashCode()) {
        case 3373707:  // name
          return name;
        case 1352637108:  // countries
          return countries;
        case -1089470353:  // currencies
          return currencies;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Builder set(String propertyName, Object newValue) {
      switch (propertyName.hashCode()) {
        case 3373707:  // name
          this.name = (String) newValue;
          break;
        case 1352637108:  // countries
          this.countries = (Set<Country>) newValue;
          break;
        case -1089470353:  // currencies
          this.currencies = (Set<Currency>) newValue;
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
    public Region build() {
      return new Region(
          name,
          countries,
          currencies);
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString() {
      StringBuilder buf = new StringBuilder(128);
      buf.append("Region.Builder{");
      buf.append("name").append('=').append(JodaBeanUtils.toString(name)).append(',').append(' ');
      buf.append("countries").append('=').append(JodaBeanUtils.toString(countries)).append(',').append(' ');
      buf.append("currencies").append('=').append(JodaBeanUtils.toString(currencies));
      buf.append('}');
      return buf.toString();
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
