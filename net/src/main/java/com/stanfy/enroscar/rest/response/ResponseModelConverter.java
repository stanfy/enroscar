package com.stanfy.enroscar.rest.response;

import java.net.URLConnection;

import com.stanfy.enroscar.content.ResponseData;
import com.stanfy.enroscar.rest.RequestMethod.RequestMethodException;
import com.stanfy.enroscar.net.operation.RequestDescription;

/**
 * Converts parsed response model to response data.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
public interface ResponseModelConverter {

  ResponseData<?> toResponseData(final RequestDescription description, final URLConnection connection, final Object model) throws RequestMethodException;

  ResponseData<?> toResponseData(final RequestDescription description, final RequestMethodException error);

}
