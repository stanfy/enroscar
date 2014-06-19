package com.stanfy.enroscar.async.internal;

import android.content.Context;

import com.stanfy.enroscar.async.Async;
import com.stanfy.enroscar.async.AsyncObserver;
import com.stanfy.enroscar.async.Releaser;

import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class AsyncContextTest {

  @Test
  public void releaseShouldDelegateReleaser() {
    class TestAsync implements Async<String>, Releaser<String> {

      @Override
      public void subscribe(AsyncObserver<String> observer) { }

      @Override
      public void cancel() { }

      @Override
      public Async<String> replicate() {
        return this;
      }

      @Override
      public void release(String data) { }

    }

    TestAsync mock = mock(TestAsync.class);
    AsyncContext<String> context = new AsyncContext<>(mock(Context.class), mock);
    context.releaseData("test");
    verify(mock).release("test");
  }

}
