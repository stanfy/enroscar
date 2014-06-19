package com.stanfy.enroscar.async;

/**
 * This interface can be implemented by {@link Async} instances that
 * deliver results that are associated with some resources and must be released
 * when they are not used anymore (e.g. operation that loads a {@code Cursor} or opens a file).
 * @param <D> type of data to be releases
 */
public interface Releaser<D> {

  void release(D data);

}
