package com.stanfy.serverapi.request;

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

  protected abstract void writeValueToParcel(final Parcel dest, final int flags);

  protected final void loadFromParcel(final Parcel source) {
    this.name = source.readString();
    loadValueFromParcel(source);
  }

  protected abstract void loadValueFromParcel(final Parcel source);

}
