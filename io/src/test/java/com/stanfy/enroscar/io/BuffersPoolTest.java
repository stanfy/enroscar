package com.stanfy.enroscar.io;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Random;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Tests for {@link BuffersPool}. Absolutely useless.
 */
public class BuffersPoolTest {

  /** Buffers pool instance. */
  private BuffersPool buffersPool;
  /** Size of the biggest buffer available in pool after initialization. */
  private int maxAvailableSize;

  /** Random. */
  private final Random r = new Random();

  @Before
  public void createPool() {
    final int maxSize = 1024;
    maxAvailableSize = maxSize;
    buffersPool = new BuffersPool(new int[][] {
        {1, maxSize}, {2, maxSize / 2}
    });

  }

  @Test
  public void shouldReportCorrectStatsAfterCreation() {
    assertThat(buffersPool.getBuffersCount()).isEqualTo(3);
    assertThat(buffersPool.getUsedBuffersCount()).isZero();
  }

  @Test
  public void shouldBeAbleToAllocateNewBuffer() {
    int prevCount = buffersPool.getBuffersCount();
    byte[] buffer = buffersPool.get(maxAvailableSize + 1);
    assertThat(buffer).isNotNull();
    assertThat(buffersPool.getBuffersCount()).isEqualTo(prevCount + 1);
  }

  @Test
  public void shouldUseAvailableBuffers() {
    int expectedBuffersCount = buffersPool.getBuffersCount();

    byte[] buffer1 = buffersPool.get(maxAvailableSize / 4);
    assertThat(buffer1).isNotNull();
    assertThat(buffersPool.getBuffersCount()).isEqualTo(expectedBuffersCount);
    assertThat(buffersPool.getUsedBuffersCount()).isEqualTo(1);

    byte[] buffer2 = buffersPool.get(maxAvailableSize / 2);
    assertThat(buffer2).isNotNull();
    assertThat(buffersPool.getBuffersCount()).isEqualTo(expectedBuffersCount);
    assertThat(buffersPool.getUsedBuffersCount()).isEqualTo(2);

    byte[] buffer3 = buffersPool.get(maxAvailableSize);
    assertThat(buffer3).isNotNull();
    assertThat(buffersPool.getBuffersCount()).isEqualTo(expectedBuffersCount);
    assertThat(buffersPool.getUsedBuffersCount()).isEqualTo(3);

    byte[] buffer4 = buffersPool.get(2);
    expectedBuffersCount++;
    assertThat(buffer4).isNotNull();
    assertThat(buffersPool.getBuffersCount()).isEqualTo(expectedBuffersCount);
    assertThat(buffersPool.getUsedBuffersCount()).isEqualTo(4);

    buffersPool.release(buffer1);
    buffersPool.release(buffer2);
    buffersPool.release(buffer3);
    buffersPool.release(buffer4);
    assertThat(buffersPool.getBuffersCount()).isEqualTo(expectedBuffersCount);
    assertThat(buffersPool.getUsedBuffersCount()).isZero();

    assertThat(buffersPool.get(maxAvailableSize / 3)).isNotNull();
    assertThat(buffersPool.getBuffersCount()).isEqualTo(expectedBuffersCount);
    assertThat(buffersPool.getUsedBuffersCount()).isEqualTo(1);
  }

  @Test
  public void threadsTest() throws Exception {
    final int usersCount = 10;

    final ArrayList<UserThread> threads = new ArrayList<UserThread>(usersCount);
    for (int i = 0; i < usersCount; i++) {
      final UserThread t = new UserThread();
      threads.add(t);
      t.start();
    }

    for (final UserThread t : threads) {
      t.join();
      assertThat(t.error).isNull();
    }

    assertThat(buffersPool.getUsedBuffersCount()).isZero();
  }

  /** User thread. */
  private class UserThread extends Thread {

    /** Caught error. */
    private Throwable error;

    @Override
    public void run() {
      final int count = 100;
      byte[] buffer = null;

      try {

        for (int i = 0; i < count; i++) {
          if ((i & 1) == 0) {
            buffer = buffersPool.get(r.nextInt(maxAvailableSize * 3));
          } else {
            buffersPool.release(buffer);
          }
        }

      } catch (Throwable e) {
        error = e;
      }
    }
  }

}
