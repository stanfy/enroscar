package com.stanfy.app;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

import com.stanfy.DebugFlags;
import com.stanfy.enroscar.beans.BeansManager;
import com.stanfy.stats.StatsManager;
import com.stanfy.utils.AppUtils;
import com.stanfy.utils.LocationMethodsSupport;

/**
 * Common behavior for all the activities.
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
public class BaseActivityBehavior {

  /** Logging tag. */
  private static final String TAG = "ActibityBehavior";
  /** Debug flag. */
  private static final boolean DEBUG = DebugFlags.DEBUG_GUI;

  /** Activity reference. */
  private final WeakReference<Activity> activityRef;

  /** Location support. */
  private LocationMethodsSupport locationSupport;

  /** Flag that indicates an attempt to bind API. */
  private boolean locationBindAttemptDone = false;

  /** First start flag. */
  private boolean firstStart = true;

  /** Stats manager. */
  private final StatsManager statsManager;
  /** GUI operations manager. */
  private final CrucialGUIOperationManager crucialGUIOperationManager;

  public BaseActivityBehavior(final Activity activity) {
    activityRef = new WeakReference<Activity>(activity);
    final BeansManager beansManager = BeansManager.get(activity);
    statsManager = beansManager.getStatsManager();
    crucialGUIOperationManager = beansManager.getCrucialGUIOperationManager();
  }

  /**
   * @return activity instance, can be null
   */
  protected final Activity getActivity() { return activityRef.get(); }

  /**
   * @see Activity#onContentChanged()
   */
  public void onContentChanged() {
    if (DEBUG) { Log.v(TAG, "onContentChanged " + activityRef.get()); }
  }

  /**
   * @see Activity#onCreate(Bundle)
   * @param savedInstanceState saved state bundle
   */
  public void onCreate(final Bundle savedInstanceState) {
    if (DEBUG) { Log.v(TAG, "create " + activityRef.get()); }
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

    // location
    bindLocation();

    // statistics
    final Activity a = getActivity();
    if (a == null) { return; }
    statsManager.onStartSession(a);
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
    crucialGUIOperationManager.dispatchCrucialGUIOperationFinish();
  }

  /**
   * @see Activity#onStop()
   */
  public void onStop() {
    if (DEBUG) { Log.v(TAG, "stop " + activityRef.get()); }
    // location
    unbindLocation();

    // statistics
    final Activity a = getActivity();
    if (a == null) { return; }
    statsManager.onLeaveScreen(a);
    statsManager.onEndSession(a);
  }

  /**
   * @see Activity#onDestroy()
   */
  public void onDestroy() {
    if (DEBUG) { Log.v(TAG, "destroy " + activityRef.get()); }
  }

  /** @return the locationSupport */
  public LocationMethodsSupport getLocationSupport() { return locationSupport; }

  /**
   * @see Activity#onKeyDown(int, KeyEvent)
   */
  public boolean onKeyDown(final int keyCode, final KeyEvent event) { return false; }

  protected void bindLocation() {
    if (locationSupport != null) { locationSupport.bind(); }
    locationBindAttemptDone = true;
  }
  protected void unbindLocation() {
    if (locationSupport != null) { locationSupport.unbind(); }
    locationBindAttemptDone = false;
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

  /**
   * @see Activity#onOptionsItemSelected(MenuItem)
   * @return true if event was processed
   */
  public boolean onOptionsItemSelected(final MenuItem item) {
    return false;
  }

  /**
   * @see Activity#onCreateOptionsMenu(Menu)
   * @return true if event was processed
   */
  public boolean onCreateOptionsMenu(final Menu menu) {
    return false;
  }

  /**
   * @see Activity#onOptionsMenuClosed(Menu)
   */
  public void onOptionsMenuClosed(final Menu menu) {
    // nothing
  }

}
