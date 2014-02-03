package com.stanfy.enroscar.net.operation;

import com.stanfy.enroscar.content.ResponseData;

import java.net.HttpURLConnection;
import java.net.URLConnection;

/**
 * Extends {@link ResponseData} with HTTP-specific fields.
 *
 * @param <T> response data type
 * @author Roman Mazur - Stanfy (http://stanfy.com)
 */
public final class HttpResponseData<T> extends ResponseData<T> {

  /** Request URL. */
  private String url;

  /** Reason from HTTP status line. */
  private String httpReason;

  public HttpResponseData(final String url, final HttpURLConnection connection) {

  }

}
