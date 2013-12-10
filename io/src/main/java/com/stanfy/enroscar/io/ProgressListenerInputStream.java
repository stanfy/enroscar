package com.stanfy.enroscar.io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;


/**
 * Input stream that can notify its listener about the reading progress.
 * This implementation does not respect marks.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
public class ProgressListenerInputStream extends FilterInputStream {

  /** Listener instance. */
  private final ProgressListener listener;

  /** Total length. */
  private final long totalLength;

  /** Throttle. */
  private final float throttle;

  /** Last progress. */
  private float lastProgress = 0;

  /** Counter. */
  private long counter;

  public ProgressListenerInputStream(final InputStream in, final ProgressListener listener, final long totalLength) {
    this(in, listener, totalLength, ProgressListenerOutputStream.THROTTLE_DEFAULT);
  }
  public ProgressListenerInputStream(final InputStream in, final ProgressListener listener, final long totalLength, final float throttle) {
    super(in);
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
    this.throttle = throttle;
    this.totalLength = totalLength;
  }

  @Override
  public int read() throws IOException {
    trackProgress(1);
    return in.read();
  }

  @Override
  public int read(final byte[] buffer) throws IOException {
    int count = in.read(buffer);
    trackProgress(count);
    return count;
  }

  @Override
  public int read(final byte[] buffer, final int offset, final int count) throws IOException {
    int result = in.read(buffer, offset, count);
    trackProgress(result);
    return result;
  }

  @Override
  public void close() throws IOException {
    super.close();
    listener.onInputClosed();
  }

  private void trackProgress(final int increment) {
    counter += increment;
    final float p = (float)counter / totalLength;
    if (p - lastProgress >= throttle) {
      lastProgress = p;
      listener.onInputProgress(counter, totalLength, p);
    }
  }

  /**
   * Progress listener for input stream.
   */
  public interface ProgressListener {
    void onInputProgress(final long bytesRead, final long totalCount, final float percent);
    void onInputClosed();
  }


}
