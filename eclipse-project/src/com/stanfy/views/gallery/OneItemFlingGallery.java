package com.stanfy.views.gallery;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Gallery that performs fling on one item only.
 * @author Vladislav Lipskiy - Stanfy (http://www.stanfy.com)
 */
public class OneItemFlingGallery extends Gallery {

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

}
