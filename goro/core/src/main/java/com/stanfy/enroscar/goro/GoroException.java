package com.stanfy.enroscar.goro;

/**
 * Exception thrown when crucial operations cannot be done.
 * Like being unable to bind to GoroService in
 * {@link com.stanfy.enroscar.goro.GoroService#bind(android.content.Context, android.content.ServiceConnection)}.
 * Also can be thrown in case of unhandled exception thrown by a task scheduled with startService().
 */
public class GoroException extends RuntimeException {

  public GoroException(final String message) {
    super(message);
  }

  public GoroException(final String message, final Throwable cause) {
    super(message, cause);
  }

}
