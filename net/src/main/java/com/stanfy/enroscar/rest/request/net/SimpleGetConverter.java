package com.stanfy.enroscar.rest.request.net;

import java.io.IOException;
import java.net.URLConnection;

import android.content.Context;

import com.stanfy.enroscar.rest.request.RequestDescription;

/**
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
public class SimpleGetConverter extends BaseRequestDescriptionConverter {

  /** Simple GET converter factory. */
  public static final ConverterFactory FACTORY = new ConverterFactory() {
    @Override
    public BaseRequestDescriptionConverter createConverter(final RequestDescription requestDescription, final Context context) {
      return new SimpleGetConverter(requestDescription, context);
    }
  };

  public SimpleGetConverter(final RequestDescription requestDescription, final Context context) {
    super(requestDescription, context);
  }

  @Override
  public URLConnection prepareConnectionInstance() throws IOException {
    final URLConnection connection = requestDescription.prepareConnectionBuilder(context)
      .setUrl(buildUri().build())
      .create();

    return connection;
  }

  @Override
  public void sendRequest(final URLConnection connection) {
    // nothing, it's GET ;)
  }

}
