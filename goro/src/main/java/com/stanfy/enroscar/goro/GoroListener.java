package com.stanfy.enroscar.goro;

import java.util.concurrent.Callable;

/**
 * Listener to Goro tasks.
 */
public interface GoroListener {

  void onTaskSchedule(Callable<?> task, String queue);

  void onTaskStart(Callable<?> task);

  void onTaskFinish(Callable<?> task, Object result);

  void onTaskCancel(Callable<?> task);

  void onTaskError(Callable<?> task, Throwable error);

}
