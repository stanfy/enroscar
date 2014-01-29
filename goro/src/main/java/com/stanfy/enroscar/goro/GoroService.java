package com.stanfy.enroscar.goro;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcelable;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;

/**
 * Service that handles tasks in multiple queues.
 */
public class GoroService extends Service {

  /** Internal debug flag. */
  private static final boolean DEBUG = true;

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


  /** Bound users flag. */
  boolean hasBoundUsers;

  /** Binder instance. */
  private GoroBinder binder;

  /** Stop handler. */
  private final StopHandler stopHandler = new StopHandler(this);

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
                                                                       final String queueName,
                                                                       final T task) {
    // XXX http://code.google.com/p/android/issues/detail?id=6822
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
   * @see #taskIntent(android.content.Context, String, java.util.concurrent.Callable)
   */
  public static <T extends Callable<?> & Parcelable> Intent taskIntent(final Context context,
                                                                       final T task) {
    return taskIntent(context, Goro.DEFAULT_QUEUE, task);
  }

  /**
   * Bind to Goro service. This method will start the service and then bind to it.
   * @param context context that is binding to the service
   * @param connection service connection callbacks
   * @throws RuntimeException when service is not declared in the application manifest
   */
  public static void bind(final Context context, final ServiceConnection connection) {
    Intent serviceIntent = new Intent(context, GoroService.class);
    if (context.startService(serviceIntent) == null) {
      throw new RuntimeException("Service " + GoroService.class + " does not seem to be included to your manifest file");
    }
    context.bindService(serviceIntent, connection, 0);
  }

  /**
   * Unbind from Goro service.
   * @param context context that is unbinding from the service
   * @param connection service connection callbacks
   */
  public static void unbind(final Context context, final ServiceConnection connection) {
    context.unbindService(connection);
  }

  private GoroBinder getBinder() {
    if (binder == null) {
      binder = new GoroBinder(createGoro(), new GoroTasksListener());
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

        getBinder().goro.schedule(queueName, task);
      }
    }
    return START_STICKY;
  }

  @Override
  public IBinder onBind(final Intent intent) {
    if (DEBUG) {
      Log.w("Goro", "bind");
    }
    hasBoundUsers = true;
    stopHandler.doNotStop();
    return getBinder();
  }

  @Override
  public boolean onUnbind(final Intent intent) {
    if (DEBUG) {
      Log.w("Goro", "unbind");
    }
    hasBoundUsers = false;
    stopHandler.checkForStop();
    return true;
  }

  @Override
  public void onRebind(final Intent intent) {
    if (DEBUG) {
      Log.w("Goro", "rebind");
    }
    hasBoundUsers = true;
    stopHandler.doNotStop();
  }


  protected Goro createGoro() {
    return delegateExecutor != null ? new Goro(delegateExecutor) : new Goro();
  }


  /** Goro service binder. */
  static class GoroBinder extends Binder {

    /** Goro instance. */
    final Goro goro;

    /** Listener. */
    final GoroTasksListener listener;

    GoroBinder(final Goro goro, final GoroTasksListener listener) {
      this.goro = goro;
      this.listener = listener;
      goro.addTaskListener(listener);
    }

  }

  /** Listens to task events. */
  private class GoroTasksListener implements GoroListener {

    int activeTasksCount;

    @Override
    public void onTaskSchedule(final Callable<?> task, final String queue) {
      stopHandler.doNotStop();
      activeTasksCount++;
    }

    @Override
    public void onTaskStart(Callable<?> task) { }

    private void taskFinish() {
      activeTasksCount--;
      stopHandler.checkForStop();
    }

    @Override
    public void onTaskFinish(Callable<?> task, Object result) {
      taskFinish();
    }

    @Override
    public void onTaskCancel(Callable<?> task) {
      taskFinish();
    }

    @Override
    public void onTaskError(Callable<?> task, Throwable error) {
      taskFinish();
    }
  }

  /** Internal handler for stopping service. */
  private static class StopHandler extends Handler {

    /** Check for stop message. */
    private static final int MSG_CHECK_FOR_STOP = 1;
    /** Stop message. */
    private static final int MSG_STOP = 2;

    /** Service instance. */
    private final WeakReference<GoroService> serviceRef;

    public StopHandler(final GoroService service) {
      this.serviceRef = new WeakReference<>(service);
    }

    public void checkForStop() {
      if (DEBUG) {
        Log.w("Goro", "send check for stop");
      }
      doNotStop(); // clear any existing checks
      sendEmptyMessage(MSG_CHECK_FOR_STOP);
    }

    public void doNotStop() {
      if (DEBUG) {
        Log.w("Goro", "do not stop now");
      }
      removeMessages(MSG_STOP);
      removeMessages(MSG_CHECK_FOR_STOP);
    }

    private static boolean isServiceActive(final GoroService service) {
      boolean tasksRunning = service.binder != null && service.binder.listener.activeTasksCount > 0;
      if (DEBUG) {
        Log.w("Goro", "isServiceActive: " + service.hasBoundUsers + ", " + tasksRunning);
      }
      return service.hasBoundUsers || tasksRunning;
    }

    @Override
    public void handleMessage(final Message msg) {
      final GoroService service = serviceRef.get();
      if (service == null) { return; }

      switch (msg.what) {
        case MSG_CHECK_FOR_STOP:
          if (!isServiceActive(service)) {
            if (DEBUG) {
              Log.w("Goro", "send stop");
            }
            sendEmptyMessage(MSG_STOP);
          }
          break;

        case MSG_STOP:
          if (DEBUG) {
            Log.w("Goro", "do stop");
          }
          service.stopSelf();
          break;

        default:
          throw new IllegalArgumentException("Unexpected message " + msg);
      }
    }

  }


}
