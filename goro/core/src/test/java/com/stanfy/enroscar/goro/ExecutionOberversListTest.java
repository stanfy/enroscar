package com.stanfy.enroscar.goro;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.Executor;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Tests for ExecutionObserversList.
 */
public class ExecutionOberversListTest {

  /** Instance under the tests. */
  private ExecutionObserversList list;

  @Before
  public void create() {
    list = new ExecutionObserversList();
  }

  @Test
  public void shouldExecuteAddedObservers() {
    Runnable r1 = mock(Runnable.class);
    Runnable r2 = mock(Runnable.class);
    Executor e1 = mock(Executor.class);
    Executor e2 = mock(Executor.class);

    list.add(r1, e1);
    list.add(r2, e2);
    verify(e1, never()).execute(any(Runnable.class));
    verify(e2, never()).execute(any(Runnable.class));

    list.execute();
    verify(e1).execute(r1);
    verify(e2).execute(r2);
  }

  @Test
  public void shouldExecuteObserverAfterMainExecution() {
    Runnable r1 = mock(Runnable.class);
    Executor e1 = mock(Executor.class);

    list.execute();
    list.add(r1, e1);

    verify(e1).execute(r1);
  }

}
