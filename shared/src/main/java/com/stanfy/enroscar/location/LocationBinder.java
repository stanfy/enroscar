package com.stanfy.enroscar.location;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

/**
 * Allows easily start and stop locations listenting.
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
@SuppressLint("HandlerLeak")
public class LocationBinder {

  /** Location binder. */
  private static final String TAG = "LocBinder";

  /** Debug flag. */
  private static final boolean DEBUG = true;

  /** Time delta threshold. */
  private static final int TIME_THRESHOLD = 1000 * 60 * 3; // 3 minutes

  /** Accuracy delta threshold. */
  private static final int ACCURACY_THRESHOLD = 200; // 200 meters

  /** Max time to listen for a fine provider. */
  private static final int MAX_FINE_PROVIDER_LISTEN_TIME = 1000 * 60 * 2; // 2 minutes
  /** Max time to listen for a coarse provider. */
  private static final int MAX_COARSE_PROVIDER_LISTEN_TIME = 1000 * 60 * 5; // 5 minutes

  /** Message to stop fine provider. */
  private static final int MSG_STOP_FINE_PROVIDER = 17;
  /** Message to stop coarse provider. */
  private static final int MSG_STOP_COARSE_PROVIDER = 18;

  private static boolean isSameProvider(final String provider1, final String provider2) {
    if (provider1 == null) { return provider2 == null; }
    return provider1.equals(provider2);
  }

  /**
   * Determines whether one Location reading is better than the current Location fix.
   * @param location  The new Location that you want to evaluate
   * @param currentBestLocation  The current Location fix, to which you want to compare the new one
   */
  static boolean isBetterLocation(final Location location, final Location currentBestLocation) {
    if (currentBestLocation == null) { return true; }

    final long timeDelta = location.getTime() - currentBestLocation.getTime();
    final boolean isSignificantlyNewer = timeDelta > TIME_THRESHOLD;
    final boolean isSignificantlyOlder = timeDelta < -TIME_THRESHOLD;
    final boolean isNewer = timeDelta > 0;

    if (isSignificantlyNewer) {
      return true;
    } else if (isSignificantlyOlder) {
      return false;
    }

    final int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
    final boolean isLessAccurate = accuracyDelta > 0;
    final boolean isMoreAccurate = accuracyDelta < 0;
    final boolean isSignificantlyLessAccurate = accuracyDelta > ACCURACY_THRESHOLD;

    final boolean isFromSameProvider = isSameProvider(location.getProvider(), currentBestLocation.getProvider());

    if (isMoreAccurate) {
      return true;
    } else if (isNewer && !isLessAccurate) {
      return true;
    } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
      return true;
    }
    return false;
  }


  /** Choosen best location. */
  private Location bestLocation;

  /** Location manager. */
  private LocationManager locationManager;

  /** Coarse listener. */
  private LocationListener coarseListener = new LListener();
  /** Fine listener. */
  private LocationListener fineListener = new LListener();

  /** Looper for handler. */
  private Looper myLooper;
  /** Timing handler. */
  private MyHandler myHandler;

  /** Binder listener. */
  private LocationProcessListener listener;

  /** @return best location */
  public Location getLocation() { return bestLocation; }

  /** @param myLooper the myLooper to set */
  public void setLooper(final Looper myLooper) { this.myLooper = myLooper; }

  /** @param listener the listener to set */
  public void setListener(final LocationProcessListener listener) { this.listener = listener; }

  static Location getLastKnown(final LocationManager locationManager) {
    if (locationManager == null) { return null; }
    Location location = null;
    for (final String p : locationManager.getProviders(false)) {
      final Location knownLoc = locationManager.getLastKnownLocation(p);
      if (knownLoc != null && isBetterLocation(knownLoc, location)) {
        location = knownLoc;
      }
    }
    return location;
  }

  /**
   * Please call it from the main thread (with looper).
   * @param context context instance
   */
  public void startListening(final Context context) {
    if (DEBUG) { Log.d(TAG, "Start location listening..."); }
    try {
      locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
      if (locationManager == null) { return; }
    } catch (final Exception e) {
      return;
    }
    myHandler = myLooper != null ? new MyHandler(myLooper) : new MyHandler();

    if (listener != null) {
      listener.onLocationStart();
    }

    final Location last = getLastKnown(locationManager);
    if (last != null) { newLocation(last); }

    final Criteria c = new Criteria();
    c.setAltitudeRequired(false);
    c.setSpeedRequired(false);
    c.setBearingRequired(false);
    c.setCostAllowed(false);

    c.setPowerRequirement(Criteria.POWER_LOW);
    c.setAccuracy(Criteria.ACCURACY_COARSE);
    final String coarseProvider = locationManager.getBestProvider(c, false);
    c.setPowerRequirement(Criteria.NO_REQUIREMENT);
    c.setAccuracy(Criteria.ACCURACY_FINE);
    final String fineProvider = locationManager.getBestProvider(c, false);

    if (DEBUG) { Log.d(TAG, "Providers " + coarseProvider + "/" + fineProvider); }
    final long minTime = 60000;
    final int minDistance = 50;
    if (coarseProvider != null) {
      if (DEBUG) { Log.d(TAG, "Register for " + coarseProvider); }
      locationManager.requestLocationUpdates(coarseProvider, minTime, minDistance, coarseListener);
      myHandler.sendEmptyMessageDelayed(MSG_STOP_COARSE_PROVIDER, MAX_COARSE_PROVIDER_LISTEN_TIME);
    }
    if (fineProvider != null) {
      if (DEBUG) { Log.d(TAG, "Register for " + fineProvider); }
      locationManager.requestLocationUpdates(fineProvider, minTime, minDistance, fineListener);
      myHandler.sendEmptyMessageDelayed(MSG_STOP_FINE_PROVIDER, MAX_FINE_PROVIDER_LISTEN_TIME);
    }
  }

  /**
   * Stop updates.
   */
  public void stopListening() {
    if (locationManager == null) { return; }
    if (DEBUG) { Log.d(TAG, "Stop location listening..."); }
    if (listener != null) {
      listener.onLocationStop();
    }
    myHandler.removeMessages(MSG_STOP_FINE_PROVIDER);
    locationManager.removeUpdates(coarseListener);
    locationManager.removeUpdates(fineListener);
    locationManager = null;
  }

  private static String toString(final Location loc) {
    return loc.getLatitude() + ";" + loc.getLongitude() + ";" + loc.getProvider() + ";" + loc.getAccuracy();
  }

  private void newLocation(final Location loc) {
    this.bestLocation = loc;
    if (listener != null) {
      listener.onLocationUpdate(loc);
    }
  }

  /**
   * Base location listener.
   * @author Roman Mazur (Stanfy - http://www.stanfy.com)
   */
  class LListener implements LocationListener {
    @Override
    public void onLocationChanged(final Location location) {
      if (location == null) { return; }
      if (DEBUG) { Log.d(TAG, "Incoming location " + LocationBinder.toString(location)); }
      if (bestLocation == null || isBetterLocation(location, bestLocation)) {
        newLocation(location);
      }
      if (DEBUG) { Log.d(TAG, "New best location " + LocationBinder.toString(bestLocation)); }
      final int greateAccuracy = 15;
      if (bestLocation.getAccuracy() < greateAccuracy) {
        stopListening();
      }
    }
    @Override
    public void onProviderDisabled(final String provider) { /* nothing */ }
    @Override
    public void onProviderEnabled(final String provider) { /* nothing */ }
    @Override
    public void onStatusChanged(final String provider, final int status, final Bundle extras) { /* nothing */ }
  }


  /**
   * Handler for timing issues.
   * @author Roman Mazur (Stanfy - http://www.stanfy.com)
   */
  class MyHandler extends Handler {

    public MyHandler() { }

    public MyHandler(final Looper looper) { super(looper); }

    @Override
    public void handleMessage(final Message msg) {
      switch (msg.what) {
      case MSG_STOP_FINE_PROVIDER:
        if (locationManager == null) { return; }
        if (DEBUG) { Log.d(TAG, "Switch off fine provider..."); }
        locationManager.removeUpdates(fineListener);
        break;
      case MSG_STOP_COARSE_PROVIDER:
        if (locationManager == null) { return; }
        if (DEBUG) { Log.d(TAG, "Switch off coarse provider..."); }
        locationManager.removeUpdates(coarseListener);
        break;
      default:
        super.handleMessage(msg);
      }
    }
  }

}
