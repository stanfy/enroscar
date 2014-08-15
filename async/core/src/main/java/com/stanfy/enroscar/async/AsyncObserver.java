package com.stanfy.enroscar.async;

/**
 * Observer of async result.
 * @param <D> data type
 */
public interface AsyncObserver<D> {

  void onError(Throwable e);

  void onResult(D data);

  void onReset();

}
