package com.stanfy.serverapi.request.net;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.util.ArrayList;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.stanfy.enroscar.beans.BeansManager;
import com.stanfy.io.BuffersPool;
import com.stanfy.io.IoUtils;
import com.stanfy.io.PoolableBufferedOutputStream;
import com.stanfy.net.UrlConnectionWrapper;
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
    final int chunkSize = 8192;
    asHttp(connection).setChunkedStreamingMode(chunkSize);
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
        doSendWorkarounds(UrlConnectionWrapper.unwrap(connection));
      } finally {
        IoUtils.closeQuietly(out);
      }
    }
  }

  protected void doSendWorkarounds(final URLConnection connection) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD && connection instanceof HttpURLConnection) {
      try {
        Field resHeaderField = connection.getClass().getDeclaredField("resHeader");
        resHeaderField.setAccessible(true);
        resHeaderField.set(connection, null);
      } catch (Exception e) {
        Log.w("Workaround", "Failed to make a wrokaround for Android 2.2", e);
      }
    }
  }

}
