package com.stanfy.enroscar.async.internal;

import com.stanfy.enroscar.async.Async;
import com.stanfy.enroscar.async.AsyncObserver;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import static org.mockito.Mockito.*;

/**
 * Tests for BaseAsync.
 */
public class BaseAsyncTest {

  /** Instance. */
  private BaseAsync<Integer> async;

  @Before
  public void init() {
    async = spy(new BaseAsync<Integer>() {
      @Override
      public Async<Integer> replicate() {
        return this;
      }
    });
  }

  @SuppressWarnings("unchecked")
  @Test
  public void postResultAfterSubscribe() {
    AsyncObserver<Integer> observer1 = mock(AsyncObserver.class);
    AsyncObserver<Integer> observer2 = mock(AsyncObserver.class);
    async.subscribe(observer1);
    async.subscribe(observer2);

    async.postResult(42);
    InOrder order = inOrder(observer1, observer2);
    order.verify(observer1).onResult(42);
    order.verify(observer2).onResult(42);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void postErrorAfterSubscribe() {
    AsyncObserver<Integer> observer1 = mock(AsyncObserver.class);
    AsyncObserver<Integer> observer2 = mock(AsyncObserver.class);
    async.subscribe(observer1);
    async.subscribe(observer2);

    Throwable t = new Throwable("e");
    async.postError(t);
    InOrder order = inOrder(observer1, observer2);
    order.verify(observer1).onError(t);
    order.verify(observer2).onError(t);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void triggerAfterSubscribe() {
    AsyncObserver<Integer> observer1 = mock(AsyncObserver.class);
    AsyncObserver<Integer> observer2 = mock(AsyncObserver.class);
    async.subscribe(observer1);
    verify(async).onTrigger();
    reset(async);
    async.subscribe(observer2);
    verify(async, never()).onTrigger();
  }

  @SuppressWarnings("unchecked")
  @Test
  public void cancelShouldClearObservers() {
    AsyncObserver<Integer> observer = mock(AsyncObserver.class);
    async.subscribe(observer);
    async.cancel();
    async.postResult(42);
    async.postError(new Throwable());
    verify(observer, never()).onResult(anyInt());
    verify(observer, never()).onError(any(Throwable.class));
  }

}
