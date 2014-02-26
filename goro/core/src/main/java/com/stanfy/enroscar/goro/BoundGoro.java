package com.stanfy.enroscar.goro;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Handles tasks in multiple queues using Android service.
 * @see com.stanfy.enroscar.goro.GoroService
 */
public abstract class BoundGoro extends Goro implements ServiceConnection {

  /** Bind to {@link com.stanfy.enroscar.goro.GoroService}. */
  public abstract void bind();

  /** Unbind from {@link com.stanfy.enroscar.goro.GoroService}. */
  public abstract void unbind();


  /** Implementation. */
  static class BoundGoroImpl extends BoundGoro implements ServiceConnection {

    /** Instance of the context used to bind to GoroService. */
    private final Context context;

    /** Temporal array of listeners that must be added after getting service connection. */
    private final BaseListenersHandler scheduledListeners = new BaseListenersHandler(2);

    /** Postponed data. */
    private final ArrayList<Postponed> postponed = new ArrayList<>(7);

    /** Protects service instance. */
    private final Object lock = new Object();

    /** Instance from service. */
    private Goro service;

    BoundGoroImpl(final Context context) {
      this.context = context;
    }

    @Override
    public void bind() {
      GoroService.bind(context, this);
    }

    @Override
    public void unbind() {
      synchronized (lock) {
        service = null;
        GoroService.unbind(context, this);
      }
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
        if (!postponed.isEmpty()) {
          for (Postponed p : postponed) {
            p.act(service);
          }
          postponed.clear();
        }
      }
    }

    boolean cancelPostponed(final Postponed p) {
      synchronized (lock) {
        return postponed.remove(p);
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
        service.removeTaskListener(listener);
      } else {
        scheduledListeners.removeTaskListener(listener);
      }
    }

    @Override
    public <T> Future<T> schedule(final Callable<T> task) {
      return schedule(DEFAULT_QUEUE, task);
    }

    @Override
    public <T> Future<T> schedule(String queueName, Callable<T> task) {
      synchronized (lock) {
        if (service != null) {
          return service.schedule(queueName, task);
        } else {
          BoundFuture<T> future = new BoundFuture<>(queueName, task);
          postponed.add(future);
          return future;
        }
      }
    }

    @Override
    public Executor getExecutor(final String queueName) {
      synchronized (lock) {
        if (service != null) {
          return service.getExecutor(queueName);
        }
        return new PostponeExecutor(queueName);
      }
    }

    /** Some postponed action. */
    private interface Postponed {
      void act(Goro goro);
    }

    /** Recorded task info. */
    private static final class RunnableData implements Postponed {
      /** Queue name. */
      final String queue;
      /** Runnable action. */
      final Runnable command;

      RunnableData(final String queue, final Runnable command) {
        this.queue = queue;
        this.command = command;
      }

      @Override
      public void act(final Goro goro) {
        goro.getExecutor(queue).execute(command);
      }
    }

    /** Executor implementation. */
    private final class PostponeExecutor implements Executor {

      /** Queue name. */
      private final String queueName;

      private PostponeExecutor(final String queueName) {
        this.queueName = queueName;
      }

      @Override
      public void execute(@SuppressWarnings("NullableProblems") final Runnable command) {
        synchronized (lock) {
          if (service != null) {
            service.getExecutor(queueName).execute(command);
          } else {
            postponed.add(new RunnableData(queueName, command));
          }
        }
      }
    }

    /** Postponed scheduled future. */
    private final class BoundFuture<T> implements Future<T>, Postponed {

      /** Queue name. */
      final String queue;
      /** Task instance. */
      final Callable<T> task;

      /** Attached Goro future. */
      private Future<T> goroFuture;

      /** Cancel flag. */
      private boolean canceled;

      private BoundFuture(final String queue, final Callable<T> task) {
        this.queue = queue;
        this.task = task;
      }

      @Override
      public synchronized void act(final Goro goro) {
        goroFuture = goro.schedule(queue, task);
      }

      @Override
      public synchronized boolean cancel(boolean mayInterruptIfRunning) {
        if (goroFuture != null) {
          return goroFuture.cancel(mayInterruptIfRunning);
        }
        if (canceled) {
          return true;
        }
        canceled = cancelPostponed(this);
        return canceled;
      }

      @Override
      public synchronized boolean isCancelled() {
        if (goroFuture != null) {
          return goroFuture.isCancelled();
        }
        return canceled;
      }

      @Override
      public synchronized boolean isDone() {
        return goroFuture != null && goroFuture.isDone();
      }

      @Override
      public synchronized T get() throws InterruptedException, ExecutionException {
        if (goroFuture != null) {
          return goroFuture.get();
        }
        // TODO, looks like we need future listeners
        throw new UnsupportedOperationException("not implemented yet");
      }

      @Override
      public T get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        if (goroFuture != null) {
          return goroFuture.get(timeout, unit);
        }
        // TODO
        throw new UnsupportedOperationException("not implemented yet");
      }
    }
  }
}
