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
  void putElement(String url, Bitmap image);

  /**
   * @param url URL
   * @return image bitmap
   */
  Bitmap getElement(String url);

  boolean contains(String url);

  Bitmap remove(String url);

  void clear();

}
