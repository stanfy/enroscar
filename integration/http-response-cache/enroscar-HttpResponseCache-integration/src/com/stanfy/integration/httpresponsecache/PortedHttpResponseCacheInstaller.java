package com.stanfy.integration.httpresponsecache;

import java.io.File;
import java.io.IOException;
import java.net.ResponseCache;

import com.integralblue.httpresponsecache.compat.libcore.net.http.HttpResponseCache;
import com.jakewharton.DiskLruCache;
import com.stanfy.io.IoUtils;
import com.stanfy.net.cache.CacheInstaller;

/**
 * Installer for a ported version of HTTP response cache.
 */
public class PortedHttpResponseCacheInstaller implements CacheInstaller<HttpResponseCache> {

  /** Instance. */
  private static PortedHttpResponseCacheInstaller instance;

  public static PortedHttpResponseCacheInstaller getInstance() {
    if (instance == null) {
      instance = new PortedHttpResponseCacheInstaller();
    }
    return instance;
  }

  private PortedHttpResponseCacheInstaller() { /* hidden */ }

  /***
   * Returns the currently-installed {@code HttpResponseCache}, or null if
   * there is no cache installed or it is not a {@code HttpResponseCache}.
   */
  public static HttpResponseCache getInstalled() {
    final ResponseCache installed = ResponseCache.getDefault();
    return installed instanceof HttpResponseCache ? (HttpResponseCache) installed : null;
  }

  @Override
  public HttpResponseCache install(final File cacheDir, final long maxSize) throws IOException {
    final HttpResponseCache installed = getInstalled();
    if (installed != null) {
      // don't close and reopen if an equivalent cache is already installed
      final DiskLruCache installedCache = installed.getCache();
      if (installedCache.getDirectory().equals(cacheDir)
          && installedCache.maxSize() == maxSize
          && !installedCache.isClosed()) {
        return installed;
      } else {
        IoUtils.closeQuietly(installed.getCache());
      }
    }

    final HttpResponseCache result = new HttpResponseCache(cacheDir, maxSize);
    ResponseCache.setDefault(result);
    return result;
  }

  @Override
  public void delete(final HttpResponseCache cache) throws IOException {
    cache.getCache().delete();
  }
  @Override
  public void close(final HttpResponseCache cache) throws IOException {
    cache.getCache().close();
  }

}
