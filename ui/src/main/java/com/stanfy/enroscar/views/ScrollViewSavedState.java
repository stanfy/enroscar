package com.stanfy.enroscar.views;

import android.os.Parcel;
import android.os.Parcelable;
import android.view.View.BaseSavedState;

/**
 * Saved state for scroll views.
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
class ScrollViewSavedState extends BaseSavedState {

  /** Creator. */
  public static final Creator<ScrollViewSavedState> CREATOR = new Creator<ScrollViewSavedState>() {
    @Override
    public ScrollViewSavedState createFromParcel(final Parcel source) { return new ScrollViewSavedState(source); }
    @Override
    public ScrollViewSavedState[] newArray(final int size) { return new ScrollViewSavedState[size]; }
  };

  /** Saved values. */
  private final float positionX, positionY;
  /** State type. */
  private final int orientation;

  ScrollViewSavedState(final Parcelable parent, final float positionX, final float positionY, final int orientation) {
    super(parent);
    this.positionX = positionX;
    this.positionY = positionY;
    this.orientation = orientation;
  }

  private ScrollViewSavedState(final Parcel in) {
    super(in);
    positionX = in.readFloat();
    positionY = in.readFloat();
    orientation = in.readInt();
  }

  @Override
  public void writeToParcel(final Parcel dest, final int flags) {
    super.writeToParcel(dest, flags);
    dest.writeFloat(positionX);
    dest.writeFloat(positionY);
    dest.writeInt(orientation);
  }

  /** @return the positionX */
  public float getPositionX(final int orienation) {
    return this.orientation == orienation ? positionX : positionY;
  }
  /** @return the positionY */
  public float getPositionY(final int orientation) {
    return this.orientation == orientation ? positionY : positionX;
  }

}
