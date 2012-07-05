package com.stanfy.io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * An InputStream that skips the exact number of bytes provided, unless it reaches EOF.
 * Used as a workaround for http://code.google.com/p/android/issues/detail?id=6066.
 */
public class FlushedInputStream extends FilterInputStream {
  public FlushedInputStream(final InputStream inputStream) {
    super(inputStream);
  }

  @Override
  public long skip(final long n) throws IOException {
    long totalBytesSkipped = 0L;
    final InputStream in = this.in;
    while (totalBytesSkipped < n) {
      long bytesSkipped = in.skip(n - totalBytesSkipped);
      if (bytesSkipped == 0L) {
        final int b = read();
        if (b < 0) {
          break;  // we reached EOF
        } else {
          bytesSkipped = 1; // we read one byte
        }
      }
      totalBytesSkipped += bytesSkipped;
    }
    return totalBytesSkipped;
  }
}
