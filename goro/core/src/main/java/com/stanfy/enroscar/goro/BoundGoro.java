package com.stanfy.enroscar.goro;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;

/**
 * Implementation that binds to GoroService.
 */
class BoundGoro extends Goro implements ServiceConnection {

  /** Instance of the context used to bind to GoroService. */
  private final Context context;

  /** Temporal array of listeners that must be added after getting service connection. */
  private BaseListenersHandler scheduledListeners = new BaseListenersHandler(2);

  /** Scheduled tasks. */
  private final ArrayList<TaskInfo> scheduledTasks = new ArrayList<>();

  /** Protects service instance. */
  private final Object lock = new Object();

  /** Instance from service. */
  private Goro service;

  BoundGoro(final Context context) {
    this.context = context;
  }

  public void bind() {
    GoroService.bind(context, this);
  }

  @Override
  public void onServiceConnected(final ComponentName name, final IBinder binder) {
    synchronized (lock) {
      if (service != null) {
        throw new GoroException("Already bound and got onServiceConnected from " + name);
      }
      service = Goro.from(binder);

      // delegate listeners
      if (!scheduledListeners.taskListeners.isEmpty()) {
        for (GoroListener listener : scheduledListeners.taskListeners) {
          service.addTaskListener(listener);
        }
        scheduledListeners.taskListeners.clear();
      }

      // delegate tasks
      if (!scheduledTasks.isEmpty()) {
        for (TaskInfo info : scheduledTasks) {
          service.schedule(info.queue, info.task);
        }
        scheduledTasks.clear();
      }
    }
  }

  @Override
  public void onServiceDisconnected(final ComponentName name) {
    synchronized (lock) {
      if (service != null) {
        throw new GoroException("GoroService disconnected while we are using it");
      }
    }
  }

  @Override
  public void addTaskListener(final GoroListener listener) {
    // main thread => no sync
    Goro service = this.service;
    if (service != null) {
      service.addTaskListener(listener);
    } else {
      scheduledListeners.addTaskListener(listener);
    }
  }

  @Override
  public void removeTaskListener(final GoroListener listener) {
    // main thread => no sync
    Goro service = this.service;
    if (service != null) {
      service.addTaskListener(listener);
    } else {
      scheduledListeners.removeTaskListener(listener);
    }
  }

  @Override
  public <T> Future<T> schedule(final Callable<T> task) {
    synchronized (lock) {
      if (service != null) {
        return service.schedule(task);
      } else {
        scheduledTasks.add(new TaskInfo(DEFAULT_QUEUE, task));
        // TODO: return good future
        return null;
      }
    }
  }

  @Override
  public <T> Future<T> schedule(String queueName, Callable<T> task) {
    return null;
  }

  @Override
  public Executor getExecutor(String queueName) {
    return null;
  }

  /** Recorded task info. */
  private static class TaskInfo {
    /** Queue name. */
    final String queue;
    /** Task instance. */
    final Callable<?> task;

    TaskInfo(final String queue, final Callable<?> task) {
      this.queue = queue;
      this.task = task;
    }
  }

}
