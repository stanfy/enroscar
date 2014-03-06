package com.stanfy.enroscar.sample.ui;

import com.stanfy.enroscar.content.async.Async;
import com.stanfy.enroscar.content.async.Load;
import com.stanfy.enroscar.sample.data.Tweet;
import com.stanfy.enroscar.sample.data.remote.TwitterApi;

import java.util.List;
import java.util.concurrent.Callable;

import static com.stanfy.enroscar.content.async.Tools.async;

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

}
