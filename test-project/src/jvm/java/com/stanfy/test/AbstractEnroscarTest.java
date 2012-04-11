package com.stanfy.test;

import org.hamcrest.Matcher;
import org.junit.Assert;
import org.junit.runner.RunWith;


/**
 * Base class for all tests.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
@RunWith(EnroscarTestRunner.class)
public abstract class AbstractEnroscarTest {

  protected static <T> void assertThat(final T actual, final Matcher<T> matcher) {
    Assert.assertThat(actual, matcher);
  }
  protected static <T> void assertThat(final String reason, final T actual, final Matcher<T> matcher) {
    Assert.assertThat(reason, actual, matcher);
  }



}
