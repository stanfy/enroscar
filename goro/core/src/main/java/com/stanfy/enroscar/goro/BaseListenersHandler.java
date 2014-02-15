package com.stanfy.enroscar.goro;

import android.os.Looper;

import java.util.ArrayList;

/**
 * Maintains list of Goro listeners.
 */
class BaseListenersHandler {

  /** Task listeners collection. */
  final ArrayList<GoroListener> taskListeners;

  public BaseListenersHandler(final int initCount) {
    taskListeners = new ArrayList<>(initCount);
  }

  static void checkThread() {
    if (Looper.myLooper() != Looper.getMainLooper()) {
      throw new GoroException("Listeners cannot be modified outside the main thread");
    }
  }

  public void addTaskListener(final GoroListener listener) {
    checkThread();
    if (taskListeners.contains(listener)) {
      throw new GoroException("Listener " + listener + " is already registered");
    }
    taskListeners.add(listener);
  }

  public void removeTaskListener(final GoroListener listener) {
    checkThread();
    if (!taskListeners.remove(listener)) {
      throw new GoroException("Listener " + listener + " is not registered");
    }
  }

}
