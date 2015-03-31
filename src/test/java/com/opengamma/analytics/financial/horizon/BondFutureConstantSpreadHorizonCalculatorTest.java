/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.horizon;

import static com.opengamma.strata.basics.BasicProjectAssertions.assertThat;
import static org.assertj.core.data.Offset.offset;
import static org.testng.Assert.assertEquals;

import java.time.Period;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.instrument.bond.BondFixedSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.BondFuturesSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.BondFuturesTransactionDefinition;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesTransaction;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.legalentity.LegalEntityFilter;
import com.opengamma.analytics.financial.legalentity.LegalEntityShortName;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.provider.calculator.issuer.PresentValueIssuerCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterIssuerProviderInterface;
import com.opengamma.analytics.financial.schedule.NoHolidayCalendar;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.util.time.DateUtils;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.financial.convention.yield.YieldConventionFactory;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.basics.currency.FxMatrix;
import com.opengamma.strata.basics.currency.MultiCurrencyAmount;
import com.opengamma.strata.collect.tuple.Pair;


/**
 * Tests the constant spread horizon calculator for bond futures.
 */
@Test
public class BondFutureConstantSpreadHorizonCalculatorTest {
  /** Currency */
  private static final Currency USD = Currency.USD;
  /** Coupon frequency */
  private static final Period PAYMENT_TENOR = Period.ofMonths(6);
  /** Holiday calendar */
  private static final Calendar CALENDAR = new NoHolidayCalendar();
  /** Issuer name */
  private static final String US_GOVT = "US";
  /** Day-count */
  private static final DayCount DAY_COUNT = DayCounts.ACT_ACT_ICMA;
  /** Business day convention */
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.FOLLOWING;
  /** Is EOM */
  private static final boolean IS_EOM = false;
  /** Settlement days */
  private static final int SETTLEMENT_DAYS = 1;
  /** Bond yield convention */
  private static final YieldConvention YIELD_CONVENTION = YieldConventionFactory.INSTANCE.getYieldConvention("STREET CONVENTION");
  /** Number of bonds in the deliverable basket */
  private static final int NB_BOND = 7;
  /** Tenors of bonds in the deliverable basket */
  private static final ZonedDateTime[] START_ACCRUAL_DATE = new ZonedDateTime[] {DateUtils.getUTCDate(2010, 11, 30), DateUtils.getUTCDate(2010, 12, 31), DateUtils.getUTCDate(2011, 1, 31),
    DateUtils.getUTCDate(2008, 2, 29), DateUtils.getUTCDate(2011, 3, 31), DateUtils.getUTCDate(2011, 4, 30), DateUtils.getUTCDate(2011, 5, 31)};
  /** Deliverable bond coupons */
  private static final double[] RATE = new double[] {0.01375, 0.02125, 0.0200, 0.02125, 0.0225, 0.0200, 0.0175};
  /** Deliverable bond conversion factors */
  private static final double[] CONVERSION_FACTOR = new double[] {.8317, .8565, .8493, .8516, .8540, .8417, .8292};
  /** Deliverable bond maturity dates */
  private static final ZonedDateTime[] MATURITY_DATE = new ZonedDateTime[NB_BOND];
  /** Deliverable bond basket */
  private static final BondFixedSecurityDefinition[] BASKET = new BondFixedSecurityDefinition[NB_BOND];
  /** Bond future last trade date */
  private static final ZonedDateTime LAST_TRADING_DATE = DateUtils.getUTCDate(2011, 9, 30);
  /** Bond future first notice date */
  private static final ZonedDateTime FIRST_NOTICE_DATE = DateUtils.getUTCDate(2011, 8, 31);
  /** Bond future last notice date */
  private static final ZonedDateTime LAST_NOTICE_DATE = DateUtils.getUTCDate(2011, 10, 4);
  /** Notional */
  private static final double NOTIONAL = 100000;
  /** Bond future security */
  private static final BondFuturesSecurityDefinition SECURITY;
  /** Quantity */
  private static final int QUANTITY = 1000;
  /** Reference date */
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 6, 20);
  /** Reference price */
  private static final double REFERENCE_PRICE = 1.0;
  /** Bond future transaction */
  private static final BondFuturesTransactionDefinition TRANSACTION;
  /** Horizon calculation date */
  private static final ZonedDateTime HORIZON_DATE = REFERENCE_DATE.plusDays(7);
  /** Present value calculator */
  private static final InstrumentDerivativeVisitor<ParameterIssuerProviderInterface, MultiCurrencyAmount> PV_CALCULATOR = PresentValueIssuerCalculator.getInstance();
  /** Horizon calculator */
  private static final HorizonCalculator<BondFuturesTransactionDefinition, IssuerProviderInterface, Double> CALCULATOR = BondFutureConstantSpreadHorizonCalculator.getInstance();
  /** Constant issuer and discounting curves */
  private static final IssuerProviderDiscount FLAT_ISSUER_MULTICURVES;
  /** Accuracy */
  private static final double EPS = 1e-9;

  static {
    for (int i = 0; i < NB_BOND; i++) {
      MATURITY_DATE[i] = START_ACCRUAL_DATE[i].plus(Period.ofYears(5));
      BASKET[i] = BondFixedSecurityDefinition.from(USD, MATURITY_DATE[i], START_ACCRUAL_DATE[i], PAYMENT_TENOR, RATE[i], SETTLEMENT_DAYS, CALENDAR,
          DAY_COUNT, BUSINESS_DAY, YIELD_CONVENTION, IS_EOM, US_GOVT);
    }
    SECURITY = new BondFuturesSecurityDefinition(LAST_TRADING_DATE, FIRST_NOTICE_DATE, LAST_NOTICE_DATE,
        NOTIONAL, BASKET, CONVERSION_FACTOR);
    TRANSACTION = new BondFuturesTransactionDefinition(SECURITY, QUANTITY, REFERENCE_DATE, REFERENCE_PRICE);
    final YieldCurve flatIssuer = YieldCurve.from(ConstantDoublesCurve.from(0.02));
    final YieldCurve flatDiscounting = YieldCurve.from(ConstantDoublesCurve.from(0.01));
    final Map<Currency, YieldAndDiscountCurve> discounting = Collections.singletonMap(USD, flatDiscounting);
    final Map<IborIndex, YieldAndDiscountCurve> ibor = Collections.emptyMap();
    final Map<IndexON, YieldAndDiscountCurve> on = Collections.emptyMap();
    final Pair<Object, LegalEntityFilter<LegalEntity>> issuerKey = Pair.of(US_GOVT, new LegalEntityShortName());
    final Map<Pair<Object, LegalEntityFilter<LegalEntity>>, YieldAndDiscountCurve> issuer = Collections.singletonMap(issuerKey, flatIssuer);
    FLAT_ISSUER_MULTICURVES = new IssuerProviderDiscount(discounting, ibor, on, issuer, FxMatrix.EMPTY_FX_MATRIX);
  }

  /**
   * Tests the exception thrown when the bond future is null
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDefinition() {
    CALCULATOR.getTheta(null, HORIZON_DATE, FLAT_ISSUER_MULTICURVES, 1, CALENDAR, 1.01);

  }

  /**
   * Tests the exception thrown when the horizon date is null
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDate() {
    CALCULATOR.getTheta(TRANSACTION, null, FLAT_ISSUER_MULTICURVES, 1, CALENDAR, 1.01);
  }

  /**
   * Tests the exception thrown when the curve data is null
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData() {
    CALCULATOR.getTheta(TRANSACTION, HORIZON_DATE, null, 1, CALENDAR, 1.01);
  }

  /**
   * Tests the exception thrown when the number of days forward is too large
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testDaysForward1() {
    CALCULATOR.getTheta(TRANSACTION, HORIZON_DATE, FLAT_ISSUER_MULTICURVES, 2, CALENDAR, 1.01);
  }

  /**
   * Tests the exception thrown when the number of days backward is too large
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testDaysForward2() {
    CALCULATOR.getTheta(TRANSACTION, HORIZON_DATE, FLAT_ISSUER_MULTICURVES, -2, CALENDAR, 1.01);
  }

  /**
   * Tests the exception thrown when the last margin price is not supplied
   */
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testNoMarginPrice() {
    CALCULATOR.getTheta(TRANSACTION, HORIZON_DATE, FLAT_ISSUER_MULTICURVES, 2, CALENDAR);
  }

  /**
   * Tests the horizon calculation when the bond future price remains constant.
   */
  @Test
  public void testConstantMarginPrice() {
    final BondFuturesTransaction today = TRANSACTION.toDerivative(HORIZON_DATE, REFERENCE_PRICE);
    final BondFuturesTransaction tomorrow = TRANSACTION.toDerivative(HORIZON_DATE.plusDays(1), REFERENCE_PRICE);
    final MultiCurrencyAmount pvToday = today.accept(PV_CALCULATOR, FLAT_ISSUER_MULTICURVES);
    final MultiCurrencyAmount pvTomorrow = tomorrow.accept(PV_CALCULATOR, FLAT_ISSUER_MULTICURVES);
    MultiCurrencyAmount expected = HorizonCalculator.subtract(pvTomorrow, pvToday);
    MultiCurrencyAmount actual = CALCULATOR.getTheta(TRANSACTION, HORIZON_DATE, FLAT_ISSUER_MULTICURVES, 1, CALENDAR, REFERENCE_PRICE);
    assertMCAEquals(expected, actual);
    final BondFuturesTransaction yesterday = TRANSACTION.toDerivative(HORIZON_DATE.minusDays(1), REFERENCE_PRICE);
    final MultiCurrencyAmount pvYesterday = yesterday.accept(PV_CALCULATOR, FLAT_ISSUER_MULTICURVES);
    expected = HorizonCalculator.subtract(pvYesterday, pvToday);
    actual = CALCULATOR.getTheta(TRANSACTION, HORIZON_DATE, FLAT_ISSUER_MULTICURVES, -1, CALENDAR, REFERENCE_PRICE);
    assertMCAEquals(expected, actual);
  }

  /**
   * Checks multiple currency amounts for equality to within a specific accuracy.
   * @param expected The expected object
   * @param actual The actual object
   */
  private void assertMCAEquals(final MultiCurrencyAmount expected, final MultiCurrencyAmount actual) {
    assertEquals(expected.getCurrencies(), actual.getCurrencies());
    actual.stream()
        .forEach(ca -> assertThat(ca).isEqualTo(expected.getAmount(ca.getCurrency()), offset(EPS)));
  }
}
