package com.stanfy.enroscar.goro;

import java.util.concurrent.Callable;

/**
 * Listener to Goro tasks.
 */
public interface GoroListener {

  /**
   * Callback method invoked when a new task is scheduled.
   * @param task task to perform
   * @param queue task queue
   */
  void onTaskSchedule(Callable<?> task, String queue);

  /**
   * Callback method invoked when a scheduled task is actually started.
   * @param task task instance
   */
  void onTaskStart(Callable<?> task);

  /**
   * Callback method invoked when a task finishes its execution successfully.
   * @param task task instance
   * @param result result of {@link Callable#call()} invocation on the task
   */
  void onTaskFinish(Callable<?> task, Object result);

  /**
   * Callback method invoked when a task is canceled.
   * @param task task instance
   */
  void onTaskCancel(Callable<?> task);

  /**
   * Callback method invoked when a task finishes its execution with an error,
   * throwing an exception. The callback should return {@code true}, if it handles the error.
   * @param task task instance
   * @param error thrown exception instance
   */
  void onTaskError(Callable<?> task, Throwable error);

}
