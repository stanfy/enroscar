package com.stanfy.enroscar.net.cache;

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

import android.annotation.TargetApi;
import android.content.Context;
import android.net.http.HttpResponseCache;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import com.stanfy.enroscar.beans.BeansContainer;
import com.stanfy.enroscar.beans.DestroyingBean;
import com.stanfy.enroscar.beans.EnroscarBean;
import com.stanfy.enroscar.beans.InitializingBean;
import com.stanfy.enroscar.net.UrlConnectionWrapper;

/**
 * Standard HTTP response cache.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
@EnroscarBean(value = StandardHttpResponseCache.BEAN_NAME, contextDependent = true)
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class StandardHttpResponseCache extends BaseSizeRestrictedCache implements EnhancedResponseCache, InitializingBean, DestroyingBean {

  /** Bean name. */
  public static final String BEAN_NAME = "StandardApiResponseCache";

  /** Logging tag. */
  private static final String TAG = BEAN_NAME;

  /** Response cache. */
  private HttpResponseCache delegate;

  public StandardHttpResponseCache(final Context context) {
    final File baseDir = Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
        ? context.getExternalCacheDir()
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
    final URLConnection coreConnection = UrlConnectionWrapper.unwrap(connection);
    return delegate.get(uri, ((HttpURLConnection)coreConnection).getRequestMethod(), coreConnection.getHeaderFields());
  }

  @Override
  public CacheRequest put(final URI uri, final URLConnection connection) throws IOException {
    if (delegate == null) { return null; }
    return delegate.put(uri, UrlConnectionWrapper.unwrap(connection));
  }

  @Override
  public boolean deleteGetEntry(final String url) throws IOException {
    return false;
  }

  @Override
  public boolean contains(final String url) {
    return false;
  }

  @Override
  public String getLocalPath(final String url) {
    return null;
  }

  @Override
  public void onInitializationFinished(final BeansContainer beansContainer) {
    final ResponseCache mainResponseCache = ResponseCache.getDefault();
    try {
      delegate = HttpResponseCache.install(getWorkingDirectory(), getMaxSize());
    } catch (final IOException e) {
      Log.e(TAG, "Cannot install HTTP cache", e);
    }

    if (mainResponseCache != null && !(mainResponseCache instanceof ResponseCacheSwitcher)) {
      Log.w(BEAN_NAME, "Response cache " + mainResponseCache + " is replaced");
    }
    if (mainResponseCache instanceof ResponseCacheSwitcher) {
      ResponseCache.setDefault(mainResponseCache);
    }
  }

  @Override
  public void onDestroy(final BeansContainer beansContainer) {
    if (delegate != null) {
      try {
        delegate.close();
      } catch (IOException e) {
        Log.e(TAG, "Cannot close HTTP cache", e);
      }
    }
  }

}
