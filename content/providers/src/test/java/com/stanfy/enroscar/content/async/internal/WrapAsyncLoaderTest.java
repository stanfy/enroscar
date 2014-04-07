package com.stanfy.enroscar.content.async.internal;

import com.stanfy.enroscar.content.async.Async;
import com.stanfy.enroscar.content.async.AsyncObserver;
import com.stanfy.enroscar.content.async.internal.WrapAsyncLoader.Result;

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
 * Tests for {@link com.stanfy.enroscar.content.async.internal.WrapAsyncLoader}
 */
@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = android.os.Build.VERSION_CODES.JELLY_BEAN_MR2)
public class WrapAsyncLoaderTest {

  /** Loader instance. */
  private WrapAsyncLoader<String> loader;

  /** Mock result. */
  private Async<String> mockAsync;

  /** Registered observer. */
  private AsyncObserver<String> registeredObserver;

  /** Invocation flag. */
  private boolean cancelInvoked;

  /** Releases data. */
  private String releasedData;

  @Before
  public void init() {
    mockAsync = new Async<String>() {
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
    releasedData = null;

    AsyncContext<String> context = new AsyncContext<String>(Robolectric.application, mockAsync) {
      @Override
      public void releaseData(final String data) {
        if (data == null) {
          throw new NullPointerException();
        }
        releasedData = data;
      }
    };
    loader = new WrapAsyncLoader<>(context);
  }

  @Test
  public void forceLoadShouldTriggerExecutor() {
    assertThat(registeredObserver).isNull();
    loader.forceLoad();
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
    assertThat(registeredObserver).isNull();
    loader.startLoading();
    assertThat(registeredObserver).isNotNull();
  }

  @Test
  public void shouldDeliverResult() {
    //noinspection unchecked
    OnLoadCompleteListener<Result<String>> listener = mock(OnLoadCompleteListener.class);
    loader.registerListener(1, listener);
    loader.startLoading();
    registeredObserver.onResult("ok");
    verify(listener).onLoadComplete(loader, new Result<>("ok", null));
  }

  @Test
  public void startLoadingShouldDeliverPreviousResult() {
    loader.startLoading();
    registeredObserver.onResult("ok");
    @SuppressWarnings("unchecked")
    OnLoadCompleteListener<Result<String>> listener = mock(OnLoadCompleteListener.class);
    loader.registerListener(1, listener);
    loader.startLoading();
    verify(listener).onLoadComplete(loader, new Result<>("ok", null));
  }

  @Test
  public void resetShouldReleaseData() {
    loader.startLoading();
    registeredObserver.onResult("ok");
    loader.reset();
    assertThat(releasedData).isEqualTo("ok");
  }

  @Test
  public void oldDataShouldBeReleased() {
    loader.startLoading();
    registeredObserver.onResult("ok");
    registeredObserver.onResult("ok2");
    assertThat(releasedData).isEqualTo("ok");
  }

  @Test
  public void stopLoadingShouldCancelAsyncResult() {
    loader.startLoading();
    loader.stopLoading();
    assertThat(cancelInvoked).isTrue();
  }

}
