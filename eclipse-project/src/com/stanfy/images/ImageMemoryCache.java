package com.stanfy.images;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import android.graphics.Bitmap;

/**
 * Images memory cache.
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
public class ImageMemoryCache {

  /** Max cache size. */
  private static final int MAX_SIZE = 3 * 1024 * 1024;
  /** Cache map. */
  private final LinkedHashMap<String, CacheRecord> cacheMap;

  /** Current size. */
  private int currentSize = 0;

  /**
   * Constructor.
   * @param maxSize maximum amount of memory
   */
  public ImageMemoryCache() {
    final int capacity = 50;
    this.cacheMap = new LinkedHashMap<String, CacheRecord>(capacity);
  }

  private void cleanup(final int requiredSize) {
    int currentSize = this.currentSize;
    final Iterator<Entry<String, CacheRecord>> iterator = cacheMap.entrySet().iterator();
    while (currentSize > requiredSize && iterator.hasNext()) {
      final CacheRecord record = iterator.next().getValue();
      iterator.remove();
      currentSize -= record.size;
    }
    this.currentSize = currentSize;
  }

  /**
   * @param url URL
   * @param image image instance
   * @param imageUrl image URL
   */
  public void putElement(final String url, final Bitmap image) {
    synchronized (cacheMap) {
      final CacheRecord record = new CacheRecord(image, url);
      cacheMap.put(url, record);
      currentSize += record.size;
      if (currentSize > MAX_SIZE) { cleanup(MAX_SIZE / 2); }
    }
  }

  /**
   * @param url URL
   * @return image bitmap
   */
  public CacheRecord getElement(final String url) {
    final LinkedHashMap<String, CacheRecord> cacheMap = this.cacheMap;
    synchronized (cacheMap) {
      final CacheRecord r = cacheMap.remove(url);
      if (r != null) { cacheMap.put(url, r); }
      return r;
    }
  }

  public boolean contains(final String url) {
    synchronized (cacheMap) {
      return cacheMap.containsKey(url);
    }
  }

  public void remove(final String url, final boolean recycle) {
    final CacheRecord record;
    synchronized (cacheMap) {
      record = cacheMap.remove(url);
      currentSize -= record.size;
    }
    if (recycle && record != null && record.bitmap != null) { record.bitmap.recycle(); }
  }

  public void clear(final boolean recycle) {
    final LinkedHashMap<String, CacheRecord> cacheMap = this.cacheMap;
    synchronized (cacheMap) {
      if (recycle) {
        for (final CacheRecord record : cacheMap.values()) {
          final Bitmap map = record != null ? record.bitmap : null;
          if (map != null) { map.recycle(); }
        }
      }
      cacheMap.clear();
      currentSize = 0;
    }
  }

  @Override
  public String toString() {
    synchronized (cacheMap) {
      return cacheMap.toString();
    }
  }

  /** Cache record. */
  public static class CacheRecord {
    /** A bitmap. */
    private final Bitmap bitmap;
    /** Image URL. */
    private final String imageUrl;
    /** Size. */
    final int size;

    public CacheRecord(final Bitmap bitmap, final String url) {
      this.bitmap = bitmap;
      this.imageUrl = url;
      this.size = bitmap.getRowBytes() * bitmap.getHeight();
    }

    /** @return the bitmap */
    public Bitmap getBitmap() { return bitmap; }
    /** @return the imageUrl */
    public String getImageUrl() { return imageUrl; }
  }

}
