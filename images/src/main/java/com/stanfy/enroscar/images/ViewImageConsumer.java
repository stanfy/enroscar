package com.stanfy.enroscar.images;

import android.view.View;
import android.view.ViewGroup.LayoutParams;


/**
 * Base class for {@link ImageConsumer}s that display loaded images in a view.
 * @param <T> view type
 */
public abstract class ViewImageConsumer<T extends View> extends ImageConsumer {
  
  /** View instance. */
  private T view;

  public ViewImageConsumer(final T view) {
    super(view.getContext());
    this.view = view;
    notifyAboutViewChanges();
  }
  
  public T getView() { return view; }
  
  /**
   * Method should be when wrapped view gets changes related to this consumer.
   */
  public void notifyAboutViewChanges() {
    final T view = this.view;
    if (view != null && view instanceof ImagesLoadListenerProvider) {
      this.listener = ((ImagesLoadListenerProvider)view).getImagesLoadListener();
    }
  }
  
  @Override
  public int getSourceDensity() {
    if (view instanceof RemoteImageDensityProvider) {
      return ((RemoteImageDensityProvider)view).getSourceDensity();
    }
    return super.getSourceDensity();
  }
  @Override
  public void post(final Runnable r) {
    view.post(r);
  }
  @Override
  public int getRequiredHeight() {
    final View view = this.view;
    final LayoutParams params = view.getLayoutParams();
    if (params == null || params.height == LayoutParams.WRAP_CONTENT) { return -1; }
    final int h = view.getHeight();
    return h > 0 ? h : params.height;
  }
  @Override
  public int getRequiredWidth() {
    final View view = this.view;
    final LayoutParams params = view.getLayoutParams();
    if (params == null || params.width == LayoutParams.WRAP_CONTENT) { return -1; }
    final int w = view.getWidth();
    return w > 0 ? w : params.width;
  }
  @Override
  public boolean isMatchingParentButNotMeasured() {
    final View view = this.view;
    final LayoutParams params = view.getLayoutParams();
    if (params == null) { return false; }
    return params.width == LayoutParams.MATCH_PARENT && view.getWidth() == 0
        || params.height == LayoutParams.MATCH_PARENT && view.getHeight() == 0;
  }
}
