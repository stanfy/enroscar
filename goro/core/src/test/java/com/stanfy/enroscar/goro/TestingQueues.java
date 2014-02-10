package com.stanfy.enroscar.goro;

import java.util.ArrayList;
import java.util.concurrent.Executor;

/**
 * Queues implementation for tests.
 */
public class TestingQueues implements Queues {

  /** Scheduled tasks. */
  private final ArrayList<Runnable> tasks = new ArrayList<>();

  /** Delegate executor. */
  private Executor delegateExecutor = new Executor() {
    @Override
    public void execute(@SuppressWarnings("NullableProblems") final Runnable command) {
      tasks.add(command);
    }
  };

  /** Last queue name. */
  private String lastQueueName;

  @Override
  public void setDelegateExecutor(final Executor delegate) {
    delegateExecutor = delegate;
  }

  @Override
  public Executor getExecutor(String queueName) {
    lastQueueName = queueName;
    return new TaskQueueExecutor(delegateExecutor);
  }

  public String getLastQueueName() {
    return lastQueueName;
  }

  public void executeAll() {
    for (Runnable command : tasks) {
      command.run();
    }
    tasks.clear();
  }

}
