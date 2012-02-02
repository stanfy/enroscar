package com.stanfy.enroscar.sample;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.stanfy.app.BaseFragment;
import com.stanfy.views.LoadableImageView;

/**
 * Sample fragment.
 */
public class DevelopersLogoFragment extends BaseFragment<SampleApplication> {

  /** Tweets button click listener. */
  private TweetsListener listener;

  @Override
  public void onAttach(final Activity activity) {
    super.onAttach(activity);
    if (activity instanceof TweetsListener) {
      this.listener = (TweetsListener) activity;
    }
  }

  @Override
  public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
    final View view = inflater.inflate(R.layout.main, container, false);
    final LoadableImageView logo = (LoadableImageView)view.findViewById(R.id.logo);
    logo.setImagesManagerContext(getOwnerActivity().getApp().getImagesContext());
    logo.setImageURI(Uri.parse("http://developer.android.com/assets/images/bg_logo.png"));
    view.findViewById(R.id.tweets_btn).setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(final View v) {
        if (listener != null) { listener.onShowTweets(); }
      }
    });
    return view;
  }

  @Override
  public void onDetach() {
    super.onDetach();
    if (listener instanceof Context) {
      this.listener = null;
    }
  }

  public interface TweetsListener {
    void onShowTweets();
  }

}
