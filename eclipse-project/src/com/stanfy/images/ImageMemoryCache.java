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
  private final ConcurrentHashMap<String, SoftReference<CacheRecord>> cacheMap;

  /**
   * Constructor.
   * @param maxSize maximum amount of memory
   */
  public ImageMemoryCache() {
    final int capacity = 40;
    this.cacheMap = new ConcurrentHashMap<String, SoftReference<CacheRecord>>(capacity);
  }

  /**
   * @param url URL
   * @param image image instance
   * @param imageUrl image URL
   */
  public void putElement(final String url, final Bitmap image, final String imageUrl) {
    cacheMap.put(url, new SoftReference<CacheRecord>(new CacheRecord(image, imageUrl)));
  }

  /**
   * @param url URL
   * @return image bitmap
   */
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

  public boolean contains(final String url) { return cacheMap.containsKey(url); }

  public void remove(final String url, final boolean recycle) {
    final SoftReference<CacheRecord> ref = cacheMap.remove(url);
    if (ref == null || !recycle) { return; }
    final CacheRecord record = ref.get();
    if (record != null && record.bitmap != null) { record.bitmap.recycle(); }
  }

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

  /** Cache record. */
  public static class CacheRecord {
    /** A bitmap. */
    private final Bitmap bitmap;
    /** Image URL. */
    private final String imageUrl;

    public CacheRecord(final Bitmap bitmap, final String url) {
      this.bitmap = bitmap;
      this.imageUrl = url;
    }

    /** @return the bitmap */
    public Bitmap getBitmap() { return bitmap; }
    /** @return the imageUrl */
    public String getImageUrl() { return imageUrl; }
  }

}
