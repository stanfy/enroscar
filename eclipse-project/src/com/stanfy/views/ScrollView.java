package com.stanfy.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

/**
 * Scroll view that can save its position.
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
public class ScrollView extends android.widget.ScrollView {

  /** Froze scroll position flag. */
  private boolean frozeScrollPosition;

  public ScrollView(final Context context) {
    this(context, null);
  }

  public ScrollView(final Context context, final AttributeSet attrs) {
    super(context, attrs);
    init(context, attrs);
  }

  public ScrollView(final Context context, final AttributeSet attrs, final int defStyle) {
    super(context, attrs, defStyle);
    init(context, attrs);
  }

  private void init(final Context context, final AttributeSet attrs) {
    final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ScrollView);
    final boolean frozeSPos = a.getBoolean(R.styleable.ScrollView_frozeScrollPosition, true);
    a.recycle();
    setFrozeScrollPosition(frozeSPos);
  }

  /** @param frozeScrollPosition the frozeScrollPosition to set */
  public void setFrozeScrollPosition(final boolean frozeScrollPosition) { this.frozeScrollPosition = frozeScrollPosition; }
  /** @return the frozeScrollPosition */
  public boolean isFrozeScrollPosition() { return frozeScrollPosition; }

  @Override
  protected Parcelable onSaveInstanceState() {
    if (!frozeScrollPosition) { return super.onSaveInstanceState(); }
    final Parcelable parent = super.onSaveInstanceState();
    return new ScrollViewSavedState(parent, (float)getScrollX() / getContentWidth(), (float)getScrollY() / getContentHeight(), LinearLayout.VERTICAL);
  }

  @Override
  protected void onRestoreInstanceState(final Parcelable state) {
    if (!frozeScrollPosition) { super.onRestoreInstanceState(state); }
    final ScrollViewSavedState myState = (ScrollViewSavedState)state;
    super.onRestoreInstanceState(myState.getSuperState());
    post(new Runnable() {
      @Override
      public void run() {
        scrollTo(
            (int)(myState.getPositionX(LinearLayout.VERTICAL) * getContentWidth()),
            (int)(myState.getPositionY(LinearLayout.VERTICAL) * getContentHeight())
        );
      }
    });
  }

  public int getContentWidth() { return getWidth(); }
  public int getContentHeight() {
    if (getChildCount() == 0) { return getHeight(); }
    final View child = getChildAt(0);
    return child.getHeight();
  }

}
