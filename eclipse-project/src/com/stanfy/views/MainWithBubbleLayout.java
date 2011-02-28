package com.stanfy.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/**
 * This layout centers that main view (number one in children list) and locate the bubble (number two) on the top
 * with a specified offset from the center.
 * Its width and height cannot be defined as UNSPECIFIED.
 * @author Roman Mazur - Stanfy (http://www.stanfy.com)
 */
public class MainWithBubbleLayout extends ViewGroup {

  /** Bubble offset. */
  private int bubbleOffsetX, bubbleOffsetY;

  public MainWithBubbleLayout(final Context context) {
    this(context, null);
  }

  public MainWithBubbleLayout(final Context context, final AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public MainWithBubbleLayout(final Context context, final AttributeSet attrs, final int defStyle) {
    super(context, attrs, defStyle);

    final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MainWithBubbleLayout, defStyle, 0);
    bubbleOffsetX = a.getDimensionPixelOffset(R.styleable.MainWithBubbleLayout_bubbleOffsetX, 0);
    bubbleOffsetY = a.getDimensionPixelOffset(R.styleable.MainWithBubbleLayout_bubbleOffsetY, 0);
    a.recycle();
  }

  @Override
  protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
    final int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
    final int widthSpecSize =  MeasureSpec.getSize(widthMeasureSpec);
    final int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
    final int heightSpecSize =  MeasureSpec.getSize(heightMeasureSpec);

    if (widthSpecMode == MeasureSpec.UNSPECIFIED || heightSpecMode == MeasureSpec.UNSPECIFIED) {
      throw new IllegalStateException("MainWithBubbleLayout cannot have UNSPECIFIED dimensions");
    }

    final int childCount = getChildCount();
    for (int i = 0; i < childCount; i++) {
      final View child = getChildAt(i);
      child.measure(MeasureSpec.makeMeasureSpec(widthSpecSize, MeasureSpec.AT_MOST), MeasureSpec.makeMeasureSpec(heightSpecSize, MeasureSpec.AT_MOST));
    }

    setMeasuredDimension(widthSpecSize, heightSpecSize);
  }

  static void layoutCenter(final View view, final int centerX, final int centerY) {
    if (view.getVisibility() == View.GONE) { return; }
    final int w = view.getMeasuredWidth(), h = view.getMeasuredHeight();
    final int x = centerX - (w >> 1), y = centerY - (h >> 1);
    view.layout(x, y, x + w, y + h);
  }

  static void layoutCenterTopWithOffset(final View view, final int centerX, final int centerY, final int offsetX, final int offsetY) {
    if (view.getVisibility() == View.GONE) { return; }
    final int w = view.getMeasuredWidth(), h = view.getMeasuredHeight();
    final int r = centerX + offsetX, l = r - w;
    int t = centerY - offsetY;
    if (t < 0) { t = 0; }
    view.layout(l, t, r, t + h);
  }

  @Override
  protected void onLayout(final boolean changed, final int l, final int t, final int r, final int b) {
    final int childCount = getChildCount();
    if (childCount == 0) { return; }
    final int w = getMeasuredWidth() - getPaddingLeft() - getPaddingRight(), h = getMeasuredHeight() - getPaddingBottom() - getPaddingTop();
    final int centerX = w >> 1, centerY = h >> 1;

    if (childCount == 1) {
      layoutCenter(getChildAt(0), centerX, centerY);
      return;
    }

    for (int i = 0; i < childCount - 1; i++) {
      layoutCenter(getChildAt(i), centerX, centerY);
    }
    final View view = getChildAt(childCount - 1);
    layoutCenterTopWithOffset(view, centerX, centerY, bubbleOffsetX, bubbleOffsetY);
  }

}
