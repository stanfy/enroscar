package com.stanfy.enroscar.images;

/**
 * Image load listener.
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
public interface ImagesLoadListener {

  void onLoadStart(final ImageConsumer consumer, final String url);

  void onLoadFinished(final ImageConsumer holder, final String url, final ImageResult result);

  void onLoadError(final ImageConsumer holder, final String url, final Throwable exception);

  void onLoadCancel(final ImageConsumer holder, final String url);

}
