package com.stanfy.enroscar.rest.response;

import android.content.Context;

import com.stanfy.enroscar.content.ResponseData;
import com.stanfy.enroscar.rest.RequestMethod.RequestMethodException;
import com.stanfy.enroscar.net.operation.RequestDescription;

/**
 * Can analyze recieved content on the service side.
 * @param <T> model type
 * @param <RT> result model type
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
public interface ContentAnalyzer<T, RT> {

  ResponseData<RT> analyze(final Context context, final RequestDescription description, final ResponseData<T> responseData) throws RequestMethodException;

}
