package com.stanfy.integration.sherlock.enroscar;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.stanfy.integration.sherlock.SherlockFragmentActivity;


public abstract class SherlockOneFragmentActivity extends SherlockFragmentActivity {

  /** Fragment instance. */
  private Fragment fragment;

  /** @return fragment instance */
  protected abstract Fragment createFragment(final Bundle savedInstanceState);

  /** @return layout identifier */
  protected abstract int getLayoutId();

  /** Fragment container ID. */
  protected abstract int getFragmentContainerId();

  /** @return the fragment */
  public Fragment getFragment() { return fragment; }

  @Override
  protected void onInitialize(final Bundle savedInstanceState) {
    super.onInitialize(savedInstanceState);
    final int layoutId = getLayoutId();
    if (layoutId != 0) { setContentView(layoutId); }
    final FragmentManager manager = getSupportFragmentManager();
    final int containerId = getFragmentContainerId();
    Fragment f = manager.findFragmentById(containerId);
    if (f == null) {
      f = createFragment(savedInstanceState);
      if (f != null) {
        getSupportFragmentManager().beginTransaction()
          .add(containerId, f)
          .commit();
      }
    }
    this.fragment = f;
  }

}
