package com.stanfy.net.cache;

import java.io.IOException;
import java.net.CacheRequest;
import java.net.CacheResponse;
import java.net.HttpURLConnection;
import java.net.ResponseCache;
import java.net.URI;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;

import com.stanfy.net.UrlConnectionWrapper;

/**
 * Response cache wrapper.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
public abstract class CacheWrapper extends ResponseCache implements EnhancedResponseCache {

  /** Core response cache instance. */
  private ResponseCache core;

  public CacheWrapper() {
    // nothing
  }
  public CacheWrapper(final ResponseCache core) {
    this.core = core;
  }

  protected void setCore(final ResponseCache core) {
    this.core = core;
  }

  @Override
  public CacheResponse get(final URI uri, final URLConnection connection) throws IOException {
    if (core instanceof EnhancedResponseCache) {
      return ((EnhancedResponseCache) core).get(uri, connection);
    }
    final URLConnection coreConnection = UrlConnectionWrapper.unwrap(connection);
    if (coreConnection instanceof HttpURLConnection) {
      final HttpURLConnection http = (HttpURLConnection) coreConnection;
      return core.get(uri, http.getRequestMethod(), http.getRequestProperties());
    }
    return null;
  }

  @Override
  public boolean deleteGetEntry(final String url) throws IOException {
    if (core instanceof EnhancedResponseCache) {
      return ((EnhancedResponseCache) core).deleteGetEntry(url);
    }
    return false;
  }

  @Override
  public boolean contains(final String url) {
    if (core instanceof EnhancedResponseCache) {
      return ((EnhancedResponseCache) core).contains(url);
    }
    return false;
  }

  @Override
  public CacheResponse get(final URI uri, final String requestMethod, final Map<String, List<String>> requestHeaders) throws IOException {
    return core.get(uri, requestMethod, requestHeaders);
  }

  @Override
  public CacheRequest put(final URI uri, final URLConnection connection) throws IOException {
    return core.put(uri, connection);
  }

}
