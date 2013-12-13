package com.stanfy.enroscar.rest.request.net;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.stanfy.enroscar.beans.BeansManager;
import com.stanfy.enroscar.io.BuffersPool;
import com.stanfy.enroscar.io.IoUtils;
import com.stanfy.enroscar.net.UrlConnectionWrapper;
import com.stanfy.enroscar.rest.request.RequestDescription;
import com.stanfy.enroscar.rest.request.binary.BinaryData;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.util.ArrayList;


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
    ArrayList<BinaryData<?>> binaryData = getRequestDescription().getBinaryData();
    if (binaryData != null) {
      final BuffersPool buffersPool = BeansManager.get(getContext()).getContainer().getBean(BuffersPool.class);
      final OutputStream out = buffersPool.bufferize(connection.getOutputStream());

      try {
        int size = binaryData.size();
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < size; i++) {
          binaryData.get(i).writeContentTo(getContext(), out);
        }
        doSendWorkarounds(UrlConnectionWrapper.unwrap(connection));
      } finally {
        IoUtils.closeQuietly(out);
      }
    }
  }

  /**
   * Make workarounds for POST requests via {@link URLConnection} on older Android versions.
   * Override this method in order to disable them.
   * @param connection connection object instance
   */
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
