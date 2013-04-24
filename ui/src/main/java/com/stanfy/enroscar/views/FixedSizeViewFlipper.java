package com.stanfy.enroscar.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

/**
 * Views flipper that fixes its size according to the first view.
 * @author Roman Mazur - Stanfy (http://www.stanfy.com)
 */
public class FixedSizeViewFlipper extends SafeViewFlipper {

  public FixedSizeViewFlipper(final Context context) {
    super(context);
  }

  public FixedSizeViewFlipper(final Context context, final AttributeSet attrs) {
    super(context, attrs);
  }

  @Override
  protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
    final int childCount = getChildCount();
    if (childCount < 2) {
      super.onMeasure(widthMeasureSpec, heightMeasureSpec);
      return;
    }
    final View mainChild = getChildAt(0);
    measureChildWithMargins(mainChild, widthMeasureSpec, 0, heightMeasureSpec, 0);
    final int mw = mainChild.getMeasuredWidth(), mh = mainChild.getMeasuredHeight();
    measureChildren(MeasureSpec.makeMeasureSpec(mw, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(mh, MeasureSpec.EXACTLY));
    setMeasuredDimension(mw, mh);
  }

}
