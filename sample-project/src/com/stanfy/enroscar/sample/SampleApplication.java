package com.stanfy.enroscar.sample;

import java.util.ArrayList;

import com.google.gson.reflect.TypeToken;
import com.stanfy.app.Application;
import com.stanfy.enroscar.sample.model.Tweet;
import com.stanfy.serverapi.RequestMethodHelper;
import com.stanfy.serverapi.request.RequestDescription;
import com.stanfy.serverapi.response.ParserContext;
import com.stanfy.serverapi.response.json.OneClassModelParserContext;

/**
 * Sample application.
 */
public class SampleApplication extends Application {

  /** Application authority for content provider configuration. */
  public static final String APP_AUTHORITY = "com.stanfy.enroscar.sample";

  @Override
  public void onCreate() {
    super.onCreate();
    setImagesDAOAuthority(APP_AUTHORITY);
  }

  @Override
  protected RequestMethodHelper createRequestMethodHelper() { return new RequestMethodProvider(); }

  /**
   * Factory for request descriptions and parser contexts.
   */
  private static class RequestMethodProvider extends RequestMethodHelper {

    public RequestMethodProvider() {
      super(TYPE_JSON, SampleApplication.APP_AUTHORITY);
    }

    @Override
    public ParserContext createParserContext(final RequestDescription requestDescription) {
      switch (OurOperation.byCode(requestDescription.getOperationCode())) {
      case GET_TWEETS:
        return OneClassModelParserContext.create(new TypeToken<ArrayList<Tweet>>() {});
      default:
        return super.createParserContext(requestDescription);
      }
    }

  }

}
