package com.stanfy.images.cache;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import android.util.Log;

import com.stanfy.enroscar.beans.Bean;
import com.stanfy.enroscar.beans.BeansContainer;
import com.stanfy.enroscar.beans.EnroscarBean;
import com.stanfy.utils.AppUtils;
import com.stanfy.utils.sdk.SDKDependentUtils;

/**
 * Memory cache based on {@link LruCache}.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
@EnroscarBean(value = ImageMemoryCache.BEAN_NAME, contextDependent = true)
public class SupportLruImageMemoryCache implements ImageMemoryCache, Bean {

  /** Recycle on remove flag. */
  private boolean recycleOnRemove = false;

  /** LRU cache instance. */
  private final LruCache<String, Bitmap> cache;

  /** SDK utilities. */
  final SDKDependentUtils sdkUtils;

  public SupportLruImageMemoryCache(final Context context) {
    this.sdkUtils = AppUtils.getSdkDependentUtils();
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
      @Override
      protected void entryRemoved(final boolean evicted, final String key, final Bitmap oldValue, final Bitmap newValue) {
        if (recycleOnRemove) { oldValue.recycle(); }
      }
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
  public void remove(final String url, final boolean recycle) {
    final Bitmap bitmap = cache.remove(url);
    if (recycle && bitmap != null) { bitmap.recycle(); }
  }

  @Override
  public void clear(final boolean recycle) {
    synchronized (this) {
      recycleOnRemove = true;
      cache.evictAll();
      recycleOnRemove = false;
    }
  }

  @Override
  public void flushResources(final BeansContainer beansContainer) {
    // do no recycle bitmaps since some of them can be in use
    clear(false);
    Log.i(BEAN_NAME, "Images memory cache flushed");
  }

}
