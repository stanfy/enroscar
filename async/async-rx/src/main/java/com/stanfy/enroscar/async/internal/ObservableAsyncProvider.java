package com.stanfy.enroscar.async.internal;

import com.stanfy.enroscar.async.Async;
import com.stanfy.enroscar.async.AsyncObserver;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action1;

/**
 * Adapts {@link rx.Observable} to {@link com.stanfy.enroscar.async.Async}.
 * @param <D> data type
 * @author Roman Mazur - Stanfy (http://stanfy.com)
 */
public abstract class ObservableAsyncProvider<D> implements AsyncProvider<D> {

  @Override
  public final Async<D> provideAsync() {
    return new RxAsync<>(provideObservable());
  }

  protected abstract Observable<D> provideObservable();

  /**
   * Adapter.
   * @param <D> data type
   */
  private static class RxAsync<D> implements Async<D> {

    /** Observable. */
    private final Observable<D> rxObservable;

    /** Cancellation flag. */
    private boolean canceled;

    /** Current subscription. */
    private Subscription subscription;

    private RxAsync(Observable<D> rxObservable) {
      this.rxObservable = rxObservable;
    }

    @Override
    public RxAsync<D> replicate() {
      return new RxAsync<>(rxObservable);
    }

    @Override
    public void subscribe(final AsyncObserver<D> observer) {
      if (canceled) {
        return;
      }

      subscription = rxObservable.subscribe(new Subscriber<D>() {
        @Override
        public void onCompleted() {
          observer.onReset();
        }
        @Override
        public void onError(final Throwable e) {
          observer.onError(e);
        }
        @Override
        public void onNext(final D d) {
          observer.onResult(d);
        }
      });
    }

    @Override
    public void cancel() {
      canceled = true;
      if (subscription != null) {
        subscription.unsubscribe();
        subscription = null;
      }
    }
  }

}
