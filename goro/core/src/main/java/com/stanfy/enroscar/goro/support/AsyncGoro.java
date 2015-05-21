package com.stanfy.enroscar.goro.support;

import com.stanfy.enroscar.async.Async;
import com.stanfy.enroscar.async.AsyncObserver;
import com.stanfy.enroscar.goro.FutureObserver;
import com.stanfy.enroscar.goro.Goro;
import com.stanfy.enroscar.goro.ObservableFuture;

import java.util.concurrent.Callable;

/**
 * Integration point for {@link com.stanfy.enroscar.async.Async}.
 * @author Roman Mazur - Stanfy (http://stanfy.com)
 */
public class AsyncGoro {

  /** Goro instance. */
  private final Goro goro;

  public AsyncGoro(final Goro goro) {
    this.goro = goro;
  }

  /**
   * @see {@link Goro#schedule(String, Callable)}
   */
  public <T> Async<T> schedule(final String queue, final Callable<T> task) {
    return new GoroAsync<>(queue, task);
  }

  /**
   * @see {@link Goro#schedule(Callable)}
   */
  public <T> Async<T> schedule(final Callable<T> task) {
    return schedule(Goro.DEFAULT_QUEUE, task);
  }

  /** @return wrapped {@link Goro} instance */
  public Goro getCore() { return goro; }

  /** Implementation. */
  private final class GoroAsync<T> implements Async<T> {

    /** Queue. */
    private final String queue;

    /** Task. */
    private Callable<T> task;

    /** Future instance. */
    private ObservableFuture<T> future;

    /** Cancel flag. */
    private boolean canceled;

    GoroAsync(final String queue, final Callable<T> task) {
      this.queue = queue;
      this.task = task;
    }

    @Override
    public GoroAsync<T> replicate() {
      return new GoroAsync<>(queue, task);
    }

    @Override
    public void subscribe(final AsyncObserver<T> observer) {
      synchronized (this) {
        if (canceled) {
          return;
        }

        if (future == null) {
          future = goro.schedule(queue, task);
        }
      }

      future.subscribe(new FutureObserver<T>() {
        @Override
        public void onSuccess(final T value) {
          observer.onResult(value);
        }
        @Override
        public void onError(final Throwable error) {
          observer.onError(error);
        }
      });
    }

    @Override
    public void cancel() {
      synchronized (this) {
        canceled = true;
        if (future != null) {
          future.cancel(true);
          future = null;
        }
        task = null;
      }
    }

  }

}
