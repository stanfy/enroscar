package com.stanfy.app.service;

import android.location.Location;

interface LocationUpdateCallback {

  void updateLocation(in Location location);

}
