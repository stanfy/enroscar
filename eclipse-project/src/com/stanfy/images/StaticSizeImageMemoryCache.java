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

  /** Default clean factor. */
  private static final float DEFAULT_CLEAN_FACTOR = 0.9f;
  /** Cache map. */
  private final LinkedHashMap<String, CacheRecord> cacheMap;

  /** Current size. */
  private int currentSize = 0;

  /** Maximum size. */
  private int maxSize;
  /** Clean factor. */
  private float cleanFactor = DEFAULT_CLEAN_FACTOR;

  /**
   * Constructor.
   * @param maxSize maximum amount of memory
   */
  public StaticSizeImageMemoryCache() {
    final int capacity = 50;
    this.cacheMap = new LinkedHashMap<String, CacheRecord>(capacity);
    final long maxMemory = Runtime.getRuntime().maxMemory();
    maxSize = (int)(maxMemory / 3);
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
      final CacheRecord prev = cacheMap.put(url, record);
      if (prev != null) { currentSize -= prev.size; }
      currentSize += record.size;
      if (currentSize > maxSize) { cleanup((int)(maxSize * cleanFactor)); }
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
      if (r != null) {
        cacheMap.put(url, r);
        return r.bitmap;
      }
      return null;
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
      return currentSize + "/" + maxSize + ":" + cacheMap.size();
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
