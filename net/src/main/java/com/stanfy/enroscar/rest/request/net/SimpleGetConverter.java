package com.stanfy.enroscar.rest.request.net;

import java.io.IOException;
import java.net.URLConnection;

import android.content.Context;
import android.net.Uri;

import com.stanfy.enroscar.rest.request.Parameter;
import com.stanfy.enroscar.rest.request.ParameterValue;
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
  public void sendRequest(final URLConnection connection) {
    // nothing, it's GET ;)
  }

}
