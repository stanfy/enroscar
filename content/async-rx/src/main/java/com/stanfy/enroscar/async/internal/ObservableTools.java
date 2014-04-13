package com.stanfy.enroscar.async.internal;

import com.stanfy.enroscar.async.AsyncObserver;

import rx.Observable;
import rx.Subscriber;

/**
 * Provides tools for creating special {@link rx.Observable}s.
 * @author Roman Mazur - Stanfy (http://stanfy.com)
 */
public final class ObservableTools {

  private ObservableTools() { /* hidden */ }

  public static <D> Observable<D> loaderObservable(final int loaderId,
                                                   final LoaderDescription description) {
    return Observable.create(new Observable.OnSubscribe<D>() {
      @Override
      public void call(final Subscriber<? super D> subscriber) {
        description.addObserver(loaderId, new AsyncObserver<D>() {
          @Override
          public void onError(final Throwable e) {
            subscriber.onError(e);
          }

          @Override
          public void onResult(D data) {
            subscriber.onNext(data);
          }
        });
      }
    });
  }

}
