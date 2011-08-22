package com.stanfy.images;

import android.database.Cursor;

import com.stanfy.images.model.CachedImage;

/**
 * Cached images DAO.
 * @param <T> cached image type
 * @author Roman Mazur - Stanfy (http://www.stanfy.com)
 */
public interface ImagesDAO<T extends CachedImage> {

  /**
   * @param image image instance to update
   */
  void updateImage(T image);

  /**
   * @param url URL
   * @return cached image
   */
  T getCachedImage(final String url);

  /**
   * @param url URL
   * @return cached image instance
   */
  T createCachedImage(final String url);

  /**
   * @param time current time
   * @return old images
   */
  Cursor getOldImages(final long time);

  /**
   * @param time current time
   * @return count of deleted images
   */
  int deleteOldImages(final long time);

}
