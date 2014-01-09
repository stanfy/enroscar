package com.stanfy.enroscar.goro;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;

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
   * Used as a workaround for http://code.google.com/p/android/issues/detail?id=6822
   */
  static final String EXTRA_TASK_BUNDLE = "task_bundle";

  /** Delegate executor. */
  private static Executor delegateExecutor;

  /** Binder instance. */
  private GoroBinder binder;

  /**
   * Set an executor instance that is used to actually perform tasks.
   * @param delegateExecutor executor instance
   */
  public static void setDelegateExecutor(final Executor delegateExecutor) {
    GoroService.delegateExecutor = delegateExecutor;
  }

  /**
   * Create an intent that contains a task that should be scheduled
   * on a defined queue.
   * Intent can be used as an argument for
   * {@link android.content.Context#startService(android.content.Intent)}.
   *
   * @param context context instance
   * @param task task instance
   * @param queueName queue name
   * @param <T> task type
   */
  public static <T extends Callable<?> & Parcelable> Intent taskIntent(final Context context,
                                                                       final T task,
                                                                       final String queueName) {
    Bundle bundle = new Bundle();
    bundle.putParcelable(EXTRA_TASK, task);
    return new Intent(context, GoroService.class)
        .putExtra(EXTRA_TASK_BUNDLE, bundle)
        .putExtra(EXTRA_QUEUE_NAME, queueName);
  }

  /**
   * Create an intent that contains a task that should be scheduled
   * on a default queue.
   * @param context context instance
   * @param task task instance
   * @param <T> task type
   * @see #taskIntent(android.content.Context, java.util.concurrent.Callable, String)
   */
  public static <T extends Callable<?> & Parcelable> Intent taskIntent(final Context context,
                                                                       final T task) {
    return taskIntent(context, task, Goro.DEFAULT_QUEUE);
  }

  private GoroBinder getBinder() {
    if (binder == null) {
      binder = new GoroBinder(createGoro());
    }
    return binder;
  }

  protected static Callable<?> getTaskFromExtras(final Intent intent) {
    if (!intent.hasExtra(EXTRA_TASK) && !intent.hasExtra(EXTRA_TASK_BUNDLE)) {
      return null;
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

    return (Callable<?>)taskArg;
  }

  @Override
  public int onStartCommand(final Intent intent, final int flags, final int startId) {
    if (intent != null) {
      Callable<?> task = getTaskFromExtras(intent);
      if (task != null) {
        String queueName = intent.hasExtra(EXTRA_QUEUE_NAME)
            ? intent.getStringExtra(EXTRA_QUEUE_NAME)
            : Goro.DEFAULT_QUEUE;

        getBinder().goro.schedule(task, queueName);
      }
    }
    return START_STICKY;
  }

  @Override
  public IBinder onBind(final Intent intent) {
    return getBinder();
  }

  protected Goro createGoro() {
    return delegateExecutor != null ? new Goro(delegateExecutor) : new Goro();
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
