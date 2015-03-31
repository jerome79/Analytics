/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.util.types;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Objects;

import com.opengamma.strata.collect.ArgChecker;

/**
 * Implementation of {@link ParameterizedType}.
 */
public final class ParameterizedTypeImpl implements ParameterizedType {

  // TODO: Use something public from another library - the Guava stuff is all private however

  private final Type[] _actualTypeArguments;
  private final Type _rawType;
  private final Type _ownerType;

  private ParameterizedTypeImpl(final Type[] actualTypeArguments, final Type rawType, final Type ownerType) {
    _actualTypeArguments = actualTypeArguments;
    _rawType = rawType;
    _ownerType = ownerType;
  }

  public static ParameterizedTypeImpl of(final Class<?> rawType, final Type... actualTypeArguments) {
    ArgChecker.notNull(rawType, "rawType");
    ArgChecker.noNulls(actualTypeArguments, "actualTypeArguments");
    return new ParameterizedTypeImpl(actualTypeArguments.clone(), rawType, rawType.getEnclosingClass());
  }

  public static ParameterizedTypeImpl of(final Class<?> rawType, final Type typeArg1) {
    ArgChecker.notNull(rawType, "rawType");
    ArgChecker.notNull(typeArg1, "typeArg1");
    return new ParameterizedTypeImpl(new Type[] {typeArg1 }, rawType, rawType.getEnclosingClass());
  }

  public static ParameterizedTypeImpl of(final Class<?> rawType, final Type typeArg1, final Type typeArg2) {
    ArgChecker.notNull(rawType, "rawType");
    ArgChecker.notNull(typeArg1, "typeArg1");
    ArgChecker.notNull(typeArg2, "typeArg2");
    return new ParameterizedTypeImpl(new Type[] {typeArg1, typeArg2 }, rawType, rawType.getEnclosingClass());
  }

  public static ParameterizedTypeImpl of(final Class<?> rawType, final Type typeArg1, final Type typeArg2, final Type typeArg3) {
    ArgChecker.notNull(rawType, "rawType");
    ArgChecker.notNull(typeArg1, "typeArg1");
    ArgChecker.notNull(typeArg2, "typeArg2");
    ArgChecker.notNull(typeArg3, "typeArg3");
    return new ParameterizedTypeImpl(new Type[] {typeArg1, typeArg2, typeArg3 }, rawType, rawType.getEnclosingClass());
  }

  public static ParameterizedTypeImpl of(final Class<?> rawType, final Type typeArg1, final Type typeArg2, final Type typeArg3, final Type typeArg4) {
    ArgChecker.notNull(rawType, "rawType");
    ArgChecker.notNull(typeArg1, "typeArg1");
    ArgChecker.notNull(typeArg2, "typeArg2");
    ArgChecker.notNull(typeArg3, "typeArg3");
    ArgChecker.notNull(typeArg4, "typeArg4");
    return new ParameterizedTypeImpl(new Type[] {typeArg1, typeArg2, typeArg3, typeArg4 }, rawType, rawType.getEnclosingClass());
  }

  @Override
  public Type[] getActualTypeArguments() {
    return _actualTypeArguments.clone();
  }

  @Override
  public Type getRawType() {
    return _rawType;
  }

  @Override
  public Type getOwnerType() {
    return _ownerType;
  }

  @Override
  public boolean equals(final Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof ParameterizedType)) {
      return false;
    }
    final ParameterizedType other = (ParameterizedType) o;
    return Objects.equals(_rawType, other.getRawType()) && Objects.equals(_ownerType, other.getOwnerType()) && Arrays.equals(_actualTypeArguments, other.getActualTypeArguments());
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(_actualTypeArguments) ^ Objects.hashCode(_ownerType) ^ Objects.hashCode(_rawType);
  }

}
