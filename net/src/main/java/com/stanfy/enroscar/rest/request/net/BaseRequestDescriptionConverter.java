package com.stanfy.enroscar.rest.request.net;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URLConnection;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.stanfy.enroscar.beans.BeansManager;
import com.stanfy.enroscar.net.UrlConnectionBuilder;
import com.stanfy.enroscar.net.UrlConnectionBuilderFactory;
import com.stanfy.enroscar.net.UrlConnectionWrapper;
import com.stanfy.enroscar.rest.Utils;
import com.stanfy.enroscar.rest.request.OperationType;
import com.stanfy.enroscar.rest.request.Parameter;
import com.stanfy.enroscar.rest.request.ParameterValue;
import com.stanfy.enroscar.rest.request.RequestDescription;

/**
 * Converts request description to {@link URLConnection}.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
public abstract class BaseRequestDescriptionConverter {

  /** Bean name of UrlConnectionBuilderFactory. */
  public static final String CONNECTION_BUILDER_FACTORY_NAME = "enroscar.RequestConnectionBuilder";

  /** Logging tag. */
  protected static final String TAG = RequestDescription.TAG;

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
    case OperationType.PAYLOAD_POST: return "POST, payload";
    default: return "unknown";
    }
  }

  public void connect(final URLConnection connection) throws IOException {
    if (Utils.isDebugRest(context)) {
      final String idPrefix = "(" + requestDescription.getId() + ") ";
      Log.d(TAG, idPrefix + "Connect to " + connection.getURL() + " <" + opertionTypeToString(requestDescription.getOperationType()) + ">");
      Log.d(TAG, idPrefix + "Headers: " + connection.getRequestProperties());
    }
    connection.connect();
  }

  /**
   * @return URL connection builder with set cache,
   */
  protected UrlConnectionBuilder prepareUrlConnectionBuilder() {
    UrlConnectionBuilderFactory factory = BeansManager.get(context).getContainer().getBean(CONNECTION_BUILDER_FACTORY_NAME, UrlConnectionBuilderFactory.class);
    if (factory == null) {
      throw new IllegalStateException("UrlConnectionBuilderFactory bean is not defined.");
    }
    return factory.newUrlConnectionBuilder()
        .setCacheManagerName(requestDescription.getCacheName())
        .setContentHandlerName(requestDescription.getContentHandler())
        .setModelType(requestDescription.getModelType());
  }

  /**
   * Composes URI builder using {@link RequestDescription#getUrl()} and {@link RequestDescription#getSimpleParameters()}.
   * @return request URI builder
   */
  protected Uri.Builder buildUri() {
    final Uri.Builder builder = Uri.parse(requestDescription.getUrl()).buildUpon();
    for (final Parameter p : requestDescription.getSimpleParameters().getChildren()) {
      if (p instanceof ParameterValue) {
        builder.appendQueryParameter(p.getName(), ((ParameterValue) p).getValue());
      }
    }
    return builder;
  }
  
  /**
   * @param connection potentially wrapped connection
   * @return unwrapped {@link HttpURLConnection} instance
   */
  protected static HttpURLConnection asHttp(final URLConnection connection) {
    return (HttpURLConnection)UrlConnectionWrapper.unwrap(connection);
  }

  /** Converter factory. */
  public interface ConverterFactory {
    /** @return converter instance */
    BaseRequestDescriptionConverter createConverter(final RequestDescription requestDescription, final Context context);
  }

}
