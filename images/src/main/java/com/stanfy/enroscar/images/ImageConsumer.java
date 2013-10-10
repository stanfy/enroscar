package com.stanfy.enroscar.images;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

/**
 * Image consumer receives an image loaded by {@link ImagesManager}. 
 * <p>
 *   Implementations must override {@link #setImage(Drawable, boolean)} method in order to consume the image. 
 *   It is called by {@link ImagesManager} in main thread when image is loaded.
 * </p> 
 * <p>
 *   Implementations must override {@link #post(Runnable)} method which is used by {@link ImagesManager} 
 *   for interaction with main thread.
 * </p>
 */
public abstract class ImageConsumer {

  /** Context instance. */
  Context context;
  /** Listener. */
  ImagesLoadListener listener;
  /** Current loader. */
  ImageLoader currentLoader;

  /** @param context context instance */
  public ImageConsumer(final Context context) {
    this.context = context;
    reset();
  }

  /* access */
  /** @param listener the listener to set */
  public final void setListener(final ImagesLoadListener listener) { this.listener = listener; }
  /** @return the context */
  public final Context getContext() { return context; }

  /* actions */
  public abstract void setImage(final Drawable d, final boolean animate);
  public void setLoadingImage(final Drawable d) { setImage(d, false); }
  public abstract void post(final Runnable r);

  /** Reset holder state. Must be called from the main thread. */
  void reset() {
    currentLoader = null;
  }

  final void cancelCurrentLoading() {
    final ImageLoader loader = currentLoader;
    if (loader != null) { loader.removeTarget(this); }
  }

  final void onStart(final ImageLoader loader, final String url) {
    this.currentLoader = loader;
    if (listener != null) { listener.onLoadStart(this, url); }
  }
  final void onFinish(final String url, final ImageResult result) {
    this.currentLoader = null;
    if (listener != null) { listener.onLoadFinished(this, url, result); }
  }
  final void onError(final String url, final Throwable exception) {
    this.currentLoader = null;
    if (listener != null) { listener.onLoadError(this, url, exception); }
  }
  final void onCancel(final String url) {
    this.currentLoader = null;
    if (listener != null) { listener.onLoadCancel(this, url); }
  }

  boolean hasUndefinedSize() { return getTargetWidth() <= 0 && getTargetHeight() <= 0; }

  boolean checkBitmapSize(final Bitmap bitmap) {
    final int safeGap = 5;
    int w = getTargetWidth(), h = getTargetHeight();
    return (w <= 0 || w - bitmap.getWidth() < safeGap)
        && (h <= 0 || h - bitmap.getHeight() < safeGap);
  }

  /* parameters */
  
  public final int getRequiredWidth() {
    int targetW = getTargetWidth();
    if (targetW > 0) { return targetW; }
    return context.getResources().getDisplayMetrics().widthPixels;
  }
  public final int getRequiredHeight() {
    int targetH = getTargetHeight();
    if (targetH > 0) { return targetH; }
    return context.getResources().getDisplayMetrics().heightPixels;
  }

  protected abstract int getTargetWidth();
  protected abstract int getTargetHeight();

  public boolean isMatchingParentButNotMeasured() { return false; }
  
  public Drawable getLoadingImage() { return null; }
  
  public boolean skipLoadingImage() { return false; }

  protected void prepareImageRequest(final ImageRequest request) {
    request.setRequiredHeight(getRequiredHeight());
    request.setRequiredWidth(getRequiredWidth());
  }

  public boolean allowSmallImagesFromCache() { return false; }

}
