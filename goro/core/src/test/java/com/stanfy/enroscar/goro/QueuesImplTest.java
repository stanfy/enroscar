package com.stanfy.enroscar.goro;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

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
    queuesImpl.setDelegateExecutor(Executors.newCachedThreadPool());
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldNotAllowNullThreadPool() {
    queuesImpl.setDelegateExecutor(null);
  }

  @Test
  public void shouldReturnExecutorsThatDelegatesToTheDefinedExecutor() {
    Executor mainExecutor = new Executor() {
      @Override
      public void execute(final Runnable command) {
        command.run();
      }
    };
    mainExecutor = spy(mainExecutor);
    Runnable task = mock(Runnable.class);

    queuesImpl.setDelegateExecutor(mainExecutor);
    Executor queueExecutor = queuesImpl.getExecutor("1");
    assertThat(queueExecutor).isNotEqualTo(mainExecutor);
    queueExecutor.execute(task);

    verify(mainExecutor).execute(any(Runnable.class));
    verify(task).run();
  }

  @Test
  public void shouldReturnMainExecutorForNullQueueName() {
    Executor mainExecutor = mock(Executor.class);
    queuesImpl.setDelegateExecutor(mainExecutor);
    assertThat(queuesImpl.getExecutor(null)).isSameAs(mainExecutor);
  }

  @Test
  public void clearShouldRemoveTasks() {
    final ArrayList<Runnable> tasks = new ArrayList<>();
    //noinspection NullableProblems
    queuesImpl.setDelegateExecutor(new Executor() {
      @Override
      public void execute(final Runnable command) {
        tasks.add(command);
      }
    });

    // normal scenario
    queuesImpl.getExecutor("q").execute(mock(Runnable.class));
    queuesImpl.getExecutor("q").execute(mock(Runnable.class));
    assertThat(tasks).hasSize(1);
    tasks.remove(0).run();
    assertThat(tasks).hasSize(1);
    tasks.remove(0).run();
    assertThat(tasks).isEmpty();

    // clear scenario
    queuesImpl.getExecutor("q").execute(mock(Runnable.class));
    queuesImpl.getExecutor("q").execute(mock(Runnable.class));
    queuesImpl.getExecutor("q").execute(mock(Runnable.class));
    queuesImpl.clear("q");
    assertThat(tasks).hasSize(1);
    tasks.remove(0).run();
    assertThat(tasks).isEmpty();
  }

}
