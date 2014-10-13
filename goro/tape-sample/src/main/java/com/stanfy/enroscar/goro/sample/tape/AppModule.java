package com.stanfy.enroscar.goro.sample.tape;

import android.content.Context;
import android.net.ConnectivityManager;

import com.squareup.tape.FileObjectQueue;
import com.squareup.tape.TaskQueue;
import com.stanfy.enroscar.goro.sample.tape.tasks.ConnectivityReceiver;
import com.stanfy.enroscar.goro.sample.tape.tasks.TransactionTask;

import java.io.IOException;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
    injects = {ConnectivityReceiver.class},
    library = true
)
public final class AppModule {

  private final Context appContext;

  AppModule(final Context appContext) {
    this.appContext = appContext.getApplicationContext();
  }

  @Provides @Singleton
  public TokenGenerator tokenGenerator() {
    return new TokenGenerator();
  }

  @Provides @Singleton
  public TaskQueue<TransactionTask> tokenTaskTaskQueue() {
    try {
      TaskSerializer serializer = new TaskSerializer();
      return new TaskQueue<>(
          new FileObjectQueue<>(appContext.getFileStreamPath("token-tasks"), serializer)
      );
    } catch (IOException e) {
      throw new AssertionError(e);
    }
  }

  @Provides
  public ConnectivityManager connectivityManager() {
    return (ConnectivityManager) appContext.getSystemService(Context.CONNECTIVITY_SERVICE);
  }

}
