package com.stanfy.enroscar.net.http;

import android.net.Uri;
import android.os.Parcel;

import com.stanfy.enroscar.net.Predicate;
import com.stanfy.enroscar.net.UrlConnectionBuilder;

import java.net.URLConnection;
import java.util.List;

/**
 * @author Roman Mazur - Stanfy (http://stanfy.com)
 */
public class Request implements Predicate<UrlConnectionBuilder> {

  /** Request URL. */
  private String url;

  /** HTTP method. */
  private Method method;

  /** Headers. */
  private List<Header> header;

  /** Query parameters. */
  private List<QueryParameter> queryParameters;

  /** Body. */
  private Body body;

  @Override
  public void apply(final UrlConnectionBuilder builder) {
    Uri.Builder uriBuilder = Uri.parse(url).buildUpon();
    if (queryParameters != null && !queryParameters.isEmpty()) {
      for (QueryParameter p : queryParameters) {
        p.apply(uriBuilder);
      }
    }
    builder.setUrl(uriBuilder.build());
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {

  }
}
