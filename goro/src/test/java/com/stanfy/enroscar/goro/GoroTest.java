package com.stanfy.enroscar.goro;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link com.stanfy.enroscar.goro.Goro}.
 */
public class GoroTest {

  /** Goro instance. */
  private Goro goro;

  /** Listener. */
  private GoroListener listener;

  /** Testing queues. */
  private TestingQueues testingQueues;

  @Before
  public void createGoro() {
    testingQueues = new TestingQueues();
    // this is not public API, yet the simplest way to get an instance
    // and it suits our test needs
    goro = new Goro(testingQueues);
    listener = mock(GoroListener.class);
  }

  @Test(expected = IllegalArgumentException.class)
  public void removeListenerShouldThrowOnUnknownListener() {
    goro.removeListener(mock(GoroListener.class));
  }

  @Test
  public void shouldBeAbleToAddAndRemoveListeners() {
    GoroListener listener = mock(GoroListener.class);
    goro.addListener(listener);
    goro.removeListener(listener);
  }

  @Test
  public void scheduleShouldReturnFuture() {
    Callable<?> task = mock(Callable.class);
    Future future1 = goro.schedule(task, "1");
    assertThat(future1).isNotNull();
    Future future2 = goro.schedule(task, "2");
    assertThat(future2).isNotNull().isNotEqualTo(future1);
  }

  @Test
  public void shouldScheduleOnDefaultQueue() {
    goro = spy(goro);
    goro.schedule(mock(Callable.class));
    verify(goro).schedule(any(Callable.class), eq(Goro.DEFAULT_QUEUE));
  }

  @Test(expected = IllegalArgumentException.class)
  public void scheduleShouldThrowWhenTaskIsNull() {
    goro.schedule(null, "1");
  }


  @Test
  public void shouldInvokeStartOnListeners() {
    goro.addListener(listener);
    Callable<?> task = mock(Callable.class);
    goro.schedule(task);
    testingQueues.executeAll();
    verify(listener).onTaskStart(task);
  }

  @Test
  public void shouldInvokeFinishOnListeners() {
    goro.addListener(listener);
    Callable<?> task = mock(Callable.class);
    goro.schedule(task);
    testingQueues.executeAll();
    InOrder order = inOrder(listener);
    order.verify(listener).onTaskStart(task);
    order.verify(listener).onTaskFinish(task);
  }

  @Test
  public void shouldInvokeCancelOnListeners() {
    goro.addListener(listener);
    Callable<?> task = mock(Callable.class);
    goro.schedule(task).cancel(true);
    testingQueues.executeAll();
    verify(listener, never()).onTaskStart(task);
    verify(listener).onTaskCancel(task);  }

  @Test
  public void shouldInvokeErrorOnListeners() {
    goro.addListener(listener);
    final Exception error = new Exception();
    Callable<?> task = new Callable<Object>() {
      @Override
      public Object call() throws Exception {
        throw error;
      }
    };
    goro.schedule(task);
    testingQueues.executeAll();
    InOrder order = inOrder(listener);
    order.verify(listener).onTaskStart(task);
    order.verify(listener).onTaskError(task, error);
  }


}
