package com.stanfy.enroscar.net;

import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import android.content.ContentResolver;
import android.content.res.AssetFileDescriptor;
import android.net.Uri;

/**
 * URL connection that deals with content resolver schemes:
 * {@link ContentResolver#SCHEME_CONTENT}, {@link ContentResolver#SCHEME_FILE},
 * {@link ContentResolver#SCHEME_ANDROID_RESOURCE}.
 */
public class ContentUriConnection extends URLConnection {

  /** Content resolver instance. */
  private final ContentResolver contentResolver;

  /** Android {@link Uri} that is used within calls to {@link ContentResolver}. */
  private final Uri resolverUri;

  /** File descriptor. */
  private AssetFileDescriptor fd;

  /** Content input. */
  private InputStream inputStream;
  /** Content output. */
  private OutputStream outputStream;

  /** Content length value. */
  private int contentLength = -1;

  /** Content type value. */
  private String contentType;
  /** Flag indicating that content type request has already been done. */
  private boolean contentTypeRequested;

  /** Close state flags for input and output streams. */
  boolean inputClosed, outputClosed;

  public ContentUriConnection(final URL url, final ContentResolver contentResolver) {
    super(url);
    this.contentResolver = contentResolver;
    this.resolverUri = Uri.parse(url.toExternalForm());
  }

  @Override
  public void connect() throws IOException {
    if (connected) { return; }

    final boolean doInput = getDoInput(), doOutput = getDoOutput();

    final StringBuilder mode = new StringBuilder(3);
    if (doInput) { mode.append('r'); }
    if (doOutput) { mode.append('w'); }

    fd = contentResolver.openAssetFileDescriptor(resolverUri, mode.toString());

    if (doInput) {
      inputStream = new ContentUriInputStreamWrapper(fd.createInputStream());
    }
    if (doOutput) {
      outputStream = new ContentUriOutputStreamWrapper(fd.createOutputStream());
    }
    connected = true;
  }

  void checkClosedStreams() throws IOException {
    if (!connected) { return; }
    if (fd != null && (inputClosed || inputStream == null) && (outputClosed || outputStream == null)) {
      fd.close();
      fd = null;
    }
  }

  /**
   * Ensure that {@link #connect()} method has been called.
   * @throws IOException if error happens
   */
  protected void ensureConnected() throws IOException {
    if (!connected) { connect(); }
  }

  @Override
  public InputStream getInputStream() throws IOException {
    if (inputClosed) {
      throw new IOException("input is closed");
    }
    ensureConnected();
    return inputStream;
  }

  @Override
  public OutputStream getOutputStream() throws IOException {
    if (outputClosed) {
      throw new IOException("output is closed");
    }
    ensureConnected();
    return outputStream;
  }

  @Override
  public Object getContent() throws IOException {
    ensureConnected();
    return super.getContent();
  }

  @Override
  public String getHeaderField(final String name) {
    if ("content-type".equalsIgnoreCase(name)) {
      return getContentType();
    }
    return super.getHeaderField(name);
  }

  @Override
  public String getContentType() {
    if (!contentTypeRequested) {
      contentTypeRequested = true;
      contentType = contentResolver.getType(resolverUri);
    }
    return contentType;
  }

  @Override
  public int getContentLength() {
    if (!connected || fd == null) { return -1; }
    if (contentLength == -1) {
      final long length = fd.getLength();
      contentLength = length > 0 && length <= Integer.MAX_VALUE ? (int)length : -1;
    }
    return contentLength;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "[" + resolverUri + "]";
  }

  /**
   * Wrapper for input stream.
   */
  private class ContentUriInputStreamWrapper extends FilterInputStream {

    ContentUriInputStreamWrapper(final InputStream in) { super(in); }

    @Override
    public void close() throws IOException {
      super.close();
      inputClosed = true;
      checkClosedStreams();
    }

  }

  /**
   * Wrapper for input stream.
   */
  private class ContentUriOutputStreamWrapper extends FilterOutputStream {

    ContentUriOutputStreamWrapper(final OutputStream out) { super(out); }

    @Override
    public void close() throws IOException {
      super.close();
      outputClosed = true;
      checkClosedStreams();
    }

  }

}
