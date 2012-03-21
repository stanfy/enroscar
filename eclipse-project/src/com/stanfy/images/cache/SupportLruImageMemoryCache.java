package com.stanfy.images.cache;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

/**
 * Memory cache based on {@link LruCache}.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 *
 */
public class SupportLruImageMemoryCache implements ImageMemoryCache {

  /** Max size. */
  private static final int MAX_SIZE = 3 * 1024 * 1024;

  /** Recycle on remove flag. */
  private boolean recycleOnRemove = false;

  /** LRU cache instance. */
  private LruCache<String, Bitmap> cache = new LruCache<String, Bitmap>(MAX_SIZE) {
    @Override
    protected int sizeOf(final String key, final Bitmap value) {
      return value.getRowBytes() * value.getHeight();
    };
    @Override
    protected void entryRemoved(final boolean evicted, final String key, final Bitmap oldValue, final Bitmap newValue) {
      if (recycleOnRemove) { oldValue.recycle(); }
    }
  };

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

}
