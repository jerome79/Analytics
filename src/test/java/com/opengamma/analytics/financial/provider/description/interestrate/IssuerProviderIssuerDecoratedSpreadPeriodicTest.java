/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description.interestrate;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.legalentity.LegalEntityFilter;
import com.opengamma.analytics.financial.legalentity.LegalEntityShortName;
import com.opengamma.analytics.financial.model.interestrate.curve.DiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldPeriodicCurve;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.ForwardSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.SimplyCompoundedForwardSensitivity;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolator;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.collect.tuple.DoublesPair;
import com.opengamma.strata.collect.tuple.Pair;


/**
 * Test {@link IssuerProviderIssuerDecoratedSpreadPeriodicTest}
 */
@Test
public class IssuerProviderIssuerDecoratedSpreadPeriodicTest {

  /* Building curves */
  private static final CombinedInterpolatorExtrapolator INTERPOLATOR = new CombinedInterpolatorExtrapolator(
      Interpolator1DFactory.LINEAR_INSTANCE, Interpolator1DFactory.FLAT_EXTRAPOLATOR_INSTANCE,
      Interpolator1DFactory.FLAT_EXTRAPOLATOR_INSTANCE);
  private static final YieldAndDiscountCurve YIELD_CURVE_DSC;
  private static final YieldAndDiscountCurve YIELD_CURVE_YLD;
  private static final YieldAndDiscountCurve YIELD_CURVE_PRD;
  private static final IssuerProviderDiscount ISSUER_PROVIDER = new IssuerProviderDiscount();
  private static final String CURVE_NAME_DSC = "testIssuerCurve1";
  private static final Double[] TIME_DSC = new Double[] {0.1, 0.5, 1.0, 2.0, 5.0, 10.0 };
  private static final Double[] FACTOR_DSC = new Double[] {0.99, 0.98, 0.975, 0.96, 0.9, 0.81 };
  private static final String ISSUER_NAME_DSC = "testIssuer1";
  static {
    InterpolatedDoublesCurve interpolatedCurve = InterpolatedDoublesCurve.fromSorted(TIME_DSC, FACTOR_DSC,
        INTERPOLATOR, CURVE_NAME_DSC);
    YIELD_CURVE_DSC = new DiscountCurve(CURVE_NAME_DSC, interpolatedCurve);
    LegalEntityFilter<LegalEntity> filter = new LegalEntityShortName();
    ISSUER_PROVIDER.setCurve(Pair.of((Object) ISSUER_NAME_DSC, filter), YIELD_CURVE_DSC);
  }
  private static final String CURVE_NAME_YLD = "testIssuerCurve2";
  private static final Double[] TIME_YLD = new Double[] {0.1, 0.5, 1.0, 2.0, 5.0, 7.0, 10.0, 15.0 };
  private static final Double[] RATE_YLD = new Double[] {0.01, 0.015, 0.015, 0.02, 0.02, 0.025, 0.0125, 0.03 };
  private static final String ISSUER_NAME_YLD = "testIssuer2";
  static {
    InterpolatedDoublesCurve interpolatedCurve = InterpolatedDoublesCurve.fromSorted(TIME_YLD, RATE_YLD,
        INTERPOLATOR, CURVE_NAME_YLD);
    YIELD_CURVE_YLD = new YieldCurve(CURVE_NAME_YLD, interpolatedCurve);
    LegalEntityFilter<LegalEntity> filter = new LegalEntityShortName();
    ISSUER_PROVIDER.setCurve(Pair.of((Object) ISSUER_NAME_YLD, filter), YIELD_CURVE_YLD);
  }
  private static final String CURVE_NAME_PRD = "testPRDcountCurve";
  private static final Double[] TIME_PRD = new Double[] {0.25, 0.5, 1.0, 3.0, 5.0, 10.0 };
  private static final Double[] RATE_PRD = new Double[] {0.02, 0.02, 0.025, 0.03, 0.03, 0.035 };
  private static final Currency USD = Currency.USD;
  static {
    InterpolatedDoublesCurve interpolatedCurve = InterpolatedDoublesCurve.fromSorted(TIME_PRD, RATE_PRD,
        INTERPOLATOR, CURVE_NAME_PRD);
    YIELD_CURVE_PRD = YieldPeriodicCurve.from(2, interpolatedCurve);
    ISSUER_PROVIDER.setCurve(USD, YIELD_CURVE_PRD);
  }

  /* Variables used for testing */
  private static final List<ForwardSensitivity> FWD_SENSITIVITY_LIST = new ArrayList<>();
  static {
    ForwardSensitivity fwdSense = new SimplyCompoundedForwardSensitivity(0.75, 1.0, 0.25, 124.0);
    FWD_SENSITIVITY_LIST.add(fwdSense);
  }
  private static final List<DoublesPair> POINT_SENSITIVITY_LIST = new ArrayList<>();
  static {
    DoublesPair pair = DoublesPair.of(0.4, 1.0);
    POINT_SENSITIVITY_LIST.add(pair);
  }
  private static final LegalEntity ISSUER_DSC = new LegalEntity(null, ISSUER_NAME_DSC, null, null, null);
  private static final LegalEntity ISSUER_YLD = new LegalEntity(null, ISSUER_NAME_YLD, null, null, null);
  private static final double[] KEYS = new double[] {-1.0, 0.0, 1.35, 12.5, 30.0 };
  private static final double NUM_KEYS = KEYS.length;

  private static final double TOL = 1.0e-12;

  /**
   * add spread
   */
  @Test
  public void spreadTest() {
    double sampleSpread1 = 0.05;
    double sampleSpread2 = 0.025;
    IssuerProviderIssuerDecoratedSpreadPeriodic providerWithSpread1 = new IssuerProviderIssuerDecoratedSpreadPeriodic(
        ISSUER_PROVIDER, ISSUER_DSC, sampleSpread1, 1);
    IssuerProviderIssuerDecoratedSpreadPeriodic providerWithSpread2 = new IssuerProviderIssuerDecoratedSpreadPeriodic(
        providerWithSpread1.getIssuerProvider(), ISSUER_YLD, sampleSpread2, 2);

    for (int i = 0; i < NUM_KEYS; ++i) {
      double zeroEquiv1 = Math.pow(ISSUER_PROVIDER.getDiscountFactor(ISSUER_DSC, KEYS[i]), -1.0 / KEYS[i]) - 1.0 +
          sampleSpread1;
      double disExpected1 = Math.pow(1.0 + zeroEquiv1, -KEYS[i]);
      double disComputed1 = providerWithSpread2.getDiscountFactor(ISSUER_DSC, KEYS[i]);
      assertEquals("accesserTest", disExpected1, disComputed1, TOL);

      double zeroEquiv2 = 2.0 * Math.pow(ISSUER_PROVIDER.getDiscountFactor(ISSUER_YLD, KEYS[i]), -1.0 / 2.0 / KEYS[i]) -
          2.0 + sampleSpread2;
      double disExpected2 = Math.pow(1.0 + zeroEquiv2 / 2.0, -2.0 * KEYS[i]);
      double disComputed2 = providerWithSpread2.getDiscountFactor(ISSUER_YLD, KEYS[i]);
      assertEquals("spreadTest", disExpected2, disComputed2, TOL);
      double disExpected3 = ISSUER_PROVIDER.getMulticurveProvider().getDiscountFactor(USD, KEYS[i]);
      double disComputed3 = providerWithSpread2.getMulticurveProvider().getDiscountFactor(USD, KEYS[i]);
      assertEquals("spreadTest", disExpected3, disComputed3, TOL);
    }

    assertTrue(providerWithSpread2.getName(ISSUER_DSC).equals(ISSUER_PROVIDER.getName(ISSUER_DSC)));
    assertTrue(providerWithSpread2.getName(ISSUER_YLD).equals(ISSUER_PROVIDER.getName(ISSUER_YLD)));
    Set<Pair<Object, LegalEntityFilter<LegalEntity>>> issuers = providerWithSpread2.getIssuers();
    assertTrue(issuers.equals(ISSUER_PROVIDER.getIssuers()));
    assertTrue(providerWithSpread2.getAllNames().equals(ISSUER_PROVIDER.getAllNames()));
    assertTrue(providerWithSpread2.getAllCurveNames().equals(ISSUER_PROVIDER.getAllCurveNames()));
    assertEquals("spreadTest", providerWithSpread2.getNumberOfParameters(CURVE_NAME_DSC),
        ISSUER_PROVIDER.getNumberOfParameters(CURVE_NAME_DSC));
    assertEquals("spreadTest", providerWithSpread2.getNumberOfParameters(CURVE_NAME_YLD),
        ISSUER_PROVIDER.getNumberOfParameters(CURVE_NAME_YLD));
    assertEquals("spreadTest", providerWithSpread2.getNumberOfParameters(CURVE_NAME_PRD),
        ISSUER_PROVIDER.getNumberOfParameters(CURVE_NAME_PRD));
    
    assertEquals("spreadTest", new ArrayList<>(), providerWithSpread2.getUnderlyingCurvesNames(CURVE_NAME_DSC));
    assertEquals("spreadTest", new ArrayList<>(), providerWithSpread1.getUnderlyingCurvesNames(CURVE_NAME_YLD));

    LegalEntityFilter<LegalEntity> filter = new LegalEntityShortName();
    assertEquals("accesserTest", CURVE_NAME_DSC,
        providerWithSpread2.getName(Pair.of((Object) ISSUER_NAME_DSC, filter)));
    assertEquals("accesserTest", CURVE_NAME_YLD,
        providerWithSpread2.getName(Pair.of((Object) ISSUER_NAME_YLD, filter)));
    
    IssuerProviderIssuerDecoratedSpreadPeriodic providerWithTinySpread = new IssuerProviderIssuerDecoratedSpreadPeriodic(
        ISSUER_PROVIDER, ISSUER_DSC, TOL, 1);

    assertArrayRelative("accesserTest",
        ISSUER_PROVIDER.parameterForwardSensitivity(CURVE_NAME_DSC, FWD_SENSITIVITY_LIST),
        providerWithTinySpread.parameterForwardSensitivity(CURVE_NAME_DSC, FWD_SENSITIVITY_LIST), TOL);
    assertArrayRelative("accesserTest",
        ISSUER_PROVIDER.parameterForwardSensitivity(CURVE_NAME_YLD, FWD_SENSITIVITY_LIST),
        providerWithTinySpread.parameterForwardSensitivity(CURVE_NAME_YLD, FWD_SENSITIVITY_LIST), TOL);
    assertArrayRelative("accesserTest",
        ISSUER_PROVIDER.parameterSensitivity(CURVE_NAME_DSC, POINT_SENSITIVITY_LIST),
        providerWithTinySpread.parameterSensitivity(CURVE_NAME_DSC, POINT_SENSITIVITY_LIST), TOL * 10.);
    assertArrayRelative("accesserTest",
        ISSUER_PROVIDER.parameterSensitivity(CURVE_NAME_YLD, POINT_SENSITIVITY_LIST),
        providerWithTinySpread.parameterSensitivity(CURVE_NAME_YLD, POINT_SENSITIVITY_LIST), TOL * 10.);
  }

  /**
   * sensitivity test against finite difference approximation
   */
  @Test
  public void sensitivityFiniteDifferenceTest() {
    double eps = 1.0e-7;
    double spreadSemiannual = -0.02;
    int nPeriodS = 2;
    double spreadQuarterly = 0.06;
    int nPeriodQ = 4;
    IssuerProviderIssuerDecoratedSpreadPeriodic providerWithSpread = new IssuerProviderIssuerDecoratedSpreadPeriodic(
        ISSUER_PROVIDER, ISSUER_DSC, spreadSemiannual, nPeriodS);
    IssuerProviderIssuerDecoratedSpreadPeriodic providerWithSpreadDouble = new IssuerProviderIssuerDecoratedSpreadPeriodic(
        providerWithSpread, ISSUER_YLD, spreadQuarterly, nPeriodQ);
    // discount curve
    {
      double[] senseComputed = providerWithSpreadDouble.parameterSensitivity(CURVE_NAME_DSC, POINT_SENSITIVITY_LIST);
      int nParams = FACTOR_DSC.length;
      double[] senseExpected = new double[nParams];
      for (int i = 0; i < nParams; ++i) {
        LegalEntityFilter<LegalEntity> filter = new LegalEntityShortName();
        IssuerProviderDiscount issuerDiscountUp = new IssuerProviderDiscount();
        IssuerProviderDiscount issuerDiscountDw = new IssuerProviderDiscount();
        Double[] pramsUp = Arrays.copyOf(FACTOR_DSC, nParams);
        Double[] pramsDw = Arrays.copyOf(FACTOR_DSC, nParams);
        pramsUp[i] += eps;
        pramsDw[i] -= eps;
        InterpolatedDoublesCurve interpolatedCurveUp = InterpolatedDoublesCurve.fromSorted(TIME_DSC, pramsUp,
            INTERPOLATOR, CURVE_NAME_DSC);
        DiscountCurve discountCurveUp = new DiscountCurve(CURVE_NAME_DSC, interpolatedCurveUp);
        issuerDiscountUp.setCurve(Pair.of((Object) ISSUER_NAME_DSC, filter), discountCurveUp);
        double up = issuerDiscountUp.getIssuerCurve(CURVE_NAME_DSC).getInterestRate(
            POINT_SENSITIVITY_LIST.get(0).getFirst());
        up = addShiftToPrdCmpRates(up, spreadSemiannual, nPeriodS);
        InterpolatedDoublesCurve interpolatedCurveDw = InterpolatedDoublesCurve.fromSorted(TIME_DSC, pramsDw,
            INTERPOLATOR, CURVE_NAME_DSC);
        DiscountCurve discountCurveDw = new DiscountCurve(CURVE_NAME_DSC, interpolatedCurveDw);
        issuerDiscountDw.setCurve(Pair.of((Object) ISSUER_NAME_DSC, filter), discountCurveDw);
        double dw = issuerDiscountDw.getIssuerCurve(CURVE_NAME_DSC).getInterestRate(
            POINT_SENSITIVITY_LIST.get(0).getFirst());
        dw = addShiftToPrdCmpRates(dw, spreadSemiannual, nPeriodS);
        senseExpected[i] = 0.5 * (up - dw) * POINT_SENSITIVITY_LIST.get(0).getSecond() / eps;
      }
      assertArrayRelative("sensitivityFiniteDifferenceDiscountTest", senseExpected, senseComputed, eps);
    }
    // yield curve
    {
      double[] senseComputed = providerWithSpreadDouble.parameterSensitivity(CURVE_NAME_YLD, POINT_SENSITIVITY_LIST);
      int nParams = RATE_YLD.length;
      double[] senseExpected = new double[nParams];
      for (int i = 0; i < nParams; ++i) {
        LegalEntityFilter<LegalEntity> filter = new LegalEntityShortName();
        IssuerProviderDiscount issuerDiscountUp = new IssuerProviderDiscount();
        IssuerProviderDiscount issuerDiscountDw = new IssuerProviderDiscount();
        Double[] pramsUp = Arrays.copyOf(RATE_YLD, nParams);
        Double[] pramsDw = Arrays.copyOf(RATE_YLD, nParams);
        pramsUp[i] += eps;
        pramsDw[i] -= eps;
        InterpolatedDoublesCurve interpolatedCurveUp = InterpolatedDoublesCurve.fromSorted(TIME_YLD, pramsUp,
            INTERPOLATOR, CURVE_NAME_YLD);
        YieldCurve discountCurveUp = new YieldCurve(CURVE_NAME_YLD, interpolatedCurveUp);
        issuerDiscountUp.setCurve(Pair.of((Object) ISSUER_NAME_YLD, filter), discountCurveUp);
        double up = issuerDiscountUp.getIssuerCurve(CURVE_NAME_YLD).getInterestRate(
            POINT_SENSITIVITY_LIST.get(0).getFirst());
        up = addShiftToPrdCmpRates(up, spreadQuarterly, nPeriodQ);
        InterpolatedDoublesCurve interpolatedCurveDw = InterpolatedDoublesCurve.fromSorted(TIME_YLD, pramsDw,
            INTERPOLATOR, CURVE_NAME_YLD);
        YieldCurve discountCurveDw = new YieldCurve(CURVE_NAME_YLD, interpolatedCurveDw);
        issuerDiscountDw.setCurve(Pair.of((Object) ISSUER_NAME_YLD, filter), discountCurveDw);
        double dw = issuerDiscountDw.getIssuerCurve(CURVE_NAME_YLD).getInterestRate(
            POINT_SENSITIVITY_LIST.get(0).getFirst());
        dw = addShiftToPrdCmpRates(dw, spreadQuarterly, nPeriodQ);
        senseExpected[i] = 0.5 * (up - dw) * POINT_SENSITIVITY_LIST.get(0).getSecond() / eps;
      }
      assertArrayRelative("sensitivityFiniteDifferenceDiscountTest", senseExpected, senseComputed, eps);
    }
    // currency-based curve is not affected
    assertArrayRelative("sensitivityFiniteDifferenceDiscountTest",
        ISSUER_PROVIDER.parameterSensitivity(CURVE_NAME_PRD, POINT_SENSITIVITY_LIST),
        providerWithSpreadDouble.parameterSensitivity(CURVE_NAME_PRD, POINT_SENSITIVITY_LIST), TOL);
  }


  private double addShiftToPrdCmpRates(double interstRate, double spread, int nPeriod) {
    return nPeriod * Math.log(Math.exp(interstRate / nPeriod) + spread / nPeriod);
  }

  /**
   * copy is not supported, although underlying IssuerProviderDiscount may support copy
   */
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void copyFailTest() {
    IssuerProviderIssuerDecoratedSpreadPeriodic wrapper = new IssuerProviderIssuerDecoratedSpreadPeriodic(
        ISSUER_PROVIDER, ISSUER_DSC, 0.1, 1);
    wrapper.copy();
  }

  private static void assertArrayRelative(String message, double[] expected, double[] obtained, double relativeTol) {
    int nData = expected.length;
    assertEquals(message, nData, obtained.length);
    for (int i = 0; i < nData; ++i) {
      assertRelative(message, expected[i], obtained[i], relativeTol);
    }
  }

  private static void assertRelative(String message, double expected, double obtained, double relativeTol) {
    double ref = Math.max(Math.abs(expected), 1.0);
    assertEquals(message, expected, obtained, ref * relativeTol);
  }
}
