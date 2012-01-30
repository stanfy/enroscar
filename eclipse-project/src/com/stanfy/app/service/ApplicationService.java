package com.stanfy.app.service;

import java.util.concurrent.atomic.AtomicBoolean;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.stanfy.DebugFlags;
import com.stanfy.app.Application;

/**
 * Base application service which provides API and location methods interfaces.
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
public class ApplicationService extends Service {

  /** Logging tag. */
  protected static final String TAG = "AppService";
  /** Debug flag. */
  protected static final boolean DEBUG = DebugFlags.DEBUG_SERVICES;

  /** Check for stop message. */
  private static final int MSG_CHECK_FOR_STOP = 1;

  /** Handler instance. */
  private Handler handler;

  /** API methods. */
  private ApiMethodsImpl apiMethodsImpl;
  /** Location methods. */
  private LocationMethodsImpl locationMethodsImpl;

  /** Usage flags. */
  private AtomicBoolean apiMethodsUse = new AtomicBoolean(false), locationMethodsUse = new AtomicBoolean(false);

  /** @return application instance */
  protected Application getApp() { return (Application)getApplication(); }

  /** @return API methods implementation */
  protected ApiMethodsImpl createApiMethods() { return new ApiMethodsImpl(this); }

  /** @return location methods implementation */
  protected LocationMethodsImpl createLocationMethods() { return new LocationMethodsImpl(this); }

  @Override
  public void onCreate() {
    super.onCreate();
    handler = new Handler() {
      @Override
      public void handleMessage(final Message msg) {
        switch (msg.what) {
        case MSG_CHECK_FOR_STOP:
          // here we decide whether to stop the service
          if (DEBUG) { Log.d(TAG, "Check for stop"); }
          final boolean hasUsers = apiMethodsUse.get() || locationMethodsUse.get();
          if (!hasUsers) {
            final boolean apiWorking = apiMethodsImpl != null && apiMethodsImpl.isWorking();
            if (!apiWorking) {
              if (DEBUG) { Log.d(TAG, "Stopping"); }
              stopSelf();
            }
          }
          break;
        default:
        }
      }
    };
    if (DEBUG) { Log.d(TAG, "Service created"); }
  }

  @Override
  public int onStartCommand(final Intent intent, final int flags, final int startId) {
    if (DEBUG) { Log.d(TAG, "Start command"); }
    handler.removeMessages(MSG_CHECK_FOR_STOP);
    return START_STICKY;
  }

  @Override
  public void onDestroy() {
    if (apiMethodsImpl != null) {
      apiMethodsImpl.destroy();
    }
    if (locationMethodsImpl != null) {
      locationMethodsImpl.destroy();
    }
    if (DEBUG) { Log.d(TAG, "Service destroyed"); }
    super.onDestroy();
  }

  private void checkForLocationMethodsSupport(final boolean force) {
    if (locationMethodsImpl != null) { return; }
    if (force || getApp().addLocationSupportToService()) {
      locationMethodsImpl = createLocationMethods();
    }
  }

  @Override
  public IBinder onBind(final Intent intent) {
    final String action = intent.getAction();
    if (DEBUG) { Log.d(TAG, "Binding to " + action); }
    if (action == null) { return null; }

    if (action.equals(ApiMethods.class.getName())) {
      apiMethodsUse.set(true);
      if (apiMethodsImpl == null) {
        apiMethodsImpl = createApiMethods();
      }
      checkForLocationMethodsSupport(false);
      return apiMethodsImpl.asBinder();
    }
    if (action.equals(LocationMethods.class.getName())) {
      checkForLocationMethodsSupport(true);
      locationMethodsUse.set(true);
      return locationMethodsImpl.asBinder();
    }

    return null;
  }

  @Override
  public void onRebind(final Intent intent) {
    final String action = intent.getAction();
    if (action == null) { return; }
    if (DEBUG) { Log.d(TAG, "Rebinding to " + action); }
    if (action.equals(ApiMethods.class.getName())) {
      apiMethodsUse.set(true);
    } else if (action.equals(LocationMethods.class.getName())) {
      locationMethodsUse.set(true);
    }
  }

  @Override
  public boolean onUnbind(final Intent intent) {
    final String action = intent.getAction();
    if (action == null) { return false; }
    if (DEBUG) { Log.d(TAG, "Unbind from " + action); }
    if (apiMethodsImpl != null && action.equals(ApiMethods.class.getName())) {
      apiMethodsUse.set(false);
      checkForStop();
    } else if (locationMethodsImpl != null && action.equals(LocationMethods.class.getName())) {
      locationMethodsUse.set(false);
      checkForStop();
    }
    return true;
  }

  void checkForStop() {
    if (DEBUG) { Log.d(TAG, "Schedule check for stop"); }
    handler.removeMessages(MSG_CHECK_FOR_STOP);
    handler.sendEmptyMessage(MSG_CHECK_FOR_STOP);
  }

}
