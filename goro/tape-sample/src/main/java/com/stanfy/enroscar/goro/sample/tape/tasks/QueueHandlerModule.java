package com.stanfy.enroscar.goro.sample.tape.tasks;

import android.content.Context;

import com.stanfy.enroscar.goro.Goro;
import com.stanfy.enroscar.goro.GoroService;
import com.stanfy.enroscar.goro.sample.tape.AppModule;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
    injects = {TapeHandler.class},
    addsTo = AppModule.class,
    complete = false
)
class QueueHandlerModule {

  private final Context context;
  private final Goro goro;

  QueueHandlerModule(final Context context, final Goro goro) {
    this.context = context;
    this.goro = goro;
  }

  @Provides @Singleton
  Context context() {
    return context;
  }

  @Provides @Singleton
  Goro goro() {
    return goro;
  }

}
