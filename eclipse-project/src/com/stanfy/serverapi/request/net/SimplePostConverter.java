package com.stanfy.serverapi.request.net;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLConnection;
import java.util.LinkedList;

import org.apache.http.client.utils.URLEncodedUtils;

import android.content.Context;
import android.util.Log;

import com.stanfy.serverapi.request.Parameter;
import com.stanfy.serverapi.request.ParameterValue;
import com.stanfy.serverapi.request.RequestDescription;

/**
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
public class SimplePostConverter extends PostConverter {

  public SimplePostConverter() {
    super("application/x-www-form-urlencoded");
  }

  @Override
  public void sendRequest(final Context context, final URLConnection connection, final RequestDescription requestDescription) throws IOException {
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
