package com.stanfy.io;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.lang.reflect.Array;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import android.util.Log;

import com.stanfy.DebugFlags;

/**
 * Internal I/O utilities.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
public final class IoUtils {

  /** Logging tag. */
  public static final String TAG = "IO";
  /** Debug flag. */
  private static final boolean DEBUG = DebugFlags.DEBUG_IO;

  private IoUtils() { /* hidden */ }

  /** UTF-8 character set. */
  public static final Charset UTF_8 = Charset.forName("UTF-8");
  /** ASCII character set. */
  public static final Charset US_ASCII = Charset.forName("US-ASCII");

  /** UTF-8 name. */
  public static final String UTF_8_NAME = UTF_8.name();

  /** 'gzip' string. */
  public static final String ENCODING_GZIP = "gzip";
  /** 'deflate' string. */
  public static final String ENCODING_DEFLATE = "deflate";

  /** Default buffer size. */
  static final int BUF_SIZE = 8192;

  /* From java.util.Arrays */
  public static <T> T[] copyOfRange(final T[] original, final int start, final int end) {
    final int originalLength = original.length; // For exception priority compatibility.
    if (start > end) {
      throw new IllegalArgumentException();
    }
    if (start < 0 || start > originalLength) {
      throw new ArrayIndexOutOfBoundsException();
    }
    final int resultLength = end - start;
    final int copyLength = Math.min(resultLength, originalLength - start);
    @SuppressWarnings("unchecked")
    final T[] result = (T[]) Array.newInstance(original.getClass().getComponentType(), resultLength);
    System.arraycopy(original, start, result, 0, copyLength);
    return result;
  }

  /* From libcore.io.IoUtils */
  public static void deleteContents(final File dir) throws IOException {
    final File[] files = dir.listFiles();
    if (files == null) {
      throw new IllegalArgumentException("not a directory: " + dir);
    }
    for (final File file : files) {
      if (file.isDirectory()) {
        deleteContents(file);
      }
      if (!file.delete()) {
        throw new IOException("failed to delete file: " + file);
      }
    }
  }

  /* From libcore.io.IoUtils */
  public static void closeQuietly(final Closeable closeable) {
    if (closeable != null) {
      try {
        closeable.close();
      } catch (final RuntimeException rethrown) {
        throw rethrown;
      } catch (final Exception ignored) {
        if (DEBUG) { Log.d(TAG, "Ignore exception on close", ignored); }
      }
    }
  }

  /* From libcore.io.Streams */
  public static String readFully(final Reader reader) throws IOException {
    try {
      final StringWriter writer = new StringWriter();
      final int blen = 1024;
      final char[] buffer = new char[blen];
      int count;
      while ((count = reader.read(buffer)) != -1) {
        writer.write(buffer, 0, count);
      }
      return writer.toString();
    } finally {
      reader.close();
    }
  }

  /* From libcore.io.Streams */
  public static String readAsciiLine(final InputStream in) throws IOException {
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
   * Input stream is closed after this method invocation.
   * @param stream input stream
   * @param charset input characters set
   * @return string
   * @throws IOException if an error happens
   */
  public static String streamToString(final InputStream stream, final Charset charset) throws IOException {
    return readFully(new InputStreamReader(stream, charset));
  }
  /**
   * Input stream is closed after this method invocation.
   * @param stream input stream
   * @return string
   * @throws IOException if an error happens
   */
  public static String streamToString(final InputStream stream) throws IOException {
    return streamToString(stream, UTF_8);
  }

  /**
   * Input stream is closed after this method invocation.
   * @param input input stream
   * @param output output stream
   * @throws IOException if an error happens
   */
  public static void transfer(final InputStream input, final OutputStream output, final BuffersPool buffersPool) throws IOException {
    final InputStream in = buffersPool == null
        ? new BufferedInputStream(input, BUF_SIZE) : new PoolableBufferedInputStream(input, BUF_SIZE, buffersPool);
    final OutputStream out = output;
    final byte[] buffer = buffersPool == null ? new byte[BUF_SIZE] : buffersPool.get(BUF_SIZE);
    int cnt;
    try {
      do {
        cnt = in.read(buffer);
        if (cnt > 0) {
          out.write(buffer, 0, cnt);
          out.flush();
        }
      } while (cnt >= 0);
    } finally {
      in.close();
      if (buffersPool != null) {
        buffersPool.release(buffer);
      }
    }
  }

  /**
   * Consume the stream and close it.
   * @param input input stream
   * @throws IOException if I/O error happens
   */
  public static void consumeStream(final InputStream input, final BuffersPool buffersPool) throws IOException {
    // do not use skip, just use a buffer and read it all
    final byte[] buffer = buffersPool != null ? buffersPool.get(BUF_SIZE) : new byte[BUF_SIZE];
    int count;
    try {
      do {
        count = input.read(buffer);
      } while (count != -1);
    } finally {
      // recycle
      if (buffersPool != null) {
        buffersPool.release(buffer);
      }
      closeQuietly(input);
    }

  }

  /**
   * @param connection given URL connection
   * @return the uncompressed {@link InputStream} for the given {@link URLConnection}.
   */
  public static InputStream getUncompressedInputStream(final URLConnection connection) throws IOException {
    final InputStream source = connection.getInputStream();
    final String encoding = connection.getContentEncoding();
    return getUncompressedInputStream(encoding, source);
  }
  /**
   * @param encoding content encoding
   * @param source source stream
   * @return the uncompressed {@link InputStream}
   */
  public static InputStream getUncompressedInputStream(final String encoding, final InputStream source) throws IOException {
    if (ENCODING_GZIP.equalsIgnoreCase(encoding)) {
      return new GZIPInputStream(source);
    }
    if (ENCODING_DEFLATE.equalsIgnoreCase(encoding)) {
      final Inflater inflater = new Inflater(/*no header*/ true);
      return new InflaterInputStream(source, inflater);
    }
    return source;
  }

}
