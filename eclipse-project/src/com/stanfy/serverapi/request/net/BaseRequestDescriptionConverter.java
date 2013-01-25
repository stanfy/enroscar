package com.stanfy.serverapi.request.net;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URLConnection;

import android.content.Context;
import android.util.Log;

import com.stanfy.DebugFlags;
import com.stanfy.net.UrlConnectionWrapper;
import com.stanfy.serverapi.request.OperationType;
import com.stanfy.serverapi.request.RequestDescription;

/**
 * Converts request description to {@link URLConnection}.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
public abstract class BaseRequestDescriptionConverter {

  /** Logging tag. */
  protected static final String TAG = RequestDescription.TAG;

  /** Debug flag. */
  protected static final boolean DEBUG = DebugFlags.DEBUG_API;

  /** Request description. */
  final RequestDescription requestDescription;

  /** Context. */
  final Context context;

  public BaseRequestDescriptionConverter(final RequestDescription requestDescription, final Context context) {
    this.requestDescription = requestDescription;
    this.context = context;
  }

  public abstract URLConnection prepareConnectionInstance() throws IOException;

  public abstract void sendRequest(final URLConnection connection) throws IOException;

  private static String opertionTypeToString(final int type) {
    switch (type) {
    case OperationType.SIMPLE_GET: return "GET";
    case OperationType.SIMPLE_POST: return "POST";
    case OperationType.UPLOAD_POST: return "POST, multipart";
    default: return "unknown";
    }
  }

  public void connect(final URLConnection connection) throws IOException {
    if (DEBUG) {
      final String idPrefix = "(" + requestDescription.getId() + ") ";
      Log.d(TAG, idPrefix + "Connect to " + connection.getURL() + " <" + opertionTypeToString(requestDescription.getOperationType()) + ">");
      Log.d(TAG, idPrefix + "Headers: " + connection.getRequestProperties());
    }
    connection.connect();
  }

  protected static HttpURLConnection asHttp(final URLConnection connection) {
    return (HttpURLConnection)UrlConnectionWrapper.unwrap(connection);
  }

  /** Converter factory. */
  public interface ConverterFactory {
    /** @return converter instance */
    BaseRequestDescriptionConverter createConverter(final RequestDescription requestDescription, final Context context);
  }

}
