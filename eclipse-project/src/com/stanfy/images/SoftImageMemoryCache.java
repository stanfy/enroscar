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
  private final ConcurrentHashMap<String, SoftReference<CacheRecord>> cacheMap;

  /**
   * Constructor.
   * @param maxSize maximum amount of memory
   */
  public SoftImageMemoryCache() {
    final int capacity = 50;
    this.cacheMap = new ConcurrentHashMap<String, SoftReference<CacheRecord>>(capacity);
  }

  @Override
  public void putElement(final String url, final Bitmap image) {
    cacheMap.put(url, new SoftReference<CacheRecord>(new CacheRecord(image, url)));
  }

  @Override
  public CacheRecord getElement(final String url) {
    final ConcurrentHashMap<String, SoftReference<CacheRecord>> cacheMap = this.cacheMap;
    final SoftReference<CacheRecord> ref = cacheMap.get(url);
    if (ref == null) { return null; }
    final CacheRecord record = ref.get();
    if (record == null) {
      cacheMap.remove(url);
      return null;
    }
    return record;
  }

  @Override
  public boolean contains(final String url) { return cacheMap.containsKey(url); }

  @Override
  public void remove(final String url, final boolean recycle) {
    final SoftReference<CacheRecord> ref = cacheMap.remove(url);
    if (ref == null || !recycle) { return; }
    final CacheRecord record = ref.get();
    if (record != null && record.bitmap != null) { record.bitmap.recycle(); }
  }

  @Override
  public void clear(final boolean recycle) {
    final ConcurrentHashMap<String, SoftReference<CacheRecord>> cacheMap = this.cacheMap;
    if (recycle) {
      for (final SoftReference<CacheRecord> ref : cacheMap.values()) {
        final CacheRecord record = ref.get();
        final Bitmap map = record != null ? record.bitmap : null;
        if (map != null) { map.recycle(); }
      }
    }
    cacheMap.clear();
  }

  @Override
  public String toString() { return cacheMap.toString(); }

}
