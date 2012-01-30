package com.stanfy.utils;

import android.content.ComponentName;
import android.content.Context;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.stanfy.DebugFlags;
import com.stanfy.app.service.LocationMethods;
import com.stanfy.app.service.LocationUpdateCallback;

/**
 * Location methods support.
 * @author Roman Mazur - Stanfy (http://www.stanfy.com)
 */
public class LocationMethodsSupport extends ApplicationServiceSupport<LocationMethods> {

  /** Logging tag. */
  static final String TAG = "LocationMethodsSupport";

  /** Debug flag. */
  private static final boolean DEBUG = DebugFlags.DEBUG_UTILS;

  /** Last callback. */
  private LocationUpdateCallback lastCallback, lastRemoveCallback;

  /** Actions. */
  private boolean shouldStart, shouldStop;

  public LocationMethodsSupport(final Context a) {
    super(a);
  }

  @Override
  protected Class<LocationMethods> getInterfaceClass() { return LocationMethods.class; }

  @Override
  public void onServiceConnected(final ComponentName name, final IBinder service) {
    serviceObject = LocationMethods.Stub.asInterface(service);
    if (shouldStart) { start(); }
    if (shouldStop) { stop(); }
    if (lastCallback != null) { registerCallback(lastCallback); }
    if (lastRemoveCallback != null) { removeCallback(lastRemoveCallback); }
    shouldStart = false;
    shouldStop = false;
    lastCallback = null;
    lastRemoveCallback = null;
  }

  @Override
  public void onServiceDisconnected(final ComponentName name) {
    serviceObject = null;
    lastCallback = null;
    lastRemoveCallback = null;
  }

  public void registerCallback(final LocationUpdateCallback callback) {
    if (serviceObject != null) {
      try {
        if (DEBUG) { Log.d(TAG, "register callback " + callback); }
        serviceObject.registerUpdateCallback(callback);
      } catch (final RemoteException e) {
        Log.e(TAG, "Cannot register callback");
      }
    } else {
      lastCallback = callback;
    }
  }

  public void removeCallback(final LocationUpdateCallback callback) {
    if (serviceObject != null) {
      try {
        if (DEBUG) { Log.d(TAG, "remove callback " + callback); }
        serviceObject.unregisterUpdateCallback(callback);
      } catch (final RemoteException e) {
        Log.e(TAG, "Cannot remove callback");
      }
    } else {
      lastRemoveCallback = callback;
    }
  }

  public void start() {
    if (serviceObject != null) {
      try {
        if (DEBUG) { Log.d(TAG, "start location"); }
        serviceObject.start();
      } catch (final RemoteException e) {
        Log.e(TAG, "Cannot start location");
      }
    } else {
      shouldStart = true;
    }
  }

  public void stop() {
    if (serviceObject != null) {
      try {
        if (DEBUG) { Log.d(TAG, "stop location"); }
        serviceObject.stop();
      } catch (final RemoteException e) {
        Log.e(TAG, "Cannot stop location");
      }
    } else {
      shouldStop = true;
    }
  }

}
