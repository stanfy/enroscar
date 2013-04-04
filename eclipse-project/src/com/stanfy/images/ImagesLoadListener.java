package com.stanfy.images;

import android.graphics.drawable.Drawable;


/**
 * Image load listener.
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
public interface ImagesLoadListener {

  void onLoadStart(final ImageConsumer holder, final String url);

  void onLoadFinished(final ImageConsumer holder, final String url, final Drawable drawable);

  void onLoadError(final ImageConsumer holder, final String url, final Throwable exception);

  void onLoadCancel(final ImageConsumer holder, final String url);

}
