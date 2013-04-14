package com.stanfy.enroscar.rest;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URLConnection;

import android.content.Context;
import android.net.TrafficStats;
import android.os.Build;
import android.util.Log;

import com.stanfy.enroscar.net.UrlConnectionWrapper;
import com.stanfy.enroscar.rest.request.RequestDescription;

/**
 * Works with the server API method. Prepares HTTP request (URL and body),
 * executes this request and processes the response.
 * @author Roman Mazur - Stanfy (http://www.stanfy.com)
 */
public class RequestMethod {

  /** Logging tag. */
  private static final String TAG = "ReqMethod";

  /**
   * @param systemContext system context
   * @param description request description
   * @throws RequestMethodException if ever
   * @return parsed object
   */
  public RequestResult perform(final Context systemContext, final RequestDescription description) throws RequestMethodException {
    final long startTime = System.currentTimeMillis();
    
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
      TrafficStats.setThreadStatsTag(description.getStatsTag());
    }
    
    URLConnection connection = null;
    try {

      // don't even make a connection if request is canceled
      if (description.isCanceled()) { return null; }
      
      // send request
      connection = description.makeConnection(systemContext);
      
      // request is canceled - don't parse a model
      if (description.isCanceled()) { return null; }
      
      // parse response
      final Object model = connection.getContent();
      // return parsed response and connection
      return new RequestResult(model, connection);

    } catch (final IOException e) {
      throw new RequestMethodException(e);
    } catch (final RuntimeException e) {
      throw new RequestMethodException(e);
    } finally {
      
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
        TrafficStats.clearThreadStatsTag();
      }
      
      // do as mom said
      if (connection != null) {
        disconnect(connection);
      }
      if (Utils.isDebugRest(systemContext)) {
        Log.d(TAG, "Request time: " + (System.currentTimeMillis() - startTime) + " ms");
      }
    }
  }

  /**
   * Perform disconnect actions.
   * @param connection connection instance (may be wrapped)
   */
  protected void disconnect(final URLConnection connection) {
    final URLConnection http = UrlConnectionWrapper.unwrap(connection);
    if (http instanceof HttpURLConnection) {
      ((HttpURLConnection) http).disconnect();
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
