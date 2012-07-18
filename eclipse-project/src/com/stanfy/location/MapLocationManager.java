package com.stanfy.location;

import static com.stanfy.utils.Time.MINUTES;
import static com.stanfy.utils.Time.SECONDS;

import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.stanfy.DebugFlags;
import com.stanfy.Destroyable;

/**
 * This class deals with a location system service and determines the current user location.
 * @author Roman Mazur (http://www.stanfy.com)
 * @author Yevgeniy Pazekha (neboskreb@narod.ru)
 */
@SuppressLint("HandlerLeak")
public final class MapLocationManager implements Destroyable {
  /** Logging tag. */
  static final String LOGTAG = "MapLocMan";
  /** Flag allowing the debug mode. */
  private static final boolean DEBUG = DebugFlags.DEBUG_LOCATION;

  /** Hundred. */
  private static final int HUNDRED = 100;

  /** Update interval for location providers. */
  public static final long LOCATION_MANAGER_COARSE_UPDATE_INTERVAL = 60 * SECONDS, LOCATION_MANAGER_FINE_UPDATE_INTERVAL = 2 * HUNDRED;
  /** Nice accuracy. */
  public static final int LOCATION_MANAGER_NICE_ACCURACY = 15;
  /** Max fine provider runs. */
  public static final int LOCATION_MANAGER_FINE_PROVIDER_MAX_RUNS_COUNT = 5;

  /** Standard age out. */
  protected static final long TIME_THRESHOLD = 3 * MINUTES;

  /** Remove fine listener message. */
  protected static final int MSG_REMOVE_FINE_LISTENER = 3, MSG_REGISTER_FINE_LISTENER = 4;

  /** Handler thread for getting updates from location providers. */
  private final HandlerThread workerThread = new HandlerThread("location-thread");
  /** Looper of this thread. */
  private final Looper looper;
  /** Handler. */
  private final Handler workerHandler;

  /** Location manager service. */
  private final LocationManager locationManager;

  /** Current location fix. */
  private Location location;

  /** Flag to reset coarse provider. */
  private volatile boolean shouldResetCoarseProvider;

  /** Flag to indicate the start of determining. */
  private volatile boolean running = false;

  /** Count of fine provider runs. */
  private volatile int fineProviderRunsCount = 0;

  /** Provider names. */
  private String coarseProvider, fineProvider;

  /** Coarse location listener. */
  private LocationListener coarseListener = new LocationListenerAdapter();
  /** Fine location listener. */
  private LocationListener fineListener = new LocationListenerAdapter();

  /** Updates listener. */
  public interface LocationUpdateListener {
    void updateLocation(final Location loc);
  }

  /**
   * Null listener.
   */
  private static final LocationUpdateListener DUMMY_LISTENER = new LocationUpdateListener() {
      @Override
      public void updateLocation(final Location loc) { }
  };

  /** This delegate is called only when an incoming fix is accepted. */
  private LocationUpdateListener locListener = DUMMY_LISTENER;

  public void registerUpdateListener(final LocationUpdateListener delegate) {
    locListener = delegate;
  }

  /**
   * @param context owner
   * @param looper looper instance
   */
  public MapLocationManager(final Context context) {
    this.locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
    workerThread.start();
    if (DEBUG) { Log.d(LOGTAG, "Waiting for looper..."); }
    looper = workerThread.getLooper();
    if (DEBUG) { Log.d(LOGTAG, "Created."); }

    workerHandler = new Handler(looper) {
      @Override
      public void dispatchMessage(final Message msg) {
        switch (msg.what) {
        case MSG_REMOVE_FINE_LISTENER:
          if (DEBUG) { Log.d(LOGTAG, "Time to remove fine listener"); }
          if (locationManager == null) { return; }
          removeFineProvider();
          break;
        case MSG_REGISTER_FINE_LISTENER:
          if (locationManager == null || fineProvider == null) { return; }
          requestLocationUpdates(fineProvider, fineListener, false);
          break;
        default:
          super.dispatchMessage(msg);
        }
      };
    };
  }

  protected void requestLocationUpdates(final String p, final LocationListener listener, final boolean firstTime) {
    final long freq = p.equals(fineProvider) ? LOCATION_MANAGER_FINE_UPDATE_INTERVAL : firstTime ? 0 : LOCATION_MANAGER_COARSE_UPDATE_INTERVAL;
    final float acc = LOCATION_MANAGER_NICE_ACCURACY / 2;
    locationManager.requestLocationUpdates(p, freq, acc, listener, looper);
    if (DEBUG) { Log.d(LOGTAG, p + " listener registered, freq= " + freq); }
    if (p.equals(fineProvider)) {
      final long maxFineTime = 90 * SECONDS;
      workerHandler.removeMessages(MSG_REMOVE_FINE_LISTENER);
      workerHandler.sendEmptyMessageDelayed(MSG_REMOVE_FINE_LISTENER, maxFineTime);
    }
  }

  protected boolean registerLocationListener() {
    final LocationManager locman = locationManager;
    if (DEBUG) { Log.d(LOGTAG, "Register phone location listener"); }
    final List<String> providers = locman.getAllProviders();
    if (DEBUG) { Log.d(LOGTAG, "All providers: " + providers); }

    final Criteria c = new Criteria();
    c.setAltitudeRequired(false);
    c.setSpeedRequired(false);
    c.setBearingRequired(false);
    c.setCostAllowed(false);

    c.setPowerRequirement(Criteria.POWER_LOW);
    c.setAccuracy(Criteria.ACCURACY_COARSE);
    coarseProvider = locman.getBestProvider(c, false);

    if (providers.contains(LocationManager.GPS_PROVIDER)) {
      fineProvider = LocationManager.GPS_PROVIDER;
    } else {
      c.setPowerRequirement(Criteria.NO_REQUIREMENT);
      c.setAccuracy(Criteria.ACCURACY_FINE);
      fineProvider = locman.getBestProvider(c, false);
    }

    if (coarseProvider == null) {
      coarseProvider = fineProvider;
      fineProvider = null;
    }

    final Location lastLocation = LocationBinder.getLastKnown(locman);
    if (lastLocation != null) {
      updateLocation(lastLocation, true);
    }

    if (DEBUG) { Log.d(LOGTAG, "Register for " + coarseProvider + " / " + fineProvider); }

    if (coarseProvider == null) {
      Log.w(LOGTAG, "Coarse provider not selected");
      return false;
    }



    if (fineProvider != null) { fineProviderRunsCount = 0; }

    requestLocationUpdates(coarseProvider, coarseListener, true);
    shouldResetCoarseProvider = true;
    if (fineProvider != null) {
      requestLocationUpdates(fineProvider, fineListener, false);
    }

    return true;
  }

  protected void unregisterLocationListener() {
    if (coarseListener != null) { locationManager.removeUpdates(coarseListener); }
    if (fineListener != null) { removeFineProvider(); }
    if (DEBUG) { Log.d(LOGTAG, "Removed location listener from the system serice"); }
  }

  /** Subscribe for location updates. */
  public synchronized boolean startUpdates() {
    if (!running) {
      running = registerLocationListener();
      if (DEBUG) { Log.d(LOGTAG, "Starting updates, running = " + running); }
    } else {
      if (DEBUG) { Log.d(LOGTAG, "Updates already running"); }
    }
    return running;
  }

  /** Stop location updates.     */
  public synchronized void stopUpdates() {
    if (!running) { return; }
    running = false;
    if (DEBUG) { Log.d(LOGTAG, "Stopping updates"); }
    unregisterLocationListener();
  }

  /** Destroy the manager. */
  @Override
  public void destroy() {
    stopUpdates();
    final Looper looper = workerThread.getLooper();
    if (looper != null) { looper.quit(); }
  }

  /**
   * Define whether the current location is better than last. And update it.
   * @param incoming current location
   */
  public synchronized boolean updateLocation(final Location incoming, final boolean cachedValue) {
    if (incoming == null) {
      Log.w(LOGTAG, "Incoming location is null. IGNORED.");
      return false;
    }

    if (DEBUG) {
      Log.d(LOGTAG, "=======================================");
      Log.d(LOGTAG, "      incoming fix: " + incoming.getProvider());
    }

    final Location current = getLocation();

    final boolean acquire = LocationBinder.isBetterLocation(incoming, current);

    if (fineProvider != null && fineProvider.equals(incoming.getProvider()) && ++fineProviderRunsCount > LOCATION_MANAGER_FINE_PROVIDER_MAX_RUNS_COUNT) {
      removeFineProvider();
    }

    if (acquire) {
      if (DEBUG) {  Log.d(LOGTAG, "***   incoming location ACQUIRED");  }
      final float accuracyDelta = current != null ? current.getAccuracy() - incoming.getAccuracy() : 1;
      setNewLocation(incoming);
      if (incoming.getAccuracy() < LOCATION_MANAGER_NICE_ACCURACY) {
        removeFineProvider();
      } else if (accuracyDelta < 0) { // accurate data was replaced
        if (DEBUG) {  Log.d(LOGTAG, "Request " + fineProvider + " listener " + fineProviderRunsCount);  }
        requestLocationUpdates(fineProvider, fineListener, false);
      }
      if (shouldResetCoarseProvider && !cachedValue && incoming.getProvider().equals(coarseProvider)) {
        locationManager.removeUpdates(coarseListener);
        requestLocationUpdates(coarseProvider, coarseListener, false);
        shouldResetCoarseProvider = false;
      }
      return true;
    } else {
      if (DEBUG) {  Log.d(LOGTAG, "***   incoming location DISCARDED");  }
      return false;
    }
  }

  protected void removeFineProvider() {
    if (DEBUG) {  Log.d(LOGTAG, "Remove " + fineProvider + " listener");  }
    locationManager.removeUpdates(fineListener);
    fineProviderRunsCount = 0;
  }

  /** @return true if the manager has determined user's location   */
  public boolean isLocationDetermined() { return location != null; }
  /** @return current location */
  public Location getLocation() { return location; }

  /**
   * This method is called when the new location is determined.
   * It triggers all the observers.
   * @param l location
   */
  private void setNewLocation(final Location l) {
    location = l;
    locListener.updateLocation(location);
  }

  /**
   * Base location listener.
   * @author Roman Mazur (Stanfy - http://www.stanfy.com)
   */
  private class LocationListenerAdapter implements LocationListener {
    @Override
    public void onLocationChanged(final Location location) {
      if (DEBUG) { Log.d(LOGTAG, "New location is received from phone"); }
      updateLocation(location, false);
    }
    @Override
    public void onProviderDisabled(final String provider) {
      /* nothing to do */
    }
    @Override
    public void onProviderEnabled(final String provider) {
      /* nothing to do */
    }
    @Override
    public void onStatusChanged(final String provider, final int status, final Bundle extras) {
      /* nothing to do */
    }
  }


}
