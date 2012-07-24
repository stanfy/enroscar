package com.stanfy.net.cache;

import java.io.File;
import java.io.IOException;

import android.annotation.TargetApi;
import android.net.http.HttpResponseCache;
import android.os.Build;

/**
 * Cache installer for ICS.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public final class IcsHttpResponseCacheInstaller implements CacheInstaller<HttpResponseCache> {

  /** Instance. */
  private static IcsHttpResponseCacheInstaller instance = null;

  private IcsHttpResponseCacheInstaller() { /* hidden */ }

  public static IcsHttpResponseCacheInstaller getInstance() {
    if (instance == null) {
      instance = new IcsHttpResponseCacheInstaller();
    }
    return instance;
  }

  @Override
  public HttpResponseCache install(final File cacheDir, final long maxSize) throws IOException {
    return HttpResponseCache.install(cacheDir, maxSize);
  }

  @Override
  public void delete(final HttpResponseCache cache) throws IOException {
    cache.delete();
  }

  @Override
  public void close(final HttpResponseCache cache) throws IOException {
    cache.close();
  }

}
