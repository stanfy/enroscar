package com.stanfy.serverapi.request.net;

import java.io.IOException;
import java.net.URLConnection;
import java.util.ArrayList;

import android.content.Context;

import com.stanfy.app.beans.BeansManager;
import com.stanfy.io.BuffersPool;
import com.stanfy.io.IoUtils;
import com.stanfy.io.PoolableBufferedOutputStream;
import com.stanfy.serverapi.request.RequestDescription;
import com.stanfy.serverapi.request.binary.BinaryData;


/**
 * Make a POST request with a payload.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
public class PayloadPostConverter extends PostConverter {

  /** Converter factory. */
  public static final ConverterFactory FACTORY = new ConverterFactory() {
    @Override
    public BaseRequestDescriptionConverter createConverter(final RequestDescription requestDescription, final Context context) {
      return new PayloadPostConverter(requestDescription, context);
    }
  };

  public PayloadPostConverter(final RequestDescription requestDescription, final Context context) {
    super(requestDescription, context, "text/plain");
  }

  @Override
  public URLConnection prepareConnectionInstance() throws IOException {
    URLConnection connection = super.prepareConnectionInstance();
    asHttp(connection).setChunkedStreamingMode(0);
    return connection;
  }

  @Override
  public void sendRequest(final URLConnection connection) throws IOException {
    ArrayList<BinaryData<?>> binaryData = requestDescription.getBinaryData();
    if (binaryData != null) {
      final BuffersPool buffersPool = BeansManager.get(context).getMainBuffersPool();
      final PoolableBufferedOutputStream out = new PoolableBufferedOutputStream(connection.getOutputStream(), buffersPool);

      try {
        int size = binaryData.size();
        for (int i = 0; i < size; i++) {
          binaryData.get(i).writeContentTo(context, out);
        }
      } finally {
        IoUtils.closeQuietly(out);
      }
    }
  }

}
