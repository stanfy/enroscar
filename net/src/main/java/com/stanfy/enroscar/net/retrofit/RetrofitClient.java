package com.stanfy.enroscar.net.retrofit;

import com.stanfy.enroscar.net.UrlConnectionBuilderFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit.RetrofitError;
import retrofit.client.Client;
import retrofit.client.Header;
import retrofit.client.Request;
import retrofit.client.Response;
import retrofit.mime.TypedInput;
import retrofit.mime.TypedOutput;

/**
 * Client implementation for Square's Retrofit.
 */
public class RetrofitClient implements Client {

  /** Value for setChunkedStreamingMode. */
  private static final int CHUNK_SIZE = 4096;

  /** Factory URL connection builder. */
  private final UrlConnectionBuilderFactory connectionBuilderFactory;

  /** Cached reference to method field in HttpURLConnection. */
  private final Field methodField;

  public RetrofitClient() {
    this(UrlConnectionBuilderFactory.DEFAULT);
  }

  public RetrofitClient(final UrlConnectionBuilderFactory connectionBuilder) {
    this.connectionBuilderFactory = connectionBuilder;
    try {
      this.methodField = HttpURLConnection.class.getDeclaredField("method");
      this.methodField.setAccessible(true);
    } catch (NoSuchFieldException e) {
      throw RetrofitError.unexpectedError(null, e);
    }
  }

  @Override
  public Response execute(final Request request) throws IOException {
    URLConnection connection = connectionBuilderFactory.newUrlConnectionBuilder()
        .setUrl(getUrl(request))
        .create();
    prepareRequest(connection, request);
    return readResponse(connection);
  }

  private String getUrl(final Request request) {
    String url = request.getUrl();
    if (url.startsWith("data:")) {
      int throwPlace = url.indexOf('?');
      if (throwPlace != -1) {
        url = url.substring(0, throwPlace);
      }
    }
    return url;
  }

  private void prepareRequest(final URLConnection connection, final Request request)
      throws IOException {
    if (connection instanceof HttpURLConnection) {
      // HttpURLConnection artificially restricts request method
      try {
        ((HttpURLConnection) connection).setRequestMethod(request.getMethod());
      } catch (ProtocolException e) {
        try {
          methodField.set(connection, request.getMethod());
        } catch (IllegalAccessException e1) {
          throw RetrofitError.unexpectedError(getUrl(request), e1);
        }
      }
    }

    connection.setDoInput(true);

    for (Header header : request.getHeaders()) {
      connection.addRequestProperty(header.getName(), header.getValue());
    }

    TypedOutput body = request.getBody();
    if (body != null) {
      connection.setDoOutput(true);
      connection.addRequestProperty("Content-Type", body.mimeType());

      long length = body.length();
      if (length != -1) {
        connection.addRequestProperty("Content-Length", String.valueOf(length));
      }
      if (connection instanceof HttpURLConnection) {
        if (length != -1) {
          ((HttpURLConnection) connection).setFixedLengthStreamingMode((int) length);
        } else {
          ((HttpURLConnection) connection).setChunkedStreamingMode(CHUNK_SIZE);
        }
      }

      body.writeTo(connection.getOutputStream());
    }
  }

  private Response readResponse(URLConnection connection) throws IOException {
    int status = HttpURLConnection.HTTP_OK;
    String reason = "";
    if (connection instanceof HttpURLConnection) {
      status = ((HttpURLConnection) connection).getResponseCode();
      reason = ((HttpURLConnection) connection).getResponseMessage();
    }

    List<Header> headers = new ArrayList<>();
    for (Map.Entry<String, List<String>> field : connection.getHeaderFields().entrySet()) {
      String name = field.getKey();
      for (String value : field.getValue()) {
        headers.add(new Header(name, value));
      }
    }

    String mimeType = connection.getContentType();
    int length = connection.getContentLength();
    InputStream stream;
    if (status >= 400 && connection instanceof HttpURLConnection) {
      stream = ((HttpURLConnection) connection).getErrorStream();
    } else {
      stream = connection.getInputStream();
    }
    TypedInput responseBody = new TypedInputStream(mimeType, length, stream);
    return new Response(connection.getURL().toString(), status, reason, headers, responseBody);
  }

  /** TypedInput implementation based on input stream from URLConnection. */
  private static class TypedInputStream implements TypedInput {
    private final String mimeType;
    private final long length;
    private final InputStream stream;

    private TypedInputStream(String mimeType, long length, InputStream stream) {
      this.mimeType = mimeType;
      this.length = length;
      this.stream = stream;
    }

    @Override public String mimeType() {
      return mimeType;
    }

    @Override public long length() {
      return length;
    }

    @Override public InputStream in() throws IOException {
      return stream;
    }
  }
}
