package com.stanfy.enroscar.sample.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.stanfy.enroscar.async.Action;
import com.stanfy.enroscar.async.Async;
import com.stanfy.enroscar.async.Load;
import com.stanfy.enroscar.async.Tools;
import com.stanfy.enroscar.goro.BoundGoro;
import com.stanfy.enroscar.goro.Goro;
import com.stanfy.enroscar.goro.support.AsyncGoro;
import com.stanfy.enroscar.sample.SampleApplication;
import com.stanfy.enroscar.sample.data.Tweet;
import com.stanfy.enroscar.sample.data.remote.TwitterApi;

import java.util.List;
import java.util.concurrent.Callable;

import javax.inject.Inject;

/**
 * @author Roman Mazur - Stanfy (http://stanfy.com)
 */
public class SampleActivity extends FragmentActivity {

  @Inject
  TwitterApi api;

  private final BoundGoro goro = Goro.bindWith(this);

  @Load Async<List<Tweet>> loadTweets(final TwitterApi api) {
    return new AsyncGoro(goro).schedule(new Callable<List<Tweet>>() {
      @Override
      public List<Tweet> call() throws Exception {
        return api.tweets("stanfy");
      }
    });
  }

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    SampleApplication.get(this).inject(this);

    SampleActivityOperator operator = SampleActivityOperator.build()
        .operations(this)
        .withinActivity(this)
        .get();

    operator.when().loadTweetsIsFinished()
        .doOnResult(new Action<List<Tweet>>() {
          @Override
          public void act(final List<Tweet> data) {
            Log.d("123123", data.toString());
          }
        });

    operator.loadTweets(api);
  }

  @Override
  protected void onStart() {
    super.onStart();
    goro.bind();
  }

  @Override
  protected void onStop() {
    super.onStop();
    goro.unbind();
  }

}
