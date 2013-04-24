package com.stanfy.enroscar.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

/**
 * Linear layout that enables overlapping first children over last ones using negative margins.
 */
public class OverlapLinearLayout extends LinearLayout {

  public OverlapLinearLayout(final Context context) {
    super(context);
    init();
  }

  public OverlapLinearLayout(final Context context, final AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  private void init() {
    setChildrenDrawingOrderEnabled(true);
  }

  @Override
  protected int getChildDrawingOrder(final int childCount, final int i) { return childCount - i - 1; }

}
