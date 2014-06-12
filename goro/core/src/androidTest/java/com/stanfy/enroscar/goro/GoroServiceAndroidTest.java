package com.stanfy.enroscar.goro;

import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.Looper;
import android.test.ServiceTestCase;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Tests for {@link Goro}.
 */
public class GoroServiceAndroidTest extends ServiceTestCase<GoroService> {

  /** Instance under the test. */
  private Goro goro;

  /** Thread pointers. */
  private Thread currentThread, startCallThread, finishCallThread, errorCallThread,
      cancelCallThread, scheduleCallThread;

  /** Invocation flag. */
  private boolean mockTaskCalled;

  /** Exception throw. */
  private Exception testException;

  /** Mock task */
  private final Callable<?> mockTask = new Callable<Object>() {
    @Override
    public Object call() throws Exception {
      mockTaskCalled = true;
      if (testException != null) {
        throw testException;
      }
      return null;
    }
  };


  /** Goro listener. */
  private GoroListener listener = new GoroListener() {
    @Override
    public void onTaskSchedule(Callable<?> task, String queue) {
      assertThat(task).isNotNull();
      scheduleCallThread = Thread.currentThread();
    }

    @Override
    public void onTaskStart(Callable<?> task) {
      assertThat(task).isNotNull();
      startCallThread = Thread.currentThread();
    }

    @Override
    public void onTaskFinish(Callable<?> task, Object result) {
      assertThat(task).isNotNull();
      finishCallThread = Thread.currentThread();
      sync.countDown();
    }

    @Override
    public void onTaskError(Callable<?> task, Throwable error) {
      assertThat(task).isNotNull();
      errorCallThread = Thread.currentThread();
      sync.countDown();
    }

    public void onTaskCancel(Callable<?> task) {
      assertThat(task).isNotNull();
      cancelCallThread = Thread.currentThread();
      sync.countDown();
    }

  };

  /** A latch. */
  private CountDownLatch sync;

  public GoroServiceAndroidTest() {
    super(GoroService.class);
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    startCallThread = finishCallThread = errorCallThread = null;
    currentThread = Thread.currentThread();

    mockTaskCalled = false;

    sync = new CountDownLatch(1);

    final CountDownLatch bindSyc = new CountDownLatch(1);
    onMainThread(new Runnable() {
      @Override
      public void run() {
        goro = Goro.from(bindService(new Intent()));
        assertThat(goro).isNotNull();
        bindSyc.countDown();
      }
    });
    bindSyc.await();
  }

  private void waitForListener() {
    try {
      assertThat(sync.await(10, TimeUnit.SECONDS)).describedAs("Listener not called").isTrue();
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

  private void onMainThread(final Runnable action) {
    new Handler(Looper.getMainLooper()).post(action);
  }

  private void addListener() {
    onMainThread(new Runnable() {
      @Override
      public void run() {
        goro.addTaskListener(listener);
      }
    });
  }

  public void testExecutionStartFinish() throws Exception {
    addListener();
    goro.schedule(mockTask);
    waitForListener();
    assertThat(startCallThread).isNotNull().isNotEqualTo(currentThread)
        .isSameAs(finishCallThread).isSameAs(scheduleCallThread);
    assertThat(errorCallThread).isNull();
    assertThat(mockTaskCalled).isTrue();
  }

  public void testExecutionStartError() throws Exception {
    addListener();
    testException = new Exception("test");

    goro.schedule(mockTask);
    waitForListener();
    assertThat(startCallThread).isNotNull().isNotEqualTo(currentThread)
        .isSameAs(errorCallThread).isSameAs(scheduleCallThread);
    assertThat(finishCallThread).isNull();
    assertThat(mockTaskCalled).isTrue();
  }

  public void testExecutionCancel() throws Exception {
    addListener();

    Future<?> future = goro.schedule(mockTask);
    assertThat(future.cancel(true)).isTrue();
    waitForListener();

    assertThat(scheduleCallThread).isNotNull();
    assertThat(cancelCallThread).isNotNull();
    assertThat(errorCallThread).isNull();
    assertThat(finishCallThread).isNull();
  }

}
