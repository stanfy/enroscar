package com.stanfy.enroscar.goro;

import java.util.concurrent.Executor;
import java.util.concurrent.Future;

/**
 * Extends {@link java.util.concurrent.Future} interface with methods that allow adding listeners
 * to task execution results.
 *
 * @param <V> task execution result type
 *
 * @author Roman Mazur - Stanfy (http://stanfy.com)
 */
public interface ObservableFuture<V> extends Future<V> {

  void subscribe(Executor executor, FutureObserver<V> observer);

  void subscribe(FutureObserver<V> observer);

}
