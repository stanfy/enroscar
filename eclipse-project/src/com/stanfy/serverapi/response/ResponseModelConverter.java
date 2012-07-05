package com.stanfy.serverapi.response;

import java.net.URLConnection;

import com.stanfy.serverapi.RequestMethod.RequestMethodException;
import com.stanfy.serverapi.request.RequestDescription;

/**
 * Converts parsed response model to response data.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
public interface ResponseModelConverter {

  ResponseData<?> toResponseData(final RequestDescription description, final URLConnection connection, final Object model) throws RequestMethodException;

  ResponseData<?> toResponseData(final RequestDescription description, final RequestMethodException error);

}
