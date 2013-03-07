package com.stanfy.enroscar.io;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Output stream that has a write progress listener.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
public class ProgressListenerOutputStream extends FilterOutputStream {

  /** Default throttle. */
  static final float THROTTLE_DEFAULT = .05f;

  /** Total length. */
  private final long totalLength;

  /** Write counter. */
  private long counter = 0;

  /** Throttle. */
  private final float throttle;

  /** Last progress. */
  private float lastProgress = 0;

  /** Listener instance. */
  private final ProgressListener listener;

  public ProgressListenerOutputStream(final OutputStream out, final ProgressListener listener, final long totalLength) {
    this(out, listener, totalLength, THROTTLE_DEFAULT);
  }
  public ProgressListenerOutputStream(final OutputStream out, final ProgressListener listener, final long totalLength, final float throttle) {
    super(out);
    if (listener == null) {
      throw new IllegalArgumentException("Listener must be not null");
    }
    if (totalLength < 0) {
      throw new IllegalArgumentException("Total length must be positive");
    }
    if (throttle <= 0 || throttle >= 1) {
      throw new IllegalArgumentException("Throttle must be in range (0, 1).");
    }
    this.listener = listener;
    this.totalLength = totalLength;
    this.throttle = throttle;
  }

  @Override
  public void write(final byte[] buffer) throws IOException {
    out.write(buffer);
    trackProgress(buffer.length);
  }

  @Override
  public void write(final byte[] buffer, final int offset, final int length) throws IOException {
    out.write(buffer, offset, length);
    trackProgress(length);
  }

  @Override
  public void write(final int oneByte) throws IOException {
    out.write(oneByte);
    trackProgress(1);
  }

  @Override
  public void close() throws IOException {
    super.close();
    listener.onOutputClosed();
  }

  private void trackProgress(final int increment) {
    counter += increment;
    final float p = (float)counter / totalLength;
    if (p - lastProgress >= throttle) {
      lastProgress = p;
      listener.onOutputProgress(counter, totalLength, p);
    }
  }

  /**
   * Progress listener for output stream.
   */
  public interface ProgressListener {
    void onOutputProgress(final long bytesWritten, final long totalCount, final float percent);
    void onOutputClosed();
  }

}
