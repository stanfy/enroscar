package com.stanfy.enroscar.rest.request.net;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.stanfy.enroscar.rest.Utils;
import com.stanfy.enroscar.net.operation.Parameter;
import com.stanfy.enroscar.net.operation.ParameterValue;
import com.stanfy.enroscar.net.operation.RequestDescription;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLConnection;

/**
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
public class SimplePostConverter extends PostConverter {

  /** Simple POST converter factory. */
  public static final ConverterFactory FACTORY = new ConverterFactory() {
    @Override
    public BaseRequestDescriptionConverter createConverter(final RequestDescription requestDescription, final Context context) {
      return new SimplePostConverter(requestDescription, context);
    }
  };

  public SimplePostConverter(final RequestDescription requestDescription, final Context context) {
    super(requestDescription, context, "application/x-www-form-urlencoded");
  }

  @Override
  public void sendRequest(final URLConnection connection) throws IOException {
    Uri.Builder builder = Uri.parse("http://any.com").buildUpon();
    for (final Parameter p : getRequestDescription().getSimpleParameters().getChildren()) {
      if (p instanceof ParameterValue) {
        builder.appendQueryParameter(p.getName(), ((ParameterValue) p).getValue());
      }
    }
    @SuppressWarnings("ConstantConditions")
    String query = builder.build().getEncodedQuery();
    if (query == null) { query = ""; }
    final byte[] content = query.getBytes(getRequestDescription().getEncoding().name());

    final OutputStream stream = connection.getOutputStream();
    stream.write(content);
    stream.flush();

    if (Utils.isDebugRest(getContext())) { Log.d(TAG, "(" + getRequestDescription().getId() + ")" + ": " + query); }
  }

}
