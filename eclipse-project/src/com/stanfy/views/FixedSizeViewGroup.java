package com.stanfy.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/**
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
public abstract class FixedSizeViewGroup extends ViewGroup {

  public FixedSizeViewGroup(final Context context, final AttributeSet attrs, final int defStyle) {
    super(context, attrs, defStyle);
  }

  protected void checkParentSpecs(final int wMode, final int hMode) {
    if (wMode == MeasureSpec.UNSPECIFIED || hMode == MeasureSpec.UNSPECIFIED) {
      throw new IllegalStateException(getClass().getSimpleName() + " cannot have UNSPECIFIED dimensions");
    }
  }

  @Override
  protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
    final int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
    final int widthSpecSize =  MeasureSpec.getSize(widthMeasureSpec);
    final int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
    final int heightSpecSize =  MeasureSpec.getSize(heightMeasureSpec);

    checkParentSpecs(widthSpecMode, heightSpecMode);

    final int childCount = getChildCount();
    for (int i = 0; i < childCount; i++) {
      final View child = getChildAt(i);
      child.measure(MeasureSpec.makeMeasureSpec(widthSpecSize, MeasureSpec.AT_MOST), MeasureSpec.makeMeasureSpec(heightSpecSize, MeasureSpec.AT_MOST));
    }

    setMeasuredDimension(widthSpecSize, heightSpecSize);
  }

}
