package com.stanfy.serverapi.request.net;

import java.io.IOException;
import java.net.URLConnection;

import android.content.Context;

import com.stanfy.DebugFlags;
import com.stanfy.net.UrlConnectionBuilder;
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

  public abstract URLConnection prepareConnectionInstance(final Context context, final RequestDescription requestDescription) throws IOException;

  public abstract void sendRequest(final Context context, final URLConnection connection, final RequestDescription requestDescription) throws IOException;

  protected UrlConnectionBuilder createUrlConnectionBuilder(final RequestDescription rd) {
    return new UrlConnectionBuilder()
      .setCacheManagerName(rd.getCacheName())
      .setContentHandlerName(rd.getContentHandler())
      .setModelType(rd.getModelType());
  }

}
