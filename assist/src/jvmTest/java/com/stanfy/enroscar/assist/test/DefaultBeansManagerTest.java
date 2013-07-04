package com.stanfy.enroscar.assist.test;

import static org.fest.assertions.api.Assertions.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import com.stanfy.enroscar.assist.DefaultBeansManager;
import com.stanfy.enroscar.beans.BeansManager;
import com.stanfy.enroscar.images.cache.ImageFileCache;
import com.stanfy.enroscar.net.EnroscarConnectionsEngine;
import com.stanfy.enroscar.rest.response.handler.GsonContentHandler;
import com.stanfy.enroscar.rest.response.handler.StringContentHandler;
import com.stanfy.enroscar.rest.response.handler.XmlGsonContentHandler;

/**
 * Tests for {@link DefaultBeansManager}.
 */
@RunWith(RobolectricTestRunner.class)
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
//    editor.commit();

//    assertThat(EnroscarConnectionsEngine.isInstalled(), is(true));
//
//    assertThat(manager.getRemoteServerApiConfiguration(), notNullValue());
//
//    assertThat(manager.getContentHandler(GsonContentHandler.BEAN_NAME), notNullValue());
//    assertThat(manager.getContentHandler(XmlGsonContentHandler.BEAN_NAME), notNullValue());
//    assertThat(manager.getContentHandler(StringContentHandler.BEAN_NAME), notNullValue());
  }

  @Test
  public void images() {
    manager.edit().images().commit();
    assertThat(manager.getImageMemoryCache()).isNotNull();
    assertThat(manager.getImagesManager()).isNotNull();
    assertThat(manager.getContainer().getBean(ImageFileCache.class)).isNotNull();
  }

  @Test
  public void activitiesBehavior() {
    manager.edit().activitiesBehavior().commit();
    assertThat(manager.getActivityBehaviorFactory()).isNotNull();
  }

}
