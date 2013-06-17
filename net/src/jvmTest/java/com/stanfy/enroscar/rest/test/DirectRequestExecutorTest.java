package com.stanfy.enroscar.rest.test;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import com.stanfy.enroscar.rest.DirectRequestExecutor;

/**
 * Direct request executor test.
 */
@RunWith(RobolectricTestRunner.class)
public class DirectRequestExecutorTest {

  @Test
  public void hooksShouldNotBeNull() {
    DirectRequestExecutor executor = new DirectRequestExecutor(Robolectric.application);
    assertThat(executor.getHooks(), instanceOf(DirectRequestExecutor.EmptyHooks.class));
  }
  
}
