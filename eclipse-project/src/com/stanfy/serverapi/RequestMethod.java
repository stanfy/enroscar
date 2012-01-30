package com.stanfy.serverapi;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.util.Log;

import com.stanfy.DebugFlags;
import com.stanfy.app.Application;
import com.stanfy.images.BuffersPool;
import com.stanfy.images.PoolableBufferedInputStream;
import com.stanfy.serverapi.cache.APICacheDAO;
import com.stanfy.serverapi.cache.APICacheDAO.CachedStreamInfo;
import com.stanfy.serverapi.request.RequestDescription;
import com.stanfy.serverapi.response.ParserContext;
import com.stanfy.serverapi.response.Response;
import com.stanfy.serverapi.response.ResponseHanlder;

/**
 * Represents the server API method. Prepares HTTP request (URL and body),
 * executes this request and processes the response.
 * @author Roman Mazur - Stanfy (http://www.stanfy.com)
 */
public abstract class RequestMethod {

  /** Logging tag. */
  private static final String TAG = "ReqMethod";
  /** Debug flag. */
  private static final boolean DEBUG = DebugFlags.DEBUG_API;

  /** Thread lock. */
  private static final ReentrantLock LOCK = new ReentrantLock();

  /** Request identifier. */
  private static Long requestId = 0L;

  /** Http client instance. */
  private HttpClient httpClient;

  /** Application authority. Used for caching */
  private final String cacheAuthority;

  /** Buffers pool. */
  private final BuffersPool buffersPool;

  protected RequestMethod(final String cacheAuthority, final BuffersPool buffersPool) {
    this.cacheAuthority = cacheAuthority;
    this.buffersPool = buffersPool;
  }

  /**
   * Input stream is closed after this method invocation.
   * @param stream input stream
   * @param charset input characters set
   * @return string
   * @throws IOException if an error happens
   */
  private static String streamToString(final InputStream stream, final Charset charset) throws IOException {
    final Reader in = new InputStreamReader(new BufferedInputStream(stream), charset);
    final StringBuilder result = new StringBuilder();
    final int bsize = 8192;
    final char[] buffer = new char[bsize];
    int cnt;
    try {
      do {
        cnt = in.read(buffer);
        if (cnt > 0) { result.append(buffer, 0, cnt); }
      } while (cnt >= 0);
      return result.toString();
    } finally {
      in.close();
    }
  }

  public void setup(final Application app) {
    this.httpClient = app.getHttpClientsPool().getHttpClient();
  }

  public void stop(final Application app) {
    app.getHttpClientsPool().releaseHttpClient(httpClient);
  }

  /** @return the httpClient */
  public HttpClient getHttpClient() { return httpClient; }
  /** @param httpClient the httpClient to set */
  public void setHttpClient(final HttpClient httpClient) {
    this.httpClient = httpClient;
  }

  /**
   * This method is called when HTTP response is received from server before parsing.
   * It won't be called if response content is cached.
   * @param httpResponse HTTP response instance
   * @param context parser context
   */
  protected void onHttpResponse(final HttpResponse httpResponse, final ParserContext context) throws RequestMethodException {
    final int code = httpResponse.getStatusLine().getStatusCode();
    final int codesRange = 100;
    final int ok = 2, clientError = 4, serverError = 5;
    final int checkValue = code / codesRange;
    if (checkValue != ok && checkValue != clientError && checkValue != serverError) {
      throw new RequestMethodException(code);
    }
  }

  /**
   * @param systemContext system context
   * @param description request description
   * @param context parser context
   * @throws RequestMethodException if ever
   */
  public void start(final Context systemContext, final RequestDescription description, final ParserContext context) throws RequestMethodException {
    LOCK.lock();
    ++requestId;
    LOCK.unlock();

    final long startTime = System.currentTimeMillis();

    InputStream cachedStream = null;
    HttpResponse response = null;
    String url = null;

    final HttpUriRequest request = description.buildRequest(requestId);
    if (request instanceof HttpGet) {
      url = request.getURI().toString();
      final File cFile = APICacheDAO.getCachedFile(systemContext, cacheAuthority, url);
      if (cFile != null) {
        try {
          cachedStream = new FileInputStream(cFile);
          if (DEBUG) { Log.i(TAG, "Cache is used"); }
        } catch (final IOException e) {
          Log.e(TAG, "Cannot read cached stream", e);
        }
      }
    }

    if (cachedStream == null) {
      try {
        response = httpClient.execute(request);
      } catch (final IOException e) {
        throw new RequestMethodException(e);
      } catch (final RuntimeException e) {
        throw new RequestMethodException(e, null);
      }
      onHttpResponse(response, context);
    }

    ResponseHanlder handler = null;
    InputStream inputStream = null;
    long cacheId = -1;
    boolean success = false;
    try {

      inputStream = cachedStream == null ? response.getEntity().getContent() : cachedStream;
      final int bsize = 8192;
      inputStream = new PoolableBufferedInputStream(inputStream, bsize, buffersPool);
      final Charset charset = Charset.forName(RequestDescription.CHARSET);

      if (RequestDescription.DEBUG) {
        final String responseString = streamToString(inputStream, charset);
        Log.d(RequestDescription.TAG, responseString);
        inputStream = new ByteArrayInputStream(responseString.getBytes(RequestDescription.CHARSET));
      }

      if (APICacheDAO.isCacheEnabled() && cachedStream == null && url != null) {
        final CachedStreamInfo cacheInfo = APICacheDAO.insert(systemContext, cacheAuthority, url, inputStream, buffersPool, bsize);
        inputStream = cacheInfo.getStream();
        cacheId = cacheInfo.getId();
      }

      handler = createResponseHandler(context, inputStream);
      handler.handleResponse();

      final Response r = context.getResponse();
      success = r != null && r.isSuccessful();

    } catch (final IOException e) {
      throw new RequestMethodException(e);
    } catch (final RuntimeException e) {
      throw new RequestMethodException(e, handler);
    } finally {
      if (inputStream != null) {
        try {
          inputStream.close();
        } catch (final IOException e) {
          Log.e(TAG, "Cannot close input stream", e);
        }
      }
      if (!success) { clearCache(systemContext, cacheId); }
      if (DEBUG) {
        Log.d(TAG, "Request time: " + (System.currentTimeMillis() - startTime) + " ms");
      }
    }

  }

  private void clearCache(final Context systemContext, final long cacheId) {
    if (DEBUG) { Log.d(TAG, "Response will not be cached"); }
    if (cacheId != -1) { APICacheDAO.delete(systemContext, cacheAuthority, cacheId); }
  }

  protected abstract ResponseHanlder createResponseHandler(final ParserContext context, final InputStream inputStream);

  /**
   * Request method exception.
   * @author Roman Mazur - Stanfy (http://www.stanfy.com)
   */
  public static class RequestMethodException extends Exception {

    /** serialVersionUID. */
    private static final long serialVersionUID = 3234117139338549788L;

    /** Connection error flag. */
    private boolean connectionError;

    public RequestMethodException(final IOException e) {
      super("Connection error", e);
      connectionError = true;
    }

    public RequestMethodException(final String message) {
      super(message);
      connectionError = false;
    }

    public RequestMethodException(final int responseCode) {
      super("Bad server answer, code=" + responseCode);
      connectionError = false;
    }

    public RequestMethodException(final XmlPullParserException e, final ResponseHanlder handler) {
      super("Response parsing exception, handler state: " + (handler != null ? handler.dumpState() : "<none>"), e);
      connectionError = false;
    }

    public RequestMethodException(final RuntimeException e, final ResponseHanlder handler) {
      super("Response parsing exception" + (handler != null ? handler.dumpState() : "<none>"), e);
      connectionError = false;
    }

    /** @return the connectionError */
    public boolean isConnectionError() { return connectionError; }

  }

}
