/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.collateralmodel;

import com.opengamma.strata.collect.ArgChecker;

/**
 * Class to define the characteristics of a collateral agreement between two counterparties (e.g. between a CCP and a GCM)
 */
public class CollateralDefinition {

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // TODO : Work - in - Progress

  // TODO : Add hashcode and equals methods
  // TODO : Will probably require a builder method to dynamically set the collateral amount

  // NOTE : We enforce the collateral amount called to be greater than zero. Therefore the calling code must have
  // NOTE : logic to ensure that the collateral 'flows' in the correct direction between counterparties

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // The time-varying amount of the collateral
  private final double _collateralAmount;

  // The amount of collateral to post irrespective of anything else
  private final double _independentAmount;

  // Calls below this amount will not result in additional collateral being posted
  private final double _minimumTransferAmount;

  // The exposure threshold that must be breached before collateral is called for
  private final double _collateralTriggerThreshold;

  // The minimum frequency with which a counterparty can call for collateral (e.g. can't be any more frequent than daily due to operational limitations)
  private final MarginCallFrequency _minimumMarginCallGrequency;

  // ----------------------------------------------------------------------------------------------------------------------------------------

  public CollateralDefinition(
      final double collateralAmount,
      final double independentAmount,
      final double minimumTransferAmount,
      final double collateralTriggerThreshold,
      final MarginCallFrequency minimumMarginCallGrequency) {

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Check the validity of the input arguments

    ArgChecker.notNegative(collateralAmount, "Collateral amount");
    ArgChecker.notNegative(independentAmount, "Independent amount");
    ArgChecker.notNegative(minimumTransferAmount, "Minimum transfer amount");
    ArgChecker.notNegative(collateralTriggerThreshold, "Collateral trigger threshold");

    ArgChecker.notNull(minimumMarginCallGrequency, "Minimum margin call frequency");

    // Verify that the collateral called is greater than the minimum transfer amount
    ArgChecker.notNegative(collateralAmount - minimumTransferAmount, "Minimum transfer amount");

    // ----------------------------------------------------------------------------------------------------------------------------------------

    _collateralAmount = collateralAmount;
    _independentAmount = independentAmount;
    _minimumTransferAmount = minimumTransferAmount;
    _collateralTriggerThreshold = collateralTriggerThreshold;

    _minimumMarginCallGrequency = minimumMarginCallGrequency;

    // ----------------------------------------------------------------------------------------------------------------------------------------
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  public double getCollateralAmount() {
    return _collateralAmount;
  }

  public double getIndependentAmount() {
    return _independentAmount;
  }

  public double getMinimumTransferAmount() {
    return _minimumTransferAmount;
  }

  public double getCollateralTriggerThreshold() {
    return _collateralTriggerThreshold;
  }

  public MarginCallFrequency getMinimumMarginCallFrequency() {
    return _minimumMarginCallGrequency;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Add builder method here

  // ----------------------------------------------------------------------------------------------------------------------------------------
}
