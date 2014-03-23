package com.stanfy.enroscar.content.async;

/**
 * Asynchronous result which may be used either to observe results of an asynchronous
 * operation or cancel its execution.
 * It's a bit similar to {@link java.util.concurrent.Future}. But instead of {@code get()} it has
 * {@link #subscribe(AsyncObserver)}.
 * After {@link #cancel()} is called observer methods won't be called.
 *
 * <p>
 *   Asynchronous operation that provides this result is triggered (meaning actually executed)
 *   not sooner than the first {@link #subscribe(AsyncObserver)} call is performed.
 *   If {@link #cancel()} is called before the first {@link #subscribe(AsyncObserver)} invocation,
 *   the operations is never executed.
 * </p>
 *
 * @param <D> data type
 * @author Roman Mazur - Stanfy (http://stanfy.com)
 */
public interface Async<D> {

  void subscribe(AsyncObserver<D> observer);

  void cancel();

}
