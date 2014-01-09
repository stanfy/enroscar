package com.stanfy.enroscar.goro;

import java.util.ArrayList;
import java.util.concurrent.Executor;

/**
 * Queues implementation for tests.
 */
public class TestingQueues implements Queues {

  /** Scheduled tasks. */
  private final ArrayList<Runnable> tasks = new ArrayList<Runnable>();

  /** Direct executor. */
  private Executor directExecutor = new Executor() {
    @Override
    public void execute(@SuppressWarnings("NullableProblems") final Runnable command) {
      tasks.add(command);
    }
  };

  @Override
  public void setDelegateExecutor(final Executor threadPool) {
    directExecutor = threadPool;
  }

  @Override
  public Executor getExecutor(String queueName) {
    return new TaskQueueExecutor(directExecutor);
  }

  public void executeAll() {
    for (Runnable command : tasks) {
      command.run();
    }
    tasks.clear();
  }

  public ArrayList<Runnable> getTasks() {
    return tasks;
  }

}
