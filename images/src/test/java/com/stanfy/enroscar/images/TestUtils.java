package com.stanfy.enroscar.images;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.CacheRequest;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;

/**
 * Test utilities.
 */
class TestUtils {

  static final String URL = "http://example.com/image";

  static final int TEST_BITMAP_SIZE = 100; // from ShadowBitmapFactory

  static void putCachedContent(final ImagesManager manager, final String url) throws Exception {
    CacheRequest cacheRequest = manager.getImagesResponseCache().put(new URI(url), fakeConnection(new URL(url)));
    OutputStream out = cacheRequest.getBody();
    out.write(new byte[]{1});
    out.close();
  }

  private static InputStream imageStream() {
    //return getClass().getResourceAsStream("/logo.png");
    return new ByteArrayInputStream(new byte[1]);
  }

  private static URLConnection fakeConnection(final URL url) {
    return new HttpURLConnection(url) {
      @Override
      public void connect() throws IOException {

      }

      @Override
      public String getRequestMethod() {
        return "GET";
      }

      @Override
      public InputStream getInputStream() throws IOException {
        return imageStream();
      }

      @Override
      public void disconnect() {

      }

      @Override
      public boolean usingProxy() {
        return false;
      }
    };
  }


  private TestUtils() { }

}
