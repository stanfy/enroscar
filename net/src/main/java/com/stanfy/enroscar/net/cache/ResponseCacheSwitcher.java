package com.stanfy.enroscar.net.cache;

import java.io.IOException;
import java.net.CacheRequest;
import java.net.CacheResponse;
import java.net.MalformedURLException;
import java.net.ResponseCache;
import java.net.URI;
import java.net.URLConnection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.util.Log;

import com.stanfy.enroscar.beans.BeansManager;
import com.stanfy.enroscar.net.UrlConnectionWrapper;

/**
 * Response cache implementation that can use different policies for different connections.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
public class ResponseCacheSwitcher extends ResponseCache {

  /** Logging tag. */
  private static final String TAG = "ResponseCacheHub";

  /** Thread local connection. */
  private final ThreadLocal<LinkedList<URLConnection>> currentUrlConnection = new ThreadLocal<LinkedList<URLConnection>>();

  /** Default cache instance. */
  private ResponseCache defaultCache;

  public static void saveUrlConnection(final URLConnection connection) {
    final ResponseCache cache = ResponseCache.getDefault();
    if (!(cache instanceof ResponseCacheSwitcher)) { return; }

    final ResponseCacheSwitcher hub = (ResponseCacheSwitcher)cache;
    LinkedList<URLConnection> stack = hub.currentUrlConnection.get();
    if (stack == null) {
      stack = new LinkedList<URLConnection>();
      hub.currentUrlConnection.set(stack);
    }
    stack.addFirst(connection);
  }

  public static void restoreUrlConnection(final URLConnection connection) {
    final ResponseCache cache = ResponseCache.getDefault();
    if (!(cache instanceof ResponseCacheSwitcher)) { return; }

    final ResponseCacheSwitcher hub = (ResponseCacheSwitcher)cache;
    final LinkedList<URLConnection> stack = hub.currentUrlConnection.get();
    if (!stack.isEmpty() && stack.peek() == connection) {
      stack.removeFirst();
    } else {
      final String message = "Bad call to restoreUrlConnection(): " + (stack.isEmpty() ? "stack is empty" : "connection does not match");
      Log.w(TAG, message);
      if (DebugFlags.STRICT_MODE) { throw new RuntimeException(message); }
    }
  }

  public static URLConnection getLastUrlConnection() {
    final ResponseCache cache = ResponseCache.getDefault();
    if (!(cache instanceof ResponseCacheSwitcher)) { return null; }
    final ResponseCacheSwitcher hub = (ResponseCacheSwitcher)cache;
    return hub.getLastSavedUrlConnection();
  }

  protected URLConnection getLastSavedUrlConnection() {
    final LinkedList<URLConnection> stack = currentUrlConnection.get();
    if (stack == null || stack.isEmpty()) {
      if (DebugFlags.DEBUG_NET_CACHE) {
        Log.i(TAG, "Connections stack is empty. Did you call saveUrlConnection(URLConnection)?");
      }
      return null;
    }
    return stack.peek();
  }

  protected URLConnection resolveCurrentUrlConnection(final URI uri) {
    final URLConnection candidate = getLastSavedUrlConnection();
    if (candidate == null) { return null; }
    try {
      return uri.toURL().equals(candidate.getURL()) ? candidate : null;
    } catch (final MalformedURLException e) {
      Log.w(TAG, "Cannot transform to URL " + uri, e);
      return null;
    }
  }

  protected ResponseCache getResponseCacheBean(final CacheControlUrlConnection connection) {
    final String name = connection.getResponseCacheName();
    // We assume that beans manager has been already created
    return BeansManager.get(null).getContainer().getBean(name, ResponseCache.class);
  }

  public void setDefaultCache(final ResponseCache defaultCache) {
    this.defaultCache = defaultCache;
  }

  @Override
  public CacheResponse get(final URI uri, final String requestMethod, final Map<String, List<String>> requestHeaders) throws IOException {
    final CacheControlUrlConnection connection = UrlConnectionWrapper.getWrapper(resolveCurrentUrlConnection(uri), CacheControlUrlConnection.class);
    if (connection != null) {
      final ResponseCache cache = getResponseCacheBean(connection);
      if (cache instanceof EnhancedResponseCache) {
        return ((EnhancedResponseCache) cache).get(uri, connection);
      }
      if (cache != null) {
        return cache.get(uri, requestMethod, requestHeaders);
      }
    }

    return defaultCache != null ? defaultCache.get(uri, requestMethod, requestHeaders) : null;
  }

  @Override
  public CacheRequest put(final URI uri, final URLConnection conn) throws IOException {
    final CacheControlUrlConnection connection = UrlConnectionWrapper.getWrapper(getLastSavedUrlConnection(), CacheControlUrlConnection.class);
    if (connection != null) {
      final ResponseCache cache = getResponseCacheBean(connection);
      if (cache != null) {
        return cache.put(uri, connection);
      }
    }
    return defaultCache != null ? defaultCache.put(uri, conn) : null;
  }

}
