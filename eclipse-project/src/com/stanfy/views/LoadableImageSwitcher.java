package com.stanfy.views;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Gallery;
import android.widget.ImageSwitcher;
import android.widget.ImageView.ScaleType;
import android.widget.ProgressBar;
import android.widget.ViewSwitcher;
import android.widget.ViewSwitcher.ViewFactory;

/**
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
@SuppressWarnings("deprecation")
public class LoadableImageSwitcher extends ImageSwitcher implements ViewFactory, OnGestureListener {

  /** Progress bar. */
  private ProgressBar progressBar;

  /** Gallery. */
  private Gallery gallery;

  /** Gesture detector. */
  private GestureDetector gestureDetector;

  /** Navigation listener. */
  private OnNavigationListener nListener;

  /** 'With gallery' mode flag. */
  private boolean withGalleryMode;

  public LoadableImageSwitcher(final Context context) {
    super(context);
    init(context);
  }

  public LoadableImageSwitcher(final Context context, final AttributeSet attrs) {
    super(context, attrs);
    init(context);
  }

  private void init(final Context context) {
    super.setFactory(this);
    gestureDetector = new GestureDetector(context, this);
    gestureDetector.setIsLongpressEnabled(true);
  }

  @Override
  public void setFactory(final ViewFactory factory) { }

  public void setNavigationListener(final OnNavigationListener l) { this.nListener = l; }

  /**
   * @param progressBar the progressBar to set
   */
  public void setProgressBar(final ProgressBar progressBar) { this.progressBar = progressBar; }

  /**
   * @param gallery the gallery to set
   */
  public void setGallery(final Gallery gallery) { this.gallery = gallery; }

  /**
   * @param withGalleryMode the withGalleryMode to set
   */
  public void setWithGalleryMode(final boolean withGalleryMode) { this.withGalleryMode = withGalleryMode; }

  @Override
  public LoadableImageView getNextView() { return (LoadableImageView)super.getNextView(); }

  @Override
  public View makeView() {
    final LoadableImageView image = new LoadableImageView(getContext());
    image.setLayoutParams(new ViewSwitcher.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
    image.setScaleType(ScaleType.FIT_CENTER);
    return image;
  }

  public void setImagePreview(final Drawable d, final String url) {
    final LoadableImageView view = getNextView();
    view.setImageDrawable(d);
    view.setImageURI(null);
    view.setImageURI(Uri.parse(url));
    showNext();
    hideProgress();
  }

  private void showProgress() {
    if (progressBar == null) { return; }
    progressBar.setVisibility(View.VISIBLE);
  }
  private void hideProgress() {
    if (progressBar == null) { return; }
    progressBar.setVisibility(View.GONE);
  }

  public void setProgress() {
    showProgress();
  }

  @Override
  public boolean onTouchEvent(final MotionEvent event) {
    if (withGalleryMode && gallery != null) { gallery.onTouchEvent(event); }
    return gestureDetector.onTouchEvent(event);
  }

  @Override
  public boolean onSingleTapUp(final MotionEvent e) {
    performClick();
    return true;
  }

  @Override
  public boolean onFling(final MotionEvent e1, final MotionEvent e2, final float velocityX, final float velocityY) {
    if (withGalleryMode || nListener == null) { return false; }
    return velocityX < 0 ? nListener.onNext() : nListener.onPrevious();
  }

  @Override
  public boolean onScroll(final MotionEvent e1, final MotionEvent e2, final float distanceX, final float distanceY) { return true; }
  @Override
  public boolean onDown(final MotionEvent e) { return true; }
  @Override
  public void onShowPress(final MotionEvent e) { /* do nothing */ }
  @Override
  public void onLongPress(final MotionEvent e) { /* do nothing */ }

  /**
   * @author Roman Mazur - Stanfy (http://www.stanfy.com)
   */
  public interface OnNavigationListener {
    boolean onNext();
    boolean onPrevious();
  }

}
