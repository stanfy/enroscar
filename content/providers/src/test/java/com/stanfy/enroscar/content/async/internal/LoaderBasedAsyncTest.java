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
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.robolectric.Robolectric.application;

/**
 * Tests for LoaderBasedAsync.
 */
@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = android.os.Build.VERSION_CODES.JELLY_BEAN_MR2)
public class LoaderBasedAsyncTest {

  /** Instance under the test. */
  private LoaderBasedAsync<Thing> async;

  @SuppressWarnings("unchecked")
  @Before
  public void init() {
    LoaderManager lm = mock(LoaderManager.class);
    AsyncContext<Thing> context = new DirectContext<Thing>(mock(Async.class), application);
    async = new LoaderBasedAsync<Thing>(lm, context, 1) { };
  }

  @Test
  public void loaderCallbacksOnCreate() {
    Loader<Result<Thing>> loader = async.onCreateLoader(1, null);
    assertThat(loader).isNotNull();
  }

  @Test(expected = IllegalStateException.class)
  public void loaderCallbacksOnCreateBadLoaderId() {
    async.onCreateLoader(2, null);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void loaderCallbacksOnLoadFinishedSuccess() {
    AsyncObserver<Thing> observer = mock(AsyncObserver.class);
    async.subscribe(observer);
    Thing thing1 = new Thing();
    async.onLoadFinished(null, new Result<>(thing1, null));
    verify(observer).onResult(thing1);
    verify(observer, never()).onError(any(Throwable.class));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void loaderCallbacksOnLoadFinishedError() {
    AsyncObserver<Thing> observer = mock(AsyncObserver.class);
    async.subscribe(observer);
    Throwable error = new Throwable();
    async.onLoadFinished(null, new Result<Thing>(null, error));
    verify(observer).onError(error);
    verify(observer, never()).onResult(any(Thing.class));
  }

}
