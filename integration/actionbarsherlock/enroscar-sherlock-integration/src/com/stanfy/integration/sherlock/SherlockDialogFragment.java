package com.stanfy.integration.sherlock;

import android.app.Activity;
import android.support.v4.app._ActionBarSherlockTrojanHorse.OnCreateOptionsMenuListener;
import android.support.v4.app._ActionBarSherlockTrojanHorse.OnOptionsItemSelectedListener;
import android.support.v4.app._ActionBarSherlockTrojanHorse.OnPrepareOptionsMenuListener;

import com.actionbarsherlock.internal.view.menu.MenuItemWrapper;
import com.actionbarsherlock.internal.view.menu.MenuWrapper;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.stanfy.app.BaseDialogFragment;

public class SherlockDialogFragment extends BaseDialogFragment implements OnCreateOptionsMenuListener, OnPrepareOptionsMenuListener, OnOptionsItemSelectedListener {
  private SherlockFragmentActivity mActivity;

  public SherlockFragmentActivity getSherlockActivity() {
    return mActivity;
  }

  @Override
  public void onAttach(final Activity activity) {
    if (!(activity instanceof SherlockFragmentActivity)) {
      throw new IllegalStateException(getClass().getSimpleName() + " must be attached to a SherlockFragmentActivity.");
    }
    mActivity = (SherlockFragmentActivity)activity;

    super.onAttach(activity);
  }

  @Override
  public void onDetach() {
    mActivity = null;
    super.onDetach();
  }

  @Override
  public final void onCreateOptionsMenu(final android.view.Menu menu, final android.view.MenuInflater inflater) {
    onCreateOptionsMenu(new MenuWrapper(menu), mActivity.getSupportMenuInflater());
  }

  @Override
  public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
    //Nothing to see here.
  }

  @Override
  public final void onPrepareOptionsMenu(final android.view.Menu menu) {
    onPrepareOptionsMenu(new MenuWrapper(menu));
  }

  @Override
  public void onPrepareOptionsMenu(final Menu menu) {
    //Nothing to see here.
  }

  @Override
  public final boolean onOptionsItemSelected(final android.view.MenuItem item) {
    return onOptionsItemSelected(new MenuItemWrapper(item));
  }

  @Override
  public boolean onOptionsItemSelected(final MenuItem item) {
    //Nothing to see here.
    return false;
  }
}

