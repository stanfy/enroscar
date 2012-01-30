package com.stanfy.views;

import com.stanfy.images.ImagesLoadListener;

/**
 * Interface for views that can provide images load listener.
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
public interface ImagesLoadListenerProvider {

  /** @return images load listener */
  ImagesLoadListener getImagesLoadListener();

}
