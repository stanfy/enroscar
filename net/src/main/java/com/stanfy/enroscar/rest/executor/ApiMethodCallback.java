package com.stanfy.enroscar.rest.executor;

import com.stanfy.enroscar.content.loader.ResponseData;
import com.stanfy.enroscar.net.operation.RequestDescription;

/**
 * Callback for {@link ApiMethods}.
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

}
