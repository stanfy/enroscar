/**
 *
 */
package com.stanfy.net.cache;

import java.io.IOException;
import java.net.CacheResponse;
import java.net.ResponseCache;
import java.net.URI;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.stanfy.app.beans.BeansContainer;
import com.stanfy.app.beans.EnroscarBean;
import com.stanfy.app.beans.InitializingBean;


/**
 * Wrapper of response cache tha uses that cache when there is no available connection.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
@EnroscarBean(value = NoConnectionResponseCache.BEAN_NAME, contextDependent = true)
public abstract class NoConnectionResponseCache extends CacheWrapper implements InitializingBean {

  /** Bean name. */
  public static final String BEAN_NAME = "NoConnetionResponseCache";

  /** Connectivity manager. */
  private final ConnectivityManager connectivityManager;

  /** Core cache bean name. */
  private final String coreCacheBeanName;

  protected NoConnectionResponseCache(final Context context, final String coreCacheBeanName) {
    this.connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
    this.coreCacheBeanName = coreCacheBeanName;
  }

  protected boolean shouldUseCache() {
    final NetworkInfo netwotkInfo = connectivityManager.getActiveNetworkInfo();
    return netwotkInfo == null || !netwotkInfo.isConnected();
  }

  @Override
  public CacheResponse get(final URI uri, final String requestMethod, final Map<String, List<String>> requestHeaders) throws IOException {
    return shouldUseCache() ? super.get(uri, requestMethod, requestHeaders) : null;
  }

  @Override
  public CacheResponse get(final URI uri, final URLConnection connection) throws IOException {
    return shouldUseCache() ? super.get(uri, connection) : null;
  }

  @Override
  public void onInitializationFinished(final BeansContainer beansContainer) {
    setCore(beansContainer.getBean(coreCacheBeanName, ResponseCache.class));
  }

}
