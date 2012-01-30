package com.stanfy.serverapi.request;

import android.os.Parcel;

/**
 * Parameter that has a simple value.
 * @author Roman Mazur - Stanfy (http://www.stanfy.com)
 */
public class ParameterValue extends Parameter {

  /** Creator. */
  public static final Creator<ParameterValue> CREATOR = new Creator<ParameterValue>() {
    @Override
    public ParameterValue createFromParcel(final Parcel source) {
      final ParameterValue result = new ParameterValue();
      result.loadFromParcel(source);
      return result;
    }
    @Override
    public ParameterValue[] newArray(final int size) { return new ParameterValue[size]; }
  };

  /** Parameter value. */
  String value;

  /** @param value the value to set */
  public void setValue(final String value) { this.value = value; }

  /** @return the value */
  public String getValue() { return value; }

  @Override
  protected void writeValueToParcel(final Parcel dest, final int flags) {
    dest.writeString(value);
  }

  @Override
  protected void loadValueFromParcel(final Parcel source) {
    value = source.readString();
  }

  @Override
  public String toString() {
    return name + "=" + value;
  }

}
