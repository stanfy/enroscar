package com.stanfy.images;

import java.lang.ref.SoftReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.graphics.Bitmap;

/**
 * Images memory cache.
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
public class ImageMemoryCache {

  /** Cache map. */
  private Map<String, SoftReference<Bitmap>> cacheMap;

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
  public synchronized void putElement(final String url, final Bitmap image) {
    cacheMap.put(url, new SoftReference<Bitmap>(image));
  }

  /**
   * @param url URL
   * @return image
   */
  public Bitmap getElement(final String url) {
    final SoftReference<Bitmap> ref = cacheMap.get(url);
    return ref != null ? ref.get() : null;
  }

  public boolean contains(final String url) {
    return cacheMap.containsKey(url);
  }

  public void remove(final String url) {
    cacheMap.remove(url);
  }

  public void clear() {
    cacheMap.clear();
  }

}
