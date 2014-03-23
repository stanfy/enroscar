package com.stanfy.enroscar.content.async;

/**
 * Observer of async result.
 *
 * @param <D> data type
 * @author Roman Mazur - Stanfy (http://stanfy.com)
 */
public interface AsyncObserver<D> {

  void onError(Throwable e);

  void onResult(D data);

}
