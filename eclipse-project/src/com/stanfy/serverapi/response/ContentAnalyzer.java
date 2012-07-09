package com.stanfy.serverapi.response;

import android.content.Context;

/**
 * Can analyze recieved content on the service side.
 * @param <T> model type
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
public interface ContentAnalyzer<T> {

  ResponseData<T> analyze(final Context context, final ResponseData<T> responseData);

}
