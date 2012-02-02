package com.stanfy.enroscar.sample;

import static com.stanfy.serverapi.request.OperationType.SIMPLE_GET;

import com.stanfy.serverapi.request.Operation;

/**
 * Enumerate your operations here.
 */
public enum OurOperation implements Operation {

  GET_TWEETS(SIMPLE_GET, "https://api.twitter.com/1/statuses/user_timeline.json");

  /** Type */
  private final int type;
  /** URL. */
  private String url;

  private OurOperation(final int type, final String url) {
    this.type = type;
    this.url = url;
  }

  @Override
  public int getCode() { return ordinal(); }
  @Override
  public int getType() { return type; }
  @Override
  public String getUrlPart() { return url; }

  public static OurOperation byCode(final int code) { return OurOperation.values()[code]; }

}
