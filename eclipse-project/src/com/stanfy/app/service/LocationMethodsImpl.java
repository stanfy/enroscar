package com.stanfy.app.service;

import android.location.Location;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;

import com.stanfy.DebugFlags;
import com.stanfy.Destroyable;
import com.stanfy.location.MapLocationManager;
import com.stanfy.location.MapLocationManager.LocationUpdateListener;

/**
 * @author Roman Mazur - Stanfy (http://www.stanfy.com)
 */
public class LocationMethodsImpl extends LocationMethods.Stub implements LocationUpdateListener, Destroyable {

  /** Logging tag. */
  private static final String TAG = "LocationMethodsImpl";

  /** Debug flag. */
  private static final boolean DEBUG = DebugFlags.DEBUG_SERVICES;

  /** Service instance. */
  final ApplicationService appService;

  /** Location manager. */
  final MapLocationManager mapLocationManager;

  /** Location callbacks. */
  private final RemoteCallbackList<LocationUpdateCallback> locationCallbacks = new RemoteCallbackList<LocationUpdateCallback>();

  /** Last location. */
  private Location lastLocation;

  public LocationMethodsImpl(final ApplicationService applicationService) {
    this.appService = applicationService;
    this.mapLocationManager = createMapLocationManager();
    mapLocationManager.registerUpdateListener(this);
  }

  /** @return the appService */
  public ApplicationService getAppService() { return appService; }

  @Override
  public void destroy() {
    lastLocation = null;
    locationCallbacks.kill();
    mapLocationManager.destroy();
    System.gc();
  }

  protected MapLocationManager createMapLocationManager() { return new MapLocationManager(appService); }

  public void provideLocation(final Location location) {
    mapLocationManager.updateLocation(location, false);
  }

  private static void notifyCallbacks(final RemoteCallbackList<LocationUpdateCallback> list, final Location loc) {
    int c = list.beginBroadcast();
    while (c > 0) {
      --c;
      try {
        list.getBroadcastItem(c).updateLocation(loc);
      } catch (final RemoteException e) {
        Log.e(TAG, "Cannot run callback", e);
      }
    }
    list.finishBroadcast();
  }

  @Override
  public void updateLocation(final Location loc) {
    lastLocation = loc;
    if (DEBUG) { Log.d(TAG, "Update location " + loc); }
    notifyCallbacks(locationCallbacks, loc);
  }

  @Override
  public Location getLastLocation() throws RemoteException { return mapLocationManager.getLocation(); }

  @Override
  public void registerUpdateCallback(final LocationUpdateCallback callback) throws RemoteException {
    if (DEBUG) { Log.d(TAG, "New callback " + callback); }
    locationCallbacks.register(callback);
    if (lastLocation != null) { callback.updateLocation(lastLocation); }
  }

  @Override
  public void unregisterUpdateCallback(final LocationUpdateCallback callback) throws RemoteException {
    if (DEBUG) { Log.d(TAG, "Remove callback " + callback); }
    locationCallbacks.unregister(callback);
  }

  @Override
  public void start() throws RemoteException {
    mapLocationManager.startUpdates();
  }

  @Override
  public void stop() throws RemoteException {
    mapLocationManager.stopUpdates();
  }

}
