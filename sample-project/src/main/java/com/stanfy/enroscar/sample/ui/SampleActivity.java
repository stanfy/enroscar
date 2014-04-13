package com.stanfy.enroscar.sample.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.stanfy.enroscar.async.AsyncObserver;
import com.stanfy.enroscar.async.Tools;
import com.stanfy.enroscar.goro.BoundGoro;
import com.stanfy.enroscar.goro.Goro;
import com.stanfy.enroscar.sample.BuildConfig;
import com.stanfy.enroscar.sample.SampleApplication;
import com.stanfy.enroscar.sample.data.Tweet;
import com.stanfy.enroscar.sample.data.remote.TwitterApi;

import java.util.List;

import javax.inject.Inject;

import retrofit.RestAdapter;

import static com.stanfy.enroscar.async.Tools.loading;
import static retrofit.RestAdapter.LogLevel.FULL;
import static retrofit.RestAdapter.LogLevel.NONE;

/**
 * @author Roman Mazur - Stanfy (http://stanfy.com)
 */
public class SampleActivity extends FragmentActivity {

  //@Inject
  TwitterApi api;

  private final BoundGoro goro = Goro.bindWith(this);

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    RestAdapter restAdapter = new RestAdapter.Builder()
        .setLogLevel(BuildConfig.DEBUG ? FULL : NONE)
        .setEndpoint("https://api.twitter.com/1.1")
        .build();
    api = restAdapter.create(TwitterApi.class);

    super.onCreate(savedInstanceState);
    //SampleApplication.get(this).inject(this);
    loading(TweetsData.class, this).loadTweets(api).subscribe(new AsyncObserver<List<Tweet>>() {
      @Override
      public void onError(Throwable e) {
        throw new RuntimeException(e);
      }


      @Override
      public void onResult(List<Tweet> data) {
        Log.d("123123", data + "");
      }
    });
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
