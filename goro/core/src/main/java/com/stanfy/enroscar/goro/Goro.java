package com.stanfy.enroscar.goro;

import android.content.Context;
import android.os.IBinder;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;

import static com.stanfy.enroscar.goro.BoundGoro.BoundGoroImpl;

/**
 * Handles tasks in multiple queues.
 */
public abstract class Goro {

  /** Default queue name. */
  public static final String DEFAULT_QUEUE = "default";

  /**
   * Gives access to Goro instance that is provided by a service.
   * @param binder Goro service binder
   * @return Goro instance provided by the service
   */
  public static Goro from(final IBinder binder) {
    if (binder instanceof GoroService.GoroBinder) {
      return ((GoroService.GoroBinder) binder).goro();
    }
    throw new IllegalArgumentException("Cannot get Goro from " + binder);
  }

  /**
   * Creates a new Goro instance which uses {@link android.os.AsyncTask#THREAD_POOL_EXECUTOR}
   * to delegate tasks on Post-Honeycomb devices or create a separate thread pool on earlier
   * Android versions.
   * @return instance of Goro
   */
  public static Goro create() {
    return new GoroImpl();
  }

  /**
   * Creates a new Goro instance which uses the specified executor to delegate tasks.
   * @param delegateExecutor executor Goro delegates tasks to
   * @return instance of Goro
   */
  public static Goro createWithDelegate(final Executor delegateExecutor) {
    GoroImpl goro = new GoroImpl();
    goro.queues.setDelegateExecutor(delegateExecutor);
    return goro;
  }

  /**
   * Creates a Goro implementation that binds to {@link com.stanfy.enroscar.goro.GoroService}
   * in order to run scheduled tasks in service context.
   * @param context context that will bind to the service
   * @return Goro implementation that binds to {@link com.stanfy.enroscar.goro.GoroService}.
   */
  public static BoundGoro bindWith(final Context context) {
    return new BoundGoroImpl(context);
  }

  /**
   * Adds a task execution listener. Must be called from the main thread.
   * @param listener listener instance
   */
  public abstract void addTaskListener(final GoroListener listener);

  /**
   * Removes a task execution listener. Must be called from the main thread.
   * @param listener listener instance
   */
  public abstract void removeTaskListener(final GoroListener listener);


  /**
   * Add a task to the default queue.
   * This methods returns a future that allows to control task execution.
   * @param task task instance
   * @return task future instance
   */
  public abstract <T> ObservableFuture<T> schedule(final Callable<T> task);

  /**
   * Add a task to the specified queue.
   * This methods returns a future that allows to control task execution.
   * Queue name may be null, if you want to execute the task beyond any queue.
   * @param queueName name of a queue to use, may be null
   * @param task task instance
   * @return task future instance
   */
  public abstract <T> ObservableFuture<T> schedule(final String queueName, final Callable<T> task);

  /**
   * Returns an executor for performing tasks in a specified queue. If queue name is null,
   * {@link #DEFAULT_QUEUE} is used.
   * @param queueName queue name
   * @return executor instance that performs tasks serially in a specified queue
   */
  public abstract Executor getExecutor(final String queueName);


  /** Main implementation. */
  static class GoroImpl extends Goro {
    /** Listeners handler. */
    final ListenersHandler listenersHandler = new ListenersHandler();

    /** Queues. */
    private final Queues queues;

    GoroImpl() {
      this(new Queues.Impl());
    }

    GoroImpl(final Queues queues) {
      this.queues = queues;
    }

    @Override
    public void addTaskListener(final GoroListener listener) {
      listenersHandler.addTaskListener(listener);
    }

    @Override
    public void removeTaskListener(final GoroListener listener) {
      listenersHandler.removeTaskListener(listener);
    }

    @Override
    public <T> ObservableFuture<T> schedule(final Callable<T> task) {
      return schedule(DEFAULT_QUEUE, task);
    }

    @Override
    public <T> ObservableFuture<T> schedule(final String queueName, final Callable<T> task) {
      if (task == null) {
        throw new IllegalArgumentException("Task must not be null");
      }

      GoroFuture<T> future = new GoroFuture<>(this, task);
      listenersHandler.postSchedule(task, queueName);
      queues.getExecutor(queueName).execute(future);
      return future;
    }

    @Override
    public Executor getExecutor(final String queueName) {
      return queues.getExecutor(queueName == null ? DEFAULT_QUEUE : queueName);
    }
  }

}
