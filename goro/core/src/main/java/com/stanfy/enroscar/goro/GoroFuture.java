package com.stanfy.enroscar.goro;

import com.stanfy.enroscar.goro.Goro.GoroImpl;

import java.lang.ref.WeakReference;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * Future implementation.
 */
/*
  TODO: future listeners (like Guava's listenable future), Rx support?, tests
 */
class GoroFuture<T> extends FutureTask<T> {

  /** Weak reference to Goro. */
  private final WeakReference<GoroImpl> goroRef;

  /** Task. */
  private Callable<T> task;

  GoroFuture(final GoroImpl goro, final Callable<T> task) {
    super(task);
    this.task = task;
    this.goroRef = new WeakReference<>(goro);
  }

  @Override
  public void run() {
    GoroImpl goro = goroRef.get();
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
    GoroImpl goro = goroRef.get();
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
