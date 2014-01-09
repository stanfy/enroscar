package com.stanfy.enroscar.goro;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;

import java.util.concurrent.Callable;

/**
 * Service that handles tasks in multiple queues.
 */
public class GoroService extends Service {

  /**
   * Used as a {@link android.os.Parcelable} field in service command intent to pass
   * a task for execution. {@link android.os.Parcelable} instance must also implement
   * {@link java.util.concurrent.Callable} interface.
   */
  public static final String EXTRA_TASK = "task";

  /**
   * Used as a {@link java.lang.String} field in service command intent to pass
   * a queue name. If this extra is not defined, {@link Goro#DEFAULT_QUEUE} is used.
   * You may manually set value to {@code null} in order to perform task beyond any queue.
   */
  public static final String EXTRA_QUEUE_NAME = "queue_name";

  /**
   * Used for a workaround described in TODO
   */
  static final String EXTRA_TASK_BUNDLE = "task_bundle";

  /** Binder instance. */
  private GoroBinder binder;

  private GoroBinder getBinder() {
    if (binder == null) {
      binder = new GoroBinder(createGoro());
    }
    return binder;
  }

  @Override
  public int onStartCommand(final Intent intent, final int flags, final int startId) {
    if (intent != null) {
      if (!intent.hasExtra(EXTRA_TASK) && !intent.hasExtra(EXTRA_TASK_BUNDLE)) {
        throw new IllegalArgumentException("Task is not defined");
      }
      Parcelable taskArg = intent.getParcelableExtra(EXTRA_TASK);
      if (taskArg == null) {
        Bundle bundle = intent.getBundleExtra(EXTRA_TASK_BUNDLE);
        if (bundle != null) {
          taskArg = bundle.getParcelable(EXTRA_TASK);
        }
      }

      if (!(taskArg instanceof Callable)) {
        throw new IllegalArgumentException("Task " + taskArg + " is not a Callable");
      }
      Callable<?> task = (Callable<?>) taskArg;

      String queueName = intent.hasExtra(EXTRA_QUEUE_NAME)
          ? intent.getStringExtra(EXTRA_QUEUE_NAME)
          : Goro.DEFAULT_QUEUE;

      getBinder().goro.schedule(task, queueName);
    }
    return START_STICKY;
  }

  @Override
  public IBinder onBind(final Intent intent) {
    return getBinder();
  }

  protected Goro createGoro() {
    return new Goro(new Queues.Impl());
  }

  /** Goro service binder. */
  static class GoroBinder extends Binder {

    /** Goro instance. */
    final Goro goro;

    public GoroBinder(final Goro goro) {
      this.goro = goro;
    }

  }

}
