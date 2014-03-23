package com.stanfy.enroscar.goro;

import java.util.ArrayList;

import static com.stanfy.enroscar.goro.Util.checkMainThread;

/**
 * Maintains list of Goro listeners.
 */
class BaseListenersHandler {

  /** Task listeners collection. */
  final ArrayList<GoroListener> taskListeners;

  public BaseListenersHandler(final int initCount) {
    taskListeners = new ArrayList<>(initCount);
  }

  private static void checkThread() {
    if (!checkMainThread()) {
      throw new GoroException("Listeners cannot be modified outside the main thread");
    }
  }

  public void addTaskListener(final GoroListener listener) {
    if (listener == null) {
      throw new IllegalArgumentException("Listener cannot be null");
    }
    checkThread();
    if (taskListeners.contains(listener)) {
      throw new GoroException("Listener " + listener + " is already registered");
    }
    taskListeners.add(listener);
  }

  public void removeTaskListener(final GoroListener listener) {
    if (listener == null) {
      throw new IllegalArgumentException("Listener cannot be null");
    }
    checkThread();
    if (!taskListeners.remove(listener)) {
      throw new GoroException("Listener " + listener + " is not registered");
    }
  }

}
