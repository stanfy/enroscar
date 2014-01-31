package com.stanfy.enroscar.goro;

import java.util.ArrayList;
import java.util.concurrent.Executor;

/**
 * Queues implementation for tests.
 */
public class TestingQueues implements Queues {

  /** Scheduled tasks. */
  private final ArrayList<Runnable> tasks = new ArrayList<>();

  /** Direct executor. */
  private Executor directExecutor = new Executor() {
    @Override
    public void execute(@SuppressWarnings("NullableProblems") final Runnable command) {
      tasks.add(command);
    }
  };

  /** Last queue name. */
  private String lastQueueName;

  @Override
  public void setDelegateExecutor(final Executor threadPool) {
    directExecutor = threadPool;
  }

  @Override
  public Executor getExecutor(String queueName) {
    lastQueueName = queueName;
    return new TaskQueueExecutor(directExecutor);
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
