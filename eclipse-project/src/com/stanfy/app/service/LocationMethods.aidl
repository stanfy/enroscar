package com.stanfy.app.service;

import com.stanfy.app.service.LocationUpdateCallback;
import android.location.Location;

interface LocationMethods {
  
  Location getLastLocation();
  
  void registerUpdateCallback(in LocationUpdateCallback callback);
  
  void unregisterUpdateCallback(in LocationUpdateCallback callback);

  void start();
  
  void stop();
  
}
