/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.greeks;

/**
 * @param <T> Return type of visitor
 */
public abstract class AbstractGreekVisitor<T> implements GreekVisitor<T> {

  @Override
  public T visitCarryRho() {
    throw new UnsupportedOperationException();
  }

  @Override
  public T visitDVannaDVol() {
    throw new UnsupportedOperationException();
  }

  @Override
  public T visitDZetaDVol() {
    throw new UnsupportedOperationException();
  }

  @Override
  public T visitDelta() {
    throw new UnsupportedOperationException();
  }

  @Override
  public T visitDeltaBleed() {
    throw new UnsupportedOperationException();
  }

  @Override
  public T visitDriftlessTheta() {
    throw new UnsupportedOperationException();
  }

  @Override
  public T visitElasticity() {
    throw new UnsupportedOperationException();
  }

  @Override
  public T visitGamma() {
    throw new UnsupportedOperationException();
  }

  @Override
  public T visitGammaBleed() {
    throw new UnsupportedOperationException();
  }

  @Override
  public T visitGammaP() {
    throw new UnsupportedOperationException();
  }

  @Override
  public T visitGammaPBleed() {
    throw new UnsupportedOperationException();
  }

  @Override
  public T visitPhi() {
    throw new UnsupportedOperationException();
  }

  @Override
  public T visitPrice() {
    throw new UnsupportedOperationException();
  }

  @Override
  public T visitRho() {
    throw new UnsupportedOperationException();
  }

  @Override
  public T visitSpeed() {
    throw new UnsupportedOperationException();
  }

  @Override
  public T visitSpeedP() {
    throw new UnsupportedOperationException();
  }

  @Override
  public T visitStrikeDelta() {
    throw new UnsupportedOperationException();
  }

  @Override
  public T visitStrikeGamma() {
    throw new UnsupportedOperationException();
  }

  @Override
  public T visitDualDelta() {
    return visitStrikeDelta();
  }

  @Override
  public T visitDualGamma() {
    return visitStrikeGamma();
  }

  @Override
  public T visitTheta() {
    throw new UnsupportedOperationException();
  }

  @Override
  public T visitUltima() {
    throw new UnsupportedOperationException();
  }

  @Override
  public T visitVanna() {
    throw new UnsupportedOperationException();
  }

  @Override
  public T visitVarianceUltima() {
    throw new UnsupportedOperationException();
  }

  @Override
  public T visitVarianceVanna() {

    throw new UnsupportedOperationException();
  }

  @Override
  public T visitVarianceVega() {
    throw new UnsupportedOperationException();
  }

  @Override
  public T visitVarianceVomma() {
    throw new UnsupportedOperationException();
  }

  @Override
  public T visitVega() {
    throw new UnsupportedOperationException();
  }

  @Override
  public T visitVegaBleed() {

    throw new UnsupportedOperationException();
  }

  @Override
  public T visitVegaP() {
    throw new UnsupportedOperationException();
  }

  @Override
  public T visitVomma() {
    throw new UnsupportedOperationException();
  }

  @Override
  public T visitVommaP() {
    throw new UnsupportedOperationException();
  }

  @Override
  public T visitZeta() {
    throw new UnsupportedOperationException();
  }

  @Override
  public T visitZetaBleed() {
    throw new UnsupportedOperationException();
  }

  @Override
  public T visitZomma() {
    throw new UnsupportedOperationException();
  }

  @Override
  public T visitZommaP() {
    throw new UnsupportedOperationException();
  }

}
