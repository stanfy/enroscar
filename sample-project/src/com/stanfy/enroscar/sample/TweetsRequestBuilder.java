package com.stanfy.enroscar.sample;

import java.util.List;

import android.content.Context;

import com.stanfy.enroscar.rest.request.BaseRequestBuilder;
import com.stanfy.enroscar.rest.request.ListRequestBuilderWrapper;
import com.stanfy.enroscar.sample.model.Tweet;

/**
 * Custom request builder.
 */
public class TweetsRequestBuilder extends BaseRequestBuilder<List<Tweet>> {

  public TweetsRequestBuilder(final Context context) {
    super(context);
    setTargetUrl("https://api.twitter.com/1/statuses/user_timeline.json");
  }

  public TweetsRequestBuilder setScreenname(final String name) {
    addSimpleParameter("screen_name", name);
    return this;
  }

  @SuppressWarnings("unchecked")
  @Override
  public ListRequestBuilderWrapper<List<Tweet>, Tweet> asLoadMoreList() {
    return asLoadMoreList("page", "count");
  }

}
