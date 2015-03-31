/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.inflationissuer;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorDelegate;
import com.opengamma.analytics.financial.interestrate.bond.definition.BillTransaction;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondCapitalIndexedSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondCapitalIndexedTransaction;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedTransaction;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondIborTransaction;
import com.opengamma.analytics.financial.interestrate.bond.provider.BillTransactionDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.bond.provider.BondCapitalIndexedSecurityDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.bond.provider.BondCapitalIndexedTransactionDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.bond.provider.BondTransactionDiscountingMethod;
import com.opengamma.analytics.financial.provider.calculator.inflation.InflationIssuerProviderAdapter;
import com.opengamma.analytics.financial.provider.calculator.inflation.PresentValueDiscountingInflationCalculator;
import com.opengamma.analytics.financial.provider.description.inflation.InflationIssuerProviderInterface;
import com.opengamma.analytics.financial.provider.description.inflation.ParameterInflationIssuerProviderInterface;
import com.opengamma.strata.basics.currency.MultiCurrencyAmount;

/**
 * Calculates the present value of an inflation instruments by discounting for a given MarketBundle
 */
public final class PresentValueDiscountingInflationIssuerCalculator 
  extends InstrumentDerivativeVisitorDelegate<ParameterInflationIssuerProviderInterface, MultiCurrencyAmount> {

  /**
   * The unique instance of the calculator.
   */
  private static final PresentValueDiscountingInflationIssuerCalculator INSTANCE = new PresentValueDiscountingInflationIssuerCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static PresentValueDiscountingInflationIssuerCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private PresentValueDiscountingInflationIssuerCalculator() {
    super(new InflationIssuerProviderAdapter<>(PresentValueDiscountingInflationCalculator.getInstance()));
  }

  /**
   * Pricing methods.
   */
  private static final BondCapitalIndexedSecurityDiscountingMethod METHOD_BOND_INFL_SEC = 
      new BondCapitalIndexedSecurityDiscountingMethod();
  private static final BondCapitalIndexedTransactionDiscountingMethod METHOD_BOND_INFL_TR = 
      new BondCapitalIndexedTransactionDiscountingMethod();
  private static final BillTransactionDiscountingMethod METHOD_BILL_TR = 
      BillTransactionDiscountingMethod.getInstance();
  private static final BondTransactionDiscountingMethod METHOD_BOND_TR = 
      BondTransactionDiscountingMethod.getInstance();

  @Override
  public MultiCurrencyAmount visitBondCapitalIndexedSecurity(final BondCapitalIndexedSecurity<?> bond,
      final ParameterInflationIssuerProviderInterface market) {
    if (!(market instanceof InflationIssuerProviderInterface)) {
      throw new UnsupportedOperationException(
          "visitBondCapitalIndexedSecurity does not support data not a InflationIssuerProviderInterface");
    }
    InflationIssuerProviderInterface inflationIssuer = (InflationIssuerProviderInterface) market;
    return METHOD_BOND_INFL_SEC.presentValue(bond, inflationIssuer);
  }

  @Override
  public MultiCurrencyAmount visitBondCapitalIndexedTransaction(final BondCapitalIndexedTransaction<?> bond,
      final ParameterInflationIssuerProviderInterface market) {
    if (!(market instanceof InflationIssuerProviderInterface)) {
      throw new UnsupportedOperationException(
          "visitBondCapitalIndexedSecurity does not support data not a InflationIssuerProviderInterface");
    }
    InflationIssuerProviderInterface inflationIssuer = (InflationIssuerProviderInterface) market;
    return METHOD_BOND_INFL_TR.presentValue(bond, inflationIssuer);
  }

  //     -----     Bond/Bill     -----

  @Override
  public MultiCurrencyAmount visitBillTransaction(final BillTransaction bill,
      final ParameterInflationIssuerProviderInterface market) {
    return METHOD_BILL_TR.presentValue(bill, market.getIssuerProvider());
  }

  @Override
  public MultiCurrencyAmount visitBondFixedTransaction(final BondFixedTransaction bond,
      final ParameterInflationIssuerProviderInterface market) {
    return METHOD_BOND_TR.presentValue(bond, market.getIssuerProvider());
  }

  @Override
  public MultiCurrencyAmount visitBondIborTransaction(final BondIborTransaction bond,
      final ParameterInflationIssuerProviderInterface market) {
    return METHOD_BOND_TR.presentValue(bond, market.getIssuerProvider());
  }

}
