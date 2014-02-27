package com.stanfy.enroscar.content.async;

/**
 * Asynchronous result.
 * @param <D> data type
 * @author Roman Mazur - Stanfy (http://stanfy.com)
 */
public interface Async<D> {

  void subscribe(AsyncObserver<D> observer);

  void cancel();

}
