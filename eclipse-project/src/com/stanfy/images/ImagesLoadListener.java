package com.stanfy.images;

import android.graphics.drawable.Drawable;

import com.stanfy.images.ImagesManager.ImageHolder;

/**
 * Image load listener.
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
public interface ImagesLoadListener {

  void onLoadStart(final ImageHolder holder, final String url);

  void onLoadFinished(final ImageHolder holder, final String url, final Drawable drawable);

  void onLoadError(final ImageHolder holder, final String url, final Throwable exception);

  void onLoadCancel(final ImageHolder holder, final String url);

}
