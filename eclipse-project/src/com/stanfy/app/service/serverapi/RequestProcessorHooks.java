package com.stanfy.app.service.serverapi;

import com.stanfy.serverapi.RequestMethod;
import com.stanfy.serverapi.request.RequestDescription;
import com.stanfy.serverapi.response.ParserContext;
import com.stanfy.serverapi.response.ResponseData;

/**
 * Request description processing hooks.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
public interface RequestProcessorHooks {
  /**
   * Called before the {@link RequestMethod} methods are invoked.
   * @param requestDescription request description to be processed
   * @param pContext parser context instance
   * @param requestMethod request method created for processing this request description
   */
  void beforeRequestProcessingStarted(final RequestDescription requestDescription, final ParserContext pContext, final RequestMethod requestMethod);
  /**
   * Called after the {@link RequestMethod} methods are invoked.
   * @param requestDescription request description to be processed
   * @param pContext parser context instance
   * @param requestMethod request method created for processing this request description
   */
  void afterRequestProcessingFinished(final RequestDescription requestDescription, final ParserContext pContext, final RequestMethod requestMethod);

  /**
   * Called when success response is retrieved.
   * @param requestDescription request description instance
   * @param responseData response data instance
   */
  void onRequestSuccess(final RequestDescription requestDescription, final ResponseData responseData);
  /**
   * Called when error response is retrieved.
   * @param requestDescription request description instance
   * @param responseData response data instance
   */
  void onRequestError(final RequestDescription requestDescription, final ResponseData responseData);
  /**
   * Called when request is canceled.
   * @param requestDescription request description instance
   */
  void onRequestCancel(final RequestDescription requestDescription);
}
