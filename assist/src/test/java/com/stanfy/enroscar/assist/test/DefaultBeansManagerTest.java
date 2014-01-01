package com.stanfy.enroscar.assist.test;

import android.os.Build;

import com.stanfy.enroscar.assist.BuffersPoolController;
import com.stanfy.enroscar.assist.DefaultBeansManager;
import com.stanfy.enroscar.beans.BeansManager;
import com.stanfy.enroscar.images.cache.ImageFileCache;
import com.stanfy.enroscar.io.BuffersPool;
import com.stanfy.enroscar.net.EnroscarConnectionsEngine;
import com.stanfy.enroscar.rest.response.handler.GsonContentHandler;
import com.stanfy.enroscar.rest.response.handler.StringContentHandler;
import com.stanfy.enroscar.rest.response.handler.XmlGsonContentHandler;
import com.stanfy.enroscar.stats.StatsManager;
import com.stanfy.enroscar.images.views.ImageConsumers;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link DefaultBeansManager}.
 */
@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class DefaultBeansManagerTest {

  /** Manager. */
  private DefaultBeansManager manager;
  
  @Before
  public void useDefaultBeansManager() {
    manager = DefaultBeansManager.get(Robolectric.application);
  }
  
  @Test
  public void afterGetCalledBeansManagerGetShouldReturnDefaultOne() {
    assertThat(BeansManager.get(Robolectric.application)).isInstanceOf(DefaultBeansManager.class);
  }
  
  @Test
  public void buffersPoolShouldBePut() {
    manager.edit().commit();
    assertThat(manager.getMainBuffersPool()).isNotNull();
  }
  
  @Test
  public void remoteServerApi() {
    DefaultBeansManager.Editor editor = manager.edit().remoteServerApi("xml", "json", "string");
    editor.commit();

    assertThat(EnroscarConnectionsEngine.isInstalled()).isTrue();

    assertThat(manager.getRemoteServerApiConfiguration()).isNotNull();

    assertThat(manager.getContentHandler(GsonContentHandler.BEAN_NAME)).isNotNull();
    assertThat(manager.getContentHandler(XmlGsonContentHandler.BEAN_NAME)).isNotNull();
    assertThat(manager.getContentHandler(StringContentHandler.BEAN_NAME)).isNotNull();
  }

  @Test
  public void images() {
    manager.edit().images().commit();
    assertThat(manager.getImageMemoryCache()).isNotNull();
    assertThat(manager.getImagesManager()).isNotNull();
    assertThat(manager.getContainer().getBean(ImageFileCache.class)).isNotNull();
    assertThat(manager.getContainer().getBean(ImageConsumers.class)).isNotNull();
  }

  @Test
  public void activitiesBehavior() {
    manager.edit().activitiesBehavior().commit();
    assertThat(manager.getActivityBehaviorFactory()).isNotNull();
  }

  @Test
  public void defaultBeansShouldNotBeOverridenTwice() {
    manager.edit().commit();
    BuffersPool pool = manager.getMainBuffersPool();
    StatsManager statsManager = manager.getStatsManager();

    assertThat(pool).isNotNull();
    assertThat(statsManager).isNotNull();

    manager.edit().commit();
    assertThat(manager.getMainBuffersPool()).isSameAs(pool);
    assertThat(manager.getStatsManager()).isSameAs(statsManager);
  }

  @Test
  public void buffersPoolMustBeFlushedOnLowMemory() {
    BuffersPool pool = mock(BuffersPool.class);
    manager.edit().put(BuffersPool.class.getName(), pool).put(BuffersPoolController.class).commit();

    manager.getContainer().onLowMemory();
    verify(pool).flush();
  }

}
