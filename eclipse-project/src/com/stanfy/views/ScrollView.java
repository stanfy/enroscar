package com.stanfy.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;

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

  @Override
  protected Parcelable onSaveInstanceState() {
    if (!frozeScrollPosition) { return super.onSaveInstanceState(); }
    final Parcelable parent = super.onSaveInstanceState();
    return new SavedState(parent, getScrollX(), getScrollY());
  }

  @Override
  protected void onRestoreInstanceState(final Parcelable state) {
    if (!frozeScrollPosition) { super.onRestoreInstanceState(state); }
    final SavedState myState = (SavedState)state;
    super.onRestoreInstanceState(myState.getSuperState());
    scrollTo(myState.positionX, myState.positionY);
  }

  /**
   * Saved state for our scroll view.
   * @author Roman Mazur (Stanfy - http://www.stanfy.com)
   */
  static class SavedState extends BaseSavedState {

    /** Creator. */
    public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
      @Override
      public SavedState createFromParcel(final Parcel source) { return new SavedState(source); }
      @Override
      public SavedState[] newArray(final int size) { return new SavedState[size]; }
    };

    /** Saved values. */
    final int positionX, positionY;

    SavedState(final Parcelable parent, final int positionX, final int positionY) {
      super(parent);
      this.positionX = positionX;
      this.positionY = positionY;
    }

    private SavedState(final Parcel in) {
      super(in);
      positionX = in.readInt();
      positionY = in.readInt();
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
      super.writeToParcel(dest, flags);
      dest.writeInt(positionX);
      dest.writeInt(positionY);
    }

  }

}
