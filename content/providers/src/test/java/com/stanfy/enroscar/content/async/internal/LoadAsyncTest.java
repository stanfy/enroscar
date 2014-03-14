package com.stanfy.enroscar.content.async.internal;

import android.support.v4.app.LoaderManager;

import com.stanfy.enroscar.content.async.Async;
import com.stanfy.enroscar.content.async.AsyncObserver;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.robolectric.Robolectric.application;

/**
 * Tests for LoadAsync.
 */
@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = android.os.Build.VERSION_CODES.JELLY_BEAN_MR2)
public class LoadAsyncTest {

  /** Loadee ID. */
  private static final int LOADER_ID = 2;

  /** Instance. */
  private LoadAsync<Thing> async;

  /** Loader manager. */
  private LoaderManager loaderManager;

  @SuppressWarnings("unchecked")
  @Before
  public void init() {
    loaderManager = mock(LoaderManager.class);
    AsyncContext<Thing> context = new AsyncContext<Thing>(mock(Async.class), application);
    async = new LoadAsync<>(loaderManager, context, LOADER_ID);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void shouldInitLoaderWhenTriggered() {
    AsyncObserver<Thing> observer = mock(AsyncObserver.class);
    async.subscribe(observer);
    verify(loaderManager).initLoader(LOADER_ID, null, async);
  }

}
