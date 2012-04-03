package com.stanfy.app.service;

import com.stanfy.app.service.ApiMethodCallback;
import com.stanfy.serverapi.request.RequestDescription;

interface ApiMethods {
  
  /**
   * Perform a request.
   * @param description request description
   */
  void performRequest(in RequestDescription description);
  /**
   * Cancel request.
   * @param id request identifier
   */
  void cancelRequest(in int id);
  
  /**
   * Register requests callback.
   */
  void registerCallback(in ApiMethodCallback callback, in boolean requiresModel);
  /**
   * Remove requests callback.
   */
  void removeCallback(in ApiMethodCallback callback);
  
}
