package com.opengamma.analytics.financial.instrument.bond;

import static org.testng.AssertJUnit.assertEquals;

import java.time.Period;
import java.time.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.analytics.convention.businessday.BusinessDayConvention;
import com.opengamma.analytics.convention.businessday.BusinessDayConventions;
import com.opengamma.analytics.convention.daycount.DayCount;
import com.opengamma.analytics.convention.daycount.DayCounts;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.util.time.DateUtils;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.basics.date.HolidayCalendar;
import com.opengamma.strata.basics.date.HolidayCalendars;


/**
 * Test.
 */
@Test
public class BondIborTransactionDefinitionTest {

  //Quarterly Libor6m 2Y
  private static final Currency CUR = Currency.EUR;
  private static final HolidayCalendar CALENDAR = HolidayCalendars.SAT_SUN;
  private static final String ISSUER_NAME = "Issuer";
  private static final DayCount DAY_COUNT = DayCounts.ACT_ACT_ISDA;
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.FOLLOWING;
  private static final boolean IS_EOM = false;
  private static final Period IBOR_TENOR = Period.ofMonths(3);
  private static final DayCount IBOR_DAY_COUNT = DayCounts.THIRTY_U_360;
  private static final int IBOR_SPOT_LAG = 2;
  private static final BusinessDayConvention IBOR_BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final boolean IBOR_IS_EOM = false;
  private static final IborIndex IBOR_INDEX = new IborIndex(CUR, IBOR_TENOR, IBOR_SPOT_LAG, IBOR_DAY_COUNT, IBOR_BUSINESS_DAY, IBOR_IS_EOM, "Ibor");
  private static final Period BOND_TENOR = Period.ofYears(2);
  private static final int SETTLEMENT_DAYS = 3; // Standard for euro-bonds.
  private static final ZonedDateTime START_ACCRUAL_DATE = DateUtils.getUTCDate(2011, 7, 13);
  private static final ZonedDateTime MATURITY_DATE = START_ACCRUAL_DATE.plus(BOND_TENOR);
  private static final BondIborSecurityDefinition BOND_DESCRIPTION = BondIborSecurityDefinition.from(MATURITY_DATE, START_ACCRUAL_DATE, IBOR_INDEX, SETTLEMENT_DAYS, DAY_COUNT, BUSINESS_DAY, IS_EOM, ISSUER_NAME, CALENDAR);
  // Transaction
  private static final double PRICE = 0.90;
  private static final ZonedDateTime SETTLEMENT_DATE = DateUtils.getUTCDate(2011, 8, 18);
  private static final double QUANTITY = 100000000; //100m
  private static final BondIborTransactionDefinition BOND_TRANSACTION = new BondIborTransactionDefinition(BOND_DESCRIPTION, QUANTITY, SETTLEMENT_DATE, PRICE);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullUnderlying() {
    new BondIborTransactionDefinition(null, QUANTITY, SETTLEMENT_DATE, PRICE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSettle() {
    new BondIborTransactionDefinition(BOND_DESCRIPTION, QUANTITY, null, PRICE);
  }

  @Test
  public void testGetters() {
    assertEquals(PRICE, BOND_TRANSACTION.getPrice());
    assertEquals(QUANTITY, BOND_TRANSACTION.getQuantity());
    assertEquals(SETTLEMENT_DATE, BOND_TRANSACTION.getSettlementDate());
    assertEquals(BOND_DESCRIPTION, BOND_TRANSACTION.getUnderlyingBond());
    assertEquals(DateUtils.getUTCDate(2011, 7, 13), BOND_TRANSACTION.getPreviousAccrualDate());
    assertEquals(DateUtils.getUTCDate(2011, 10, 13), BOND_TRANSACTION.getNextAccrualDate());
  }
}
