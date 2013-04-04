package com.stanfy.images.cache;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import android.util.Log;

import com.stanfy.enroscar.beans.Bean;
import com.stanfy.enroscar.beans.BeansContainer;
import com.stanfy.enroscar.beans.BeansManager;
import com.stanfy.enroscar.beans.EnroscarBean;
import com.stanfy.enroscar.sdkdep.SDKDependentUtilsFactory;
import com.stanfy.enroscar.sdkdep.SdkDependentUtils;

/**
 * Memory cache based on {@link LruCache}.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
@EnroscarBean(value = ImageMemoryCache.BEAN_NAME, contextDependent = true)
public class SupportLruImageMemoryCache implements ImageMemoryCache, Bean {

  /** LRU cache instance. */
  private final LruCache<String, Bitmap> cache;

  /** SDK utilities. */
  final SdkDependentUtils sdkUtils;

  public SupportLruImageMemoryCache(final Context context) {
    // TODO change it: remove construction
    this.sdkUtils = BeansManager.get(context).getContainer().getBean(SDKDependentUtilsFactory.class).createSdkDependentUtils();
    int memClass = ((ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
    if (memClass == 0) { // can be in tests
      memClass = 3;
    }
    final int mb = 1024 * 1024, part = 8;
    final int cacheSize = memClass * mb / part;
    Log.i(BEAN_NAME, "Images cache size: " + cacheSize + "(" + (cacheSize / mb) + " MB)");
    this.cache = new LruCache<String, Bitmap>(cacheSize) {
      @Override
      protected int sizeOf(final String key, final Bitmap value) {
        return sdkUtils.getBitmapSize(value);
      };
    };
  }

  @Override
  public void putElement(final String url, final Bitmap image) {
    cache.put(url, image);
  }

  @Override
  public Bitmap getElement(final String url) { return cache.get(url); }

  @Override
  public boolean contains(final String url) {
    return cache.get(url) != null;
  }

  @Override
  public Bitmap remove(final String url) {
    return cache.remove(url);
  }

  @Override
  public void clear() {
    cache.evictAll();
  }

  @Override
  public void flushResources(final BeansContainer beansContainer) {
    clear();
    Log.i(BEAN_NAME, "Images memory cache flushed");
  }

}
