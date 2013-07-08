package com.stanfy.enroscar.integration.sherlock;

import java.util.List;

import android.app.Activity;
import android.support.v4.app.Watson.OnCreateOptionsMenuListener;
import android.support.v4.app.Watson.OnOptionsItemSelectedListener;
import android.support.v4.app.Watson.OnPrepareOptionsMenuListener;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.internal.view.menu.MenuItemWrapper;
import com.actionbarsherlock.internal.view.menu.MenuWrapper;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.stanfy.app.fragments.list.RequestBuilderListFragment;
import com.stanfy.enroscar.content.UniqueObject;

public abstract class SherlockRequestBuilderListFragment<MT extends UniqueObject, LT extends List<MT>>
  extends RequestBuilderListFragment<MT, LT>
  implements OnCreateOptionsMenuListener, OnPrepareOptionsMenuListener, OnOptionsItemSelectedListener {

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
  public void onCreateOptionsMenu(final android.view.Menu menu, final android.view.MenuInflater inflater) {
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
