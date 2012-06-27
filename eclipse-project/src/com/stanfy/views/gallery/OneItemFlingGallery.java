package com.stanfy.views.gallery;

import android.content.Context;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Gallery that performs fling on one item only.
 * @author Vladislav Lipskiy - Stanfy (http://www.stanfy.com)
 */
public class OneItemFlingGallery extends Gallery {

  /** Scroll and fling selection threshold. */
  private static final long SCROLL_THRESHOLD = 100;

  /** Item selection change timestamp. */
  private long selectionTime;

  public OneItemFlingGallery(final Context context) {
    super(context);
  }
  public OneItemFlingGallery(final Context context, final AttributeSet attrs) {
    super(context, attrs);
  }

  public OneItemFlingGallery(final Context context, final AttributeSet attrs, final int defStyle) {
    super(context, attrs, defStyle);
  }

  @Override
  public boolean onFling(final MotionEvent e1, final MotionEvent e2, final float velocityX, final float velocityY) {
    return velocityX < 0 ? moveNext() : movePrevious();
  }

  @Override
  boolean movePrevious() {
    final long currTime = SystemClock.uptimeMillis();
    final int offset = currTime - selectionTime < SCROLL_THRESHOLD ? 0 : 1;
    if (mItemCount > 0 && mSelectedPosition > 0) {
      scrollToChild(mSelectedPosition - mFirstPosition - offset);
      return true;
    } else {
      return false;
    }
  }

  @Override
  boolean moveNext() {
    final long currTime = SystemClock.uptimeMillis();
    final int offset = currTime - selectionTime < SCROLL_THRESHOLD ? 0 : 1;
    if (mItemCount > 0 && mSelectedPosition < mItemCount - 1) {
      scrollToChild(mSelectedPosition - mFirstPosition + offset);
      return true;
    } else {
      return false;
    }
  }

  @Override
  void setSelectedPositionInt(final int position) {
    super.setSelectedPositionInt(position);
    selectionTime = SystemClock.uptimeMillis();
  }

}
