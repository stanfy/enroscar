package com.stanfy.images;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.stanfy.images.ImagesManager.ImageLoader;

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
  /** Current image URL. Set this field from the GUI thread only! */
  String currentUrl;
  /** Listener. */
  ImagesLoadListener listener;
  /** Current loader. */
  ImageLoader currentLoader;
  /** Loader key. */
  private String loaderKey;

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
    currentUrl = null;
    loaderKey = null;
  }
  final void performCancellingLoader() {
    final String url = currentUrl;
    final ImageLoader loader = currentLoader;
    if (ImagesManager.DEBUG) { Log.d(ImagesManager.TAG, "Cancel URL: " + url + "\nLoader: " + loader); }
    if (loader != null) { loader.removeTarget(this); }
  }

  final void onStart(final ImageLoader loader, final String url) {
    this.currentLoader = loader;
    if (listener != null) { listener.onLoadStart(this, url); }
  }
  final void onFinish(final String url, final Drawable drawable) {
    this.currentLoader = null;
    if (listener != null) { listener.onLoadFinished(this, url, drawable); }
  }
  final void onError(final String url, final Throwable exception) {
    this.currentLoader = null;
    if (listener != null) { listener.onLoadError(this, url, exception); }
  }
  final void onCancel(final String url) {
    this.currentLoader = null;
    if (listener != null) { listener.onLoadCancel(this, url); }
    this.currentUrl = null;
  }

  /* parameters */
  
  public abstract int getRequiredWidth();
  
  public abstract int getRequiredHeight();
  
  public boolean isDynamicSize() { return getRequiredWidth() <= 0 || getRequiredHeight() <= 0; }
  
  public boolean isMatchingParentButNotMeasured() { return false; }
  
  public Drawable getLoadingImage() { return null; }
  
  public int getImageType() { return 0; }
  
  public int getSourceDensity() { return -1; }
  
  String getLoaderKey() {
    if (loaderKey == null) {
      loaderKey = currentUrl + "!" + getRequiredWidth() + "x" + getRequiredHeight();
    }
    return loaderKey;
  }

  /* options */
  
  public boolean skipScaleBeforeCache() { return false; }
  
  public boolean skipLoadingImage() { return false; }
  
  public boolean useSampling() { return true; }
  
  public boolean allowSmallImagesFromCache() { return false; }
  
}
