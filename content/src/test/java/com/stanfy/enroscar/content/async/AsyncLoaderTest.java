package com.stanfy.enroscar.content.async;

import android.content.Context;

import com.stanfy.enroscar.content.async.AsyncLoader.Result;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static android.support.v4.content.Loader.OnLoadCompleteListener;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link com.stanfy.enroscar.content.async.AsyncLoader}
 */
@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = android.os.Build.VERSION_CODES.JELLY_BEAN_MR2)
public class AsyncLoaderTest {

  /** Loader instance. */
  private AsyncLoader<String> loader;

  /** Executor. */
  private AsyncExecutor<String> executor;

  /** Mock result. */
  private Async<String> mockResult;

  /** Registered observer. */
  private AsyncObserver<String> registeredObserver;

  /** Invocation flag. */
  private boolean cancelInvoked;

  @Before
  public void init() {
    executor = new AsyncExecutor<String>() {
      @Override
      public Async<String> startExecution() {
        return mockResult;
      }

      @Override
      public Context provideContext() {
        return Robolectric.application;
      }
    };
    executor = spy(executor);
    mockResult = new Async<String>() {
      @Override
      public void subscribe(final AsyncObserver<String> observer) {
        registeredObserver = observer;
      }

      @Override
      public void cancel() {
        cancelInvoked = true;
      }
    };
    registeredObserver = null;
    cancelInvoked = false;

    loader = spy(new AsyncLoader<>(executor));
  }

  @Test
  public void forceLoadShouldTriggerExecutor() {
    loader.forceLoad();
    verify(executor).startExecution();
    assertThat(registeredObserver).isNotNull();
  }

  @Test
  public void forceLoadShouldCancelPrevious() {
    loader.forceLoad();
    assertThat(cancelInvoked).isFalse();
    loader.forceLoad();
    assertThat(cancelInvoked).isTrue();
  }

  @Test
  public void startLoadingShouldForceLoading() {
    loader.startLoading();
    verify(executor).startExecution();
  }

  @Test
  public void shouldDeliverResult() {
    //noinspection unchecked
    OnLoadCompleteListener<Result<String>> listener = mock(OnLoadCompleteListener.class);
    loader.registerListener(1, listener);
    loader.forceLoad();
    registeredObserver.onResult("ok");
    verify(listener).onLoadComplete(loader, new Result<>("ok", null));
  }

  @Test
  public void startLoadingShouldDeliverResult() {
    loader.forceLoad();
    registeredObserver.onResult("ok");
    //noinspection unchecked
    OnLoadCompleteListener<Result<String>> listener = mock(OnLoadCompleteListener.class);
    loader.registerListener(1, listener);
    loader.startLoading();
    verify(listener).onLoadComplete(loader, new Result<>("ok", null));
  }

  @Test
  public void resetShouldReleaseData() {
    loader.forceLoad();
    registeredObserver.onResult("ok");
    loader.reset();
    verify(loader).onReleaseData(new Result<>("ok", null));
  }

  @Test
  public void oldDataShouldBeReleased() {
    loader.forceLoad();
    registeredObserver.onResult("ok");
    registeredObserver.onResult("ok2");
    verify(loader).onReleaseData(new Result<>("ok", null));
  }

}
