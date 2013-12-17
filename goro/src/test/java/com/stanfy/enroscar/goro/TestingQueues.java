package com.stanfy.enroscar.goro;

import java.util.concurrent.Executor;

/**
 * Queues implementation for tests.
 */
public class TestingQueues implements Queues {

  /** Direct executor. */
  private Executor directExecutor = new Executor() {
    @Override
    public void execute(final Runnable command) {
      command.run();
    }
  };

  @Override
  public void setThreadPool(final Executor threadPool) {
    directExecutor = threadPool;
  }

  @Override
  public Executor getExecutor(String queueName) {
    return new TaskQueueExecutor(directExecutor);
  }

}
