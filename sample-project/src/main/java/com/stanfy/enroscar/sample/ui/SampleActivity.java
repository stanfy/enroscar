package com.stanfy.enroscar.sample.ui;

import android.app.Activity;
import android.os.Bundle;

import com.stanfy.enroscar.goro.BoundGoro;
import com.stanfy.enroscar.goro.Goro;
import com.stanfy.enroscar.sample.SampleApplication;
import com.stanfy.enroscar.sample.data.Tweet;
import com.stanfy.enroscar.sample.data.remote.TwitterApi;

import javax.inject.Inject;

import rx.Observable;

import java.util.List;

/**
 * @author Roman Mazur - Stanfy (http://stanfy.com)
 */
public class SampleActivity extends Activity {

  @Inject
  private TwitterApi api;

  private final BoundGoro goro = Goro.bindWith(this);

  private static class Loader {

    public Observable<List<Tweet>> loadTweets() {

    }

  }

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    SampleApplication.get(this).inject(this);
    goro.
  }

  @Override
  protected void onStart() {
    super.onStart();
    goro.bind();
  }

  @Override
  protected void onStop() {
    super.onStop();
    goro.bind();
  }

}
