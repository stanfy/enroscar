package com.stanfy.images;

import android.graphics.Bitmap;

/**
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
public interface ImageMemoryCache {

  /**
   * @param url URL
   * @param image image instance
   * @param imageUrl image URL
   */
  void putElement(final String url, final Bitmap image);

  /**
   * @param url URL
   * @return image bitmap
   */
  CacheRecord getElement(final String url);

  boolean contains(final String url);

  void remove(final String url, final boolean recycle);

  void clear(final boolean recycle);

  /** Cache record. */
  public static class CacheRecord {
    /** A bitmap. */
    final Bitmap bitmap;
    /** Image URL. */
    final String imageUrl;
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
