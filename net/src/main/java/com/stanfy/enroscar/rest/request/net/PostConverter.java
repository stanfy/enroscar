package com.stanfy.enroscar.rest.request.net;

import android.content.Context;

import com.stanfy.enroscar.net.UrlConnectionWrapper;
import com.stanfy.enroscar.rest.request.RequestDescription;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URLConnection;

/**
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
public abstract class PostConverter extends BaseRequestDescriptionConverter {

  /** Default content type. */
  private final String contentType;

  public PostConverter(final RequestDescription requestDescription, final Context context, final String contentType) {
    super(requestDescription, context);
    this.contentType = contentType;
  }

  /**
   * @return URL used as a parameter for {@link com.stanfy.enroscar.net.UrlConnectionBuilder#setUrl(String)}
   */
  protected String getRequestUrl() {
    return buildUri().build().toString();
  }
  
  @Override
  public URLConnection prepareConnectionInstance() throws IOException {
    final URLConnection connection = prepareUrlConnectionBuilder()
        .setUrl(getRequestUrl())
        .create();

    URLConnection core = UrlConnectionWrapper.unwrap(connection);
    if (core instanceof HttpURLConnection) {
      ((HttpURLConnection) core).setRequestMethod("POST");
    }

    connection.setDoInput(true);
    connection.setDoOutput(true);

    final String rdContentType = getRequestDescription().getContentType();
    if (rdContentType == null) {
      connection.addRequestProperty("Content-Type", this.contentType);
    }

    return connection;
  }

}
