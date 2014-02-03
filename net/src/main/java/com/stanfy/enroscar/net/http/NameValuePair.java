package com.stanfy.enroscar.net.http;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author Roman Mazur - Stanfy (http://stanfy.com)
 */
class NameValuePair implements Parcelable {

  /** Name. */
  final String name;
  /** And value. */
  final String value;

  public NameValuePair(String name, String value) {
    if (name == null) {
      throw new IllegalArgumentException("name is null");
    }
    if (value == null) {
      throw new IllegalArgumentException("value is null");
    }
    this.name = name;
    this.value = value;
  }

  public String getName() {
    return name;
  }

  public String getValue() {
    return value;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(name);
    dest.writeString(value);
  }

  @Override
  public boolean equals(final Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof NameValuePair)) {
      return false;
    }
    NameValuePair another = (NameValuePair) o;
    return name.equals(another.name) && value.equals(another.value);
  }

  @Override
  public int hashCode() {
    int result = name.hashCode();
    return 31 * result + value.hashCode();
  }

  @Override
  public String toString() {
    return "{" + name + ": " + value + "}";
  }

}
