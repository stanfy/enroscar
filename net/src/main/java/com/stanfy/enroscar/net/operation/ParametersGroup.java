package com.stanfy.enroscar.net.operation;

import java.util.LinkedList;

import android.os.Parcel;

/**
 * Group of parameters.
 * @author Roman Mazur - Stanfy (http://www.stanfy.com)
 */
public class ParametersGroup extends Parameter {

  /** Creator. */
  public static final Creator<ParametersGroup> CREATOR = new Creator<ParametersGroup>() {
    @Override
    public ParametersGroup createFromParcel(final Parcel source) {
      final ParametersGroup result = new ParametersGroup();
      result.loadFromParcel(source);
      return result;
    }
    @Override
    public ParametersGroup[] newArray(final int size) { return new ParametersGroup[size]; }
  };

  /** Child parameters. */
  LinkedList<Parameter> children = new LinkedList<Parameter>();

  /** @return the children */
  public LinkedList<Parameter> getChildren() { return children; }

  public ParameterValue addSimpleParameter(final String name, final String value) {
    final ParameterValue pv = new ParameterValue();
    pv.name = name;
    pv.value = value;
    children.add(pv);
    return pv;
  }

  public void addParameter(final Parameter p) {
    children.add(p);
  }

  @Override
  protected void writeValueToParcel(final Parcel dest, final int flags) {
    final int size = children.size();
    dest.writeInt(size);
    if (size > 0) {
      for (final Parameter p : children) { dest.writeParcelable(p, flags); }
    }
  }

  @Override
  protected void loadValueFromParcel(final Parcel source) {
    final int size = source.readInt();
    if (size > 0) {
      ClassLoader cl = getClass().getClassLoader();
      for (int i = size - 1; i >= 0; i--) {
        final Parameter p = source.readParcelable(cl);
        children.add(p);
      }
    }
  }

  @Override
  public String toString() { return name + ":" + children; }

}
