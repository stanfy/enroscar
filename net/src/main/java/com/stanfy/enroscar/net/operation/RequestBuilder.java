package com.stanfy.enroscar.net.operation;

import android.content.Context;
import android.support.v4.content.Loader;

import com.stanfy.enroscar.content.ResponseData;
import com.stanfy.enroscar.rest.EntityTypeToken;
import com.stanfy.enroscar.net.operation.executor.RequestExecutor;

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
   * @return this instance for chaining
   */
  RequestBuilder<MT> setExecutor(final RequestExecutor executor);

  /**
   * @return type token of the expected model
   */
  EntityTypeToken getExpectedModelType();

}
