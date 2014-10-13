package com.stanfy.enroscar.goro.sample.tape;

import android.app.Application;
import android.content.Context;

import dagger.ObjectGraph;

public class SampleApplication extends Application {

  private ObjectGraph objectGraph;

  @Override
  public void onCreate() {
    super.onCreate();
    objectGraph = ObjectGraph.create(new AppModule(this));
  }

  public static ObjectGraph graph(final Context context, final Object... modules) {
    SampleApplication app = (SampleApplication) context.getApplicationContext();
    return app.objectGraph.plus(modules);
  }

}
