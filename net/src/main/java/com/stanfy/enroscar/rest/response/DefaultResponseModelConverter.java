package com.stanfy.enroscar.rest.response;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URLConnection;

import android.os.Build;

import com.stanfy.enroscar.net.UrlConnectionWrapper;
import com.stanfy.enroscar.net.cache.CacheControlUrlConnection;
import com.stanfy.enroscar.rest.ErrorCodes;
import com.stanfy.enroscar.rest.RequestMethod.RequestMethodException;
import com.stanfy.enroscar.rest.request.RequestDescription;

/**
 * Default converter.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
public class DefaultResponseModelConverter implements ResponseModelConverter {

  @Override
  public ResponseData<?> toResponseData(final RequestDescription description, final URLConnection connection, final Object model) throws RequestMethodException {
    final ResponseData<?> data = new ResponseData<Object>(model);
    final URLConnection conn = UrlConnectionWrapper.unwrap(connection);
    if (conn instanceof HttpURLConnection) {
      try {
        final HttpURLConnection http = (HttpURLConnection) conn;
        final int code = http.getResponseCode();
        data.setErrorCode(isHttpCodeOk(code, connection) ? 0 : ErrorCodes.ERROR_CODE_SERVER_COMUNICATION);
        data.setMessage(http.getResponseMessage());
      } catch (final IOException e) {
        throw new RequestMethodException(e);
      }
    }
    return data;
  }

  /**
   * @param code HTTP status code
   * @param connection connection instance
   * @return whether status indicates successful invocation
   */
  protected static boolean isHttpCodeOk(final int code, final URLConnection connection) {
    if (code == HttpURLConnection.HTTP_OK) { return true; }
    final CacheControlUrlConnection cacheControl = UrlConnectionWrapper.getWrapper(connection, CacheControlUrlConnection.class);
    if (cacheControl == null) { return false; }
    return Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH && code == -1;
  }

  @Override
  public ResponseData<?> toResponseData(final RequestDescription description, final RequestMethodException error) {
    final ResponseData<?> data = new ResponseData<Object>();
    data.setErrorCode(error.isConnectionError() ? ErrorCodes.ERROR_CODE_CONNECTION : ErrorCodes.ERROR_CODE_SERVER_COMUNICATION);
    data.setMessage(error.getMessage());
    return data;
  }

}
