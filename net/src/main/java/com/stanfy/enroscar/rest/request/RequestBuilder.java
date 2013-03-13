package com.stanfy.enroscar.rest.request;

import android.content.Context;
import android.support.v4.content.Loader;

import com.stanfy.enroscar.rest.response.ResponseData;
import com.stanfy.enroscar.utils.ModelTypeToken;

/**
 * Request builder interface.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 *
 * @param <MT> model type
 */
public interface RequestBuilder<MT> {

  /** @return application context */
  Context getContext();

  /** @return corresponding loader */
  Loader<ResponseData<MT>> getLoader();

  /**
   * Send request.
   * @return request identifier
   */
  int execute();

  /**
   * @param executor request performer instance
   */
  void setExecutor(final RequestExecutor executor);

  /**
   * @return type token of the expected model
   */
  ModelTypeToken getExpectedModelType();

}
