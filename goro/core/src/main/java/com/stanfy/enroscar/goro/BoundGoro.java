package com.stanfy.enroscar.goro;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.stanfy.enroscar.goro.GoroFuture.IMMEDIATE;
import static com.stanfy.enroscar.goro.Util.checkMainThread;

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
    public <T> ObservableFuture<T> schedule(final Callable<T> task) {
      return schedule(DEFAULT_QUEUE, task);
    }

    @Override
    public <T> ObservableFuture<T> schedule(String queueName, Callable<T> task) {
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
    private final class BoundFuture<T> implements ObservableFuture<T>, Postponed {

      /** Queue name. */
      final String queue;
      /** Task instance. */
      final Callable<T> task;

      /** Attached Goro future. */
      private GoroFuture<T> goroFuture;

      /** Cancel flag. */
      private boolean canceled;

      /** Observers list. */
      private PendingObserversList pendingObservers;

      private BoundFuture(final String queue, final Callable<T> task) {
        this.queue = queue;
        this.task = task;
      }

      @Override
      public synchronized void act(final Goro goro) {
        goroFuture = (GoroFuture<T>) goro.schedule(queue, task);
        if (pendingObservers != null) {
          pendingObservers.execute();
          pendingObservers = null;
        }
        notifyAll();
      }

      @Override
      public synchronized boolean cancel(boolean mayInterruptIfRunning) {
        if (goroFuture != null) {
          return goroFuture.cancel(mayInterruptIfRunning);
        }
        if (!canceled) {
          cancelPostponed(this);
          pendingObservers = null;
          canceled = true;
        }
        notifyAll();
        return true;
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
        return canceled || goroFuture != null && goroFuture.isDone();
      }

      @Override
      public synchronized T get() throws InterruptedException, ExecutionException {
        // delegate
        if (goroFuture != null) {
          return goroFuture.get();
        }
        if (checkMainThread()) {
          throw new GoroException("Blocking main thread here will lead to a deadlock");
        }

        if (canceled) {
          throw new CancellationException("Task was canceled");
        }

        // wait for act() or cancel()
        wait();

        // either we got a delegate
        if (goroFuture != null) {
          return goroFuture.get();
        }
        // or we are canceled
        if (canceled) {
          throw new CancellationException("Task was canceled");
        }

        // wtf?
        throw new IllegalStateException("get() is unblocked but there is neither result"
            + " nor cancellation");
      }

      @Override
      public synchronized T get(final long timeout, @SuppressWarnings("NullableProblems") final TimeUnit unit)
          throws InterruptedException, ExecutionException, TimeoutException {
        // delegate
        if (goroFuture != null) {
          return goroFuture.get(timeout, unit);
        }
        if (checkMainThread()) {
          throw new GoroException("Blocking main thread here will lead to a deadlock");
        }

        if (canceled) {
          throw new CancellationException("Task was canceled");
        }

        // wait for act() or cancel()
        wait(unit.toMillis(timeout));

        // either we got a delegate
        if (goroFuture != null) {
          return goroFuture.get();
        }
        // or we are canceled
        if (canceled) {
          throw new CancellationException("Task was canceled");
        }

        // otherwise it's a timeout
        throw new TimeoutException();
      }

      @Override
      public synchronized void subscribe(final Executor executor, final FutureObserver<T> observer) {
        if (goroFuture != null) {
          goroFuture.subscribe(executor, observer);
          return;
        }
        if (canceled) {
          return;
        }

        if (pendingObservers == null) {
          pendingObservers = new PendingObserversList();
        }
        pendingObservers.add(new GoroFuture.ObserverRunnable<>(observer, null), executor);
      }

      @Override
      public void subscribe(final FutureObserver<T> observer) {
        subscribe(IMMEDIATE, observer);
      }

      /** List of pending observers. */
      private final class PendingObserversList extends ExecutionObserversList {
        @Override
        protected void executeObserver(final Executor executor, final Runnable what) {
          GoroFuture.ObserverRunnable runnable = (GoroFuture.ObserverRunnable) what;
          runnable.future = goroFuture;
          goroFuture.observers.add(what, executor);
        }
      }
    }
  }
}
