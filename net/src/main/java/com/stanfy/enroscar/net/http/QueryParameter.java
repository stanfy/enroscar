package com.stanfy.enroscar.net.http;

import android.net.Uri;
import android.os.Parcel;

import com.stanfy.enroscar.net.Predicate;

/**
 * URL query parameter.
 *
 * @author Roman Mazur - Stanfy (http://stanfy.com)
 */
public class QueryParameter extends NameValuePair implements Predicate<Uri.Builder> {

  /** Creator instance. */
  public static final Creator<QueryParameter> CREATOR = new Creator<QueryParameter>() {
    @Override
    public QueryParameter createFromParcel(final Parcel source) {
      return new QueryParameter(source.readString(), source.readString());
    }
    @Override
    public QueryParameter[] newArray(final int size) {
      return new QueryParameter[size];
    }
  };

  public QueryParameter(final String name, final String value) {
    super(name, value);
  }

  @Override
  public void apply(final Uri.Builder builder) {
    builder.appendQueryParameter(name, value);
  }

}
