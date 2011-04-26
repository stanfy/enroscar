package com.stanfy.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * @author Roman Mazur - Stanfy (http://www.stanfy.com)
 */
public class Gallery extends android.widget.Gallery implements Runnable {

  /** Fling mode. */
  private boolean flingMode = false;

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
    if (action == MotionEvent.ACTION_UP && !flingMode) {
      // lifted finger action
      final long safeDelay = 100;
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
  public final void run() {
    performItemClick(null, getSelectedItemPosition(), getSelectedItemId());
  }

}
