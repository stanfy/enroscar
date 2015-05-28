package com.stanfy.enroscar.views;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ViewFlipper;

/**
 * Works around Android Bug 6191 by catching IllegalArgumentException after
 * detached from the window.
 *
 * @author Eric Burke (eric@squareup.com)
 * @author Olexandr Tereshchuk - Stanfy (http://www.stanfy.com)
 */
public class SafeViewFlipper extends ViewFlipper {
  
  public SafeViewFlipper(final Context context) {
    super(context);
  }

  public SafeViewFlipper(final Context context, final AttributeSet attrs) {
    super(context, attrs);
  }

  /**
   * Workaround for Android Bug 6191:
   * http://code.google.com/p/android/issues/detail?id=6191
   * ViewFlipper occasionally throws an IllegalArgumentException after
   * screen rotations.
   */
  @Override
  protected void onDetachedFromWindow() {
    try {
      super.onDetachedFromWindow();
    } catch (IllegalArgumentException e) {
      Log.w(VIEW_LOG_TAG, "SafeViewFlipper ignoring IllegalArgumentException");

      // Call stopFlipping() in order to kick off updateRunning()
      stopFlipping();
    }
  }
}
