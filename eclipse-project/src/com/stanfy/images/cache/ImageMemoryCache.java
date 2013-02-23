package com.stanfy.images.cache;

import android.graphics.Bitmap;

import com.stanfy.enroscar.beans.FlushableBean;

/**
 * Interface of images memory cache.
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
public interface ImageMemoryCache extends FlushableBean {

  /** Bean name. */
  String BEAN_NAME = "ImageMemoryCache";

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
  Bitmap getElement(final String url);

  boolean contains(final String url);

  void remove(final String url, final boolean recycle);

  void clear(final boolean recycle);

}
