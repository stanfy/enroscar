package com.stanfy.enroscar.rest.test;

import com.stanfy.enroscar.net.operation.executor.DirectRequestExecutor;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.assertj.core.api.Assertions.assertThat;

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
