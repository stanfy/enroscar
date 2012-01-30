package com.stanfy.images;

import java.lang.ref.SoftReference;
import java.util.concurrent.ConcurrentHashMap;

import android.graphics.Bitmap;

/**
 * Images memory cache based on soft references.
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
public class SoftImageMemoryCache implements ImageMemoryCache {

  /** Cache map. */
  private final ConcurrentHashMap<String, SoftReference<Bitmap>> cacheMap;

  /**
   * Constructor.
   * @param maxSize maximum amount of memory
   */
  public SoftImageMemoryCache() {
    final int capacity = 50;
    this.cacheMap = new ConcurrentHashMap<String, SoftReference<Bitmap>>(capacity);
  }

  @Override
  public void putElement(final String url, final Bitmap image) {
    cacheMap.put(url, new SoftReference<Bitmap>(image));
  }

  @Override
  public Bitmap getElement(final String url) {
    final ConcurrentHashMap<String, SoftReference<Bitmap>> cacheMap = this.cacheMap;
    final SoftReference<Bitmap> ref = cacheMap.get(url);
    if (ref == null) { return null; }
    final Bitmap bitmap = ref.get();
    if (bitmap == null) {
      cacheMap.remove(url);
      return null;
    }
    return bitmap;
  }

  @Override
  public boolean contains(final String url) { return cacheMap.containsKey(url); }

  @Override
  public void remove(final String url, final boolean recycle) {
    final SoftReference<Bitmap> ref = cacheMap.remove(url);
    if (ref == null || !recycle) { return; }
    final Bitmap bitmap = ref.get();
    if (bitmap != null) { bitmap.recycle(); }
  }

  @Override
  public void clear(final boolean recycle) {
    final ConcurrentHashMap<String, SoftReference<Bitmap>> cacheMap = this.cacheMap;
    if (recycle) {
      for (final SoftReference<Bitmap> ref : cacheMap.values()) {
        final Bitmap map = ref.get();
        if (map != null) { map.recycle(); }
      }
    }
    cacheMap.clear();
  }

  @Override
  public String toString() { return cacheMap.toString(); }

}
