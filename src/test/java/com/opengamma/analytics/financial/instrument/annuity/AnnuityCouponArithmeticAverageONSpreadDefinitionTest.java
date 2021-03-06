package com.opengamma.analytics.financial.instrument.annuity;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.time.Period;
import java.time.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.instrument.index.IndexONMaster;
import com.opengamma.analytics.financial.instrument.payment.CouponONArithmeticAverageSpreadDefinition;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.util.time.DateUtils;
import com.opengamma.strata.basics.date.BusinessDayConvention;
import com.opengamma.strata.basics.date.BusinessDayConventions;
import com.opengamma.strata.basics.date.HolidayCalendar;
import com.opengamma.strata.basics.date.HolidayCalendars;
import com.opengamma.strata.basics.schedule.StubConvention;

/**
 * Test.
 */
@Test
public class AnnuityCouponArithmeticAverageONSpreadDefinitionTest {

  private static final IndexON FEDFUND = IndexONMaster.getInstance().getIndex("FED FUND");
  private static final ZonedDateTime EFFECTIVE_DATE = DateUtils.getUTCDate(2013, 9, 9);
  private static final Period LEG_TENOR = Period.ofYears(10);
  private static final Period PAY_TENOR = Period.ofMonths(3);
  private static final double NOTIONAL = 100000000; // 100m
  private static final double SPREAD = 0.0010; // 10bps
  private static final int PAY_LAG = 2;

  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.FOLLOWING;
  private static final HolidayCalendar CALENDAR = HolidayCalendars.SAT_SUN;

  private static final double TOLERANCE_NOTIONAL = 1E-2;
  private static final double TOLERANCE_RATE = 1.0E-10;

  @Test
  public void from() {
    final ZonedDateTime maturity = EFFECTIVE_DATE.plus(LEG_TENOR);
    final AnnuityDefinition<CouponONArithmeticAverageSpreadDefinition> leg = AnnuityCouponArithmeticAverageONSpreadDefinition.from(EFFECTIVE_DATE, maturity, NOTIONAL, SPREAD,
        true, PAY_TENOR, FEDFUND, PAY_LAG, BUSINESS_DAY, true, CALENDAR, StubConvention.SHORT_INITIAL);
    final int nbCoupon = leg.getNumberOfPayments();
    assertEquals("AnnuityCouponArithmeticAverageONSpreadDefinition", nbCoupon, 40); // nb coupons: 10Y quarterly
    for (int loopc = 0; loopc < nbCoupon; loopc++) {
      assertTrue("AnnuityCouponArithmeticAverageONSpreadDefinition: Payer", leg.getNthPayment(loopc).getNotional() < 0);
      assertEquals("AnnuityCouponArithmeticAverageONSpreadDefinition: Notional", leg.getNthPayment(loopc).getNotional(), -NOTIONAL, TOLERANCE_NOTIONAL);
      assertEquals("AnnuityCouponArithmeticAverageONSpreadDefinition: Spread", leg.getNthPayment(loopc).getSpread(), SPREAD, TOLERANCE_RATE);
      assertEquals("AnnuityCouponArithmeticAverageONSpreadDefinition: Pay lag", leg.getNthPayment(loopc).getPaymentDate(),
          ScheduleCalculator.getAdjustedDate(leg.getNthPayment(loopc).getAccrualEndDate(), PAY_LAG, CALENDAR));
    }
  }

  @Test
  public void fromStub() {
    final ZonedDateTime maturity = EFFECTIVE_DATE.plus(LEG_TENOR).plusMonths(1);
    final AnnuityDefinition<CouponONArithmeticAverageSpreadDefinition> legShortStart = AnnuityCouponArithmeticAverageONSpreadDefinition.from(EFFECTIVE_DATE, maturity, NOTIONAL, SPREAD,
        true, PAY_TENOR, FEDFUND, PAY_LAG, BUSINESS_DAY, true, CALENDAR, StubConvention.SHORT_INITIAL);
    final AnnuityDefinition<CouponONArithmeticAverageSpreadDefinition> legShortStart2 = AnnuityCouponArithmeticAverageONSpreadDefinition.from(EFFECTIVE_DATE, maturity, NOTIONAL, SPREAD,
        true, PAY_TENOR, FEDFUND, PAY_LAG, BUSINESS_DAY, true, CALENDAR);
    assertEquals("AnnuityCouponArithmeticAverageONSpreadDefinition: Default", legShortStart, legShortStart2);
    final int nbCouponShortStart = legShortStart.getNumberOfPayments();
    assertEquals("AnnuityCouponArithmeticAverageONSpreadDefinition", 41, nbCouponShortStart); // nb coupons: 10Y+1M quarterly
    assertTrue("AnnuityCouponArithmeticAverageONSpreadDefinition: Short start",
        legShortStart.getNthPayment(0).getAccrualStartDate().getMonth().plus(1) == legShortStart.getNthPayment(0).getAccrualEndDate().getMonth());

    final AnnuityDefinition<CouponONArithmeticAverageSpreadDefinition> legShortEnd = AnnuityCouponArithmeticAverageONSpreadDefinition.from(EFFECTIVE_DATE, maturity, NOTIONAL, SPREAD,
        true, PAY_TENOR, FEDFUND, PAY_LAG, BUSINESS_DAY, true, CALENDAR, StubConvention.SHORT_FINAL);
    final int nbCouponShortEnd = legShortEnd.getNumberOfPayments();
    assertEquals("AnnuityCouponArithmeticAverageONSpreadDefinition", 41, nbCouponShortEnd); // nb coupons: 10Y+1M quarterly
    assertTrue("AnnuityCouponArithmeticAverageONSpreadDefinition: Short end",
        legShortEnd.getNthPayment(nbCouponShortEnd - 1).getAccrualStartDate().getMonth().plus(1) == legShortEnd.getNthPayment(nbCouponShortEnd - 1).getAccrualEndDate().getMonth());

    final AnnuityDefinition<CouponONArithmeticAverageSpreadDefinition> legLongStart = AnnuityCouponArithmeticAverageONSpreadDefinition.from(EFFECTIVE_DATE, maturity, NOTIONAL, SPREAD,
        true, PAY_TENOR, FEDFUND, PAY_LAG, BUSINESS_DAY, true, CALENDAR, StubConvention.LONG_INITIAL);
    final int nbCouponLongStart = legLongStart.getNumberOfPayments();
    assertEquals("AnnuityCouponArithmeticAverageONSpreadDefinition", 40, nbCouponLongStart); // nb coupons: 10Y+1M quarterly
    assertTrue("AnnuityCouponArithmeticAverageONSpreadDefinition: Short start",
        legLongStart.getNthPayment(0).getAccrualStartDate().getMonth().plus(4) == legLongStart.getNthPayment(0).getAccrualEndDate().getMonth());

    final AnnuityDefinition<CouponONArithmeticAverageSpreadDefinition> legLongEnd = AnnuityCouponArithmeticAverageONSpreadDefinition.from(EFFECTIVE_DATE, maturity, NOTIONAL, SPREAD,
        true, PAY_TENOR, FEDFUND, PAY_LAG, BUSINESS_DAY, true, CALENDAR, StubConvention.LONG_FINAL);
    final int nbCouponLongEnd = legLongEnd.getNumberOfPayments();
    assertEquals("AnnuityCouponArithmeticAverageONSpreadDefinition", 40, nbCouponLongEnd); // nb coupons: 10Y+1M quarterly
    assertTrue("AnnuityCouponArithmeticAverageONSpreadDefinition: Short end",
        legLongEnd.getNthPayment(nbCouponLongEnd - 1).getAccrualStartDate().getMonth().plus(4) == legLongEnd.getNthPayment(nbCouponLongEnd - 1).getAccrualEndDate().getMonth());
  }

}
