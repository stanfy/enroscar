package com.stanfy.enroscar.async;

/**
 * Represents some asynchronous operation that can observed and/or canceled.
 * It's a bit similar to {@link java.util.concurrent.Future}. But instead of {@code get()} it has
 * {@link #subscribe(AsyncObserver)}.
 * After {@link #cancel()} is called observer methods won't be called.
 *
 * <p>
 *   Asynchronous operation that provides the result is triggered (meaning actually executed)
 *   not sooner than the first {@link #subscribe(AsyncObserver)} call is performed.
 *   If {@link #cancel()} is called before the first {@link #subscribe(AsyncObserver)} invocation,
 *   the operations is never executed, and {@code subscribe()} is no-op.
 * </p>
 *
 * <p>
 *   Implementations should not keep strong references to {@code Activity}s since they can be
 *   retained during Activity recreation.
 * </p>
 *
 * @param <D> data type
 * @author Roman Mazur - Stanfy (http://stanfy.com)
 */
public interface Async<D> {

  void subscribe(AsyncObserver<D> observer);

  void cancel();

}
