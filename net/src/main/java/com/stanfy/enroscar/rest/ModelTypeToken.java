package com.stanfy.enroscar.rest;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import android.annotation.SuppressLint;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.internal.$Gson$Types;

/**
 * Have similar functions to {@link com.google.gson.reflect.TypeToken}.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 *
 */
@SuppressLint("FieldGetter")
public final class ModelTypeToken implements Parcelable {

  /** Creator. */
  public static final Creator<ModelTypeToken> CREATOR = new Creator<ModelTypeToken>() {
    @Override
    public ModelTypeToken createFromParcel(final Parcel source) { return new ModelTypeToken(source); }
    @Override
    public ModelTypeToken[] newArray(final int size) { return new ModelTypeToken[size]; }
  };

  /** Type. */
  private final Type type;
  /** Raw class. */
  private final Class<?> rawClass;

  ModelTypeToken(final Parcel in) {
    this.type = (Type)in.readSerializable();
    final String modelClassName = in.readString();
    try {
      this.rawClass = Class.forName(modelClassName);
    } catch (final Exception e) {
      throw new RuntimeException("Cannot load raw class for model type token", e);
    }
  }

  private ModelTypeToken(final Class<?> clazz) {
    this(getType(clazz));
  }

  private ModelTypeToken(final Type type) {
    this.type = type;
    this.rawClass = $Gson$Types.getRawType(this.type);
  }

  public static ModelTypeToken fromRequestBuilderClass(final Class<?> clazz) {
    return new ModelTypeToken(clazz);
  }
  public static ModelTypeToken fromModelType(final Type type) {
    return new ModelTypeToken(type);
  }

  private static Type getType(final Class<?> clazz) {
    Class<?> treatedType = clazz;
    Type superclass;
    do {
      superclass = treatedType.getGenericSuperclass();
      treatedType = treatedType.getSuperclass();
    } while (superclass instanceof Class && treatedType != Object.class);
    if (treatedType == Object.class) {
      throw new RuntimeException("Missing type parameter.");
    }
    final ParameterizedType parameterized = (ParameterizedType) superclass;
    return $Gson$Types.canonicalize(parameterized.getActualTypeArguments()[0]);
  }

  @Override
  public int describeContents() { return 0; }

  @Override
  public void writeToParcel(final Parcel dest, final int flags) {
    dest.writeSerializable((Serializable)type);
    dest.writeString(this.rawClass.getName());
  }

  public Type getType() { return type; }
  public Class<?> getRawClass() { return rawClass; }

}
