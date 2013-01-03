package com.stanfy.enroscar.sample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.View.OnClickListener;

import com.stanfy.app.activities.OneFragmentActivity;
import com.stanfy.enroscar.sample.DevelopersLogoFragment.TweetsListener;
import com.stanfy.enroscar.sample.other.GallerySampleActivity;
import com.stanfy.enroscar.sample.other.QuickActionsSampleActivity;

public class SampleActivity extends OneFragmentActivity implements TweetsListener, OnClickListener {

  @Override
  protected int getLayoutId() { return R.layout.activity; }

  @Override
  protected void onInitialize(final Bundle savedInstanceState) {
    super.onInitialize(savedInstanceState);
    findViewById(R.id.button_qademo).setOnClickListener(this);
    findViewById(R.id.button_gallerydemo).setOnClickListener(this);
    findViewById(R.id.button_pendingdemo).setOnClickListener(this);
  }

  @Override
  protected Fragment createFragment(final Bundle savedInstanceState) {
    return new DevelopersLogoFragment();
  }

  @Override
  public void onShowTweets() {
    getSupportFragmentManager().beginTransaction()
      .replace(R.id.fragment_container, new TwitterFragment())
      .commit();
  }

  @Override
  public void onClick(final View v) {
    switch (v.getId()) {
    case R.id.button_qademo:
      startActivity(new Intent(this, QuickActionsSampleActivity.class));
      break;
    case R.id.button_gallerydemo:
      startActivity(new Intent(this, GallerySampleActivity.class));
      break;
    case R.id.button_pendingdemo:
      startActivity(new Intent(this, PendingRequestExample.class));
    default:
    }
  }

  @Override
  public void loadProfile() {
    getSupportFragmentManager().beginTransaction()
    .replace(R.id.fragment_container, new LoadProfileFragment())
    .commit();
  }

}
