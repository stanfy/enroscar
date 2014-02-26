package com.stanfy.enroscar.net.operation;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Request parameter.
 * @author Roman Mazur - Stanfy (http://www.stanfy.com)
 */
public abstract class Parameter implements Parcelable {

  /** Parameter name. */
  String name;

  /** @return name  */
  public String getName() { return name; }
  /** @param name the name to set */
  public void setName(final String name) { this.name = name; }

  @Override
  public int describeContents() { return 0; }

  @Override
  public final void writeToParcel(final Parcel dest, final int flags) {
    dest.writeString(name);
    writeValueToParcel(dest, flags);
  }

  /**
   * This method implementations must perform writing parameter value to the parcel.
   * Do not call this method directly. Use {@link Parcel#writeParcelable(Parcelable, int)}.
   * @param dest destination parcel
   * @param flags writing flags
   */
  protected abstract void writeValueToParcel(final Parcel dest, final int flags);

  /**
   * Read this parameter properties from a parcel.
   * @param source source parcel
   */
  protected final void loadFromParcel(final Parcel source) {
    this.name = source.readString();
    loadValueFromParcel(source);
  }

  /**
   * This method implementations must perform reading parameter value from the parcel.
   * Do not call this method directly. Use {@link #loadFromParcel(Parcel)}.
   * @param source source parcel
   */
  protected abstract void loadValueFromParcel(final Parcel source);



}
