/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.convention;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentMap;

import com.google.common.collect.Maps;
import com.opengamma.strata.collect.ArgChecker;

/**
 * An abstract factory for named instances.
 * <p>
 * A named instance is a type where each instance is uniquely identified by a name.
 * This factory provides access to all the instances.
 * <p>
 * Implementations should typically be singletons with a public static factory instance
 * named 'INSTANCE'.
 * 
 * @param <T> type of objects returned
 */
public abstract class AbstractNamedInstanceFactory<T extends NamedInstance>
    implements NamedInstanceFactory<T> {

  /**
   * The named instance type.
   */
  private final Class<T> _type;
  /**
   * Map of primary instances.
   */
  private final ConcurrentMap<String, T> _instanceMap = Maps.newConcurrentMap();
  /**
   * Map of all instances.
   */
  private final ConcurrentMap<String, T> _instanceMapAltNames = Maps.newConcurrentMap();
  /**
   * Lookup map of instances keyed by lower case.
   */
  private final ConcurrentMap<String, T> _lookupMap = Maps.newConcurrentMap();

  /**
   * Creates the factory.
   * 
   * @param type  the type of named instance, not null
   */
  protected AbstractNamedInstanceFactory(Class<T> type) {
    _type = ArgChecker.notNull(type, "type");
  }

  //-------------------------------------------------------------------------
  /**
   * Adds an instance, potentially using a different name.
   * 
   * @param instance  the named instance, not null
   * @param alternativeNames  the alternative names to use in addition to the instance name, not null
   * @return the instance, not null
   */
  protected T addInstance(T instance, String... alternativeNames) {
    ArgChecker.notNull(instance, "instance");
    ArgChecker.notNull(alternativeNames, "alternativeNames");
    _instanceMap.put(instance.getName(), instance);
    _instanceMapAltNames.put(instance.getName(), instance);
    _lookupMap.put(instance.getName().toLowerCase(Locale.ENGLISH), instance);
    for (String altName : alternativeNames) {
      _instanceMapAltNames.put(altName, instance);
      _lookupMap.put(altName.toLowerCase(Locale.ENGLISH), instance);
    }
    return instance;
  }

  /**
   * Loads instances from a properties file based on the type.
   * <p>
   * The properties file must be a name key to a class name.
   */
  protected void loadFromProperties() {
    loadFromProperties(_type.getName());
  }

  /**
   * Loads instances from a properties file.
   * <p>
   * The properties file must be a name key to a class name.
   * 
   * @param bundleName  the bundle name, not null
   */
  protected void loadFromProperties(String bundleName) {
    ArgChecker.notNull(bundleName, "bundleName");
    ResourceBundle bundle = ResourceBundle.getBundle(bundleName);
    Map<String, T> instances = Maps.newHashMap();
    for (String name : bundle.keySet()) {
      String implementationType = bundle.getString(name);
      T instance = instances.get(implementationType);
      if (instance == null) {
        try {
          instance = loadClassRuntime(implementationType).asSubclass(_type).newInstance();
          instances.put(implementationType, instance);
        } catch (Exception ex) {
          throw new IllegalStateException("Error loading properties for " + _type.getSimpleName(), ex);
        }
      }
      addInstance(instance, name);
    }
  }

  private static Class<?> loadClassRuntime(String className) {
    try {
      return Class.forName(className);
    } catch (ClassNotFoundException ex) {
      throw new RuntimeException(ex);
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public T instance(String name) {
    ArgChecker.notNull(name, "name");
    T result = _lookupMap.get(name.toLowerCase(Locale.ENGLISH));
    if (result == null) {
      throw new IllegalArgumentException("Unknown " + _type.getSimpleName() + ": " + name);
    }
    return result;
  }

  @Override
  public Map<String, T> instanceMap() {
    return Collections.unmodifiableMap(_instanceMap);
  }

  @Override
  public Map<String, T> instanceMapWithAlternateNames() {
    return Collections.unmodifiableMap(_instanceMapAltNames);
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return "NamedInstanceFactory[" + _type.getSimpleName() + ",size=" + _instanceMapAltNames.size() + "]";
  }

}
