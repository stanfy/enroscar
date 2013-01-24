package com.stanfy.app.beans;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

import org.junit.Test;

import android.content.Context;

import com.stanfy.app.beans.BeansManager.Editor;
import com.stanfy.images.cache.SupportLruImageMemoryCache;
import com.stanfy.net.EnroscarConnectionsEngine;
import com.stanfy.test.AbstractEnroscarTest;
import com.xtremelabs.robolectric.Robolectric;

/**
 * Beans editor test.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
public class BeansEditorTest extends AbstractEnroscarTest {

  /** Class for testing editor. */
  private static class MyImagesCache extends SupportLruImageMemoryCache {
    public MyImagesCache(final Context context) {
      super(context);
    }
  }

  @Override
  protected void configureBeansManager(final Editor editor) {
    editor
    // set defaults
      .defaults()
    // rewrite memory cache
      .put(MyImagesCache.class);
  }

  @Test
  public void testBeansConfiguration() {
    assertThat(BeansManager.get(Robolectric.application).getImageMemoryCache(), is(instanceOf(MyImagesCache.class)));
  }

  @Test
  public void remoteServerConfigurationShouldInstallConnectionsEngine() {
    assertThat(EnroscarConnectionsEngine.isInstalled(), is(true));
  }

  public void secondConnectionsInstallShouldNotFail() {
    EnroscarConnectionsEngine.config().install(getApplication());
    assertThat(EnroscarConnectionsEngine.isInstalled(), is(true));
  }

}
