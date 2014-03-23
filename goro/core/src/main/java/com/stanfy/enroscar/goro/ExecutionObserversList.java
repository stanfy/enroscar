package com.stanfy.enroscar.goro;

import java.util.concurrent.Executor;

/**
 * Shamelessly taken from Guava with some reworks.
 * See ExecutionList there.
 * Main change: execution order has no guarantees.
 */
class ExecutionObserversList {

  /** Linked list pointer to the first subscriber. */
  private ObserverExecutorPair observersHead;

  /** Flag indicating that subscribers are already notified. */
  private boolean executed;

  public void add(final Runnable runnable, final Executor executor) {

    synchronized (this) {
      if (!executed) {
        observersHead = new ObserverExecutorPair(runnable, executor, observersHead);
        return;
      }
    }

    executor.execute(runnable);
  }

  public void execute() {
    ObserverExecutorPair head;
    synchronized (this) {
      if (executed) {
        return;
      }
      executed = true;
      head = observersHead;
      // 1. allow GC to free listeners even if this stays around for a while.
      // 2. make it impossible to write to this head after exiting the current block
      observersHead = null;
    }

    // It's assumed that all writes to the observers head were made before the block above.
    // And we can iterate over the elements without holding a lock.

    for (; head != null; head = head.next) {
      executeObserver(head.executor, head.what);
    }
  }

  protected void executeObserver(final Executor executor, final Runnable what) {
    executor.execute(what);
  }

  /** Pair of observers' runnable and its executor inside our linked list. */
  private static final class ObserverExecutorPair {
    /** What to do. */
    final Runnable what;
    /** How to do. */
    final Executor executor;
    /** Next element pointer. */
    ExecutionObserversList.ObserverExecutorPair next;

    ObserverExecutorPair(final Runnable what, final Executor executor,
                         final ObserverExecutorPair next) {
      this.what = what;
      this.executor = executor;
      this.next = next;
    }
  }

}
