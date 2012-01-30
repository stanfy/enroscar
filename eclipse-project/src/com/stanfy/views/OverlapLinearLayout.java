package com.stanfy.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

/**
 * Layout with overlapping.
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
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
