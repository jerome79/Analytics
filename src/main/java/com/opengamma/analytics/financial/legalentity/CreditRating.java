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
 * Class representing a credit rating, with information about the rating,
 * rating description (e.g. prime, investment grade, etc.), the rating
 * agency name and whether the rating is long- or short-term.
 * <p>
 * This class is immutable and thread-safe.
 */
@BeanDefinition(builderScope = "private")
public final class CreditRating implements ImmutableBean, Serializable {

  /** Serialization version */
  private static final long serialVersionUID = 1L;

  /**
   * The rating value.
   */
  @PropertyDefinition(get = "manual", validate = "notNull")
  private final String rating;

  /**
   * The rating description.
   */
  @PropertyDefinition(get = "manual")
  private final String ratingDescription;

  /**
   * The name of the rating agency.
   */
  @PropertyDefinition
  private final String agencyName;

  /**
   * True if the rating is long-term, false if short-term
   */
  @PropertyDefinition
  private final boolean longTerm;

  /**
   * Constructs a credit rating
   * @param rating The rating value, not null
   * @param agencyName The agency name
   * @param isLongTerm True if the rating is long-term
   * @return The rating
   */
  public static CreditRating of(final String rating, final String agencyName, final boolean isLongTerm) {
    return new CreditRating(rating, null, agencyName, isLongTerm);
  }

  /**
   * Constructs a credit rating
   * @param rating The rating value, not null
   * @param ratingDescription The rating value, not null
   * @param agencyName The agency name, not null
   * @param isLongTerm True if the rating is long-term
   * @return The rating
   */
  public static CreditRating of(final String rating, final String ratingDescription, final String agencyName,
      final boolean isLongTerm) {
    ArgChecker.notNull(ratingDescription, "rating description");
    return new CreditRating(rating, ratingDescription, agencyName, isLongTerm);
  }

  /**
   * @param rating The rating value
   * @param ratingDescription The description
   * @param agencyName The agency name
   * @param isLongTerm True if the rating is long-term
   */
  @ImmutableConstructor
  private CreditRating(final String rating, final String ratingDescription, final String agencyName,
      final boolean isLongTerm) {
    ArgChecker.notNull(rating, "rating");
    this.rating = rating;
    this.ratingDescription = ratingDescription;
    this.agencyName = agencyName;
    this.longTerm = isLongTerm;
  }

  /**
   * Gets the rating value.
   * @return The rating value
   */
  public String getRating() {
    return rating;
  }

  /**
   * Gets the rating description.
   * @return The rating description
   */
  public String getRatingDescription() {
    return ratingDescription;
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code CreditRating}.
   * @return the meta-bean, not null
   */
  public static CreditRating.Meta meta() {
    return CreditRating.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(CreditRating.Meta.INSTANCE);
  }

  @Override
  public CreditRating.Meta metaBean() {
    return CreditRating.Meta.INSTANCE;
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
   * Gets the name of the rating agency.
   * @return the value of the property
   */
  public String getAgencyName() {
    return agencyName;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets true if the rating is long-term, false if short-term
   * @return the value of the property
   */
  public boolean isLongTerm() {
    return longTerm;
  }

  //-----------------------------------------------------------------------
  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      CreditRating other = (CreditRating) obj;
      return JodaBeanUtils.equal(getRating(), other.getRating()) &&
          JodaBeanUtils.equal(getRatingDescription(), other.getRatingDescription()) &&
          JodaBeanUtils.equal(getAgencyName(), other.getAgencyName()) &&
          (isLongTerm() == other.isLongTerm());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash = hash * 31 + JodaBeanUtils.hashCode(getRating());
    hash = hash * 31 + JodaBeanUtils.hashCode(getRatingDescription());
    hash = hash * 31 + JodaBeanUtils.hashCode(getAgencyName());
    hash = hash * 31 + JodaBeanUtils.hashCode(isLongTerm());
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(160);
    buf.append("CreditRating{");
    buf.append("rating").append('=').append(getRating()).append(',').append(' ');
    buf.append("ratingDescription").append('=').append(getRatingDescription()).append(',').append(' ');
    buf.append("agencyName").append('=').append(getAgencyName()).append(',').append(' ');
    buf.append("longTerm").append('=').append(JodaBeanUtils.toString(isLongTerm()));
    buf.append('}');
    return buf.toString();
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code CreditRating}.
   */
  public static final class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code rating} property.
     */
    private final MetaProperty<String> _rating = DirectMetaProperty.ofImmutable(
        this, "rating", CreditRating.class, String.class);
    /**
     * The meta-property for the {@code ratingDescription} property.
     */
    private final MetaProperty<String> _ratingDescription = DirectMetaProperty.ofImmutable(
        this, "ratingDescription", CreditRating.class, String.class);
    /**
     * The meta-property for the {@code agencyName} property.
     */
    private final MetaProperty<String> _agencyName = DirectMetaProperty.ofImmutable(
        this, "agencyName", CreditRating.class, String.class);
    /**
     * The meta-property for the {@code longTerm} property.
     */
    private final MetaProperty<Boolean> _longTerm = DirectMetaProperty.ofImmutable(
        this, "longTerm", CreditRating.class, Boolean.TYPE);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "rating",
        "ratingDescription",
        "agencyName",
        "longTerm");

    /**
     * Restricted constructor.
     */
    private Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -938102371:  // rating
          return _rating;
        case 1353926175:  // ratingDescription
          return _ratingDescription;
        case -1646362576:  // agencyName
          return _agencyName;
        case -2074418936:  // longTerm
          return _longTerm;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends CreditRating> builder() {
      return new CreditRating.Builder();
    }

    @Override
    public Class<? extends CreditRating> beanType() {
      return CreditRating.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code rating} property.
     * @return the meta-property, not null
     */
    public MetaProperty<String> rating() {
      return _rating;
    }

    /**
     * The meta-property for the {@code ratingDescription} property.
     * @return the meta-property, not null
     */
    public MetaProperty<String> ratingDescription() {
      return _ratingDescription;
    }

    /**
     * The meta-property for the {@code agencyName} property.
     * @return the meta-property, not null
     */
    public MetaProperty<String> agencyName() {
      return _agencyName;
    }

    /**
     * The meta-property for the {@code longTerm} property.
     * @return the meta-property, not null
     */
    public MetaProperty<Boolean> longTerm() {
      return _longTerm;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -938102371:  // rating
          return ((CreditRating) bean).getRating();
        case 1353926175:  // ratingDescription
          return ((CreditRating) bean).getRatingDescription();
        case -1646362576:  // agencyName
          return ((CreditRating) bean).getAgencyName();
        case -2074418936:  // longTerm
          return ((CreditRating) bean).isLongTerm();
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
   * The bean-builder for {@code CreditRating}.
   */
  private static final class Builder extends DirectFieldsBeanBuilder<CreditRating> {

    private String rating;
    private String ratingDescription;
    private String agencyName;
    private boolean longTerm;

    /**
     * Restricted constructor.
     */
    private Builder() {
    }

    //-----------------------------------------------------------------------
    @Override
    public Object get(String propertyName) {
      switch (propertyName.hashCode()) {
        case -938102371:  // rating
          return rating;
        case 1353926175:  // ratingDescription
          return ratingDescription;
        case -1646362576:  // agencyName
          return agencyName;
        case -2074418936:  // longTerm
          return longTerm;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
    }

    @Override
    public Builder set(String propertyName, Object newValue) {
      switch (propertyName.hashCode()) {
        case -938102371:  // rating
          this.rating = (String) newValue;
          break;
        case 1353926175:  // ratingDescription
          this.ratingDescription = (String) newValue;
          break;
        case -1646362576:  // agencyName
          this.agencyName = (String) newValue;
          break;
        case -2074418936:  // longTerm
          this.longTerm = (Boolean) newValue;
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
    public CreditRating build() {
      return new CreditRating(
          rating,
          ratingDescription,
          agencyName,
          longTerm);
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString() {
      StringBuilder buf = new StringBuilder(160);
      buf.append("CreditRating.Builder{");
      buf.append("rating").append('=').append(JodaBeanUtils.toString(rating)).append(',').append(' ');
      buf.append("ratingDescription").append('=').append(JodaBeanUtils.toString(ratingDescription)).append(',').append(' ');
      buf.append("agencyName").append('=').append(JodaBeanUtils.toString(agencyName)).append(',').append(' ');
      buf.append("longTerm").append('=').append(JodaBeanUtils.toString(longTerm));
      buf.append('}');
      return buf.toString();
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
