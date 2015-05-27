package com.stanfy.enroscar.rest.test;

import com.stanfy.enroscar.beans.BeansManager;
import com.stanfy.enroscar.net.operation.RequestBuilder;
import com.stanfy.enroscar.net.operation.RequestDescription;
import com.stanfy.enroscar.net.operation.SimpleRequestBuilder;
import com.stanfy.enroscar.net.operation.executor.RequestExecutor;
import com.stanfy.enroscar.net.test.cache.DummyResponseCache;
import com.stanfy.enroscar.rest.RemoteServerApiConfiguration;
import com.stanfy.enroscar.rest.response.handler.StringContentHandler;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link RemoteServerApiConfiguration}.
 */
@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = 18)
public class RemoteServerApiConfigurationTest {

  @Before
  public void configureBean() {
    BeansManager manager = BeansManager.get(Robolectric.application);
    manager.edit()
      .put("defaultCacheBeanName", new DummyResponseCache())
      .put("defaultContentHandlerName", new StringContentHandler(Robolectric.application))
      .put(RemoteServerApiConfiguration.class)
      .commit();
    
    RemoteServerApiConfiguration config = manager.getContainer().getBean(RemoteServerApiConfiguration.class);
    config.setDefaultCacheBeanName("defaultCacheBeanName");
    config.setDefaultContentHandlerName("defaultContentHandlerName");
  }
  
  private RequestDescription createRequestDescription() {
    RequestBuilder<?> rb = new SimpleRequestBuilder<String>(Robolectric.application) { }
      .setUrl("http://example.com");
    final RequestDescription[] rd = new RequestDescription[1];
    rb.setExecutor(new RequestExecutor() {
      @Override
      public void performRequest(final RequestDescription d) {
        rd[0] = d;
      }
    });
    rb.execute();
    return rd[0];
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void badCacheBeanNameShouldThrow() {
    RemoteServerApiConfiguration config = BeansManager.get(Robolectric.application).getContainer().getBean(RemoteServerApiConfiguration.class);
    config.setDefaultCacheBeanName("bad");
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void badContentHandlerBeanNameShouldThrow() {
    RemoteServerApiConfiguration config = BeansManager.get(Robolectric.application).getContainer().getBean(RemoteServerApiConfiguration.class);
    config.setDefaultContentHandlerName("bad");
  }

  @Test
  public void setDefaultCacheBeanNameShouldAffectRequestDescriptions() {
    assertThat(createRequestDescription().getCacheName()).isEqualTo("defaultCacheBeanName");
  }
  
  @Test
  public void setDefaultContentHandlerNameShouldAffectRequestDescriptions() {
    assertThat(createRequestDescription().getContentHandler()).isEqualTo("defaultContentHandlerName");
  }
  
}
