package com.stanfy.enroscar.goro;

import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.Looper;
import android.test.ServiceTestCase;
import android.util.Log;

import com.google.android.apps.common.testing.ui.espresso.base.MainThread;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Tests for {@link Goro}.
 */
public class GoroServiceAndroidTest extends ServiceTestCase<GoroService> {

  /** Executor for tests. */
  private final ControlledExecutor executor = new ControlledExecutor();

  /** Instance under the test. */
  private Goro goro;

  /** Exception throw. */
  private Exception testException;

  /** Mock task */
  private MockTask mockTask;

  /** Goro listener. */
  private GoroListener listener = new GoroListener() {
    @Override
    public void onTaskSchedule(Callable<?> task, String queue) {
      assertThat(task).isNotNull();
      ((MockTask) task).scheduled = true;
    }

    @Override
    public void onTaskStart(Callable<?> task) {
      assertThat(task).isNotNull();
      ((MockTask) task).started = true;
    }

    @Override
    public void onTaskFinish(Callable<?> task, Object result) {
      assertThat(task).isNotNull();
      ((MockTask) task).finished = true;
    }

    @Override
    public void onTaskError(Callable<?> task, Throwable error) {
      assertThat(task).isNotNull();
      ((MockTask) task).errored = true;
    }

    public void onTaskCancel(Callable<?> task) {
      assertThat(task).isNotNull();
      ((MockTask) task).canceled = true;
    }

  };

  public GoroServiceAndroidTest() {
    super(GoroService.class);
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    Log.i("GoroTest", "SET UP");
    mockTask = new MockTask();
    executor.commands.clear();
    GoroService.setDelegateExecutor(executor);

    final CountDownLatch bindSyc = new CountDownLatch(1);
    onMainThread(new Runnable() {
      @Override
      public void run() {
        Log.i("GoroTest", "BINDING...");
        goro = Goro.from(bindService(new Intent()));
        assertThat(goro).isNotNull();
        bindSyc.countDown();
      }
    });
    bindSyc.await();
    Log.i("GoroTest", "Got Goro");
    assertThat(goro).isNotNull();
    addListener();
  }

  public void testGoroFromBadBinderShouldThrow() {
    try {
      Goro.from(new Binder());
      fail("Missing exception");
    } catch (IllegalArgumentException e) {
      assertThat(e).hasMessageContaining("Cannot get Goro");
    }
  }

  static void onMainThread(final Runnable action) {
    new Handler(Looper.getMainLooper()).post(action);
  }

  private void addListener() throws Exception {
    final CountDownLatch listenerSync = new CountDownLatch(1);
    onMainThread(new Runnable() {
      @Override
      public void run() {
        Log.i("GoroTest", "Adding a listener to " + goro);
        goro.addTaskListener(listener);
        listenerSync.countDown();
      }
    });
    listenerSync.await();
    Log.i("GoroTest", "Listener ready");
  }

  public void testExecutionStartFinish() throws Exception {
    Log.i("GoroTest", "testExecutionStartFinish");
    goro.schedule(mockTask);
    executor.runAllAndClean();
    final CountDownLatch sync = new CountDownLatch(1);
    onMainThread(new Runnable() {
      @Override
      public void run() {
        assertThat(mockTask.scheduled).isTrue();
        assertThat(mockTask.started).isTrue();
        assertThat(mockTask.called).isTrue();
        assertThat(mockTask.finished).isTrue();
        assertThat(mockTask.canceled).isFalse();
        assertThat(mockTask.errored).isFalse();
        sync.countDown();
      }
    });
    sync.await();
  }

  @MainThread
  public void testExecutionStartError() throws Exception {
    Log.i("GoroTest", "testExecutionStartError");
    testException = new Exception("test");
    goro.schedule(mockTask);
    executor.runAllAndClean();
    final CountDownLatch sync = new CountDownLatch(1);
    onMainThread(new Runnable() {
      @Override
      public void run() {
        assertThat(mockTask.scheduled).isTrue();
        assertThat(mockTask.started).isTrue();
        assertThat(mockTask.called).isTrue();
        assertThat(mockTask.finished).isFalse();
        assertThat(mockTask.canceled).isFalse();
        assertThat(mockTask.errored).isTrue();
        sync.countDown();
      }
    });
    sync.await();
  }

  @MainThread
  public void testExecutionCancel() throws Exception {
    Log.i("GoroTest", "testExecutionCancel");
    Future<?> future = goro.schedule(mockTask);
    assertThat(future.cancel(true)).isTrue();
    executor.runAllAndClean();
    final CountDownLatch sync = new CountDownLatch(1);
    onMainThread(new Runnable() {
      @Override
      public void run() {
        assertThat(mockTask.scheduled).isTrue();
        assertThat(mockTask.started).isFalse();
        assertThat(mockTask.called).isFalse();
        assertThat(mockTask.finished).isFalse();
        assertThat(mockTask.canceled).isTrue();
        assertThat(mockTask.errored).isFalse();
        sync.countDown();
      }
    });
    sync.await();
  }

  private class MockTask implements Callable<String> {

    boolean called, scheduled, started, finished, canceled, errored;

    @Override
    public String call() throws Exception {
      called = true;
      if (testException != null) {
        throw testException;
      }
      return null;
    }
  }

}
