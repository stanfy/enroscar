package com.stanfy.enroscar.goro;

import com.stanfy.enroscar.goro.Goro.GoroImpl;

import java.lang.ref.WeakReference;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.FutureTask;

/**
 * Future implementation.
 */
/*
  TODO: Rx support?
 */
final class GoroFuture<T> extends FutureTask<T> implements ObservableFuture<T> {

  /** Immediate executor. */
  static final Executor IMMEDIATE = new Executor() {
    @Override
    public void execute(final @SuppressWarnings("NullableProblems") Runnable command) {
      command.run();
    }
  };

  /** Observers list. */
  final ExecutionObserversList observers = new ExecutionObserversList();

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
    if (goro != null && task != null && !isDone()) {
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
      observers.execute();
    }

  }

  @Override
  public void subscribe(final Executor executor, final FutureObserver<T> observer) {
    observers.add(new ObserverRunnable<>(observer, this), executor);
  }

  @Override
  public void subscribe(final FutureObserver<T> observer) {
    subscribe(IMMEDIATE, observer);
  }

  /**
   * What can be added to the observers list.
   * @param <T> future data type
   */
  static final class ObserverRunnable<T> implements Runnable {

    /** Observer reference. */
    private final FutureObserver<T> observer;

    /** Future instance. */
    GoroFuture<T> future;

    ObserverRunnable(final FutureObserver<T> observer, final GoroFuture<T> future) {
      this.observer = observer;
      this.future = future;
    }

    @Override
    public void run() {
      T value = null;
      Throwable error = null;
      boolean cancelled = false;

      try {
        value = future.get();
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      } catch (CancellationException e) {
        cancelled = true;
      } catch (ExecutionException e) {
        error = e.getCause();
      } finally {
        future = null;
      }

      if (cancelled) {
        return;
      }

      if (error != null) {
        observer.onError(error);
      } else {
        observer.onSuccess(value);
      }
    }
  }

}
