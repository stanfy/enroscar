package com.stanfy.enroscar.net.test.cache;

import static org.fest.assertions.api.Assertions.assertThat;

import java.io.IOException;
import java.net.ResponseCache;
import java.net.URLConnection;

import org.junit.Before;
import org.junit.Test;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;

import com.google.mockwebserver.MockResponse;
import com.google.mockwebserver.MockWebServer;
import com.stanfy.enroscar.beans.BeansManager;
import com.stanfy.enroscar.beans.BeansManager.Editor;
import com.stanfy.enroscar.io.BuffersPool;
import com.stanfy.enroscar.net.UrlConnectionBuilder;
import com.stanfy.enroscar.net.cache.ResponseCacheSwitcher;
import com.stanfy.enroscar.net.test.AbstractMockServerTest;
import com.stanfy.enroscar.shared.test.EnroscarConfiguration;

/**
 * Tests for {@link com.stanfy.net.cache.ResponseCacheSwitcher}.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
@EnroscarConfiguration(connectionEngineRequired = true)
@Config(emulateSdk = 18)
public class CacheSwitcherTest extends AbstractMockServerTest {

  /** Cache instances. */
  private SimpleFileCache cache1, cache2;

  @Override
  protected void configureBeansManager(final Editor editor) {
    super.configureBeansManager(editor);
    editor
          .put(BuffersPool.class)
          .put("testCache1", new SimpleFileCache("testCache1"))
          .put("testCache2", new SimpleFileCache("testCache2"));
  }

  @Before
  public void setupCache() throws IOException {
    final BeansManager beansManager = BeansManager.get(Robolectric.application);
    cache1 = (SimpleFileCache)beansManager.getContainer().getBean("testCache1", SimpleFileCache.class);
    cache2 = (SimpleFileCache)beansManager.getContainer().getBean("testCache2", SimpleFileCache.class);
  }

  @Test
  public void testCacheSwitcherInitialized() {
    assertThat(ResponseCache.getDefault()).isInstanceOf(ResponseCacheSwitcher.class);
    assertThat(cache1).isNotNull();
    assertThat(cache2).isNotNull();
  }

  private void perform(final String path, final SimpleFileCache cache, final String response, final boolean cached) throws IOException {
    final URLConnection connection = new UrlConnectionBuilder()
        .setUrl(getWebServer().getUrl(path))
        .setCacheManagerName(cache.getName())
        .create();
    assertResponse(connection, response, cached);
    assertThat(cache.getWriteSuccessCount()).isEqualTo(1);
    assertThat(cache.getHitCount()).isEqualTo(cached ? 1 : 0);
    // check switcher connections stack
    assertThat(ResponseCacheSwitcher.getLastUrlConnection()).isNull();
  }

  @Test
  public void testCacheSwitching() throws IOException {
    final String response1 = "<response to be cached by the FIRST testCache>";
    final String response2 = "<response to be cached by the SECOND testCache>";
    final MockWebServer webServer = getWebServer();

    // two responses
    webServer.enqueue(new MockResponse().setBody(response1));
    webServer.enqueue(new MockResponse().setBody(response2));

    // first request, first cache => not cached
    perform("/1/", cache1, response1, false);
    // second request, first cache => cached
    perform("/1/", cache1, response1, true);

    // first request, second cache => not cached
    perform("/2/", cache2, response2, false);
    // second request, second cache => cached
    perform("/2/", cache2, response2, true);
  }

}
