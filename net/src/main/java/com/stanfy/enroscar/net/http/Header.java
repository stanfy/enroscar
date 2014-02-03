package com.stanfy.enroscar.net.http;

import android.os.Parcel;

import com.stanfy.enroscar.net.Predicate;

import java.net.URLConnection;

/**
 * HTTP header: name-value pair.
 *
 * @author Roman Mazur - Stanfy (http://stanfy.com)
 */
public final class Header extends NameValuePair implements Predicate<URLConnection> {

  /** Creator instance. */
  public static final Creator<Header> CREATOR = new Creator<Header>() {
    @Override
    public Header createFromParcel(final Parcel source) {
      return new Header(source.readString(), source.readString());
    }
    @Override
    public Header[] newArray(final int size) {
      return new Header[size];
    }
  };

  public Header(final String name, final String value) {
    super(name, value);
  }

  @Override
  public void apply(final URLConnection connection) {
    connection.addRequestProperty(name, value);
  }

}
