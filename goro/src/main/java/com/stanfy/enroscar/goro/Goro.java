package com.stanfy.enroscar.goro;

import android.os.IBinder;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service that handles tasks in multiple queues.
 */
public class Goro {

  /** Default queue name. */
  public static final String DEFAULT_QUEUE = "default";

  /** Execution IDs provider. */
  private final AtomicInteger idCounter = new AtomicInteger();

  /** Execution listeners. */
  private final ArrayList<GoroListener> listeners = new ArrayList<GoroListener>();

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
    listeners.add(listener);
  }

  /**
   * Removes a task execution listener.
   * @param listener listener instance
   */
  public void removeListener(final GoroListener listener) {
    if (!listeners.remove(listener)) {
      throw new IllegalArgumentException("Listener " + listener + " is not registered");
    }
  }


  /**
   * Add a task to the default queue.
   * This methods returns an execution ID that may be used for further task cancellation.
   * @param task task instance
   * @return execution identifier
   * @see #cancel(int)
   */
  public int schedule(final Runnable task) {
    return schedule(task, DEFAULT_QUEUE);
  }

  /**
   * Add a task to the specified queue.
   * This methods returns an execution ID that may be used for further task cancellation.
   * @param task task instance
   * @param queueName name of a queue to use
   * @return execution identifier
   * @see #cancel(int)
   */
  public int schedule(final Runnable task, final String queueName) {
    if (task == null) {
      throw new IllegalArgumentException("Task must not be null");
    }
    int id = idCounter.incrementAndGet();
    return id;
  }

  /**
   * Cancel execution of a task with the specified ID.
   * @param executionId execution identifier
   * @see #schedule(Runnable, String)
   */
  public void cancel(final int executionId) {

  }


}
