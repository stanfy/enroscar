package com.stanfy.enroscar.io.test;

import static org.fest.assertions.api.Assertions.*;

import static com.stanfy.enroscar.io.test.ProgressListenerInputStreamTest.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;

import com.stanfy.enroscar.io.IoUtils;
import com.stanfy.enroscar.io.ProgressListenerOutputStream;
import com.stanfy.enroscar.io.ProgressListenerOutputStream.ProgressListener;

/**
 * Tests for {@link ProgressListenerOutputStream}.
 */
public class ProgressListenerOutputStreamTest {

  /** Stream for testing. */
  private ProgressListenerOutputStream stream;

  /** Source. */
  private InputStream source;

  /** Last saved progress value. */
  private float lastProgress = 0;

  /** Listener. */
  private final ProgressListener listener = new ProgressListener() {

    @Override
    public void onOutputProgress(final long bytesWritten, final long totalCount, final float percent) {
      if (progressCallCounter > 0) {
        assertThat(percent - lastProgress).isGreaterThan(THROTTLE);
      }
      ++progressCallCounter;
      lastProgress = percent;
    }

    @Override
    public void onOutputClosed() {
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
    source = new ByteArrayInputStream(data);
    stream = new ProgressListenerOutputStream(new ByteArrayOutputStream(), listener, LENGTH, THROTTLE);

    progressCallCounter = 0;
    closeCalled = false;
  }

  @Test
  public void listenerShouldBeNotifiedAboutProgress() throws Exception {
    IoUtils.transfer(source, stream, null);
    stream.close();
    assertThat(closeCalled).isTrue();
    assertThat(progressCallCounter).isGreaterThanOrEqualTo(LENGTH_FACTOR - 1);
    assertThat(1 - lastProgress).isLessThan(THROTTLE);
  }

}
