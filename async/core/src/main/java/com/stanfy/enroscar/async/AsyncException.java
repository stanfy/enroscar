package com.stanfy.enroscar.async;

/**
 * Runtime exception that is thrown when error posted by an async operation
 * is not processed.
 * @author Roman Mazur - Stanfy (http://stanfy.com)
 */
public class AsyncException extends RuntimeException {

  public AsyncException(final Throwable cause) {
    super(cause);
  }

}
