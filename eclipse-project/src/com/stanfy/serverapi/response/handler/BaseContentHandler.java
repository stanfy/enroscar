package com.stanfy.serverapi.response.handler;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.ContentHandler;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.nio.charset.Charset;

import android.util.Log;

import com.stanfy.DebugFlags;
import com.stanfy.enroscar.beans.BeansContainer;
import com.stanfy.enroscar.beans.InitializingBean;
import com.stanfy.enroscar.utils.ModelTypeToken;
import com.stanfy.io.BuffersPool;
import com.stanfy.io.IoUtils;
import com.stanfy.io.PoolableBufferedInputStream;
import com.stanfy.net.ContentControlUrlConnection;
import com.stanfy.net.UrlConnectionWrapper;
import com.stanfy.serverapi.request.RequestDescription;
import com.stanfy.serverapi.response.Model;

/**
 * Base content handler. Takes care about buffers and gzip.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
public abstract class BaseContentHandler extends ContentHandler implements InitializingBean {

  /** Logging tag. */
  private static final String TAG = RequestDescription.TAG;
  /** Debug flag. */
  private static final boolean DEBUG = DebugFlags.DEBUG_API_RESPONSE;

  /** Default date format. */
  public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss Z";

  /** Buffers pool. */
  private BuffersPool buffersPool;

  /** Content characters set. */
  private Charset charset = IoUtils.UTF_8;

  @Override
  public final Object getContent(final URLConnection uConn) throws IOException {
    final ContentControlUrlConnection connection = UrlConnectionWrapper.getWrapper(uConn, ContentControlUrlConnection.class);
    if (connection == null) {
      throw new IllegalArgumentException("Connection is not wrapped with " + ContentControlUrlConnection.class);
    }

    // try to get input stream
    InputStream responseStream = null;
    try {
      responseStream = connection.getInputStream();
    } catch (final IOException responseStreamException) {
      if (DEBUG) { Log.v(TAG, "Cannot get input stream, message: " + responseStreamException.getMessage() + ", try to use error stream"); }

      final URLConnection orig = UrlConnectionWrapper.unwrap(connection);
      if (orig instanceof HttpURLConnection) {
        responseStream = ((HttpURLConnection) orig).getErrorStream();
      }

      // no error stream?
      if (responseStream == null) {
        throw responseStreamException;
      }
    }

    // we have input => wrap it for reading

    InputStream source = IoUtils.getUncompressedInputStream(
        connection.getContentEncoding(),
        new PoolableBufferedInputStream(responseStream, buffersPool)
    );

    if (RequestDescription.DEBUG && DEBUG) {
      final String responseString = IoUtils.streamToString(source); // source is now closed, don't worry
      Log.d(TAG, responseString);
      source = new ByteArrayInputStream(responseString.getBytes(IoUtils.UTF_8_NAME));
    }

    try {
      return getContent(connection, source, connection.getModelType());
    } finally {
      // do not forget to close the source
      IoUtils.closeQuietly(source);
    }
  }

  protected abstract Object getContent(final URLConnection connection, final InputStream source, final ModelTypeToken modelType) throws IOException;

  protected Type getModelType(final ModelTypeToken modelType) {
    final Class<?> modelClass = modelType.getRawClass();

    // check for wrappers
    final Model modelAnnotation = modelClass.getAnnotation(Model.class);
    if (modelAnnotation == null) { return modelType.getType(); }

    final Class<?> wrapper = modelAnnotation.wrapper();
    return wrapper != null && wrapper != Model.class ? wrapper : modelType.getType();
  }

  public void setCharset(final Charset charset) { this.charset = charset; }
  public Charset getCharset() { return charset; }

  @Override
  public void onInitializationFinished(final BeansContainer beansContainer) {
    this.buffersPool = beansContainer.getBean(BuffersPool.class);
  }

}
