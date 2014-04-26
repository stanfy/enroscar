package com.stanfy.enroscar.goro;

import android.test.AndroidTestCase;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Test oneshot Goro usage.
 */
public class BindOneshotTest extends AndroidTestCase {

  private BoundGoro goro;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    goro = Goro.bindWith(getContext());
  }

  public void testScheduleBindGet() {
    Future<?> future = goro.schedule(new Callable<Object>() {
      @Override
      public Object call() throws Exception {
        throw new Exception("aha!");
      }
    });
    goro.bindOneshot();

    try {
      future.get(1, TimeUnit.SECONDS);
      fail("Exception expected");
    } catch (InterruptedException e) {
      throw new AssertionError(e);
    } catch (ExecutionException e) {
      assertEquals("aha!", e.getCause().getMessage());
    } catch (TimeoutException e) {
      fail("Not executed");
    }

    // unbound?
    assertNull(((BoundGoro.BoundGoroImpl) goro).getServiceObject());
  }

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
        assertEquals("ok", value);
        sync.countDown();
      }
      @Override
      public void onError(Throwable error) {
        fail(error.getMessage());
      }
    });

    goro.bindOneshot();

    try {
      assertEquals(true, sync.await(1, TimeUnit.SECONDS));
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    // unbound?
    assertNull(((BoundGoro.BoundGoroImpl) goro).getServiceObject());
  }

}
