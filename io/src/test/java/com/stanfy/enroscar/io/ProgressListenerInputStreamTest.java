package com.stanfy.enroscar.io;

import com.stanfy.enroscar.io.ProgressListenerInputStream.ProgressListener;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.util.Random;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Tests for {@link ProgressListenerInputStream}.
 */
public class ProgressListenerInputStreamTest {

  /** Length factor. */
  static final int LENGTH_FACTOR = 10;

  /** Length. */
  static final int LENGTH = 8192 * LENGTH_FACTOR;

  /** Used throttle. */
  static final float THROTTLE = 0.07f;

  /** Stream for testing. */
  private ProgressListenerInputStream stream;

  /** Last saved progress value. */
  private float lastProgress = 0;

  /** Listener. */
  private final ProgressListener listener = new ProgressListener() {

    @Override
    public void onInputProgress(final long bytesRead, final long totalCount, final float percent) {
      if (progressCallCounter > 0) {
        assertThat(percent - lastProgress).isGreaterThan(THROTTLE);
      }
      ++progressCallCounter;
      lastProgress = percent;
    }

    @Override
    public void onInputClosed() {
      closeCalled = true;
    }

  };

  /** Call indicators. */
  private boolean closeCalled;

  /** Hoe many times throttle was checked. */
  private int progressCallCounter;

  @Before
  public void setup() {
    Random r = new Random();
    byte[] data = new byte[LENGTH];
    r.nextBytes(data);
    ByteArrayInputStream dataIn = new ByteArrayInputStream(data);
    stream = new ProgressListenerInputStream(dataIn, listener, LENGTH, THROTTLE);

    progressCallCounter = 0;
    closeCalled = false;
  }

  @Test
  public void listenerShouldBeNotifiedAboutProgress() throws Exception {
    IoUtils.consumeStream(stream, null);
    assertThat(closeCalled).isTrue();
    assertThat(progressCallCounter).isGreaterThanOrEqualTo(LENGTH_FACTOR - 1);
    assertThat(1 - lastProgress).isLessThan(THROTTLE);
  }

}
