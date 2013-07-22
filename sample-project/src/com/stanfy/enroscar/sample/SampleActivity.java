package com.stanfy.enroscar.sample;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.stanfy.enroscar.activities.OneFragmentActivity;

public class SampleActivity extends OneFragmentActivity {

  @Override
  protected int getLayoutId() { return 0; }

  @Override
  protected int getFragmentContainerId() {
    return android.R.id.content;
  }

  @Override
  protected Fragment createFragment(final Bundle savedInstanceState) {
    return new FlickrFragment();
  }

}
