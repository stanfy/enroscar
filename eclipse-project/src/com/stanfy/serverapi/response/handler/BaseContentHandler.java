package com.stanfy.serverapi.response.handler;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.ContentHandler;
import java.net.URLConnection;
import java.nio.charset.Charset;

import android.util.Log;

import com.stanfy.DebugFlags;
import com.stanfy.app.beans.BeansManager;
import com.stanfy.app.beans.InitializingBean;
import com.stanfy.io.BuffersPool;
import com.stanfy.io.IoUtils;
import com.stanfy.io.PoolableBufferedInputStream;
import com.stanfy.net.ContentControlUrlConnection;
import com.stanfy.net.UrlConnectionWrapper;
import com.stanfy.serverapi.request.RequestDescription;
import com.stanfy.serverapi.response.Model;
import com.stanfy.serverapi.response.ModelTypeToken;

/**
 * Base content handler. Takes care about buffers and gzip.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
public abstract class BaseContentHandler extends ContentHandler implements InitializingBean {

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

    InputStream source = IoUtils.getUncompressedInputStream(
        connection.getContentEncoding(),
        new PoolableBufferedInputStream(connection.getInputStream(), buffersPool)
    );

    if (RequestDescription.DEBUG && DebugFlags.DEBUG_API_RESPONSE) {
      final String responseString = IoUtils.streamToString(source);
      Log.d(RequestDescription.TAG, responseString);
      source = new ByteArrayInputStream(responseString.getBytes(IoUtils.UTF_8_NAME));
    }

    try {
      return getContent(connection, source, connection.getModelType());
    } finally {
      IoUtils.closeQuietly(source);
    }
  }

  protected abstract Object getContent(final URLConnection connection, final InputStream source, final ModelTypeToken modelType) throws IOException;

  protected Type getModelType(final ModelTypeToken modelType) {
    final Class<?> modelClass = modelType.getRawClass();
    final Model modelAnnotation = modelClass.getAnnotation(Model.class);
    if (modelAnnotation == null) { return modelType.getType(); }
    final Class<?> wrapper = modelAnnotation.wrapper();
    return wrapper != null ? wrapper : modelType.getType();
  }

  public void setCharset(final Charset charset) { this.charset = charset; }
  public Charset getCharset() { return charset; }

  @Override
  public void onInititializationFinished() {
    this.buffersPool = BeansManager.get(null).getMainBuffersPool();
  }

}
