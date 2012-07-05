package com.stanfy.serverapi.cache;

import java.io.File;
import java.io.IOException;
import java.net.CacheRequest;
import java.net.CacheResponse;
import java.net.HttpURLConnection;
import java.net.ResponseCache;
import java.net.URI;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.os.Build.VERSION;
import android.os.Environment;
import android.util.Log;

import com.stanfy.app.beans.DestroyingBean;
import com.stanfy.app.beans.EnroscarBean;
import com.stanfy.app.beans.InitializingBean;
import com.stanfy.net.UrlConnectionWrapper;
import com.stanfy.net.cache.BaseSizeRestrictedCache;
import com.stanfy.net.cache.CacheInstaller;
import com.stanfy.net.cache.EnhancedResponseCache;
import com.stanfy.net.cache.ResponseCacheSwitcher;
import com.stanfy.utils.AppUtils;

/**
 * Standard HTTP response cache.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
@EnroscarBean(value = StandardApiResponseCache.BEAN_NAME, contextDependent = true)
public class StandardApiResponseCache extends BaseSizeRestrictedCache implements EnhancedResponseCache, InitializingBean, DestroyingBean {

  /** Bean name. */
  public static final String BEAN_NAME = "StandardApiResponseCache";

  /** Logging tag. */
  private static final String TAG = BEAN_NAME;

  /** Installer. */
  private CacheInstaller<ResponseCache> cacheInstaller;

  /** Response cache. */
  private ResponseCache delegate;

  public StandardApiResponseCache(final Context context) {
    final File baseDir = Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
        ? AppUtils.getSdkDependentUtils().getExternalCacheDir(context)
        : context.getCacheDir();
    setWorkingDirectory(new File(baseDir, "api-http-cache"));
  }

  @Override
  public CacheResponse get(final URI uri, final String requestMethod, final Map<String, List<String>> requestHeaders) throws IOException {
    if (delegate == null) { return null; }
    return delegate.get(uri, requestMethod, requestHeaders);
  }

  @Override
  public CacheResponse get(final URI uri, final URLConnection connection) throws IOException {
    if (delegate == null) { return null; }
    if (delegate instanceof EnhancedResponseCache) {
      return ((EnhancedResponseCache) delegate).get(uri, connection);
    }
    final URLConnection coreConnection = UrlConnectionWrapper.unwrap(connection);
    return delegate.get(uri, ((HttpURLConnection)coreConnection).getRequestMethod(), coreConnection.getHeaderFields());
  }

  @Override
  public CacheRequest put(final URI uri, final URLConnection connection) throws IOException {
    if (delegate == null) { return null; }
    return delegate instanceof EnhancedResponseCache
        ? delegate.put(uri, connection)
        : delegate.put(uri, UrlConnectionWrapper.unwrap(connection));
  }

  @Override
  public boolean deleteGetEntry(final String url) throws IOException {
    if (delegate instanceof EnhancedResponseCache) {
      return ((EnhancedResponseCache) delegate).deleteGetEntry(url);
    }
    return false;
  }

  @Override
  public void onInititializationFinished() {
    final ResponseCache mainResponseCache = ResponseCache.getDefault();
    try {
      cacheInstaller = AppUtils.getSdkDependentUtils().getSystemResponseCacheInstaller();
      if (cacheInstaller != null) {
        delegate = cacheInstaller.install(getWorkingDirectory(), getMaxSize());
      } else {
        Log.w(TAG, "Cannot install system cache for " + VERSION.CODENAME + ": installer not provided");
      }
    } catch (final IOException e) {
      Log.e(TAG, "Cannot install HTTP cache", e);
    }

    if (mainResponseCache != null && !(mainResponseCache instanceof ResponseCacheSwitcher)) {
      Log.w(BEAN_NAME, "Response cache " + mainResponseCache + " is replaced");
    }
    if (mainResponseCache != null && mainResponseCache instanceof ResponseCacheSwitcher) {
      ResponseCache.setDefault(mainResponseCache);
    }
  }

  @Override
  public void onDestroy() {
    if (cacheInstaller != null) {
      try {
        cacheInstaller.close(delegate);
      } catch (final IOException e) {
        Log.e(TAG, "Cannot uninstall cache");
      }
    }
  }

}
