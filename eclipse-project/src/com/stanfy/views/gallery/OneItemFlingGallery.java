package com.stanfy.views.gallery;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Gallery that performs fling on one item only.
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
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
    final float divider = 1.5f;
    return super.onFling(e1, e2, velocityX / divider, velocityY);
  }

}
