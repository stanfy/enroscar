package com.stanfy.enroscar.goro.support;

import android.os.Build;

import com.stanfy.enroscar.async.Async;
import com.stanfy.enroscar.async.AsyncObserver;
import com.stanfy.enroscar.goro.Goro;
import com.stanfy.enroscar.goro.ObservableFuture;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.concurrent.Callable;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests for AsyncGoro.
 */
@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class AsyncGoroTest {

  /** Mock Goro instance. */
  private Goro goro;

  /** Instance under the test. */
  private AsyncGoro asyncGoro;

  @Before
  public void init() {
    goro = mock(Goro.class);
    doReturn(mock(ObservableFuture.class)).when(goro).schedule(anyString(), any(Callable.class));
    asyncGoro = new AsyncGoro(goro);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void firstSubscribeShouldCallSchedule() {
    Callable<?> task = mock(Callable.class);
    Async<?> async = asyncGoro.schedule("1", task);
    verify(goro, never()).schedule(anyString(), any(Callable.class));

    async.subscribe(mock(AsyncObserver.class));
    verify(goro).schedule("1", task);

    async.subscribe(mock(AsyncObserver.class));
    verify(goro, times(1 /* not 2 */)).schedule(anyString(), any(Callable.class));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void cancelShouldMakeSubscribeNoOp() {
    Async<?> async = asyncGoro.schedule("1", mock(Callable.class));
    async.cancel();
    async.subscribe(mock(AsyncObserver.class));
    verify(goro, never()).schedule(anyString(), any(Callable.class));
  }

}
