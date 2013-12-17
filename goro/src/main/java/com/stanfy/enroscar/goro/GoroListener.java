package com.stanfy.enroscar.goro;

/**
 * Listener to Goro tasks.
 */
public interface GoroListener {

  void onTaskStart(Runnable task);

  void onTaskFinish(Runnable task);

  void onTaskError(Runnable task, Throwable error);

}
