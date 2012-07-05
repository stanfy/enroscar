package com.stanfy.app.service;

import com.stanfy.serverapi.request.RequestDescription;
import com.stanfy.serverapi.response.ResponseData;

/**
 * Callback for {@link ApiMethods}.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
public interface ApiMethodCallback {

  /**
   * Report API operation success.
   * @param requestDescription request description
   * @param responseData response data instance
   */
  void reportSuccess(final RequestDescription requestDescription, final ResponseData<?> responseData);

  /**
   * Report API operation error.
   * @param requestDescription request description
   * @param responseData response data instance
   */
  void reportError(final RequestDescription requestDescription, final ResponseData<?> responseData);

  /**
   * Report API operation cancel.
   * @param requestDescription request description
   * @param responseData response data instance
   */
  void reportCancel(final RequestDescription requestDescription, final ResponseData<?> responseData);

  /**
   * Report about pending API operation (this operation is handled by the main queue executor).
   * @param requestId request ID
   */
  void reportPending(final int requestId);

  /**
   * Report about last operation (this operation was handled by the main queue executor).
   * @param requestId request ID
   * @param responseData response data instance
   */
  void reportLastOperation(final int requestId, final ResponseData<?> responseData);

}
