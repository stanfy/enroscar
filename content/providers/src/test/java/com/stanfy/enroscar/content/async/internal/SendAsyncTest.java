package com.stanfy.enroscar.content.async.internal;

import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

import com.stanfy.enroscar.content.async.Async;
import com.stanfy.enroscar.content.async.AsyncObserver;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static com.stanfy.enroscar.content.async.internal.AsyncContext.DirectContext;
import static com.stanfy.enroscar.content.async.internal.WrapAsyncLoader.Result;
import static org.mockito.Mockito.*;
import static org.robolectric.Robolectric.application;

/**
 * Tests for SendAsync.
 */
@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = android.os.Build.VERSION_CODES.JELLY_BEAN_MR2)
public class SendAsyncTest {

  /** Loader ID. */
  private static final int LOADER_ID = 3;

  /** Loader manager. */
  private LoaderManager loaderManager;
  /** Context. */
  private AsyncContext<Thing> context;

  @SuppressWarnings("unchecked")
  @Before
  public void init() {
    loaderManager = mock(LoaderManager.class);
    context = new DirectContext<Thing>(mock(Async.class), application);
  }

  @Test
  public void shouldBindCallbacksIfLoaderCreated() {
    doReturn(mock(Loader.class)).when(loaderManager).getLoader(LOADER_ID);
    SendAsync<Thing> async = new SendAsync<>(loaderManager, context, LOADER_ID);
    verify(loaderManager).initLoader(LOADER_ID, null, async);
  }

  @Test
  public void shouldDestroyLoaderWhenLoadingIsFinished() {
    SendAsync<Thing> async = new SendAsync<>(loaderManager, context, LOADER_ID);
    async.onLoadFinished(null, new Result<>(new Thing(), null));
    verify(loaderManager).destroyLoader(LOADER_ID);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void shouldRestartLoaderWhenTriggered() {
    SendAsync<Thing> async = new SendAsync<>(loaderManager, context, LOADER_ID);
    AsyncObserver<Thing> observer = mock(AsyncObserver.class);
    async.subscribe(observer);
    verify(loaderManager).restartLoader(LOADER_ID, null, async);
  }

}
