package com.stanfy.enroscar.goro;

import java.util.ArrayList;
import java.util.concurrent.Executor;

/**
 * Executor for tests.
 * @author Roman Mazur - Stanfy (http://stanfy.com)
 */
final class ControlledExecutor implements Executor {

  ArrayList<Runnable> commands = new ArrayList<>();

  @Override
  public void execute(Runnable command) {
    commands.add(command);
  }

  void runAllAndClean() {
    for (Runnable r : commands) {
      r.run();
    }
    commands.clear();
  }

}
