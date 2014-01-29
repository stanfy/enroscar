package com.stanfy.enroscar.net;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.net.ResponseCache;
import java.net.URL;

import android.os.Build.VERSION_CODES;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Tests for EnroscarConnectionsEngine.
 */
@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = VERSION_CODES.JELLY_BEAN_MR2)
public class EnroscarConnectionsEngineTest {

  @Before
  public void resetBefore() {
    reset();
  }

  @After
  public void resetAfter() {
    reset();
  }

  private static void reset() {
    EnroscarConnectionsEngine.config().withNothing().setup(Robolectric.application);
  }

  @Test
  public void shouldBeAbleToSetupCacheSwitcher() throws Exception {
    EnroscarConnectionsEngine.config().withNothing().withCacheSwitcher(true)
        .setup(Robolectric.application);
    assertThat(ResponseCache.getDefault()).isNotNull();
  }

  @Test
  public void shouldBeAbleToSetupUrlStreamHandler() throws Exception {
    EnroscarConnectionsEngine.config().withNothing().withStreamHandlers(true)
        .setup(Robolectric.application);
    assertThat(new URL("content://authority/data").openConnection()).isNotNull();
  }

}
