package com.stanfy.enroscar.async;

/**
 * Runtime exceptions that is thrown when error posted by an async operation
 * is not processed.
 * @author Roman Mazur - Stanfy (http://stanfy.com)
 */
public class AsyncError extends RuntimeException {

  public AsyncError(final Throwable cause) {
    super(cause);
  }

}
