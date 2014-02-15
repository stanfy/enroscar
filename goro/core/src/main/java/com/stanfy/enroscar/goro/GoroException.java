package com.stanfy.enroscar.goro;

/**
 * Exception thrown when crucial operations cannot be done.
 * Like being unable to bind to GoroService in
 * {@link com.stanfy.enroscar.goro.GoroService#bind(android.content.Context, android.content.ServiceConnection)}.
 */
public class GoroException extends RuntimeException {

  public GoroException(final String message) {
    super(message);
  }

}
