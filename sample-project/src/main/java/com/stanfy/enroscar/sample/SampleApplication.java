package com.stanfy.enroscar.sample;

import android.app.Application;
import android.content.Context;

import dagger.ObjectGraph;

/**
 * Sample application.
 */
public class SampleApplication extends Application {

  /** Dagger graph. */
  private ObjectGraph graph;

  public static SampleApplication get(final Context context) {
    return (SampleApplication) context.getApplicationContext();
  }

  @Override
  public void onCreate() {
    super.onCreate();
    graph = ObjectGraph.create(new AppModule(this));
  }

  public void inject(final Object target) {
    graph.inject(target);
  }

}
