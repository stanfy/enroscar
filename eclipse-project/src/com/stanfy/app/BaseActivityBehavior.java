package com.stanfy.app;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;

import com.stanfy.DebugFlags;
import com.stanfy.serverapi.request.RequestExecutor;
import com.stanfy.stats.StatsManager;
import com.stanfy.utils.ApiMethodsSupport;
import com.stanfy.utils.AppUtils;
import com.stanfy.utils.LocationMethodsSupport;

/**
 * Common behavior for all the activities.
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
public class BaseActivityBehavior implements RequestExecutorProvider {

  /** Logging tag. */
  private static final String TAG = "ActibityBehavior";
  /** Debug flag. */
  private static final boolean DEBUG = DebugFlags.DEBUG_GUI;

  /** Activity reference. */
  private final WeakReference<Activity> activityRef;

  /** Action bar support. */
  private ActionBarSupport actionBarSupport;

  /** Server API support. */
  private ApiMethodsSupport serverApiSupport;

  /** Location support. */
  private LocationMethodsSupport locationSupport;

  /** Flag that indicates an attempt to bind API. */
  private boolean apiBindAttemptDone = false, locationBindAttemptDone = false;

  /** First start flag. */
  private boolean firstStart = true;

  /** Stats manager. */
  private final StatsManager statsManager;

  public BaseActivityBehavior(final Activity activity) {
    activityRef = new WeakReference<Activity>(activity);
    statsManager = ((Application)activity.getApplication()).getStatsManager();
  }

  /**
   * @return action bar support
   */
  private ActionBarSupport createActionBarSupport() {
    final Activity a = activityRef.get();
    final Application app = (Application)a.getApplication();
    return app.createActionBarSupport();
  }

  /**
   * @return activity instance, can be null
   */
  protected final Activity getActivity() { return activityRef.get(); }

  /**
   * @see Activity#onContentChanged()
   */
  public void onContentChanged() {
    if (actionBarSupport != null) {
      actionBarSupport.destroy();
    }
    actionBarSupport = createActionBarSupport();
    actionBarSupport.doInitialize(activityRef.get());
  }

  /**
   * @see Activity#onCreate(Bundle)
   * @param savedInstanceState saved state bundle
   * @param serverApiSupport server API support
   */
  public void onCreate(final Bundle savedInstanceState, final ApiMethodsSupport serverApiSupport) {
    if (DEBUG) { Log.v(TAG, "create " + activityRef.get()); }
    if (serverApiSupport != null) {
      if (DEBUG) { Log.i(TAG, "has api support"); }
      this.serverApiSupport = serverApiSupport;
    }
  }

  /**
   * @see Activity#onRestart()
   */
  public void onRestart() {
    if (DEBUG) { Log.v(TAG, "restart " + activityRef.get()); }
    firstStart = false;
  }

  /**
   * @see Activity#onStart()
   */
  public void onStart() {
    if (DEBUG) { Log.v(TAG, "start " + activityRef.get()); }
    // server API
    bindAPI();
    // location
    bindLocation();
    // statistics
    final Activity a = getActivity();
    if (a == null) { return; }
    if (firstStart) {
      statsManager.onStartScreen(a);
    }
    statsManager.onComeToScreen(a);
  }

  /**
   * @see Activity#onResume()
   */
  public void onResume() {
    if (DEBUG) { Log.v(TAG, "resume " + activityRef.get()); }
  }

  /**
   * @see Activity#onSaveInstanceState(Bundle)
   * @param outState result bundle
   */
  public void onSaveInstanceState(final Bundle outState) {

  }

  /**
   * @see Activity#onRestoreInstanceState(Bundle)
   * @param savedInstanceState saved state bundle
   */
  public void onRestoreInstanceState(final Bundle savedInstanceState) {

  }

  /**
   * @see Activity#onPause()
   */
  public void onPause() {
    if (DEBUG) { Log.v(TAG, "pause " + activityRef.get()); }
    ((Application)getActivity().getApplication()).dispatchCrucialGUIOperationFinish();
  }

  /**
   * @see Activity#onStop()
   */
  public void onStop() {
    if (DEBUG) { Log.v(TAG, "stop " + activityRef.get()); }
    // server API
    unbindAPI();
    // location
    unbindLocation();

    // statistics
    final Activity a = getActivity();
    if (a == null) { return; }
    statsManager.onLeaveScreen(a);
  }

  /**
   * @see Activity#onDestroy()
   */
  public void onDestroy() {
    if (DEBUG) { Log.v(TAG, "destroy " + activityRef.get()); }
    if (actionBarSupport != null) {
      actionBarSupport.destroy();
    }
  }

  /** @return the actionBarSupport */
  public ActionBarSupport getActionBarSupport() { return actionBarSupport; }

  /** @return the locationSupport */
  public LocationMethodsSupport getLocationSupport() { return locationSupport; }

  /**
   * @see Activity#onKeyDown(int, KeyEvent)
   */
  boolean onKeyDown(final int keyCode, final KeyEvent event) { return false; }

  @Override
  public RequestExecutor getRequestExecutor() { return serverApiSupport; }

  private void bindAPI() {
    if (serverApiSupport != null) {
      if (DEBUG) { Log.v(TAG, "bind to API methods"); }
      serverApiSupport.bind();
      serverApiSupport.registerListener();
    }
    apiBindAttemptDone = true;
  }
  private void unbindAPI() {
    if (serverApiSupport != null) {
      serverApiSupport.removeListener();
      serverApiSupport.unbind();
    }
    apiBindAttemptDone = false;
  }
  private void bindLocation() {
    if (locationSupport != null) { locationSupport.bind(); }
    locationBindAttemptDone = true;
  }
  private void unbindLocation() {
    if (locationSupport != null) { locationSupport.unbind(); }
    locationBindAttemptDone = false;
  }

  /**
   * @param apiSupport API support instance
   */
  public void forceAPIBinding(final ApiMethodsSupport apiSupport) {
    if (serverApiSupport == null) {
      this.serverApiSupport = apiSupport;
      if (apiBindAttemptDone) { bindAPI(); }
    }
  }

  /**
   * Setup location support.
   */
  public void setupLocationSupport() {
    if (locationSupport == null) {
      locationSupport = new LocationMethodsSupport(getActivity());
      if (locationBindAttemptDone) { bindLocation(); }
    }
  }

  public boolean ensureRoot() {
    final Activity a = activityRef.get();
    if (!a.isTaskRoot() && AppUtils.isStartedFromLauncher(a)) {
      a.finish();
      return false;
    }
    return true;
  }

  /** @return the serverApiSupport */
  public ApiMethodsSupport getServerApiSupport() { return serverApiSupport; }

}
