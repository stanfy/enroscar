package com.stanfy.integration.sherlock;

import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;

import com.actionbarsherlock.ActionBarSherlock;
import com.actionbarsherlock.ActionBarSherlock.OnActionModeFinishedListener;
import com.actionbarsherlock.ActionBarSherlock.OnActionModeStartedListener;
import com.actionbarsherlock.ActionBarSherlock.OnCreatePanelMenuListener;
import com.actionbarsherlock.ActionBarSherlock.OnMenuItemSelectedListener;
import com.actionbarsherlock.ActionBarSherlock.OnPreparePanelListener;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.stanfy.DebugFlags;
import com.stanfy.app.Application;
import com.stanfy.app.BaseFragmentActivity;

/**
 * Sherlock activity that extends {@link BaseFragmentActivity}.
 */
public abstract class SherlockFragmentActivity<AT extends Application> extends BaseFragmentActivity<AT> implements OnCreatePanelMenuListener, OnPreparePanelListener, OnMenuItemSelectedListener, OnActionModeStartedListener, OnActionModeFinishedListener {

  static final boolean DEBUG = DebugFlags.DEBUG_GUI;
  private static final String TAG = "SherlockFragmentActivity";

  private ActionBarSherlock mSherlock;
  private boolean mIgnoreNativeCreate = false;
  private boolean mIgnoreNativePrepare = false;
  private boolean mIgnoreNativeSelected = false;
  private Boolean mOverrideNativeCreate = null;

  protected final ActionBarSherlock getSherlock() {
    if (mSherlock == null) {
      mSherlock = ActionBarSherlock.wrap(this, ActionBarSherlock.FLAG_DELEGATE);
    }
    return mSherlock;
  }


  ///////////////////////////////////////////////////////////////////////////
  // Action bar and mode
  ///////////////////////////////////////////////////////////////////////////

  public ActionBar getSupportActionBar() {
    return getSherlock().getActionBar();
  }

  public ActionMode startActionMode(final ActionMode.Callback callback) {
    return getSherlock().startActionMode(callback);
  }

  @Override
  public void onActionModeStarted(final ActionMode mode) {}

  @Override
  public void onActionModeFinished(final ActionMode mode) {}


  ///////////////////////////////////////////////////////////////////////////
  // General lifecycle/callback dispatching
  ///////////////////////////////////////////////////////////////////////////

  @Override
  public void onConfigurationChanged(final Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    getSherlock().dispatchConfigurationChanged(newConfig);
  }

  @Override
  protected void onPostResume() {
    super.onPostResume();
    getSherlock().dispatchPostResume();
  }

  @Override
  protected void onPause() {
    getSherlock().dispatchPause();
    super.onPause();
  }

  @Override
  protected void onStop() {
    getSherlock().dispatchStop();
    super.onStop();
  }

  @Override
  protected void onPostCreate(final Bundle savedInstanceState) {
    getSherlock().dispatchPostCreate(savedInstanceState);
    super.onPostCreate(savedInstanceState);
  }

  @Override
  protected void onTitleChanged(final CharSequence title, final int color) {
    getSherlock().dispatchTitleChanged(title, color);
    super.onTitleChanged(title, color);
  }

  @Override
  public final boolean onMenuOpened(final int featureId, final android.view.Menu menu) {
    if (getSherlock().dispatchMenuOpened(featureId, menu)) {
      return true;
    }
    return super.onMenuOpened(featureId, menu);
  }

  @Override
  public void onPanelClosed(final int featureId, final android.view.Menu menu) {
    getSherlock().dispatchPanelClosed(featureId, menu);
    super.onPanelClosed(featureId, menu);
  }

  @Override
  public boolean dispatchKeyEvent(final KeyEvent event) {
    if (getSherlock().dispatchKeyEvent(event)) {
      return true;
    }
    return super.dispatchKeyEvent(event);
  }


  ///////////////////////////////////////////////////////////////////////////
  // Native menu handling
  ///////////////////////////////////////////////////////////////////////////

  public MenuInflater getSupportMenuInflater() {
    if (DEBUG) Log.d(TAG, "[getSupportMenuInflater]");

    return getSherlock().getMenuInflater();
  }

  @Override
  public void invalidateOptionsMenu() {
    if (DEBUG) Log.d(TAG, "[invalidateOptionsMenu]");

    getSherlock().dispatchInvalidateOptionsMenu();
  }

  protected void supportInvalidateOptionsMenu() {
    if (DEBUG) Log.d(TAG, "[supportInvalidateOptionsMenu]");

    invalidateOptionsMenu();
  }

  @Override
  public final boolean onCreatePanelMenu(final int featureId, final android.view.Menu menu) {
    if (DEBUG) Log.d(TAG, "[onCreatePanelMenu] featureId: " + featureId + ", menu: " + menu);

    if (featureId == Window.FEATURE_OPTIONS_PANEL && !mIgnoreNativeCreate) {
      mIgnoreNativeCreate = true;
      final boolean result = getSherlock().dispatchCreateOptionsMenu(menu);
      mIgnoreNativeCreate = false;

      if (DEBUG) Log.d(TAG, "[onCreatePanelMenu] returning " + result);
      return result;
    }
    return super.onCreatePanelMenu(featureId, menu);
  }

  @Override
  public final boolean onCreateOptionsMenu(final android.view.Menu menu) {
    return (mOverrideNativeCreate != null) ? mOverrideNativeCreate.booleanValue() : true;
  }

  @Override
  public final boolean onPreparePanel(final int featureId, final View view, final android.view.Menu menu) {
    if (DEBUG) Log.d(TAG, "[onPreparePanel] featureId: " + featureId + ", view: " + view + ", menu: " + menu);

    if (featureId == Window.FEATURE_OPTIONS_PANEL && !mIgnoreNativePrepare) {
      mIgnoreNativePrepare = true;
      final boolean result = getSherlock().dispatchPrepareOptionsMenu(menu);
      mIgnoreNativePrepare = false;

      if (DEBUG) Log.d(TAG, "[onPreparePanel] returning " + result);
      return result;
    }
    return super.onPreparePanel(featureId, view, menu);
  }

  @Override
  public final boolean onPrepareOptionsMenu(final android.view.Menu menu) {
    return true;
  }

  @Override
  public final boolean onMenuItemSelected(final int featureId, final android.view.MenuItem item) {
    if (DEBUG) Log.d(TAG, "[onMenuItemSelected] featureId: " + featureId + ", item: " + item);

    if (featureId == Window.FEATURE_OPTIONS_PANEL && !mIgnoreNativeSelected) {
      mIgnoreNativeSelected = true;
      final boolean result = getSherlock().dispatchOptionsItemSelected(item);
      mIgnoreNativeSelected = false;

      if (DEBUG) Log.d(TAG, "[onMenuItemSelected] returning " + result);
      return result;
    }
    return super.onMenuItemSelected(featureId, item);
  }

  @Override
  public final boolean onOptionsItemSelected(final android.view.MenuItem item) {
    return false;
  }

  @Override
  public void openOptionsMenu() {
    if (!getSherlock().dispatchOpenOptionsMenu()) {
      super.openOptionsMenu();
    }
  }

  @Override
  public void closeOptionsMenu() {
    if (!getSherlock().dispatchCloseOptionsMenu()) {
      super.closeOptionsMenu();
    }
  }


  ///////////////////////////////////////////////////////////////////////////
  // Sherlock menu handling
  ///////////////////////////////////////////////////////////////////////////

  @Override
  public boolean onCreatePanelMenu(final int featureId, final Menu menu) {
    if (DEBUG) Log.d(TAG, "[onCreatePanelMenu] featureId: " + featureId + ", menu: " + menu);

    if (featureId == Window.FEATURE_OPTIONS_PANEL) {
      boolean result = onCreateOptionsMenu(menu);

      //Dispatch to parent panel creation for fragment dispatching
      if (DEBUG) Log.d(TAG, "[onCreatePanelMenu] dispatching to native with mule");
      mOverrideNativeCreate = result;
      final boolean fragResult = super.onCreatePanelMenu(featureId, new MenuMule(menu));
      mOverrideNativeCreate = null;

      if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
        result |= menu.hasVisibleItems();
      } else {
        result |= fragResult;
      }

      return result;
    }
    return false;
  }

  public boolean onCreateOptionsMenu(final Menu menu) {
    return true;
  }

  @Override
  public boolean onPreparePanel(final int featureId, final View view, final Menu menu) {
    if (DEBUG) Log.d(TAG, "[onPreparePanel] featureId: " + featureId + ", view: " + view + " menu: " + menu);

    if (featureId == Window.FEATURE_OPTIONS_PANEL) {
      final boolean result = onPrepareOptionsMenu(menu);

      //Dispatch to parent panel preparation for fragment dispatching
      if (DEBUG) Log.d(TAG, "[onPreparePanel] dispatching to native with mule");
      super.onPreparePanel(featureId, view, new MenuMule(menu));

      return result;
    }
    return false;
  }

  public boolean onPrepareOptionsMenu(final Menu menu) {
    return true;
  }

  @Override
  public boolean onMenuItemSelected(final int featureId, final MenuItem item) {
    if (DEBUG) Log.d(TAG, "[onMenuItemSelected] featureId: " + featureId + ", item: " + item);

    if (featureId == Window.FEATURE_OPTIONS_PANEL) {
      boolean result = onOptionsItemSelected(item);

      //Dispatch to parent panel selection for fragment dispatching
      if (DEBUG) Log.d(TAG, "[onMenuItemSelected] dispatching to native with mule");
      result |= super.onMenuItemSelected(featureId, new MenuItemMule(item));

      return result;
    }
    return false;
  }

  public boolean onOptionsItemSelected(final MenuItem item) {
    return false;
  }


  ///////////////////////////////////////////////////////////////////////////
  // Content
  ///////////////////////////////////////////////////////////////////////////

  @Override
  public void addContentView(final View view, final LayoutParams params) {
    getSherlock().addContentView(view, params);
  }

  @Override
  public void setContentView(final int layoutResId) {
    getSherlock().setContentView(layoutResId);
  }

  @Override
  public void setContentView(final View view, final LayoutParams params) {
    getSherlock().setContentView(view, params);
  }

  @Override
  public void setContentView(final View view) {
    getSherlock().setContentView(view);
  }

  public void requestWindowFeature(final long featureId) {
    getSherlock().requestFeature((int)featureId);
  }


  ///////////////////////////////////////////////////////////////////////////
  // Progress Indication
  ///////////////////////////////////////////////////////////////////////////

  public void setSupportProgress(final int progress) {
    getSherlock().setProgress(progress);
  }

  public void setSupportProgressBarIndeterminate(final boolean indeterminate) {
    getSherlock().setProgressBarIndeterminate(indeterminate);
  }

  public void setSupportProgressBarIndeterminateVisibility(final boolean visible) {
    getSherlock().setProgressBarIndeterminateVisibility(visible);
  }

  public void setSupportProgressBarVisibility(final boolean visible) {
    getSherlock().setProgressBarVisibility(visible);
  }

  public void setSupportSecondaryProgress(final int secondaryProgress) {
    getSherlock().setSecondaryProgress(secondaryProgress);
  }

}
