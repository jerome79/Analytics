package com.opengamma.analytics.financial.interestrate.payments.provider;

import static org.testng.AssertJUnit.assertEquals;

import java.time.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.analytics.convention.daycount.DayCount;
import com.opengamma.analytics.convention.daycount.DayCountUtils;
import com.opengamma.analytics.convention.daycount.DayCounts;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.financial.util.AssertSensitivityObjects;
import com.opengamma.analytics.util.time.DateUtils;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.basics.currency.MultiCurrencyAmount;

/**
 * Test.
 */
@Test
public class CouponFixedDiscountingProviderMethodTest {

  private static final IborIndex[] IBOR_INDEXES = MulticurveProviderDiscountDataSets.getIndexesIborMulticurveEurUsd();
  private static final IborIndex EURIBOR3M = IBOR_INDEXES[0];
  private static final Currency EUR = EURIBOR3M.getCurrency();

  private static final DayCount DAY_COUNT_COUPON = DayCounts.ACT_365F;
  private static final ZonedDateTime ACCRUAL_START_DATE = DateUtils.getUTCDate(2011, 5, 23);
  private static final ZonedDateTime ACCRUAL_END_DATE = DateUtils.getUTCDate(2011, 8, 22);
  private static final double ACCRUAL_FACTOR = DayCountUtils.yearFraction(DAY_COUNT_COUPON, ACCRUAL_START_DATE, ACCRUAL_END_DATE);
  private static final double NOTIONAL = 100000000; //1m
  private static final double FIXED_RATE = 0.02;
  private static final CouponFixedDefinition CPN_FIXED_DEFINITION = new CouponFixedDefinition(EUR, ACCRUAL_END_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, FIXED_RATE);

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2010, 12, 27);
  private static final CouponFixed CPN_FIXED = CPN_FIXED_DEFINITION.toDerivative(REFERENCE_DATE);

  private static final MulticurveProviderDiscount PROVIDER = MulticurveProviderDiscountDataSets.createMulticurveEurUsd();

  private static final CouponFixedDiscountingMethod METHOD_CPN_FIXED = CouponFixedDiscountingMethod.getInstance();
  private static final PresentValueDiscountingCalculator PVC = PresentValueDiscountingCalculator.getInstance();
  private static final PresentValueCurveSensitivityDiscountingCalculator PVCSC = PresentValueCurveSensitivityDiscountingCalculator.getInstance();

  private static final double TOLERANCE_PV = 1.0E-2;
  private static final double TOLERANCE_DELTA = 1.0E+2;

  @Test
  public void presentValue() {
    final MultiCurrencyAmount pvComputed = METHOD_CPN_FIXED.presentValue(CPN_FIXED, PROVIDER);
    final double df = PROVIDER.getDiscountFactor(EUR, CPN_FIXED.getPaymentTime());
    final double pvExpected = CPN_FIXED.getAmount() * df;
    assertEquals("CouponFixedDiscountingMarketMethod: present value", pvExpected, pvComputed.getAmount(EUR).getAmount(), TOLERANCE_PV);
  }

  @Test
  public void presentValueMethodVsCalculator() {
    final MultiCurrencyAmount pvMethod = METHOD_CPN_FIXED.presentValue(CPN_FIXED, PROVIDER);
    final MultiCurrencyAmount pvCalculator = CPN_FIXED.accept(PVC, PROVIDER);
    assertEquals("CouponFixedDiscountingMarketMethod: present value", pvMethod.getAmount(EUR).getAmount(), pvCalculator.getAmount(EUR).getAmount(), TOLERANCE_PV);
  }

  // Testing note: the presentValueMarketSensitivity is tested in ParameterSensitivityProviderCalculatorTest

  @Test
  public void presentValueMarketSensitivityMethodVsCalculator() {
    final MultipleCurrencyMulticurveSensitivity pvcsMethod = METHOD_CPN_FIXED.presentValueCurveSensitivity(CPN_FIXED, PROVIDER);
    final MultipleCurrencyMulticurveSensitivity pvcsCalculator = CPN_FIXED.accept(PVCSC, PROVIDER);
    AssertSensitivityObjects.assertEquals("CouponFixedDiscountingMarketMethod: presentValueMarketSensitivity", pvcsMethod, pvcsCalculator, TOLERANCE_DELTA);
  }

}
