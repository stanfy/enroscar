package com.stanfy.enroscar.net.cache;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.text.TextUtils;
import android.util.Log;

import com.jakewharton.disklrucache.DiskLruCache.Editor;
import com.stanfy.enroscar.io.IoUtils;
import com.stanfy.enroscar.net.UrlConnectionWrapper;
import com.stanfy.enroscar.utils.Time;

import java.io.EOFException;
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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;

/** Cache entry. */
public class CacheEntry {

  /** Logging tag. */
  private static final String TAG = "CacheEntry";

  /** This comparator is required to add null keys to the {@link TreeMap}. */
  private static final Comparator<String> STRINGS_COMPARATOR = new Comparator<String>() {
    @Override
    public int compare(final String lhs, final String rhs) {
      if (lhs == null) { return 0; }
      if (rhs == null) { return 1; }
      return lhs.compareTo(rhs);
    }
  };

  /** We respect our own time rules, so give rather big max age to HTTP engine. Measured in seconds. */
  private static final long MAX_AGE_TIME = 30 * Time.DAYS;

  /** Format for the 'Date header'. */
  private static final ThreadLocal<DateFormat> STANDARD_DATE_FORMAT = new ThreadLocal<DateFormat>() {
    @Override
    protected DateFormat initialValue() {
      final DateFormat rfc1123 = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
      rfc1123.setTimeZone(TimeZone.getTimeZone("UTC"));
      return rfc1123;
    }
  };

  /** Request URI. */
  private String uri;
  /** Request method. */
  private String requestMethod;

  /** Time stamp. */
  private long timestamp;

  /** Content encoding. */
  private String encoding;

  /** Response status line. */
  private String statusLine;

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

  public void setResponseData(final URLConnection conn) {
    try {
      this.statusLine = conn.getHeaderField(null);
    } catch (final Exception e) {
      // :( old Android versions have bad URLConnection implementations
      final URLConnection connection = UrlConnectionWrapper.unwrap(conn);
      if (connection instanceof HttpURLConnection) {
        final HttpURLConnection http = (HttpURLConnection) connection;
        try {
          this.statusLine = "HTTP/1.1 " + http.getResponseCode() + " " + http.getResponseMessage();
        } catch (final IOException ioe) {
          Log.e(TAG, "Cannot set status line", ioe);
        }
      }
    }
    this.encoding = conn.getContentEncoding();
  }

  public String getUri() { return uri; }
  public String getRequestMethod() { return requestMethod; }
  public String getEncoding() { return encoding; }

  /**
   * @param uri URI for this cache entry
   */
  protected void setUri(final String uri) { this.uri = uri; }
  /**
   * @param requestMethod request method for this cache entry
   */
  protected void setRequestMethod(final String requestMethod) { this.requestMethod = requestMethod; }

  public String getCacheKey() { return Md5.getMd5(uri); }

  public boolean canBeCached() {
    return !TextUtils.isEmpty(uri) && !TextUtils.isEmpty(requestMethod) && uri.startsWith("http") && isRequestMethodCacheable();
  }

  /**
   * Override this method if you want other methods than GET to be cached.
   * @return true if method of the request binded to this entry allows to cache response 
   */
  protected boolean isRequestMethodCacheable() {
    return "GET".equalsIgnoreCase(requestMethod);
  }
  
  public final void readFrom(final InputStream in) throws IOException {
    try {
      uri = readString(in);
      requestMethod = readString(in);
      timestamp = readLong(in);
      encoding = readString(in);
      statusLine = readString(in);
      if (encoding.length() == 0) {
        this.encoding = null;
      }
      readMetaData(in);
    } finally {
      in.close();
    }
  }

  /**
   * Read some special metadata about this cache entry.
   * @param in cache entry input stream
   * @throws IOException if error happens
   */
  protected void readMetaData(final InputStream in) throws IOException {
    // nothing
  }

  public final void writeTo(final OutputStream out) throws IOException {
    final Writer writer = new OutputStreamWriter(out, IoUtils.UTF_8);
    writeString(writer, uri);
    writeString(writer, requestMethod);
    writeLong(writer, timestamp);
    writeString(writer, encoding);
    writeString(writer, statusLine);
    writeMetaData(writer);
    writer.close();
  }

  /**
   * Write some special metadata for this entry.
   * @param writer writer for this cache entry
   * @throws IOException if error happens
   */
  protected void writeMetaData(final Writer writer) throws IOException {
    // nothing
  }

  /**
   * @param in input stream
   * @return next line from the stream parsed as an integer
   * @throws IOException if error i/O happens or line has bad format (int cannot be parsed)
   */
  protected static int readInt(final InputStream in) throws IOException {
    final String intString = readString(in);
    try {
      return Integer.parseInt(intString);
    } catch (final NumberFormatException e) {
      throw new IOException("expected an int but was \"" + intString + "\"");
    }
  }
  /**
   * @param in input stream
   * @return next line from the stream parsed as a long integer
   * @throws IOException if error i/O happens or line has bad format (long cannot be parsed)
   */
  protected final long readLong(final InputStream in) throws IOException {
    final String longString = readString(in);
    try {
      return Long.parseLong(longString);
    } catch (final NumberFormatException e) {
      throw new IOException("expected a long but was \"" + longString + "\"");
    }
  }

  /**
   * Write next line as an int.
   * @param writer output writer
   * @param value integer value
   * @throws IOException if error happens
   */
  protected static void writeInt(final Writer writer, final int value) throws IOException {
    writer.write(new StringBuilder().append(value).append('\n').toString());
  }
  /**
   * Write next line as a long.
   * @param writer output writer
   * @param value integer value
   * @throws IOException if error happens
   */
  protected static void writeLong(final Writer writer, final long value) throws IOException {
    writer.write(new StringBuilder().append(value).append('\n').toString());
  }

  /**
   * @param in input stream
   * @return next line
   * @throws IOException if error happens
   */
  protected static String readString(final InputStream in) throws IOException {
    // TODO support UTF-8 here instead

    final StringBuilder result = new StringBuilder(80);
    while (true) {
      final int c = in.read();
      if (c == -1) {
        throw new EOFException();
      } else if (c == '\n') {
        break;
      }

      result.append((char) c);
    }
    final int length = result.length();
    if (length > 0 && result.charAt(length - 1) == '\r') {
      result.setLength(length - 1);
    }
    return result.toString();
  }

  /**
   * Write next line to the output.
   * @param writer output writer
   * @param line line to write (if null just a new empty line is inserted)
   * @throws IOException if error happens
   */
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

    /**
     * @param in cache entry data input stream
     */
    public CacheEntryResponse(final InputStream in) {
      this.in = in;
    }

    @Override
    public InputStream getBody() throws IOException { return in; }
    @Override
    public Map<String, List<String>> getHeaders() throws IOException {
      final TreeMap<String, List<String>> result = new TreeMap<String, List<String>>(STRINGS_COMPARATOR);

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
        result.put(null, Collections.singletonList(statusLine));
      }

      // NB! use lower case for header names: for old Android versions
      if (encoding != null) {
        result.put("content-encoding", Collections.singletonList(encoding));
      }
      // force HTTP engine use cache response
      result.put("date", Collections.singletonList(STANDARD_DATE_FORMAT.get().format(new Date())));
      result.put("cache-control", Collections.singletonList(
          "max-age=" + Time.asSeconds(System.currentTimeMillis() + MAX_AGE_TIME)
      ));

      return result;
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

    /**
     * @param cacheOut cache entry data output stream
     * @param cacheEditor cache entry editor
     * @param listener entry listener
     */
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

    /**
     * @param cacheOut cache entry data output stream
     * @param cacheEditor cache entry editor
     * @param listener entry listener
     */
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
