package com.stanfy.enroscar.net.operation.executor;

import com.stanfy.enroscar.net.operation.RequestDescription;

/**
 * Request executor performs a network operation described with {@link RequestDescription}.
 */
public interface RequestExecutor {

  /**
   * @param rd request description
   */
  void performRequest(RequestDescription rd);

}
