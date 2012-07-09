package com.stanfy.serverapi.request.net;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.http.util.EncodingUtils;

import android.content.Context;

import com.stanfy.app.beans.BeansManager;
import com.stanfy.io.BuffersPool;
import com.stanfy.io.PoolableBufferedOutputStream;
import com.stanfy.net.UrlConnectionWrapper;
import com.stanfy.serverapi.request.Parameter;
import com.stanfy.serverapi.request.ParameterValue;
import com.stanfy.serverapi.request.RequestDescription;
import com.stanfy.serverapi.request.binary.BinaryData;
import com.stanfy.serverapi.request.net.multipart.Part;
import com.stanfy.serverapi.request.net.multipart.StringPart;

/**
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
public class UploadPostConverter extends PostConverter {

  /**
   * The pool of ASCII chars to be used for generating a multipart boundary.
   */
  private static final byte[] MULTIPART_CHARS = EncodingUtils.getAsciiBytes("-_1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ");

  /**
   * Generates a random multipart boundary string.
   */
  /* From MultipartEntity */
  private static byte[] generateMultipartBoundary() {
    final Random rand = new Random();
    final int c11 = 11, c30 = 30;
    final byte[] bytes = new byte[rand.nextInt(c11) + c30]; // a random size from 30 to 40
    for (int i = 0; i < bytes.length; i++) {
      bytes[i] = MULTIPART_CHARS[rand.nextInt(MULTIPART_CHARS.length)];
    }
    return bytes;
  }

  /** Boundary. */
  private final byte[] boundary;

  public UploadPostConverter() {
    super(null);
    this.boundary = generateMultipartBoundary();
  }

  @Override
  public URLConnection prepareConnectionInstance(final Context context, final RequestDescription requestDescription) throws IOException {
    final URLConnection connection = super.prepareConnectionInstance(context, requestDescription);
    connection.addRequestProperty("Content-Type", "multipart/form-data; boundary=" + EncodingUtils.getAsciiString(boundary));
    return connection;
  }

  @Override
  public void sendRequest(final Context context, final URLConnection connection, final RequestDescription requestDescription) throws IOException {
    // TODO Content Length
    final Part[] parts = composeParts(context, requestDescription);

    final HttpURLConnection http = (HttpURLConnection)UrlConnectionWrapper.unwrap(connection);
    http.setFixedLengthStreamingMode((int)Part.getLengthOfParts(parts, boundary));

    final BuffersPool buffersPool = BeansManager.get(context).getMainBuffersPool();
    final PoolableBufferedOutputStream out = new PoolableBufferedOutputStream(connection.getOutputStream(), buffersPool);

    try {
      Part.sendParts(out, parts, boundary);
    } finally {
      out.close();
    }

  }

  protected Part[] composeParts(final Context context, final RequestDescription requestDescription) throws IOException {
    final ArrayList<BinaryData<?>> binaryData = requestDescription.getBinaryData();
    final List<Parameter> params = requestDescription.getSimpleParameters().getChildren();
    int realCount = 0;
    final int binaryCount = binaryData != null ? binaryData.size() : 0;
    Part[] parts = new Part[params.size() + binaryCount];
    for (final Parameter p : params) {
      if (p instanceof ParameterValue) {
        final ParameterValue pv = (ParameterValue)p;
        if (pv.getValue() == null) { continue; }
        parts[realCount++] = new StringPart(pv.getName(), pv.getValue(), requestDescription.getEncoding().name());
      }
    }
    for (int i = 0; i < binaryCount; i++) {
      final Part part = binaryData.get(i).createHttpPart(context);
      if (part != null) {
        parts[realCount++] = part;
      }
    }
    if (realCount < parts.length) {
      final Part[] trim = new Part[realCount];
      System.arraycopy(parts, 0, trim, 0, realCount);
      parts = trim;
    }
    return parts;
  }

}
