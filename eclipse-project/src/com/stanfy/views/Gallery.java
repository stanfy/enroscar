package com.stanfy.views;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * @author Roman Mazur - Stanfy (http://www.stanfy.com)
 */
public class Gallery extends android.widget.Gallery implements Runnable {

  /** Fling mode. */
  private boolean flingMode = false;

  /** Last selected position. */
  private int lastSelectedPosition = -1;

  public Gallery(final Context context) {
    this(context, null);
  }

  public Gallery(final Context context, final AttributeSet attrs) {
    this(context, attrs, android.R.attr.galleryStyle);
  }

  public Gallery(final Context context, final AttributeSet attrs, final int defStyle) {
    super(context, attrs, defStyle);
  }

  @Override
  public boolean onTouchEvent(final MotionEvent event) {
    flingMode = false;
    final boolean consumed = super.onTouchEvent(event);
    final int action = event.getAction();
    final int currentPosition = getSelectedItemPosition();
    Log.d(VIEW_LOG_TAG, "lastSelectedPosition=" + lastSelectedPosition + ", currentPosiotin=" + currentPosition);
    if (action == MotionEvent.ACTION_UP && !flingMode && lastSelectedPosition != currentPosition) {
      // lifted finger action
      lastSelectedPosition = currentPosition;
      Log.d(VIEW_LOG_TAG, "lastSelectedPosition=" + lastSelectedPosition);
      final long safeDelay = 50;
      postDelayed(this, safeDelay);
    }
    return consumed;
  }

  @Override
  public boolean onFling(final MotionEvent e1, final MotionEvent e2, final float velocityX, final float velocityY) {
    flingMode = true;
    return super.onFling(e1, e2, velocityX, velocityY);
  }

  @Override
  public boolean performItemClick(final View view, final int position, final long id) {
    lastSelectedPosition = -1;
    return super.performItemClick(view, position, id);
  }

  @Override
  public final void run() {
    final int current = getSelectedItemPosition();
    if (lastSelectedPosition == current) {
      performItemClick(null, current, getSelectedItemId());
    }
  }

}
