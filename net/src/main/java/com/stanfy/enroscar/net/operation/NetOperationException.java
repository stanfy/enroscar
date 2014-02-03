package com.stanfy.enroscar.net.operation;

import java.io.IOException;

/**
 *
 * @author Roman Mazur - Stanfy (http://stanfy.com)
 */
public class NetOperationException extends Exception {

  private static final long serialVersionUID = 3234117139338549788L;

  /** Connection error flag. */
  private final boolean connectionError;

  /** Request. */
  private final RequestDescription requestDescription;

  public static NetOperationException connectionError(final RequestDescription rd,
                                                      final IOException ioException) {
    return new NetOperationException(true, rd, ioException);
  }

  public static NetOperationException analyzerError(final RequestDescription rd,
                                                    final Exception cause) {
    return new NetOperationException(false, rd, cause);
  }

  private NetOperationException(final boolean connectionError,
                                final RequestDescription rd,
                                final Throwable cause) {
    super(cause);
    this.connectionError = connectionError;
    this.requestDescription = rd;
  }

  public boolean isConnectionError() {
    return connectionError;
  }

  public RequestDescription getRequestDescription() {
    return requestDescription;
  }

}
