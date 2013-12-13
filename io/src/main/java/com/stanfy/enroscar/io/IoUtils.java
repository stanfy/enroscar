package com.stanfy.enroscar.io;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

/**
 * Internal I/O utilities.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
public final class IoUtils {

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

  /** 8K. */
  public static final int BUFFER_SIZE_8K = 8192;
  /** 16K. */
  public static final int BUFFER_SIZE_16K = 16 * 1024;

  /** End of file code. */
  private static final int EOF = -1;


  private IoUtils() { /* hidden */ }


  /**
   * Closes `closeable` ignoring any non-runtime exceptions.
   * @param closeable whatever that can be closed, may be null
   */
  public static void closeQuietly(final Closeable closeable) {
    if (closeable != null) {
      try {
        closeable.close();
      } catch (final RuntimeException rethrown) {
        throw rethrown;
      } catch (final Exception ignored) {
        // ignore
      }
    }
  }

  /**
   * Read all the bytes from input stream and convert them to a string.
   * Input stream is closed after this method invocation.
   * @param stream input stream
   * @param charset input characters set name
   * @param buffersPool buffers pool instance, may be null
   * @return string
   * @throws IOException if an error happens
   */
  public static String streamToString(final InputStream stream, final String charset, final BuffersPool buffersPool) throws IOException {
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    transfer(stream, output, buffersPool);
    return new String(output.toByteArray(), charset);
  }

  /**
   * Read all the bytes from input stream and convert them to a string using UTF-8 charset.
   * Input stream is closed after this method invocation.
   * @param stream input stream
   * @param buffersPool buffers pool instance, may be null
   * @return string
   * @throws IOException if an error happens
   */
  public static String streamToString(final InputStream stream, final BuffersPool buffersPool) throws IOException {
    return streamToString(stream, UTF_8_NAME, buffersPool);
  }

  /**
   * Transfers all the bytes from input to output stream.
   * If read/write operations are successful, output stream will be flushed.
   * Input stream is always closed after this method invocation.
   *
   * @param input input stream
   * @param output output stream
   * @param buffersPool buffers pool used to obtain a temporal bytes buffer, may be nil
   * @throws IOException if an error happens
   */
  public static void transfer(final InputStream input, final OutputStream output, final BuffersPool buffersPool) throws IOException {
    final InputStream in = buffersPool == null
        ? new BufferedInputStream(input, BUFFER_SIZE_8K)
        : new PoolableBufferedInputStream(input, BUFFER_SIZE_8K, buffersPool);

    final byte[] buffer = buffersPool == null
        ? new byte[BUFFER_SIZE_8K]
        : buffersPool.get(BUFFER_SIZE_8K);

    try {
      int cnt;
      while ((cnt = in.read(buffer)) != EOF) {
        output.write(buffer, 0, cnt);
      }
      output.flush();
    } finally {
      closeQuietly(in);

      if (buffersPool != null) {
        buffersPool.release(buffer);
      }
    }
  }

  /**
   * Consume the stream and close it.
   * This implementation calls {@link InputStream#read(byte[])} method and ignores any read bytes.
   *
   * @param input input stream
   * @throws IOException if I/O error happens
   */
  public static void consumeStream(final InputStream input, final BuffersPool buffersPool) throws IOException {
    // do not use skip, just use a buffer and read it all
    final byte[] buffer = buffersPool == null
        ? new byte[BUFFER_SIZE_8K]
        : buffersPool.get(BUFFER_SIZE_8K);

    try {
      //noinspection StatementWithEmptyBody
      while (input.read(buffer) != EOF);
    } finally {
      closeQuietly(input);

      if (buffersPool != null) {
        buffersPool.release(buffer);
      }
    }

  }

  /**
   * Gets stream of uncompressed bytes for the {@link URLConnection} wrapping its input stream
   * according to what is defined in its content encoding.
   * Supported encodings: {@link #ENCODING_GZIP}, {@link #ENCODING_DEFLATE}.
   *
   * @param connection given URL connection
   * @return an uncompressed {@link InputStream} for the given {@link URLConnection}.
   * @see #getUncompressedInputStream(String, java.io.InputStream)
   */
  public static InputStream getUncompressedInputStream(final URLConnection connection) throws IOException {
    final InputStream source = connection.getInputStream();
    final String encoding = connection.getContentEncoding();
    return getUncompressedInputStream(encoding, source);
  }

  /**
   * Wraps the supplied stream into either {@link GZIPInputStream} or {@link InflaterInputStream}
   * depending on {@code encoding} parameter value.
   *
   * @param encoding content encoding, supported values: {@link #ENCODING_GZIP}, {@link #ENCODING_DEFLATE}
   * @param source source stream
   * @return the uncompressed {@link InputStream}
   * @see #getUncompressedInputStream(java.net.URLConnection)
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
