package com.stanfy.enroscar.goro;

import android.content.Intent;
import android.os.Binder;
import android.test.ServiceTestCase;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link com.stanfy.enroscar.goro.Goro}.
 */
public class GoroServiceTest extends ServiceTestCase<GoroService> {

  /** Instance under the test. */
  private Goro goro;

  /** Thread pointers. */
  private Thread currentThread, startCallThread, finishCallThread, errorCallThread;

  /** Goro listener. */
  private GoroListener listener = new GoroListener() {
    @Override
    public void onTaskStart(Runnable task) {
      startCallThread = Thread.currentThread();
    }

    @Override
    public void onTaskFinish(Runnable task) {
      finishCallThread = Thread.currentThread();
      sync.countDown();
    }

    @Override
    public void onTaskError(Runnable task, Throwable error) {
      errorCallThread = Thread.currentThread();
      sync.countDown();
    }
  };

  /** A latch. */
  private CountDownLatch sync;

  public GoroServiceTest() {
    super(GoroService.class);
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    startCallThread = finishCallThread = errorCallThread = null;
    currentThread = Thread.currentThread();

    sync = new CountDownLatch(1);

    goro = Goro.from(bindService(new Intent()));
    assertThat(goro).isNotNull();
  }

  private void waitForListener() {
    try {
      assertThat(sync.await(2, TimeUnit.SECONDS)).describedAs("Listener not called").isTrue();
    } catch (InterruptedException e) {
      fail("Wait for listener interrupted");
    }
  }

  public void testGoroFromBadBinderShouldThrow() {
    try {
      Goro.from(new Binder());
      fail("Missing exception");
    } catch (IllegalArgumentException e) {
      assertThat(e).hasMessageContaining("Cannot get Goro");
    }
  }

  public void testExecutionStartFinish() {
    goro.addListener(listener);
    Runnable task = mock(Runnable.class);

    goro.schedule(task);
    waitForListener();
    assertThat(startCallThread).isNotNull().isNotEqualTo(currentThread).isSameAs(finishCallThread);
    assertThat(errorCallThread).isNull();
    verify(task).run();
  }

  public void testExecutionStartError() {
    listener = spy(listener);
    goro.addListener(listener);
    Runnable task = mock(Runnable.class);
    Exception testException = new Exception("test");
    doThrow(testException).when(task).run();

    goro.schedule(task);
    waitForListener();
    assertThat(startCallThread).isNotNull().isNotEqualTo(currentThread).isSameAs(errorCallThread);
    assertThat(finishCallThread).isNull();
    verify(task).run();
    verify(listener).onTaskError(task, testException);
  }

}
