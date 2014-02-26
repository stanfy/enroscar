package com.stanfy.enroscar.rest;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.reflect.TypeToken;

/**
 * Represents entity type.
 * @see com.google.gson.reflect.TypeToken
 */
public final class EntityTypeToken implements Parcelable {

  /** Creator. */
  public static final Creator<EntityTypeToken> CREATOR = new Creator<EntityTypeToken>() {
    @Override
    public EntityTypeToken createFromParcel(final Parcel source) {
      return new EntityTypeToken(source);
    }
    @Override
    public EntityTypeToken[] newArray(final int size) {
      return new EntityTypeToken[size];
    }
  };

  /** Type. */
  private final Type type;
  /** Raw class. */
  private final Class<?> rawClass;

  EntityTypeToken(final Parcel in) {
    this.type = (Type) in.readSerializable();
    final String entityClassName = in.readString();
    try {
      this.rawClass = Class.forName(entityClassName);
    } catch (final Exception e) {
      throw new RuntimeException("Cannot load raw class for entity type token. Type: "
          + this.type, e);
    }
  }

  private EntityTypeToken(final Type type) {
    TypeToken<?> token = TypeToken.get(type);
    this.type = token.getType();
    this.rawClass = token.getRawType();
  }

  public static EntityTypeToken fromClassParameter(final Class<?> clazz) {
    return new EntityTypeToken(getParameterType(clazz));
  }

  public static EntityTypeToken fromEntityType(final Type type) {
    return new EntityTypeToken(type);
  }

  private static Type getParameterType(final Class<?> clazz) {
    Class<?> treatedType = clazz;
    Type superclass;
    do {
      superclass = treatedType.getGenericSuperclass();
      treatedType = treatedType.getSuperclass();
    } while (superclass instanceof Class && treatedType != Object.class);
    if (treatedType == Object.class) {
      throw new RuntimeException("Cannot find type parameters for class " + clazz
          + " and its ancestors");
    }
    final ParameterizedType parameterized = (ParameterizedType) superclass;
    return TypeToken.get(parameterized.getActualTypeArguments()[0]).getType();
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
