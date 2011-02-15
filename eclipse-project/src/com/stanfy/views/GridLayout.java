package com.stanfy.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.GridLayoutAnimationController;

/**
 * A GridLayout positions its children in a static grid, defined by a fixed number of rows
 * and columns. The size of the rows and columns is dynamically computed depending on the
 * size of the GridLayout itself. As a result, GridLayout children's layout parameters
 * are ignored.
 *
 * The number of rows and columns are specified in XML using the attributes android:numRows
 * and android:numColumns.
 *
 * The GridLayout cannot be used when its size is unspecified.
 * @author Romain Guy (Android GUI Developer)
 * @author Roman Mazur - Stanfy (http://www.stanfy.com)
 */
public class GridLayout extends ViewGroup {
  /** Number of columns. */
  private int numColumns;
  /** Number of rows. */
  private int numRows;

  /** Column width. */
  private int columnWidth;
  /** Row height. */
  private int rowHeight;

  public GridLayout(final Context context) {
    this(context, null);
  }

  public GridLayout(final Context context, final AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public GridLayout(final Context context, final AttributeSet attrs, final int defStyle) {
    super(context, attrs, defStyle);
    final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.GridLayout, defStyle, 0);

    numColumns = a.getInt(R.styleable.GridLayout_numColumns, 2);
    numRows = a.getInt(R.styleable.GridLayout_numRows, 2);

    a.recycle();
  }

  @Override
  protected void attachLayoutAnimationParameters(final View child, final ViewGroup.LayoutParams params, final int index, final int count) {
    GridLayoutAnimationController.AnimationParameters animationParams =
      (GridLayoutAnimationController.AnimationParameters)params.layoutAnimationParameters;

    if (animationParams == null) {
      animationParams = new GridLayoutAnimationController.AnimationParameters();
      params.layoutAnimationParameters = animationParams;
    }

    animationParams.count = count;
    animationParams.index = index;
    animationParams.columnsCount = numColumns;
    animationParams.rowsCount = numRows;

    animationParams.column = index % numColumns;
    animationParams.row = index / numColumns;
  }

  @Override
  protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
    final int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
    final int widthSpecSize =  MeasureSpec.getSize(widthMeasureSpec);

    final int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
    final int heightSpecSize =  MeasureSpec.getSize(heightMeasureSpec);

    if (widthSpecMode == MeasureSpec.UNSPECIFIED || heightSpecMode == MeasureSpec.UNSPECIFIED) {
      throw new IllegalStateException("GridLayout cannot have UNSPECIFIED dimensions");
    }

    final int width = widthSpecSize - getPaddingLeft() - getPaddingRight();
    final int height = heightSpecSize - getPaddingTop() - getPaddingBottom();

    final int cw = width / numColumns;
    columnWidth = cw;
    final int rh = height / numRows;
    rowHeight = rh;

    final int count = getChildCount();

    for (int i = 0; i < count; i++) {
      final View child = getChildAt(i);

      final int childWidthSpec = MeasureSpec.makeMeasureSpec(cw, MeasureSpec.AT_MOST);
      final int childheightSpec = MeasureSpec.makeMeasureSpec(rh, MeasureSpec.AT_MOST);

      child.measure(childWidthSpec, childheightSpec);
    }

    setMeasuredDimension(widthSpecSize, heightSpecSize);
  }

  @Override
  protected void onLayout(final boolean changed, final int l, final int t, final int r, final int b) {
    final int columns = numColumns;
    final int paddingLeft = getPaddingLeft();
    final int paddingTop = getPaddingTop();
    final int cw = columnWidth, hcw = cw >> 1;
    final int rh = rowHeight, hrh = rh >> 1;
    final int count = getChildCount();

    for (int i = 0; i < count; i++) {
      final View child = getChildAt(i);
      if (child.getVisibility() != GONE) {
        final int column = i % columns;
        final int row = i / columns;

        final LayoutParams params = child.getLayoutParams();
        if (params.width == LayoutParams.FILL_PARENT) {
          final int childLeft = paddingLeft + column * cw;
          final int childTop = paddingTop + row * rh;
          child.layout(childLeft, childTop, childLeft + child.getMeasuredWidth(),
              childTop + child.getMeasuredHeight());
        } else {
          final int centerX = paddingLeft + column * cw + hcw;
          final int centerY = paddingTop + row * rh + hrh;
          MainWithBubbleLayout.layoutCenter(child, centerX, centerY);
        }
      }
    }
  }
}
