package com.stanfy.enroscar.io;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Tests for IoUtils.
 */
public class IoUtilsTest {

  /** Test input stream. */
  private InputStream testInput;

  /** Buffers pool. */
  private BuffersPool buffersPool;

  @Before
  public void makeTestInput() {
    testInput = new ByteArrayInputStream("test data".getBytes()) {

      private boolean closed;

      @Override
      public void close() throws IOException {
        if (closed) {
          throw new IOException("already closed");
        }
        super.close();
        closed = true;
      }
    };
  }

  @Before
  public void initBuffersPool() {
    buffersPool = spy(new BuffersPool());
  }

  private void assertInputClosed() {
    try {
      testInput.close();
      fail("Not closed");
    } catch (IOException e) {
      assertThat(e).hasMessageContaining("close");
    }
  }

  private void assertBuffersPoolUsed(final int count) {
    verify(buffersPool, times(count)).get(anyInt());
    verify(buffersPool, times(count)).release(any(byte[].class));
  }

  @Test
  public void consumeStreamShouldCloseTheStream() throws Exception {
    IoUtils.consumeStream(testInput, buffersPool);
    assertInputClosed();
  }

  @Test
  public void consumeStreamShouldUserBuffersPool() throws Exception {
    IoUtils.consumeStream(testInput, buffersPool);
    assertBuffersPoolUsed(1);
  }

  @Test
  public void transferShouldCloseInputStream() throws Exception {
    OutputStream output = mock(OutputStream.class);
    IoUtils.transfer(testInput, output, buffersPool);
    assertInputClosed();
  }

  @Test
  public void transferShouldFlushOutputStream() throws Exception {
    OutputStream output = mock(OutputStream.class);
    IoUtils.transfer(testInput, output, buffersPool);
    verify(output, atLeast(1)).write(any(byte[].class), anyInt(), anyInt());
    verify(output).flush();
  }

  @Test
  public void transferShouldUseBuffersPool() throws Exception {
    OutputStream output = mock(OutputStream.class);
    IoUtils.transfer(testInput, output, buffersPool);
    assertBuffersPoolUsed(2);
  }

}
