package com.stanfy.app.service;

import com.stanfy.app.service.ApiMethodCallback;
import com.stanfy.serverapi.request.RequestDescription;

interface ApiMethods {
  
  void performRequest(in RequestDescription description);
  void cancelRequest(in int id);
  
  void registerCallback(in ApiMethodCallback callback, in boolean requiresModel);
  void removeCallback(in ApiMethodCallback callback);
  
}
