package com.stanfy.serverapi.response;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URLConnection;

import com.stanfy.net.UrlConnectionWrapper;
import com.stanfy.serverapi.ErrorCodes;
import com.stanfy.serverapi.RequestMethod.RequestMethodException;
import com.stanfy.serverapi.request.RequestDescription;

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
        data.setErrorCode(code == HttpURLConnection.HTTP_OK ? 0 : ErrorCodes.ERROR_CODE_SERVER_COMUNICATION);
        data.setMessage(code + " " + http.getResponseMessage());
      } catch (final IOException e) {
        throw new RequestMethodException(e);
      }
    }
    return data;
  }

  @Override
  public ResponseData<?> toResponseData(final RequestDescription description, final RequestMethodException error) {
    final ResponseData<?> data = new ResponseData<Object>();
    data.setErrorCode(error.isConnectionError() ? ErrorCodes.ERROR_CODE_CONNECTION : ErrorCodes.ERROR_CODE_SERVER_COMUNICATION);
    data.setMessage(error.getMessage());
    return data;
  }

}
