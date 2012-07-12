package com.stanfy.serverapi.request;

import android.content.Context;

import com.stanfy.app.loader.RequestBuilderLoader;
import com.stanfy.serverapi.response.ModelTypeToken;
import com.stanfy.utils.RequestExecutor;

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
  RequestBuilderLoader<MT> getLoader();

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
