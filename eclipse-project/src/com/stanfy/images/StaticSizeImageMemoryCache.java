package com.stanfy.images;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import android.graphics.Bitmap;

/**
 * Images memory cache.
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
public class StaticSizeImageMemoryCache implements ImageMemoryCache {

  /** Max cache size. */
  private static final int DEFUALT_MAX_SIZE = 5 * 1024 * 1024;
  /** Cache map. */
  private final LinkedHashMap<String, CacheRecord> cacheMap;

  /** Current size. */
  private int currentSize = 0;

  /** Maximum size. */
  private int maxSize = DEFUALT_MAX_SIZE;

  /**
   * Constructor.
   * @param maxSize maximum amount of memory
   */
  public StaticSizeImageMemoryCache() {
    final int capacity = 50;
    this.cacheMap = new LinkedHashMap<String, CacheRecord>(capacity);
  }

  /** @param maxSize the maxSize to set */
  public void setMaxSize(final int maxSize) { this.maxSize = maxSize; }

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
  @Override
  public void putElement(final String url, final Bitmap image) {
    synchronized (cacheMap) {
      final CacheRecord record = new CacheRecord(image);
      cacheMap.put(url, record);
      currentSize += record.size;
      final float factor = 0.9f;
      if (currentSize > maxSize) { cleanup((int)(maxSize * factor)); }
    }
  }

  /**
   * @param url URL
   * @return image bitmap
   */
  @Override
  public Bitmap getElement(final String url) {
    final LinkedHashMap<String, CacheRecord> cacheMap = this.cacheMap;
    synchronized (cacheMap) {
      final CacheRecord r = cacheMap.remove(url);
      if (r != null) { cacheMap.put(url, r); }
      return r.bitmap;
    }
  }

  @Override
  public boolean contains(final String url) {
    synchronized (cacheMap) {
      return cacheMap.containsKey(url);
    }
  }

  @Override
  public void remove(final String url, final boolean recycle) {
    final CacheRecord record;
    synchronized (cacheMap) {
      record = cacheMap.remove(url);
      if (record != null) { currentSize -= record.size; }
    }
    if (recycle && record != null && record.bitmap != null) { record.bitmap.recycle(); }
  }

  @Override
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
  private static class CacheRecord {
    /** A bitmap. */
    final Bitmap bitmap;
    /** Size. */
    final int size;

    public CacheRecord(final Bitmap bitmap) {
      this.bitmap = bitmap;
      this.size = bitmap.getRowBytes() * bitmap.getHeight();
    }
  }

}
