package com.stanfy.enroscar.net.test.cache;

import java.io.IOException;
import java.net.CacheRequest;
import java.net.CacheResponse;
import java.net.ResponseCache;
import java.net.URI;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;

/** Dummy response cache. */
public class DummyResponseCache extends ResponseCache {

  @Override
  public CacheResponse get(final URI uri, final String rqstMethod, final Map<String, List<String>> rqstHeaders) throws IOException {
    return null;
  }

  @Override
  public CacheRequest put(final URI uri, final URLConnection conn) throws IOException {
    return null;
  }

}
