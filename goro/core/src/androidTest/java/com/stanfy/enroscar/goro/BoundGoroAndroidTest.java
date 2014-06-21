package com.stanfy.enroscar.goro;

import android.test.AndroidTestCase;
import android.test.FlakyTest;

import com.google.android.apps.common.testing.ui.espresso.Espresso;
import com.stanfy.enroscar.async.AsyncObserver;
import com.stanfy.enroscar.goro.support.AsyncGoro;
import com.stanfy.enroscar.goro.support.RxGoro;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import rx.functions.Action0;
import rx.functions.Action1;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Test oneshot Goro usage.
 */
public class BoundGoroAndroidTest extends AndroidTestCase {

  private BoundGoro goro;

  private String res;

  private CountDownLatch scheduleSync;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    GoroService.setDelegateExecutor(null);
    goro = Goro.bindWith(getContext());
    res = "fail";

    scheduleSync = prepareScheduleSync();
  }

  private CountDownLatch prepareScheduleSync() throws InterruptedException {
    final CountDownLatch scheduleSync = new CountDownLatch(1);
    final CountDownLatch listenerSync = new CountDownLatch(1);
    GoroServiceAndroidTest.onMainThread(new Runnable() {
      @Override
      public void run() {
        goro.addTaskListener(new GoroListener() {
          @Override
          public void onTaskSchedule(Callable<?> task, String queue) {
            scheduleSync.countDown();
          }

          @Override
          public void onTaskStart(Callable<?> task) {

          }

          @Override
          public void onTaskFinish(Callable<?> task, Object result) {

          }

          @Override
          public void onTaskCancel(Callable<?> task) {

          }

          @Override
          public void onTaskError(Callable<?> task, Throwable error) {

          }
        });
        listenerSync.countDown();
      }
    });
    listenerSync.await();
    return scheduleSync;
  }

  @FlakyTest
  public void testScheduleBindGet() {
    Future<?> future = goro.schedule(new Callable<Object>() {
      @Override
      public Object call() throws Exception {
        throw new Exception("aha!");
      }
    });
    goro.bindOneshot();
    awaitScheduling();

    try {
      future.get(10, TimeUnit.SECONDS);
      fail("Exception expected");
    } catch (InterruptedException e) {
      throw new AssertionError(e);
    } catch (ExecutionException e) {
      assertEquals("aha!", e.getCause().getMessage());
    } catch (TimeoutException e) {
      fail("Not executed");
    }

    // unbound?
    // FIXME: bindOneShot is still flaky
    // assertNull(((BoundGoro.BoundGoroImpl) goro).getServiceObject());
  }

  private void awaitScheduling() {
    try {
      assertThat(scheduleSync.await(20, TimeUnit.SECONDS)).isTrue();
    } catch (InterruptedException e) {
      throw new AssertionError(e);
    }
  }

  @FlakyTest
  public void testScheduleObserveBind() {
    final CountDownLatch sync = new CountDownLatch(1);
    goro.schedule(new Callable<String>() {
      @Override
      public String call() throws Exception {
        return "ok";
      }
    }).subscribe(new FutureObserver<String>() {
      @Override
      public void onSuccess(String value) {
        res = value;
        sync.countDown();
      }
      @Override
      public void onError(Throwable error) {
        fail(error.getMessage());
      }
    });

    goro.bindOneshot();
    awaitScheduling();

    await(sync);

    // unbound?
    assertNull(((BoundGoro.BoundGoroImpl) goro).getServiceObject());

    assertEquals("ok", res);
  }

  public void testWithAsyncIntegration() {
    final CountDownLatch sync = new CountDownLatch(1);
    new AsyncGoro(goro).schedule(new Callable<String>() {
      @Override
      public String call() throws Exception {
        return "async";
      }
    }).subscribe(new AsyncObserver<String>() {
      @Override
      public void onError(final Throwable e) {
        throw new AssertionError(e);
      }
      @Override
      public void onResult(final String data) {
        res = data;
        sync.countDown();
      }
    });

    goro.bindOneshot();
    awaitScheduling();

    await(sync);
    assertEquals("async", res);
  }

  public void testWithRxIntegration() {
    final CountDownLatch sync = new CountDownLatch(2);
    new RxGoro(goro).schedule(new Callable<String>() {
      @Override
      public String call() throws Exception {
        return "rx";
      }
    }).doOnCompleted(new Action0() {
      @Override
      public void call() {
        sync.countDown();
      }
    }).subscribe(new Action1<String>() {
      @Override
      public void call(String o) {
        res = o;
        sync.countDown();
      }
    });

    goro.bindOneshot();
    awaitScheduling();

    await(sync);

    assertEquals("rx", res);
  }

  private void await(CountDownLatch sync) {
    try {
      assertEquals(true, sync.await(10, TimeUnit.SECONDS));
    } catch (InterruptedException e) {
      throw new AssertionError(e);
    }
  }

}
