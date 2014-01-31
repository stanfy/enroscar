package com.stanfy.enroscar.rest;

import com.stanfy.enroscar.content.loader.ResponseData;
import com.stanfy.enroscar.net.operation.RequestDescription;

/**
 * Hooks for {@link DirectRequestExecutor}.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
public interface DirectRequestExecutorHooks {
  /**
   * Called before the {@link RequestMethod} methods are invoked.
   * @param requestDescription request description to be processed
   * @param requestMethod request method created for processing this request description
   */
  void beforeRequestProcessingStarted(final RequestDescription requestDescription, final RequestMethod requestMethod);
  /**
   * Called after the {@link RequestMethod} methods are invoked.
   * @param requestDescription request description to be processed
   * @param requestMethod request method created for processing this request description
   */
  void afterRequestProcessingFinished(final RequestDescription requestDescription, final RequestMethod requestMethod);

  /**
   * Called when success response is retrieved.
   * @param requestDescription request description instance
   * @param responseData response data instance
   */
  void onRequestSuccess(final RequestDescription requestDescription, final ResponseData<?> responseData);
  /**
   * Called when error response is retrieved.
   * @param requestDescription request description instance
   * @param responseData response data instance
   */
  void onRequestError(final RequestDescription requestDescription, final ResponseData<?> responseData);
  /**
   * Called when request is canceled.
   * @param requestDescription request description instance
   * @param responseData response data instance
   */
  void onRequestCancel(final RequestDescription requestDescription, final ResponseData<?> responseData);
}
