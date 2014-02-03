package com.stanfy.enroscar.rest.test;

import static org.fest.assertions.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import com.stanfy.enroscar.net.operation.executor.DirectRequestExecutor;

/**
 * Direct request executor test.
 */
@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = 18)
public class DirectRequestExecutorTest {

  @Test
  public void hooksShouldNotBeNull() {
    DirectRequestExecutor executor = new DirectRequestExecutor(Robolectric.application);
    assertThat(executor.getHooks()).isInstanceOf(DirectRequestExecutor.EmptyHooks.class);
  }
  
}
