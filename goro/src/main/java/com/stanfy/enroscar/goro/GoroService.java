package com.stanfy.enroscar.goro;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

/**
 * Service that handles tasks in multiple queues.
 */
public class GoroService extends Service {

  /** Binder instance. */
  private GoroBinder binder;

  @Override
  public IBinder onBind(final Intent intent) {
    if (binder == null) {
      binder = new GoroBinder();
    }
    return binder;
  }

  /** Goro service binder. */
  static class GoroBinder extends Binder {

    /** Goro instance. */
    final Goro goro = new Goro(new Queues.Impl());

  }

}
