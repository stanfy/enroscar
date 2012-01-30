package com.stanfy.app;

import com.stanfy.serverapi.request.RequestExecutor;

/**
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
public interface RequestExecutorProvider {

  /** @return request executor that can be used to create request builders */
  RequestExecutor getRequestExecutor();

}
