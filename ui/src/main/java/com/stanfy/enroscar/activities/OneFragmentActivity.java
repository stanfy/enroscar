package com.stanfy.enroscar.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

/**
 * Activity that contains one fragment only.
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
public abstract class OneFragmentActivity extends BaseFragmentActivity {

  /** Fragment instance. */
  private Fragment fragment;

  /** @return fragment instance */
  protected abstract Fragment createFragment(final Bundle savedInstanceState);

  /** @return layout identifier */
  protected int getLayoutId() { return 0; }

  /** Fragment container ID. */
  protected int getFragmentContainerId() { return android.R.id.content; }

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
