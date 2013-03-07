package com.stanfy.enroscar.net.test.cache;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import org.junit.Test;

import com.google.mockwebserver.MockResponse;
import com.jakewharton.DiskLruCache;

/**
 * Tests for {@link com.stanfy.net.cache.BaseFileResponseCache}.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
public class BaseFileCacheTest extends AbstractOneCacheTest {

  @Test
  public void testCache() throws IOException {
    final String text = "ABC";
    getWebServer().enqueue(new MockResponse().setBody(text));

    final URL url = getWebServer().getUrl("/");
    final URLConnection connection = url.openConnection();

    // real request has been successfully performed
    assertResponse(connection, text, false);
    // cache entry has been written
    assertThat(cache.getWriteSuccessCount(), equalTo(1));
    assertThat(cache.getHitCount(), equalTo(0));

    final URLConnection secondConnection = url.openConnection();
    // real request has not been performed
    assertResponse(secondConnection, text, true);
    // nothing has been written
    assertThat(cache.getWriteSuccessCount(), equalTo(1));
    assertThat(cache.getHitCount(), equalTo(1));

    // check disk content
    final DiskLruCache diskCache = cache.getDiskCache();
    assertThat(diskCache.size(), greaterThan((long)text.length()));
    // 3 filed should be here: journal, body, metadata
    assertThat(diskCache.getDirectory().list().length, equalTo(3));

  }

}
