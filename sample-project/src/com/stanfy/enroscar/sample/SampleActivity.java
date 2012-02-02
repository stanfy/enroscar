package com.stanfy.enroscar.sample;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.stanfy.app.activities.OneFragmentActivity;
import com.stanfy.enroscar.sample.DevelopersLogoFragment.TweetsListener;

public class SampleActivity extends OneFragmentActivity<SampleApplication> implements TweetsListener {

  @Override
  protected Fragment createFragment(final Bundle savedInstanceState) {
    return new DevelopersLogoFragment();
  }

  @Override
  protected int getLayoutId() { return R.layout.activity; }

  @Override
  public void onShowTweets() {
    getSupportFragmentManager().beginTransaction()
      .replace(R.id.fragment_container, new TwitterFragment())
      .commit();
  }

}
