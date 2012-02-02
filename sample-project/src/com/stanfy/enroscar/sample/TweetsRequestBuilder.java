package com.stanfy.enroscar.sample;

import android.content.Context;

import com.stanfy.serverapi.request.ListRequestBuilder;
import com.stanfy.serverapi.request.Operation;

/**
 * Request builder for {@link OurOperation#GET_TWEETS} operation.
 */
public class TweetsRequestBuilder extends ListRequestBuilder {

  public TweetsRequestBuilder(final Context context) {
    super(context);
  }

  @Override
  public Operation getOperation() { return OurOperation.GET_TWEETS; }

  public TweetsRequestBuilder setScreenname(final String name) {
    addSimpleParameter("screen_name", name);
    return this;
  }

}
