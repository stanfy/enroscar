package com.stanfy.enroscar.goro;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

/**
 * Tests for {@link com.stanfy.enroscar.goro.Queues.Impl}.
 */
public class QueuesImplTest {

  /** Instance under tests. */
  private Queues.Impl queuesImpl;

  @Before
  public void createQueuesImpl() {
    queuesImpl = new Queues.Impl();
  }

  @Test
  public void shouldReturnSameExecutorForSameQueue() {
    Executor executor = queuesImpl.getExecutor("1");
    assertThat(executor).isNotNull().isSameAs(queuesImpl.getExecutor("1"));
  }

  @Test(expected = IllegalStateException.class)
  public void shouldNotAllowThreadsPoolChangeAfterQueueCreation() {
    queuesImpl.getExecutor("1");
    queuesImpl.setThreadPool(Executors.newCachedThreadPool());
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldNotAllowNullThreadPool() {
    queuesImpl.setThreadPool(null);
  }

  @Test
  public void shouldReturnExecutorsThatDelegateToThreadPool() {
    Executor threadPool = new Executor() {
      @Override
      public void execute(final Runnable command) {
        command.run();
      }
    };
    threadPool = spy(threadPool);
    Runnable task = mock(Runnable.class);

    queuesImpl.setThreadPool(threadPool);
    Executor queueExecutor = queuesImpl.getExecutor("1");
    assertThat(queueExecutor).isNotEqualTo(threadPool);
    queueExecutor.execute(task);

    verify(threadPool).execute(any(Runnable.class));
    verify(task).run();
  }

}
