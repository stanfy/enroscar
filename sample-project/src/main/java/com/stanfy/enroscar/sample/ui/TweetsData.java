package com.stanfy.enroscar.sample.ui;

import com.stanfy.enroscar.async.Async;
import com.stanfy.enroscar.async.Load;
import com.stanfy.enroscar.async.Send;
import com.stanfy.enroscar.sample.data.Tweet;
import com.stanfy.enroscar.sample.data.remote.TwitterApi;

import java.util.List;
import java.util.concurrent.Callable;

import static com.stanfy.enroscar.async.Tools.async;

/**
* @author Roman Mazur - Stanfy (http://stanfy.com)
*/
class TweetsData {

  @Load
  Async<List<Tweet>> loadTweets(final TwitterApi api) {
    return async(new Callable<List<Tweet>>() {
      @Override
      public List<Tweet> call() throws Exception {
        return api.tweets("stanfy");
      }
    });
  }

  @Send
  Async<String> send(final int param) {
    return async(new Callable<String>() {
      @Override
      public String call() throws Exception {
        return "ok " + param;
      }
    });
  }

}
