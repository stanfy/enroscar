package com.stanfy.enroscar.sample;

import com.stanfy.enroscar.sample.data.remote.TwitterApi;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit.RestAdapter;

import static retrofit.RestAdapter.LogLevel.FULL;
import static retrofit.RestAdapter.LogLevel.NONE;

/**
 * Application module.
 */
@Module(injects = {
    SampleApplication.class
})
class AppModule {

  /** Application instance. */
  private final SampleApplication application;

  /** REST adapter. */
  private final RestAdapter restAdapter;

  public AppModule(final SampleApplication application) {
    this.application = application;
    restAdapter = new RestAdapter.Builder()
        .setLogLevel(BuildConfig.DEBUG ? FULL : NONE)
        .setEndpoint("https://api.twitter.com/1.1")
        .build();
  }

  @Provides @Singleton SampleApplication application() {
    return application;
  }

  @Provides @Singleton TwitterApi twitterApi() {
    return restAdapter.create(TwitterApi.class);
  }

}
