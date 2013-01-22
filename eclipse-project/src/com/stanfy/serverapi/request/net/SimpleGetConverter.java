package com.stanfy.serverapi.request.net;

import java.io.IOException;
import java.net.URLConnection;

import android.content.Context;
import android.net.Uri;

import com.stanfy.serverapi.request.Parameter;
import com.stanfy.serverapi.request.ParameterValue;
import com.stanfy.serverapi.request.RequestDescription;

/**
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
public class SimpleGetConverter extends BaseRequestDescriptionConverter {

  /** Simple GET converter factory. */
  public static final ConverterFactory FACTORY = new ConverterFactory() {
    @Override
    public BaseRequestDescriptionConverter createConverter() {
      return new SimpleGetConverter();
    }
  };

  @Override
  public URLConnection prepareConnectionInstance(final Context context, final RequestDescription requestDescription) throws IOException {
    final Uri.Builder builder = Uri.parse(requestDescription.getUrl()).buildUpon();
    for (final Parameter p : requestDescription.getSimpleParameters().getChildren()) {
      if (p instanceof ParameterValue) {
        builder.appendQueryParameter(p.getName(), ((ParameterValue) p).getValue());
      }
    }

    final URLConnection connection = requestDescription.prepareConnectionBuilder(context)
      .setUrl(builder.build())
      .create();

    return connection;
  }

  @Override
  public void sendRequest(final Context context, final URLConnection connection, final RequestDescription requestDescription) {
    // nothing, it's GET ;)
  }

}
