package com.stanfy.enroscar.integration.sherlock;

import android.os.Bundle;
import android.view.KeyEvent;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.stanfy.enroscar.activities.ActivityBehaviorFactory;
import com.stanfy.enroscar.activities.BaseActivityBehavior;

/**
 * Sherlock activity that extends {@link com.stanfy.enroscar.activities.BaseFragmentActivity}.
 */
public class SherlockFragmentActivity extends com.actionbarsherlock.app.SherlockFragmentActivity {

  /** Behavior. */
  private SherlockActivityBehavior behavior;

  /** @return the behavior */
  protected BaseActivityBehavior getBehavior() { return behavior; }

  /**
   * Perform here all the operations required before creating API methods support.
   * @param savedInstanceState saved state
   */
  protected void onInitialize(final Bundle savedInstanceState) {
    // nothing
  }

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    behavior = (SherlockActivityBehavior) ActivityBehaviorFactory.createBehavior(this);
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
    return behavior.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event);
  }

  @Override
  public boolean onOptionsItemSelected(final MenuItem item) {
    return behavior.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
  }
  @Override
  public boolean onCreateOptionsMenu(final Menu menu) {
    return behavior.onCreateOptionsMenu(menu) || super.onCreateOptionsMenu(menu);
  }

  @Override
  public void onOptionsMenuClosed(final android.view.Menu menu) {
    behavior.onOptionsMenuClosed(menu);
    super.onOptionsMenuClosed(menu);
  }

}
