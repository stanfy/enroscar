package com.stanfy.io;

import static com.stanfy.io.ProgressListenerInputStreamTest.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;

import com.stanfy.io.ProgressListenerOutputStream.ProgressListener;

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
        assertThat(percent - lastProgress, greaterThan(THROTTLE));
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
    assertThat(closeCalled, is(true));
    assertThat(progressCallCounter, greaterThanOrEqualTo(LENGTH_FACTOR - 1));
    assertThat(1 - lastProgress, lessThan(THROTTLE));
  }

}
