package com.stanfy.enroscar.goro;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Build;

import java.util.HashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Internal queues.
 */
interface Queues {

  /**
   * Set main executor used to really perform tasks.
   * @param mainExecutor executor that tasks are delegated to
   */
  void setDelegateExecutor(Executor mainExecutor);

  /**
   * @param queueName queue name
   * @return executor that performs all the tasks in a given queue
   */
  Executor getExecutor(String queueName);


  /** Default implementation. */
  class Impl implements Queues {

    /** Thread pool parameter. */
    private static final int CORE_POOL_SIZE = 5,
        MAXIMUM_POOL_SIZE = 32,
        KEEP_ALIVE = 1,
        MAX_QUEUE_LENGTH = 100;

    /** Threads pool. */
    private static Executor defaultThreadPoolExecutor;
    static {
      // TODO think about rejects
      Executor executor = getAsyncTaskThreadPool();
      if (executor == null) {
        final AtomicInteger threadCounter = new AtomicInteger();
        ThreadFactory tFactory = new ThreadFactory() {
          @Override
          public Thread newThread(final Runnable r) {
            return new Thread(r, "Goro Thread #" + threadCounter.incrementAndGet());
          }
        };
        final LinkedBlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>(MAX_QUEUE_LENGTH);
        executor = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE, TimeUnit.SECONDS, queue, tFactory);
      }
      defaultThreadPoolExecutor = executor;
    }

    /** Executors map. */
    private final HashMap<String, Executor> executorsMap = new HashMap<String, Executor>();

    /** Used threads pool. */
    private Executor delegateExecutor;

    {
      this.delegateExecutor = defaultThreadPoolExecutor;
    }


    @SuppressLint("NewApi")
    private static Executor getAsyncTaskThreadPool() {
      return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ? AsyncTask.THREAD_POOL_EXECUTOR : null;
    }

    @Override
    public void setDelegateExecutor(final Executor mainExecutor) {
      if (mainExecutor == null) {
        throw new IllegalArgumentException("Null threads pool");
      }
      synchronized (executorsMap) {
        if (!executorsMap.isEmpty()) {
          throw new IllegalStateException("Threads poll cannot be changed after any queue is created");
        }
        this.delegateExecutor = mainExecutor;
      }
    }

    @Override
    public Executor getExecutor(final String queueName) {
      synchronized (executorsMap) {
        if (queueName == null) {
          return delegateExecutor;
        }
        Executor exec = executorsMap.get(queueName);
        if (exec == null) {
          exec = new TaskQueueExecutor(delegateExecutor);
          executorsMap.put(queueName, exec);
        }
        return exec;
      }
    }

  }

  /** Executor for the task queue. */
  class TaskQueueExecutor implements Executor {
    /** Delegate executor. */
    final Executor delegate;
    /** Tasks queue. */
    final LinkedBlockingQueue<Runnable> tasks = new LinkedBlockingQueue<Runnable>();
    /** Active task. */
    Runnable activeTask;

    public TaskQueueExecutor(final Executor delegate)  {
      this.delegate = delegate;
    }

    @Override
    public synchronized void execute(final Runnable r) {
      tasks.offer(new Runnable() {
        @Override
        public void run() {
          try {
            r.run();
          } finally {
            scheduleNext();
          }
        }
      });
      if (activeTask == null) {
        scheduleNext();
      }
    }

    synchronized void scheduleNext() {
      activeTask = tasks.poll();
      if (activeTask != null) {
        delegate.execute(activeTask);
      }
    }
  }

//  /** Processor hooks. */
//  private final DirectRequestExecutorHooks commonProcessorHooks;
//
//  /** API callbacks. */
//  private final ArrayList<ApiMethodCallback> apiCallbacks = new ArrayList<ApiMethodCallback>();
//  /** Map of active requests by their IDs. */
//  private final SparseArray<RequestTracker> trackersMap = new SparseArray<RequestTracker>();
//
//  /**
//   * Constructs remote API methods implementation.
//   * @param appService application service
//   */
//  protected ApiMethods(final ApplicationService appService) {
//    this.appService = appService;
//    this.commonProcessorHooks = createRequestDescriptionHooks();
//  }
//
//  /**
//   * @return request description processing hooks
//   */
//  protected DirectRequestExecutorHooks createRequestDescriptionHooks() { return new CommonHooks(); }
//
//  boolean isWorking() {
//    synchronized (trackersMap) {
//      return trackersMap.size() > 0;
//    }
//  }
//
//  /**
//   * Service is going to be stopped. Do everything you have to.
//   */
//  protected void destroy() {
//    apiCallbacks.clear();
//    if (DEBUG) { Log.d(TAG, "API methods destroyed"); }
//  }
//
//  /** @return application service that owns this implementation */
//  protected ApplicationService getAppService() { return appService; }
//
//  /**
//   * Look at request description and construct an appropriate tracker for it (either enqueue or do parallel processing).
//   * @param description request description to process
//   * @return request tracker instance
//   */
//  protected RequestTracker createRequestTracker(final RequestDescription description) {
//    return description.isParallelMode()
//        ? new ParallelRequestTracker(description, commonProcessorHooks)   // request must be parallel
//        : new TaskQueueRequestTracker(description, commonProcessorHooks); // request must be enqueued
//  }
//
//  // -------------------------------------------- Client-side API ------------------------------------------------
//
//  public void performRequest(final RequestDescription description) {
//    if (DEBUG) { Log.d(TAG, "Perform " + description + " " + this); }
//
//    final RequestTracker tracker = createRequestTracker(description);
//    synchronized (trackersMap) {
//      trackersMap.put(tracker.requestDescription.getId(), tracker);
//    }
//    tracker.performRequest();
//  }
//
//  public boolean cancelRequest(final int id) {
//    final RequestTracker tracker;
//    synchronized (trackersMap) {
//      tracker = trackersMap.get(id);
//    }
//    if (tracker != null) {
//      return tracker.cancelRequest();
//    }
//    return false;
//  }
//
//  public void registerCallback(final ApiMethodCallback callback) {
//    if (DEBUG) { Log.d(TAG, "Register API callback " + callback + " to " + this); }
//    synchronized (apiCallbacks) {
//      apiCallbacks.add(callback);
//    }
//  }
//
//  public void removeCallback(final ApiMethodCallback callback) {
//    if (DEBUG) { Log.d(TAG, "Remove API callback " + callback); }
//    synchronized (apiCallbacks) {
//      apiCallbacks.remove(callback);
//    }
//  }
//
//  // --------------------------------------------------------------------------------------------
//
//  /** Calls on of {@link ApiMethodCallback} methods. */
//  private abstract static class CallbackReporter {
//    /** Reporter name. */
//    final String name;
//    protected CallbackReporter(final String name) {
//      this.name = name;
//    }
//    abstract void report(final ApiMethodCallback callback, final RequestDescription requestDescription, final ResponseData<?> responseData);
//  }
//
//  /** Executor for the task queue. */
//  private static class TaskQueueExecutor implements Executor {
//    /** Tasks queue. */
//    final LinkedBlockingQueue<Runnable> tasks = new LinkedBlockingQueue<Runnable>();
//    /** Active task. */
//    Runnable activeTask;
//
//    @Override
//    public synchronized void execute(final Runnable r) {
//      tasks.offer(new Runnable() {
//        @Override
//        public void run() {
//          try {
//            r.run();
//          } finally {
//            scheduleNext();
//          }
//        }
//      });
//      if (activeTask == null) {
//        scheduleNext();
//      }
//    }
//
//    synchronized void scheduleNext() {
//      activeTask = tasks.poll();
//      if (activeTask != null) {
//        THREAD_POOL_EXECUTOR.execute(activeTask);
//      }
//    }
//  }
//
//  /** Task that processes request description. */
//  protected final class RequestDescriptionTask implements Callable<Void> {
//
//    /** Invokation flag. */
//    final AtomicBoolean invoked = new AtomicBoolean(false);
//
//    /** RD to process. */
//    final RequestDescription target;
//    /** Processing hooks. */
//    final DirectRequestExecutorHooks hooks;
//
//    /**
//     * @param target request description to process
//     * @param hooks processor hooks
//     */
//    public RequestDescriptionTask(final RequestDescription target, final DirectRequestExecutorHooks hooks) {
//      this.hooks = hooks;
//      this.target = target;
//    }
//
//    @Override
//    public Void call() throws Exception {
//      invoked.set(true);
//      new DirectRequestExecutor(appService, hooks).performRequest(target);
//      return null;
//    }
//
//    void callHooksIfNotInvoked() {
//      if (!invoked.get()) {
//        hooks.beforeRequestProcessingStarted(target, null);
//        hooks.onRequestCancel(target, null);
//        hooks.afterRequestProcessingFinished(target, null);
//      }
//    }
//
//  }
//
//  /**
//   * Request tracker. It knows how to start or cancel request.
//   */
//  protected abstract static class RequestTracker {
//    /** Request description. */
//    final RequestDescription requestDescription;
//
//    /**
//     * @param rd request description to process
//     */
//    public RequestTracker(final RequestDescription rd) {
//      this.requestDescription = rd;
//    }
//
//    /** @return request description */
//    protected RequestDescription getRequestDescription() {
//      return requestDescription;
//    }
//
//    /** Start a request. */
//    public abstract void performRequest();
//    /**
//     * Abort a request.
//     * @return true id request was aborted
//     */
//    public abstract boolean cancelRequest();
//  }
//
//  /**
//   * Tracker for enqueued requests.
//   * @author Roman Mazur (Stanfy - http://stanfy.com)
//   */
//  protected class TaskQueueRequestTracker extends RequestTracker {
//
//    /** Future task. */
//    final FutureTask<Void> future;
//
//    /**
//     * @param rd request description to process
//     * @param hooks processor hooks
//     */
//    public TaskQueueRequestTracker(final RequestDescription rd, final DirectRequestExecutorHooks hooks) {
//      super(rd);
//      final RequestDescriptionTask worker = new RequestDescriptionTask(rd, hooks);
//      future = new FutureTask<Void>(worker) {
//        @Override
//        protected void done() {
//          try {
//
//            get();
//            worker.callHooksIfNotInvoked();
//
//          } catch (InterruptedException e) {
//            Log.w(TAG, e);
//          } catch (ExecutionException e) {
//            throw new RuntimeException("An error occured while processing request description", e.getCause());
//          } catch (CancellationException e) {
//
//            worker.callHooksIfNotInvoked();
//
//          } catch (Throwable t) {
//            throw new RuntimeException("An error occured while processing request description", t);
//          }
//        }
//      };
//    }
//
//    @Override
//    public void performRequest() {
//      String queueName = requestDescription.getTaskQueueName();
//      if (queueName == null) { queueName = DEFAULT_QUEUE; }
//      if (DEBUG) { Log.d(TAG, "Will process request description in queue " + queueName + ", rd=" + requestDescription); }
//      Executor exec = getTaskQueueExecutor(queueName);
//      if (DEBUG) {
//        synchronized (TASK_QUEUE_EXECUTORS) {
//          Log.v(TAG, "Executors: " + TASK_QUEUE_EXECUTORS.keySet());
//        }
//      }
//      exec.execute(future);
//    }
//
//    @Override
//    public boolean cancelRequest() {
//      requestDescription.setCanceled(true);
//      return future.cancel(false); // TODO test with true
//    }
//
//  }
//
//  /**
//   * Tracker for parallel requests.
//   * @author Roman Mazur (Stanfy - http://stanfy.com)
//   */
//  protected class ParallelRequestTracker extends TaskQueueRequestTracker {
//
//    /**
//     * @param rd request description to process
//     * @param hooks processor hooks
//     */
//    public ParallelRequestTracker(final RequestDescription rd, final DirectRequestExecutorHooks hooks) {
//      super(rd, hooks);
//    }
//
//    @Override
//    public void performRequest() {
//      if (DEBUG) { Log.d(TAG, "Will process request description in parallelly, rd=" + requestDescription); }
//      THREAD_POOL_EXECUTOR.execute(future);
//    }
//
//  }
//
//  /**
//   * Common hooks implementation. Performs request callbacks reporting.
//   * @author Roman Mazur (Stanfy - http://stanfy.com)
//   */
//  protected class CommonHooks implements DirectRequestExecutorHooks {
//    @Override
//    public void beforeRequestProcessingStarted(final RequestDescription requestDescription, final RequestMethod requestMethod) {
//      // nothing
//    }
//    @Override
//    public void afterRequestProcessingFinished(final RequestDescription requestDescription, final RequestMethod requestMethod) {
//      synchronized (trackersMap) {
//        trackersMap.remove(requestDescription.getId());
//        if (DEBUG) { Log.d(TAG, "Request trackers count: " + trackersMap.size()); }
//      }
//      appService.checkForStop();
//    }
//
//    /**
//     * @param description request description that has been processed
//     * @param responseData obtained response data (may be null if processing is canceled)
//     * @param reporter reporter instance (the one who knows what callback to call)
//     */
//    protected void reportToCallbacks(final RequestDescription description, final ResponseData<?> responseData, final CallbackReporter reporter) {
//      if (DEBUG) { Log.v(TAG, "Start broadcast"); }
//      final ArrayList<ApiMethodCallback> apiCallbacks = ApiMethods.this.apiCallbacks;
//
//      synchronized (apiCallbacks) {
//
//        int callbacksCount = apiCallbacks.size();
//        while (callbacksCount > 0) {
//          --callbacksCount;
//
//          final ApiMethodCallback callback = apiCallbacks.get(callbacksCount);
//          if (DEBUG) { Log.d(TAG, "Report API " + reporter.name + "/id=" + description.getId() + "/callback=" + callbacksCount + ": " + callback); }
//          reporter.report(callback, description, responseData);
//        }
//
//      }
//
//      if (DEBUG) { Log.v(TAG, "Finish broadcast"); }
//    }
//
//    @Override
//    public void onRequestSuccess(final RequestDescription requestDescription, final ResponseData<?> responseData) {
//      reportToCallbacks(requestDescription, responseData, SUCCESS_REPORTER);
//    }
//    @Override
//    public void onRequestError(final RequestDescription requestDescription, final ResponseData<?> responseData) {
//      reportToCallbacks(requestDescription, responseData, ERROR_REPORTER);
//    }
//    @Override
//    public void onRequestCancel(final RequestDescription requestDescription, final ResponseData<?> responseData) {
//      reportToCallbacks(requestDescription, responseData, CANCEL_REPORTER);
//    }
//  }

}
