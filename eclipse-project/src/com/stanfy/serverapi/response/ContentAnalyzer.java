package com.stanfy.serverapi.response;

import android.content.Context;

import com.stanfy.serverapi.request.RequestDescription;

/**
 * Can analyze recieved content on the service side.
 * @param <T> model type
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
public interface ContentAnalyzer<T> {

  ResponseData<T> analyze(final Context context, final RequestDescription description, final ResponseData<T> responseData);

}
