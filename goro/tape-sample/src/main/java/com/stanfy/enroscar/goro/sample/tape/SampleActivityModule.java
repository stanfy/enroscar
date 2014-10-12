package com.stanfy.enroscar.goro.sample.tape;

import com.stanfy.enroscar.goro.BoundGoro;
import com.stanfy.enroscar.goro.Goro;
import com.stanfy.enroscar.goro.support.AsyncGoro;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/** Module for SampleActivity. */
@Module(
    injects = SampleActivity.class,
    addsTo = AppModule.class
)
class SampleActivityModule {

  private final BoundGoro goro;

  SampleActivityModule(final SampleActivity activity) {
    this.goro = Goro.bindWith(activity);
  }

  @Provides @Singleton
  BoundGoro boundGoro() {
    return goro;
  }

}
