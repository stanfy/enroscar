package com.stanfy.app.service;

import com.stanfy.serverapi.response.ResponseData;

interface ApiMethodCallback {
  /**
   * Notify about successful operation finish. 
   */
  void reportSuccess(in int token, in int operation, in ResponseData response);
  /**
   * Notify about operation error. 
   */
  void reportError(in int token, in int operation, in ResponseData response);
  /**
   * Notify about pending operation. This method can be called when a callback is registered. 
   */
  void reportPending(in int token, in int operation);
  /**
   * Notify about last operation. Response data will NOT contain model!
   */
  void reportLastOperation(in int token, in int operation, in ResponseData response);
}
