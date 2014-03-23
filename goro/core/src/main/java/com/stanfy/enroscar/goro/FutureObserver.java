package com.stanfy.enroscar.goro;

/**
 * Task execution result listener.
 *
 * @param <V> execution result type
 *
 * @author Roman Mazur - Stanfy (http://stanfy.com)
 */
public interface FutureObserver<V> {

  void onSuccess(V value);

  void onError(Throwable error);

}
