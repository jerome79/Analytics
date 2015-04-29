/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.minimization;

import org.testng.annotations.Test;



/**
 * Test.
 */
@Test
public class ConjugateDirectionTest extends MultidimensionalMinimizerTestCase {
  private static final double EPS = 1e-7;

  private static ScalarMinimizer LINE_MINIMIZER = new BrentMinimizer1D();
  private static ConjugateDirectionVectorMinimizer MINIMIZER = new ConjugateDirectionVectorMinimizer(LINE_MINIMIZER, EPS, 100000);

  @Test
  public void testSolvingRosenbrock() {
    super.assertSolvingRosenbrock(MINIMIZER, EPS);
  }

  /**
   * This function is inherently challenging, giant number of iterations needed
   * as the direction vector ricochets around in a groove. 
   */
  @Test
  public void testSolvingCoupledRosenbrock() {
    super.assertSolvingCoupledRosenbrock(MINIMIZER, EPS);
  }

}
