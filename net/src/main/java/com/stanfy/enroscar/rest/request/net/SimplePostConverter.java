package com.stanfy.enroscar.rest.request.net;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLConnection;
import java.util.LinkedList;

import org.apache.http.client.utils.URLEncodedUtils;

import android.content.Context;
import android.util.Log;

import com.stanfy.enroscar.rest.request.Parameter;
import com.stanfy.enroscar.rest.request.ParameterValue;
import com.stanfy.enroscar.rest.request.RequestDescription;

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
    final LinkedList<ParameterValue> parameters = new LinkedList<ParameterValue>();
    for (final Parameter p : requestDescription.getSimpleParameters().getChildren()) {
      if (p instanceof ParameterValue) {
        parameters.add((ParameterValue)p);
      }
    }
    final String encoding = requestDescription.getEncoding().name();
    final byte[] content = URLEncodedUtils.format(parameters, encoding).getBytes(encoding);

    final OutputStream stream = connection.getOutputStream();
    stream.write(content);
    stream.flush();

    if (DEBUG) { Log.d(TAG, "(" + requestDescription.getId() + ")" + ": " + parameters.toString()); }
  }

}
