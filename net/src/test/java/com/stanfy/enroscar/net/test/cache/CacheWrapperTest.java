package com.stanfy.enroscar.net.test.cache;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.ResponseCache;

import org.junit.Before;
import org.junit.Test;
import org.robolectric.Robolectric;

import com.google.mockwebserver.MockResponse;
import com.stanfy.enroscar.beans.BeansContainer;
import com.stanfy.enroscar.beans.BeansManager;
import com.stanfy.enroscar.beans.BeansManager.Editor;
import com.stanfy.enroscar.beans.InitializingBean;
import com.stanfy.enroscar.io.BuffersPool;
import com.stanfy.enroscar.io.IoUtils;
import com.stanfy.enroscar.net.UrlConnectionBuilder;
import com.stanfy.enroscar.net.cache.CacheWrapper;
import com.stanfy.enroscar.shared.test.EnroscarConfiguration;

/**
 * Test for {@link CacheWrapper}.
 */
@EnroscarConfiguration(connectionEngineRequired = true)
public class CacheWrapperTest extends AbstractOneCacheTest {

  @Override
  @Before
  public void setupCache() throws IOException {
    cache = (SimpleFileCache) BeansManager.get(Robolectric.application).getContainer().getBean(CACHE_NAME, ResponseCache.class);
  }

  @Override
  protected void configureBeansManager(final Editor editor) {
    super.configureBeansManager(editor);
    editor.put("cacheWrapper", new SimpleWrapper());
  }

  @Test
  public void shouldAffectCoreCache() throws Exception {
    final String text = "ABC";
    getWebServer().enqueue(new MockResponse().setBody(text));

    InputStream stream = new UrlConnectionBuilder()
        .setCacheManagerName("cacheWrapper")
        .setUrl(getWebServer().getUrl("/"))
        .create()
        .getInputStream();

    IoUtils.consumeStream(stream, BeansManager.get(getApplication()).getContainer().getBean(BuffersPool.class));

    // cache entry has been written to the CORE cache
    assertThat(cache.getWriteSuccessCount(), equalTo(1));
    assertThat(cache.getHitCount(), equalTo(0));

    // we can read from cache
    stream = new UrlConnectionBuilder()
      .setCacheManagerName("cacheWrapper")
      .setUrl(getWebServer().getUrl("/"))
      .create()
      .getInputStream();

    final String response = IoUtils.streamToString(stream);
    assertThat(response, equalTo(text));

    assertThat(cache.getWriteSuccessCount(), equalTo(1));
    assertThat(cache.getHitCount(), equalTo(1));

  }

  /** Test wrapper. */
  public static class SimpleWrapper extends CacheWrapper implements InitializingBean {

    @Override
    public void onInitializationFinished(final BeansContainer beansContainer) {
      setCore(BeansManager.get(Robolectric.application).getContainer().getBean(CACHE_NAME, ResponseCache.class));
    }

  }

}
