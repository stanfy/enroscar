package com.stanfy.net.cache;

import static org.hamcrest.Matchers.equalTo;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;

import com.google.mockwebserver.MockResponse;
import com.stanfy.app.beans.BeansContainer;
import com.stanfy.app.beans.BeansManager;
import com.stanfy.app.beans.BeansManager.Editor;
import com.stanfy.app.beans.InitializingBean;
import com.stanfy.io.IoUtils;
import com.stanfy.net.UrlConnectionBuilder;
import com.stanfy.test.EnroscarConfiguration;
import com.xtremelabs.robolectric.Robolectric;

/**
 * Test for {@link CacheWrapper}.
 */
@EnroscarConfiguration(connectionEngineRequired = true)
public class CacheWrapperTest extends AbstractOneCacheTest {

  /** Test wrapper. */
  public static class SimpleWrapper extends CacheWrapper implements InitializingBean {

    @Override
    public void onInititializationFinished(final BeansContainer beansContainer) {
      setCore(BeansManager.get(Robolectric.application).getResponseCache(CACHE_NAME));
    }

  }

  @Override
  @Before
  public void setupCache() throws IOException {
    cache = (SimpleFileCache) BeansManager.get(Robolectric.application).getResponseCache(CACHE_NAME);
  }

  @Override
  protected void configureBeansManager(final Editor editor) {
    super.configureBeansManager(editor);
    editor.remoteServerApi().put("cacheWrapper", new SimpleWrapper());
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

    IoUtils.consumeStream(stream, BeansManager.get(getApplication()).getMainBuffersPool());

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

}
