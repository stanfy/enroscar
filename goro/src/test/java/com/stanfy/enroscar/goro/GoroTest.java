package com.stanfy.enroscar.goro;

import org.junit.Before;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link com.stanfy.enroscar.goro.Goro}.
 */
public class GoroTest {

  /** Goro instance. */
  private Goro goro;

  @Before
  public void createGoro() {
    // this is not public API, yet the simplest way to get an instance
    // and it suits our test needs
    goro = new Goro(new TestingQueues());
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
  public void scheduleShouldReturnExecutionId() {
    Runnable task = mock(Runnable.class);
    int id1 = goro.schedule(task, "1");
    assertThat(id1).isGreaterThan(0);
    int id2 = goro.schedule(task, "1");
    assertThat(id2).isGreaterThan(0).isNotEqualTo(id1);
  }

  @Test
  public void shouldScheduleOnDefaultQueue() {
    goro = spy(goro);
    goro.schedule(mock(Runnable.class));
    verify(goro).schedule(any(Runnable.class), eq(Goro.DEFAULT_QUEUE));
  }

  @Test(expected = IllegalArgumentException.class)
  public void scheduleShouldThrowWhenTaskIsNull() {
    goro.schedule(null, "1");
  }


  /** Empty listener. */
  private class EmptyListener implements GoroListener {

    @Override
    public void onTaskStart(final Runnable task) {

    }

    @Override
    public void onTaskFinish(final Runnable task) {

    }

    @Override
    public void onTaskError(final Runnable task, final Throwable error) {

    }
  }

}
