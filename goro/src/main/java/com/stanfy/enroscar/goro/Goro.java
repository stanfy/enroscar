package com.stanfy.enroscar.goro;

import android.os.IBinder;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service that handles tasks in multiple queues.
 */
public class Goro {

  /** Default queue name. */
  public static final String DEFAULT_QUEUE = "default";

  /** Execution listeners. */
  final ArrayList<GoroListener> listeners = new ArrayList<GoroListener>();

  /** Queues. */
  private final Queues queues;

  // There is no public constructor for Goro.
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
   * Adds a task execution listener.
   * @param listener listener instance
   */
  public void addListener(final GoroListener listener) {
    synchronized (listeners) {
      listeners.add(listener);
    }
  }

  /**
   * Removes a task execution listener.
   * @param listener listener instance
   */
  public void removeListener(final GoroListener listener) {
    synchronized (listeners) {
      if (!listeners.remove(listener)) {
        throw new IllegalArgumentException("Listener " + listener + " is not registered");
      }
    }
  }


  /**
   * Add a task to the default queue.
   * This methods returns a future that allows to control task execution.
   * @param task task instance
   * @return task future instance
   */
  public <T> Future<T> schedule(final Callable<T> task) {
    return schedule(task, DEFAULT_QUEUE);
  }

  /**
   * Add a task to the specified queue.
   * This methods returns a future that allows to control task execution.
   * Queue name may be null, if you want to execute the task beyond any queue.
   * @param task task instance
   * @param queueName name of a queue to use, may be null
   * @return task future instance
   */
  public <T> Future<T> schedule(final Callable<T> task, final String queueName) {
    if (task == null) {
      throw new IllegalArgumentException("Task must not be null");
    }
    GoroFuture<T> future = new GoroFuture<T>(this, task);
    queues.getExecutor(queueName).execute(future);
    return future;
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
      this.goroRef = new WeakReference<Goro>(goro);
    }

    @Override
    public void run() {
      Goro goro = goroRef.get();
      Callable<?> task = this.task;

      // invoke onTaskStart
      if (goro != null && task != null) {
        synchronized (goro.listeners) {
          if (!goro.listeners.isEmpty()) {
            for (GoroListener listener : goro.listeners) {
              listener.onTaskStart(task);
            }
          }
        }
      }

      super.run();
    }


    @Override
    protected void done() {
      Goro goro = goroRef.get();
      if (goro == null) {
        return;
      }

      ArrayList<GoroListener> listeners = goro.listeners;
      try {
        get();

        // invoke onTaskFinish
        synchronized (goro.listeners) {
          if (!listeners.isEmpty()) {
            for (GoroListener listener : listeners) {
              listener.onTaskFinish(task);
            }
          }
        }

      } catch (CancellationException e) {

        // invoke onTaskCancel
        synchronized (goro.listeners) {
          if (!listeners.isEmpty()) {
            for (GoroListener listener : listeners) {
              listener.onTaskCancel(task);
            }
          }
        }

      } catch (ExecutionException e) {

        // invoke onTaskError
        synchronized (goro.listeners) {
          if (!listeners.isEmpty()) {
            Throwable cause = e.getCause();
            for (GoroListener listener : listeners) {
              listener.onTaskError(task, cause);
            }
          }
        }

      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      } finally {
        task = null;
      }

    }

  }

}
