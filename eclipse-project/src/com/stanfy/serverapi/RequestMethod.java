package com.stanfy.serverapi;

import java.io.IOException;
import java.net.URLConnection;

import android.content.Context;
import android.util.Log;

import com.stanfy.DebugFlags;
import com.stanfy.serverapi.request.RequestDescription;

/**
 * Works with the server API method. Prepares HTTP request (URL and body),
 * executes this request and processes the response.
 * @author Roman Mazur - Stanfy (http://www.stanfy.com)
 */
public class RequestMethod {

  /** Logging tag. */
  private static final String TAG = "ReqMethod";
  /** Debug flag. */
  private static final boolean DEBUG = DebugFlags.DEBUG_API;

  /**
   * @param systemContext system context
   * @param description request description
   * @throws RequestMethodException if ever
   * @return parsed object
   */
  public RequestResult perform(final Context systemContext, final RequestDescription description) throws RequestMethodException {
    final long startTime = System.currentTimeMillis();
    try {
      final URLConnection connection = description.makeConnection(systemContext);
      final Object model = connection.getContent();
      return new RequestResult(model, connection);
    } catch (final IOException e) {
      throw new RequestMethodException(e);
    } catch (final RuntimeException e) {
      throw new RequestMethodException(e);
    } finally {
      if (DEBUG) {
        Log.d(TAG, "Request time: " + (System.currentTimeMillis() - startTime) + " ms");
      }
    }
  }

  /** Request result. */
  public static class RequestResult {
    /** Model instance. */
    private final Object model;
    /** Connection instance. */
    private final URLConnection connection;

    public RequestResult(final Object model, final URLConnection connection) {
      this.model = model;
      this.connection = connection;
    }

    public Object getModel() { return model; }
    public URLConnection getConnection() { return connection; }
  }

  /**
   * Request method exception.
   * @author Roman Mazur - Stanfy (http://www.stanfy.com)
   */
  public static class RequestMethodException extends Exception {

    /** serialVersionUID. */
    private static final long serialVersionUID = 3234117139338549788L;

    /** Connection error flag. */
    private boolean connectionError;

    public RequestMethodException(final IOException e) {
      super("Connection error", e);
      connectionError = true;
    }

    public RequestMethodException(final String message) {
      super(message);
      connectionError = false;
    }

    public RequestMethodException(final RuntimeException e) {
      super("Response parsing exception", e);
      connectionError = false;
    }

    /** @return the connectionError */
    public boolean isConnectionError() { return connectionError; }

  }

}
