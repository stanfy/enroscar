package com.stanfy.enroscar.goro;

import android.os.IBinder;

import java.lang.ref.WeakReference;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

/**
 * Service that handles tasks in multiple queues.
 */
public class Goro {

  /** Default queue name. */
  public static final String DEFAULT_QUEUE = "default";

  /** Listeners handler. */
  final ListenersHandler listenersHandler = new ListenersHandler();

  /** Queues. */
  private final Queues queues;

  /**
   * This constructor will be removed in 2.0 version.
   * @param delegateExecutor executor Goro delegates tasks to
   * @deprecated use {@link #createWithDelegate(Executor)} factory method instead
   */
  @Deprecated
  public Goro(final Executor delegateExecutor) {
    this();
    this.queues.setDelegateExecutor(delegateExecutor);
  }

  /**
   * This constructor will be removed in 2.0 version.
   * @deprecated use {@link #create()} factory method instead
   */
  @Deprecated
  public Goro() {
    this(new Queues.Impl());
  }

  Goro(final Queues queues) {
    this.queues = queues;
  }

  public static Goro from(final IBinder binder) {
    if (binder instanceof GoroService.GoroBinder) {
      return ((GoroService.GoroBinder) binder).goro;
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
    return new Goro();
  }

  /**
   * Creates a new Goro instance which uses the specified executor to delegate tasks.
   * @param delegateExecutor executor Goro delegates tasks to
   * @return instance of Goro
   */
  public static Goro createWithDelegate(final Executor delegateExecutor) {
    return new Goro(delegateExecutor);
  }

  /**
   * Adds a task execution listener.
   * @param listener listener instance
   */
  public void addTaskListener(final GoroListener listener) {
    listenersHandler.addTaskListener(listener);
  }

  /**
   * Removes a task execution listener.
   * @param listener listener instance
   */
  public void removeTaskListener(final GoroListener listener) {
    listenersHandler.removeTaskListener(listener);
  }


  /**
   * Add a task to the default queue.
   * This methods returns a future that allows to control task execution.
   * @param task task instance
   * @return task future instance
   */
  public <T> Future<T> schedule(final Callable<T> task) {
    return schedule(DEFAULT_QUEUE, task);
  }

  /**
   * Add a task to the specified queue.
   * This methods returns a future that allows to control task execution.
   * Queue name may be null, if you want to execute the task beyond any queue.
   * @param queueName name of a queue to use, may be null
   * @param task task instance
   * @return task future instance
   */
  public <T> Future<T> schedule(final String queueName, final Callable<T> task) {
    if (task == null) {
      throw new IllegalArgumentException("Task must not be null");
    }

    GoroFuture<T> future = new GoroFuture<>(this, task);
    listenersHandler.postSchedule(task, queueName);
    queues.getExecutor(queueName).execute(future);
    return future;
  }

  /**
   * Returns an executor for performing tasks in a specified queue. If queue name is null,
   * {@link #DEFAULT_QUEUE} is used.
   * @param queueName queue name
   * @return executor instance that performs tasks serially in a specified queue
   */
  public Executor getExecutor(final String queueName) {
    return queues.getExecutor(queueName == null ? DEFAULT_QUEUE : queueName);
  }


  /**
   * Future implementation.
   */
  private static class GoroFuture<T> extends FutureTask<T> {

    /** Weak reference to Goro. */
    private final WeakReference<Goro> goroRef;

    /** Task. */
    private Callable<T> task;

    GoroFuture(final Goro goro, final Callable<T> task) {
      super(task);
      this.task = task;
      this.goroRef = new WeakReference<>(goro);
    }

    @Override
    public void run() {
      Goro goro = goroRef.get();
      Callable<?> task = this.task;

      // if task is null, it's already canceled

      // invoke onTaskStart
      if (goro != null && task != null) {
        goro.listenersHandler.postStart(task);
      }

      super.run();
    }


    @Override
    protected void done() {
      Goro goro = goroRef.get();
      if (goro == null) {
        return;
      }

      try {
        Object result = get();
        // invoke onTaskFinish
        goro.listenersHandler.postFinish(task, result);
      } catch (CancellationException e) {
        // invoke onTaskCancel
        goro.listenersHandler.postCancel(task);
      } catch (ExecutionException e) {
        // invoke onTaskError
        goro.listenersHandler.postError(task, e.getCause());
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      } finally {
        task = null;
      }

    }

  }

}
