package com.stanfy.enroscar.net.http;

import android.os.Parcel;

import com.stanfy.enroscar.net.Predicate;

import java.net.URLConnection;

/**
 * Request body.
 *
 * @author Roman Mazur - Stanfy (http://stanfy.com)
 */
public class Body implements Predicate<URLConnection> {

  @Override
  public void apply(URLConnection subject) {

  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {

  }
}
