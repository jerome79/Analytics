/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.isdastandardmodel;

import static com.opengamma.analytics.convention.businessday.BusinessDayDateUtils.addWorkDays;
import static org.testng.AssertJUnit.assertEquals;

import java.time.LocalDate;
import java.time.Period;
import java.util.Arrays;

import org.testng.annotations.Test;

import com.opengamma.analytics.convention.daycount.DayCount;
import com.opengamma.analytics.convention.daycount.DayCounts;
import com.opengamma.strata.basics.date.HolidayCalendar;
import com.opengamma.strata.basics.date.HolidayCalendars;

/**
 * Test.
 */
@Test
public class ISDACompliantCreditCurveCalibratorTest {

  private static final HolidayCalendar DEFAULT_CALENDAR = HolidayCalendars.SAT_SUN;

  private static final AnalyticCDSPricer PRICER = new AnalyticCDSPricer();
  private static final ISDACompliantPresentValueCreditDefaultSwap TEST_PRICER = new ISDACompliantPresentValueCreditDefaultSwap();

  private static final LocalDate TODAY = LocalDate.of(2013, 4, 21);
  private static final LocalDate BASE_DATE = TODAY;

  private static final LocalDate[] YC_DATES = new LocalDate[] {LocalDate.of(2013, 6, 27), LocalDate.of(2013, 8, 27), LocalDate.of(2013, 11, 27), LocalDate.of(2014, 5, 27), LocalDate.of(2015, 5, 27),
    LocalDate.of(2016, 5, 27), LocalDate.of(2018, 5, 27), LocalDate.of(2020, 5, 27), LocalDate.of(2023, 5, 27), LocalDate.of(2028, 5, 27), LocalDate.of(2033, 5, 27), LocalDate.of(2043, 5, 27) };
  private static final double[] YC_RATES;
  private static final double[] DISCOUNT_FACT;
  private static final double[] YC_TIMES;
  private static final ISDACompliantDateYieldCurve YIELD_CURVE;
  private static final DayCount ACT365 = DayCounts.ACT_365F;

  static {
    final int ycPoints = YC_DATES.length;
    YC_RATES = new double[ycPoints];
    DISCOUNT_FACT = new double[ycPoints];
    Arrays.fill(DISCOUNT_FACT, 1.0);
    YC_TIMES = new double[ycPoints];
    for (int i = 0; i < ycPoints; i++) {
      YC_TIMES[i] = ACT365.yearFraction(BASE_DATE, YC_DATES[i]);
    }
    YIELD_CURVE = new ISDACompliantDateYieldCurve(BASE_DATE, YC_DATES, YC_RATES);
  }

  @SuppressWarnings({"deprecation" })
  public void test() {

    final LocalDate today = LocalDate.of(2013, 2, 2);
    final LocalDate stepinDate = today.plusDays(1); // aka effective date
    final LocalDate valueDate = addWorkDays(today, 3, DEFAULT_CALENDAR); // 3 working days on
    final LocalDate startDate = LocalDate.of(2012, 7, 29);
    final LocalDate[] endDates = new LocalDate[] {LocalDate.of(2013, 6, 20), LocalDate.of(2013, 9, 20), LocalDate.of(2014, 3, 20), LocalDate.of(2015, 3, 20), LocalDate.of(2016, 3, 20),
      LocalDate.of(2018, 3, 20), LocalDate.of(2023, 3, 20) };

    final double[] coupons = new double[] {50, 70, 100, 150, 200, 400, 1000 };
    final int n = coupons.length;
    for (int i = 0; i < n; i++) {
      coupons[i] /= 10000;
    }

    final Period tenor = Period.ofMonths(3);
    final StubType stubType = StubType.FRONTSHORT;
    final boolean payAccOndefault = true;
    final boolean protectionStart = true;
    final double recovery = 0.4;

    final SimpleCreditCurveBuilder calibrator = new SimpleCreditCurveBuilder();
    final ISDACompliantCreditCurve hc = calibrator.calibrateCreditCurve(today, stepinDate, valueDate, startDate, endDates, coupons, payAccOndefault, tenor, stubType, protectionStart, YIELD_CURVE,
        recovery);

    final ISDACompliantDateCreditCurve hcDate = new ISDACompliantDateCreditCurve(today, endDates, hc.getKnotZeroRates());

    final CDSAnalytic[] cds = new CDSAnalytic[n];
    for (int i = 0; i < n; i++) {
      cds[i] = new CDSAnalytic(today, stepinDate, valueDate, startDate, endDates[i], payAccOndefault, tenor, stubType, protectionStart, recovery);
      final double pv = 1e7 * PRICER.pv(cds[i], YIELD_CURVE, hc, coupons[i]);
      assertEquals(0.0, pv, 1e-8); // on a notional of 1e7

      // test against 'old' pricer as well
      final double rpv01 = TEST_PRICER.pvPremiumLegPerUnitSpread(today, stepinDate, valueDate, startDate, endDates[i], payAccOndefault, tenor, stubType, YIELD_CURVE, hcDate, protectionStart,
          PriceType.CLEAN);
      final double proLeg = TEST_PRICER.calculateProtectionLeg(today, stepinDate, valueDate, startDate, endDates[i], YIELD_CURVE, hcDate, recovery, protectionStart);
      final double pv2 = 1e7 * (proLeg - coupons[i] * rpv01);
      assertEquals(0.0, pv2, 1e-7); // we drop a slight bit of accuracy here
    }

  }
}
