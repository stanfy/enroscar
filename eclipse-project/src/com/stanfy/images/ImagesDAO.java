package com.stanfy.images;

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

}
