/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.forex.conversion;

import java.util.HashMap;
import java.util.Map;

import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.basics.currency.MultiCurrencyAmount;
import com.opengamma.strata.collect.ArgChecker;

/**
 * 
 */
public final class ForexDomesticPipsToPresentValueConverter {

  public static MultiCurrencyAmount convertDomesticPipsToFXPresentValue(final double domesticPipsPV, final double spotFX, final Currency putCurrency, final Currency callCurrency,
      final double putAmount, final double callAmount) {
    ArgChecker.isTrue(domesticPipsPV >= 0.0, "Negative price given");
    ArgChecker.isTrue(spotFX > 0.0, "Spot rate must be greater than zero. value gvien is {}", spotFX);
    ArgChecker.notNull(putCurrency, "put currency");
    ArgChecker.notNull(callCurrency, "call currency");
    final Map<Currency, Double> amountMap = new HashMap<>();
    final double putPV = putAmount * domesticPipsPV;
    final double callPV = callAmount * domesticPipsPV / spotFX;
    amountMap.put(callCurrency, callPV);
    amountMap.put(putCurrency, putPV);
    return MultiCurrencyAmount.of(amountMap);
  }
}
