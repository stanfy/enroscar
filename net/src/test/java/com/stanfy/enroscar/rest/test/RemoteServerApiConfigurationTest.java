package com.stanfy.enroscar.rest.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import com.stanfy.enroscar.beans.BeansManager;
import com.stanfy.enroscar.net.test.Runner;
import com.stanfy.enroscar.net.test.cache.DummyResponseCache;
import com.stanfy.enroscar.rest.RemoteServerApiConfiguration;
import com.stanfy.enroscar.rest.RequestExecutor;
import com.stanfy.enroscar.rest.request.RequestBuilder;
import com.stanfy.enroscar.rest.request.RequestDescription;
import com.stanfy.enroscar.rest.request.SimpleRequestBuilder;
import com.stanfy.enroscar.rest.response.handler.StringContentHandler;

/**
 * Tests for {@link RemoteServerApiConfiguration}.
 */
@RunWith(Runner.class)
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
      public int performRequest(final RequestDescription d) {
        rd[0] = d;
        return 0;
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
    assertThat(createRequestDescription().getCacheName(), equalTo("defaultCacheBeanName"));
  }
  
  @Test
  public void setDefaultContentHandlerNameShouldAffectRequestDescriptions() {
    assertThat(createRequestDescription().getContentHandler(), equalTo("defaultContentHandlerName"));
  }
  
}
