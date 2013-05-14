package com.stanfy.enroscar.io;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.util.Log;

/**
 * Source is based on BufferedOutputStream.
 * @see BuffersPool
 * @author Roman Mazur - Stanfy (http://www.stanfy.com)
 */
public class PoolableBufferedOutputStream extends FilterOutputStream {

  /** Buffers pool. */
  private final BuffersPool pool;

  /**
   * The buffer containing the bytes to be written to the target stream.
   */
  private byte[] buf;

  /**
   * The total number of bytes inside the byte array {@code buf}.
   */
  private int count;

  /** Call stack. */
  private Throwable stack;

  /**
   * Constructs a new {@code BufferedOutputStream}, providing {@code out} with a buffer
   * of 8192 bytes.
   *
   * @param out the {@code OutputStream} the buffer writes to.
   * @param buffersPool buffers pool instance
   */
  public PoolableBufferedOutputStream(final OutputStream out, final BuffersPool buffersPool) {
    this(out, IoUtils.BUF_SIZE, buffersPool);
  }

  /**
   * Constructs a new {@code BufferedOutputStream}, providing {@code out} with {@code size} bytes
   * of buffer.
   *
   * @param out the {@code OutputStream} the buffer writes to.
   * @param size the size of buffer in bytes.
   * @param buffersPool buffers pool instance
   * @throws IllegalArgumentException if {@code size <= 0}.
   */
  public PoolableBufferedOutputStream(final OutputStream out, final int size, final BuffersPool buffersPool) {
    super(out);
    if (size <= 0) {
      throw new IllegalArgumentException("size <= 0");
    }
    this.pool = buffersPool;
    buf = buffersPool.get(size);
    if (buf == null) {
      buf = new byte[size];
    }
    if (DebugFlags.STRICT_MODE) {
      this.stack = new Throwable();
    }
  }

  /**
   * Flushes this stream to ensure all pending data is written out to the
   * target stream. In addition, the target stream is flushed.
   *
   * @throws IOException
   *             if an error occurs attempting to flush this stream.
   */
  @Override
  public synchronized void flush() throws IOException {
    checkNotClosed();
    flushInternal();
    out.flush();
  }

  private void checkNotClosed() throws IOException {
    if (buf == null) {
      throw new IOException("BufferedOutputStream is closed");
    }
  }

  /**
   * Writes {@code count} bytes from the byte array {@code buffer} starting at
   * {@code offset} to this stream. If there is room in the buffer to hold the
   * bytes, they are copied in. If not, the buffered bytes plus the bytes in
   * {@code buffer} are written to the target stream, the target is flushed,
   * and the buffer is cleared.
   *
   * @param buffer
   *            the buffer to be written.
   * @param offset
   *            the start position in {@code buffer} from where to get bytes.
   * @param length
   *            the number of bytes from {@code buffer} to write to this
   *            stream.
   * @throws IndexOutOfBoundsException
   *             if {@code offset < 0} or {@code length < 0}, or if
   *             {@code offset + length} is greater than the size of
   *             {@code buffer}.
   * @throws IOException
   *             if an error occurs attempting to write to this stream.
   * @throws NullPointerException
   *             if {@code buffer} is {@code null}.
   * @throws ArrayIndexOutOfBoundsException
   *             If offset or count is outside of bounds.
   */
  @Override
  public synchronized void write(final byte[] buffer, final int offset, final int length) throws IOException {
    checkNotClosed();

    if (buffer == null) {
      throw new NullPointerException("buffer == null");
    }

    final byte[] internalBuffer = buf;
    if (length >= internalBuffer.length) {
      flushInternal();
      out.write(buffer, offset, length);
      return;
    }

    if ((offset | length) < 0 || offset > buffer.length || buffer.length - offset < length) {
      throw new ArrayIndexOutOfBoundsException("length=" + buffer.length + "; regionStart=" + offset
          + "; regionLength=" + length);
    }

    // flush the internal buffer first if we have not enough space left
    if (length > (internalBuffer.length - count)) {
      flushInternal();
    }

    System.arraycopy(buffer, offset, internalBuffer, count, length);
    count += length;
  }

  @Override
  public synchronized void close() throws IOException {
    if (buf == null) {
      return;
    }

    try {
      super.close();
    } finally {
      pool.release(buf);
      buf = null;
    }
  }

  /**
   * Writes one byte to this stream. Only the low order byte of the integer
   * {@code oneByte} is written. If there is room in the buffer, the byte is
   * copied into the buffer and the count incremented. Otherwise, the buffer
   * plus {@code oneByte} are written to the target stream, the target is
   * flushed, and the buffer is reset.
   *
   * @param oneByte
   *            the byte to be written.
   * @throws IOException
   *             if an error occurs attempting to write to this stream.
   */
  @Override
  public synchronized void write(final int oneByte) throws IOException {
    checkNotClosed();
    if (count == buf.length) {
      out.write(buf, 0, count);
      count = 0;
    }
    buf[count++] = (byte) oneByte;
  }

  /**
   * Flushes only internal buffer.
   */
  private void flushInternal() throws IOException {
    if (count > 0) {
      out.write(buf, 0, count);
      count = 0;
    }
  }

  @Override
  protected void finalize() throws Throwable {
    super.finalize();
    if (DebugFlags.STRICT_MODE && pool != null && buf != null) {
      Log.e(BuffersPool.BEAN_NAME, "Poolable stream was not closed " + this + " / " + buf.length, stack);
    }
  }

}
