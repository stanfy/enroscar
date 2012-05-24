package com.stanfy.app;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

import com.stanfy.serverapi.request.RequestExecutor;
import com.stanfy.utils.ApiMethodsSupport;
import com.stanfy.utils.LocationMethodsSupport;

/**
 * Preference activity with common behavior.
 * @param <AT> application type
 * @see BaseActivityBehavior
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
public abstract class BasePreferenceActivity<AT extends Application> extends PreferenceActivity implements ActionBarActivity, RequestExecutorProvider, LocationSupportProvider {

  /** Behavior. */
  private BaseActivityBehavior behavior;

  /** @return the behavior */
  protected BaseActivityBehavior getBehavior() { return behavior; }

  /** @return application instance */
  @SuppressWarnings("unchecked")
  public AT getApp() { return (AT)getApplication(); }

  /**
   * This method is called from {@link #onCreate(Bundle)}.
   * @return server API methods support
   */
  protected ApiMethodsSupport createApiMethodsSupport() { return null; }

  /**
   * Ensure that this activity is a root task when started from launcher.
   * Usage:
   * <pre>
   *   public void onCreate() {
   *     super.onCreate();
   *     if (!ensureRoot()) { return; }
   *     ...
   *   }
   * </pre>
   * @return false id this activity was incorrectly started from launcher
   */
  protected boolean ensureRoot() { return behavior.ensureRoot(); }

  @Override
  public void setupLocationSupport() { behavior.setupLocationSupport(); }
  @Override
  public LocationMethodsSupport getLocationSupport() { return behavior.getLocationSupport(); }

  @Override
  public RequestExecutor getRequestExecutor() { return behavior.getRequestExecutor(); }

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    behavior = getApp().createActivityBehavior(this);
    super.onCreate(savedInstanceState);
    behavior.onCreate(savedInstanceState, createApiMethodsSupport());
  }
  @Override
  protected void onStart() {
    super.onStart();
    behavior.onStart();
  }
  @Override
  protected void onRestart() {
    super.onRestart();
    behavior.onRestart();
  }
  @Override
  protected void onResume() {
    super.onResume();
    behavior.onResume();
  }
  @Override
  protected void onSaveInstanceState(final Bundle outState) {
    behavior.onSaveInstanceState(outState);
    super.onSaveInstanceState(outState);
  }
  @Override
  protected void onRestoreInstanceState(final Bundle savedInstanceState) {
    super.onRestoreInstanceState(savedInstanceState);
    behavior.onRestoreInstanceState(savedInstanceState);
  }
  @Override
  protected void onPause() {
    behavior.onPause();
    super.onPause();
  }
  @Override
  protected void onStop() {
    behavior.onStop();
    super.onStop();
  }
  @Override
  protected void onDestroy() {
    behavior.onDestroy();
    super.onDestroy();
  }
  @Override
  public void onContentChanged() {
    behavior.onContentChanged();
    super.onContentChanged();
  }
  @Override
  public ActionBarSupport getActionBarSupport() { return behavior.getActionBarSupport(); }
  @Override
  public boolean onKeyDown(final int keyCode, final KeyEvent event) {
    final boolean r = behavior.onKeyDown(keyCode, event);
    if (r) { return true; }
    return super.onKeyDown(keyCode, event);
  }
  @Override
  public boolean onOptionsItemSelected(final MenuItem item) {
    final boolean r = behavior.onOptionsItemSelected(item);
    if (r) { return true; }
    return super.onOptionsItemSelected(item);
  }
  @Override
  public boolean onCreateOptionsMenu(final Menu menu) {
    final boolean r = behavior.onCreateOptionsMenu(menu);
    if (r) { return true; }
    return super.onCreateOptionsMenu(menu);
  }
  @Override
  public void onOptionsMenuClosed(final Menu menu) {
    behavior.onOptionsMenuClosed(menu);
    super.onOptionsMenuClosed(menu);
  }

}
