package com.stanfy.app;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

import com.stanfy.utils.LocationMethodsSupport;

/**
 * Fragment activity with common behavior.
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
@SuppressWarnings("deprecation")
public abstract class BaseFragmentActivity extends FragmentActivity implements LocationSupportProvider {

  /** Behavior. */
  private BaseActivityBehavior behavior;

  /** @return the behavior */
  protected BaseActivityBehavior getBehavior() { return behavior; }

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

  /**
   * @deprecated waiting for new implementation
   */
  @Override
  @Deprecated
  public void setupLocationSupport() { behavior.setupLocationSupport(); }
  @Override
  public LocationMethodsSupport getLocationSupport() { return behavior.getLocationSupport(); }

  /**
   * Perform here all the operations required before creating API methods support.
   * @param savedInstanceState saved state
   */
  protected void onInitialize(final Bundle savedInstanceState) {
    // nothing
  }

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    behavior = ActivityBehaviorFactory.createBehavior(this);
    super.onCreate(savedInstanceState);
    onInitialize(savedInstanceState);
    behavior.onCreate(savedInstanceState);
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
    super.onContentChanged();
    behavior.onContentChanged();
  }
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
