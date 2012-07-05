package com.stanfy.utils;

import com.stanfy.serverapi.request.RequestDescription;

/**
 * Request executor. Used by request builder to perform a request described as {@link RequestDescription}.
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
public interface RequestExecutor {

  /**
   * @param rd request description
   * @return unique request identifier
   */
  int performRequest(final RequestDescription rd);

}
