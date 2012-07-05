package com.stanfy.net.cache;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.CacheRequest;
import java.net.CacheResponse;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import android.annotation.TargetApi;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.text.TextUtils;
import android.util.Log;

import com.stanfy.io.DiskLruCache.Editor;
import com.stanfy.io.IoUtils;
import com.stanfy.net.UrlConnectionWrapper;
import com.stanfy.utils.AppUtils;

/** Cache entry. */
public class CacheEntry {

  /** Logging tag. */
  private static final String TAG = "CacheEntry";

  /** Request URI. */
  private String uri;
  /** Request method. */
  private String requestMethod;

  /** Time stamp. */
  private long timestamp;

  /** Content encoding. */
  private String encoding;

  /** Listener. */
  private CacheEntryListener listener;

  /** Time rules. */
  private CacheTimeRule[] timeRules;

  public void setTimeRules(final CacheTimeRule[] timeRules) {
    this.timeRules = timeRules;
  }

  void renewTimestamp() {
    this.timestamp = System.currentTimeMillis();
  }
  public long getTimestamp() { return timestamp; }

  public void setListener(final CacheEntryListener listener) {
    this.listener = listener;
  }

  public void set(final URI uri, final String requestMethod, final Map<String, List<String>> headers) {
    this.uri = uri.toString();
    this.requestMethod = requestMethod;
    renewTimestamp();
  }

  public void setFrom(final URLConnection conn) {
    final URLConnection connection = UrlConnectionWrapper.unwrap(conn);
    try {
      this.uri = connection.getURL().toURI().toString();
      if (connection instanceof HttpURLConnection) {
        this.requestMethod = ((HttpURLConnection) connection).getRequestMethod();
      }
      renewTimestamp();
    } catch (final URISyntaxException e) {
      Log.e(TAG, "Cannot convert URL to URI", e);
    }
  }

  public String getUri() { return uri; }
  public String getRequestMethod() { return requestMethod; }
  public String getEncoding() { return encoding; }

  protected void setUri(final String uri) { this.uri = uri; }
  protected void setRequestMethod(final String requestMethod) { this.requestMethod = requestMethod; }
  public void setEncoding(final String encoding) { this.encoding = encoding; }

  public String getCacheKey() { return AppUtils.getMd5(uri); }

  public boolean canBeCached() {
    return !TextUtils.isEmpty(uri) && !TextUtils.isEmpty(requestMethod) && uri.startsWith("http");
  }

  public final void readFrom(final InputStream in) throws IOException {
    try {
      uri = readString(in);
      requestMethod = readString(in);
      timestamp = readLong(in);
      encoding = readString(in);
      if (encoding.length() == 0) {
        this.encoding = null;
      }
      readMetaData(in);
    } finally {
      in.close();
    }
  }

  protected void readMetaData(final InputStream in) throws IOException {
    // nothing
  }

  public final void writeTo(final OutputStream out) throws IOException {
    final Writer writer = new OutputStreamWriter(out, IoUtils.UTF_8);
    writeString(writer, uri);
    writeString(writer, requestMethod);
    writeLong(writer, timestamp);
    writeString(writer, encoding);
    writeMetaData(writer);
    writer.close();
  }

  protected void writeMetaData(final Writer writer) throws IOException {
    // nothing
  }

  protected final int readInt(final InputStream in) throws IOException {
    final String intString = IoUtils.readAsciiLine(in);
    try {
      return Integer.parseInt(intString);
    } catch (final NumberFormatException e) {
      throw new IOException("expected an int but was \"" + intString + "\"");
    }
  }
  protected final long readLong(final InputStream in) throws IOException {
    final String longString = IoUtils.readAsciiLine(in);
    try {
      return Long.parseLong(longString);
    } catch (final NumberFormatException e) {
      throw new IOException("expected a long but was \"" + longString + "\"");
    }
  }

  protected final void writeInt(final Writer writer, final int value) throws IOException {
    writer.write(new StringBuilder().append(value).append('\n').toString());
  }
  protected final void writeLong(final Writer writer, final long value) throws IOException {
    writer.write(new StringBuilder().append(value).append('\n').toString());
  }

  protected final String readString(final InputStream in) throws IOException {
    return IoUtils.readAsciiLine(in);
  }

  protected final void writeString(final Writer writer, final String line) throws IOException {
    if (line != null) {
      writer.write(line + '\n');
    } else {
      writer.write('\n');
    }
  }

  public boolean matches(final CacheEntry requestInfo) {
    return this.uri.equals(requestInfo.uri) && this.requestMethod.equals(requestInfo.requestMethod);
  }

  public boolean canBeUsed() {
    if (timeRules == null) { return true; }
    for (final CacheTimeRule rule : timeRules) {
      if (rule.matches(this)) { return rule.isActual(timestamp); }
    }
    return true;
  }

  public CacheResponse newCacheResponse(final InputStream in) { return new CacheEntryResponse(in); }

  public CacheRequest newCacheRequest(final OutputStream output, final Editor editor) {
    return VERSION.SDK_INT >= VERSION_CODES.GINGERBREAD
        ? new CacheEntryRequest(output, editor, listener)
        : new OldApiCacheEntryRequest(output, editor, listener);
  }

  /**
   * Cache response.
   */
  protected class CacheEntryResponse extends CacheResponse {

    /** Input stream. */
    private final InputStream in;

    public CacheEntryResponse(final InputStream in) {
      this.in = in;
    }

    @Override
    public InputStream getBody() throws IOException { return in; }
    @Override
    public Map<String, List<String>> getHeaders() throws IOException {
      return encoding == null
          ? Collections.<String, List<String>>emptyMap()
          : Collections.singletonMap("Content-Encoding", Collections.singletonList(encoding));
    }

  }

  /**
   * Cache request.
   */
  @TargetApi(VERSION_CODES.GINGERBREAD)
  protected class CacheEntryRequest extends CacheRequest {

    /** Events listener. */
    private final CacheEntryListener listener;

    /** Cache output. */
    private final OutputStream cacheOut;
    /** Output stream. */
    final OutputStream body;
    /** Cache editor. */
    private final Editor cacheEditor;

    /** Close flag. */
    private boolean done;

    public CacheEntryRequest(final OutputStream cacheOut, final Editor cacheEditor, final CacheEntryListener listener) {
      this.cacheEditor = cacheEditor;
      this.cacheOut = cacheOut;
      this.listener = listener;
      this.body = new FilterOutputStream(cacheOut) {
        @Override
        public void write(final byte[] buffer, final int offset, final int length) throws IOException {
          out.write(buffer, offset, length);
        }
        @Override
        public void close() throws IOException {
          synchronized (CacheEntryRequest.this) {
            if (done) { return; }
            done = true;
          }
          super.close();
          cacheEditor.commit();
          if (listener != null) { listener.onCacheEntryWriteSuccess(CacheEntryRequest.this); }
        }
      };
    }

    @Override
    public OutputStream getBody() throws IOException { return body; }

    @Override
    public void abort() {
      synchronized (CacheEntryRequest.this) {
        if (done) { return; }
        done = true;
      }
      IoUtils.closeQuietly(this.cacheOut);
      try {
        cacheEditor.abort();
      } catch (final IOException ignored) {
        Log.w(TAG, "Cannot abort editor", ignored);
      }
      if (listener != null) { listener.onCacheEntryWriteAbort(this); }
    }

  }

  /**
   * Cache request implementation for Eclair and Froyo.
   * @author Roman Mazur (Stanfy - http://stanfy.com)
   */
  @TargetApi(VERSION_CODES.FROYO)
  protected class OldApiCacheEntryRequest extends CacheEntryRequest {

    public OldApiCacheEntryRequest(final OutputStream cacheOut, final Editor cacheEditor, final CacheEntryListener listener) {
      super(cacheOut, cacheEditor, listener);
    }

    @Override
    public void abort() {
      // old implementations call abort when body stream should have been close
      IoUtils.closeQuietly(body);
    }

  }

  /** Cache entry listener. */
  public interface CacheEntryListener {
    void onCacheEntryWriteSuccess(CacheEntryRequest request);
    void onCacheEntryWriteAbort(CacheEntryRequest request);
  }

}
