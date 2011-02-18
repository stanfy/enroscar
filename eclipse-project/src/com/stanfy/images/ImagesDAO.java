package com.stanfy.images;

import com.stanfy.images.model.CachedImage;

/**
 * Cached images DAO.
 * @author Roman Mazur - Stanfy (http://www.stanfy.com)
 */
public interface ImagesDAO {

  /**
   * @param image image instance to update
   */
  void updateImage(CachedImage image);

  /**
   * @param url URL
   * @return cached image
   */
  CachedImage getCachedImage(final String url);

}
