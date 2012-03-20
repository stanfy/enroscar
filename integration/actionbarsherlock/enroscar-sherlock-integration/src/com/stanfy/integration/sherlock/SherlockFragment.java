package com.stanfy.integration.sherlock;

import static com.stanfy.integration.sherlock.SherlockFragmentActivity.DEBUG;
import android.app.Activity;
import android.util.Log;

import com.actionbarsherlock.internal.view.menu.MenuItemMule;
import com.actionbarsherlock.internal.view.menu.MenuMule;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.stanfy.app.Application;
import com.stanfy.app.BaseFragment;

public class SherlockFragment<AT extends Application> extends BaseFragment<AT> {
  private static final String TAG = "SherlockFragment";

  @Override
  protected SherlockFragmentActivity<AT> getOwnerActivity() { return (SherlockFragmentActivity<AT>)super.getOwnerActivity(); }

  private SherlockFragmentActivity<?> mActivity;

  @Override
  public void onAttach(final Activity activity) {
    if (!(activity instanceof SherlockFragmentActivity)) {
      throw new IllegalStateException(TAG + " must be attached to a SherlockFragmentActivity.");
    }
    mActivity = (SherlockFragmentActivity<?>)activity;

    super.onAttach(activity);
  }

  @Override
  public final void onCreateOptionsMenu(final android.view.Menu menu, final android.view.MenuInflater inflater) {
    if (DEBUG) Log.d(TAG, "[onCreateOptionsMenu] menu: " + menu + ", inflater: " + inflater);

    if (menu instanceof MenuMule) {
      onCreateOptionsMenu(((MenuMule)menu).unwrap(), mActivity.getSupportMenuInflater());
    }
  }

  public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
    //Nothing to see here.
  }

  @Override
  public final void onPrepareOptionsMenu(final android.view.Menu menu) {
    if (DEBUG) Log.d(TAG, "[onPrepareOptionsMenu] menu: " + menu);

    if (menu instanceof MenuMule) {
      onPrepareOptionsMenu(((MenuMule)menu).unwrap());
    }
  }

  public void onPrepareOptionsMenu(final Menu menu) {
    //Nothing to see here.
  }

  @Override
  public final boolean onOptionsItemSelected(final android.view.MenuItem item) {
    if (DEBUG) Log.d(TAG, "[onOptionsItemSelected] item: " + item);

    if (item instanceof MenuItemMule) {
      return onOptionsItemSelected(((MenuItemMule)item).unwrap());
    }
    return false;
  }

  public boolean onOptionsItemSelected(final MenuItem item) {
    //Nothing to see here.
    return false;
  }

}
