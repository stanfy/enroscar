package com.stanfy.images;

import java.lang.ref.SoftReference;
import java.util.concurrent.ConcurrentHashMap;

import android.graphics.Bitmap;

/**
 * Images memory cache.
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
public class ImageMemoryCache {

  /** Cache map. */
  private final ConcurrentHashMap<String, SoftReference<Bitmap>> cacheMap;

  /**
   * Constructor.
   * @param maxSize maximum amount of memory
   */
  public ImageMemoryCache() {
    final int max = 40;
    this.cacheMap = new ConcurrentHashMap<String, SoftReference<Bitmap>>(max);
  }

  /**
   * @param url URL
   * @param image image instance
   */
  public void putElement(final String url, final Bitmap image) {
    cacheMap.put(url, new SoftReference<Bitmap>(image));
  }

  /**
   * @param url URL
   * @return image bitmap
   */
  public Bitmap getElement(final String url) {
    final ConcurrentHashMap<String, SoftReference<Bitmap>> cacheMap = this.cacheMap;
    final SoftReference<Bitmap> ref = cacheMap.get(url);
    if (ref == null) { return null; }
    final Bitmap bm = ref.get();
    if (bm == null) {
      cacheMap.remove(url);
      return null;
    }
    return bm;
  }

  public boolean contains(final String url) { return cacheMap.containsKey(url); }

  public void remove(final String url) {
    final SoftReference<Bitmap> ref = cacheMap.remove(url);
    if (ref == null) { return; }
    final Bitmap map = ref.get();
    if (map != null) { map.recycle(); }
  }

  public void clear() {
    final ConcurrentHashMap<String, SoftReference<Bitmap>> cacheMap = this.cacheMap;
    for (final SoftReference<Bitmap> ref : cacheMap.values()) {
      final Bitmap map = ref.get();
      if (map != null) { map.recycle(); }
    }
    cacheMap.clear();
  }

  @Override
  public String toString() { return cacheMap.toString(); }

}
