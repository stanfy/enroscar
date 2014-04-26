package com.stanfy.enroscar.goro.support;

import com.stanfy.enroscar.goro.FutureObserver;
import com.stanfy.enroscar.goro.Goro;

import java.util.concurrent.Callable;

import rx.Observable;
import rx.Subscriber;

/**
 * Integration point for RxJava.
 * @author Roman Mazur - Stanfy (http://stanfy.com)
 */
public class RxGoro {

  /** Goro instance. */
  private final Goro goro;

  public RxGoro(final Goro goro) {
    this.goro = goro;
  }

  /**
   * @see {@link Goro#schedule(Callable)}
   */
  public <T> Observable<T> schedule(final Callable<T> task) {
    return schedule(Goro.DEFAULT_QUEUE, task);
  }

  /**
   * @see {@link Goro#schedule(String, Callable)}
   */
  public <T> Observable<T> schedule(final String queue, final Callable<T> task) {
    return Observable.create(new Observable.OnSubscribe<T>() {
      @Override
      public void call(final Subscriber<? super T> subscriber) {
        goro.schedule(queue, task).subscribe(new FutureObserver<T>() {
          @Override
          public void onSuccess(final T value) {
            subscriber.onNext(value);
            subscriber.onCompleted();
          }

          @Override
          public void onError(Throwable error) {
            subscriber.onError(error);
          }
        });
      }
    });
  }

}
