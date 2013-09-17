package com.stanfy.enroscar.rest.test;

import static org.fest.assertions.api.Assertions.assertThat;

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
    assertThat(executor.getHooks()).isInstanceOf(DirectRequestExecutor.EmptyHooks.class);
  }
  
}
