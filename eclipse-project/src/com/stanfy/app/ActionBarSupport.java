package com.stanfy.app;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;

import com.stanfy.Destroyable;
import com.stanfy.app.activities.ActionBarConfigurator;
import com.stanfy.views.R;

/**
 * Action bar support for activity.
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
public class ActionBarSupport implements Destroyable {

  /** Logging tag. */
  protected static final String TAG = "ActionBar";

  /** Main container. */
  private View mainActionBarContainer;

  /** @return spinner view (indeterminate progress) */
  protected View getNetworkSpinner() { return null; }

  /**
   * Show or hide network activity indicator.
   * @param value progress visibility
   */
  public void setProgressVisibility(final boolean value) {
    final View s = getNetworkSpinner();
    if (s == null) { return; }
  }

  void doInitialize(final Activity activity) {
    if (activity == null) { return; }
    initialize(activity);
    if (activity instanceof ActionBarConfigurator) {
      if (mainActionBarContainer == null) { Log.w(TAG, "Main action bar container is not defined!"); }
      ((ActionBarConfigurator) activity).onInitializeActionBar(this);
    }
  }

  /**
   * Initialize action bar.
   * @param activity owning activity
   */
  public void initialize(final Activity activity) {
    // nothing
  }

  @Override
  public void destroy() {
    if (mainActionBarContainer != null) {
      mainActionBarContainer.clearAnimation();
    }
    mainActionBarContainer = null;
  }

  /** @return the mainActionBarContainer */
  public View getMainActionBarContainer() { return mainActionBarContainer; }

  /** @param mainActionBarContainer the mainActionBarContainer to set */
  protected void setMainActionBarContainer(final View mainActionBarContainer) {
    this.mainActionBarContainer = mainActionBarContainer;
  }

  /**
   * @see #setFullScreen(boolean, boolean, long)
   * @param fullScreen full screen mode flag
   * @param animate whether to use animation
   */
  public void setFullScreen(final boolean fullScreen, final boolean animate) {
    setFullScreen(fullScreen, animate, -1);
  }
  /**
   * Switch full screen mode (remove or add action bar).
   * @param fullScreen full screen mode flag
   * @param animate whether to use animation
   * @param duration animation millis
   */
  public void setFullScreen(final boolean fullScreen, final boolean animate, final long duration) {
    if (mainActionBarContainer == null) { return; }
    final boolean currentFs = mainActionBarContainer.getVisibility() == View.GONE;
    if (currentFs == fullScreen) { return; }
    mainActionBarContainer.clearAnimation();
    if (animate) {
      final Animation animation = AnimationUtils.loadAnimation(mainActionBarContainer.getContext(), fullScreen ? R.anim.disappear_bottom_top : R.anim.appear_top_bottom);
      if (duration > 0) { animation.setDuration(duration); }
      animation.setAnimationListener(new AnimationListener() {
        @Override
        public void onAnimationStart(final Animation animation) { }
        @Override
        public void onAnimationRepeat(final Animation animation) { }
        @Override
        public void onAnimationEnd(final Animation animation) {
          mainActionBarContainer.setVisibility(fullScreen ? View.GONE : View.VISIBLE);
        }
      });
      mainActionBarContainer.startAnimation(animation);
    } else {
      mainActionBarContainer.setVisibility(fullScreen ? View.GONE : View.VISIBLE);
    }
  }

}
