package com.stanfy.enroscar.async.internal;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.fail;
import static org.mockito.Mockito.mock;

@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = android.os.Build.VERSION_CODES.JELLY_BEAN_MR2)
public class ObserverCallbacksTest {

  private ObserverCallbacks<Thing> callbacks;

  @Before
  public void init() {
    callbacks = new ObserverCallbacks<>(
        mock(AsyncProvider.class),
        new OperatorBase.OperatorContext<>(),
        mock(LoaderDescription.class),
        1,
        false
    );
  }

  @Test
  public void throwIfNoCallbacks() {
    Exception e = new Exception("test");
    try {
      callbacks.onLoadFinished(null, new WrapAsyncLoader.Result<Thing>(null, e));
      fail("Error not thrown");
    } catch (Exception thrown) {
      assertThat(thrown.getCause()).isSameAs(e);
    }

  }

}