package com.stanfy.net.cache;

import static org.hamcrest.Matchers.*;

import java.io.IOException;
import java.net.ResponseCache;
import java.net.URLConnection;

import org.junit.Before;
import org.junit.Test;

import com.google.mockwebserver.MockResponse;
import com.google.mockwebserver.MockWebServer;
import com.stanfy.app.beans.BeansManager;
import com.stanfy.app.beans.BeansManager.Editor;
import com.stanfy.net.UrlConnectionBuilder;
import com.stanfy.test.AbstractMockServerTest;
import com.stanfy.test.EnroscarConfiguration;
import com.xtremelabs.robolectric.Robolectric;

/**
 * Tests for {@link com.stanfy.net.cache.ResponseCacheSwitcher}.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
@EnroscarConfiguration(connectionEngineRequired = true)
public class CacheSwitcherTest extends AbstractMockServerTest {

  /** Cache instances. */
  private SimpleFileCache cache1, cache2;

  @Override
  protected void configureBeansManager(final Editor editor) {
    super.configureBeansManager(editor);
    editor.put("testCache1", new SimpleFileCache("testCache1"))
          .put("testCache2", new SimpleFileCache("testCache2"));
  }

  @Before
  public void setupCache() throws IOException {
    final BeansManager beansManager = BeansManager.get(Robolectric.application);
    cache1 = (SimpleFileCache)beansManager.getResponseCache("testCache1");
    cache2 = (SimpleFileCache)beansManager.getResponseCache("testCache2");
  }

  @Test
  public void testCacheSwitcherInitialized() {
    assertThat(ResponseCache.getDefault(), instanceOf(ResponseCacheSwitcher.class));
    assertThat(cache1, is(notNullValue()));
    assertThat(cache2, is(notNullValue()));
  }

  private void perform(final String path, final SimpleFileCache cache, final String response, final boolean cached) throws IOException {
    final URLConnection connection = new UrlConnectionBuilder()
        .setUrl(getWebServer().getUrl(path))
        .setCacheManagerName(cache.getName())
        .create();
    assertResponse(connection, response, cached);
    assertThat(cache.getWriteSuccessCount(), equalTo(1));
    assertThat(cache.getHitCount(), equalTo(cached ? 1 : 0));
    // check switcher connections stack
    assertThat(ResponseCacheSwitcher.getLastUrlConnection(), is(nullValue()));
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
