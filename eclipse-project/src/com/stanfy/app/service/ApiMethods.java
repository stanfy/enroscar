package com.stanfy.app.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;
import android.util.SparseArray;

import com.stanfy.DebugFlags;
import com.stanfy.app.service.serverapi.RequestDescriptionProcessor;
import com.stanfy.app.service.serverapi.RequestProcessorHooks;
import com.stanfy.serverapi.RequestMethod;
import com.stanfy.serverapi.request.RequestDescription;
import com.stanfy.serverapi.response.ResponseData;
import com.stanfy.utils.AppUtils;

/**
 * Implementation for {@link ApiMethods}.
 * <p>
 *   There are two options how to handle incoming remote API request:
 *   <ol>
 *     <li>enqueue it so that incoming requests are processed one by one in a separate thread in FIFO order</li>
 *     <li>run it in parallel with other requests</li>
 *   </ol>
 * </p>
 * @author Roman Mazur - Stanfy (http://www.stanfy.com)
 */
public class ApiMethods {

  /** Default queue name. */
  public static final String DEFAULT_QUEUE = "default";

  /** Logging tag. */
  static final String TAG = "ApiMethods";

  /** Debug flag. */
  private static final boolean DEBUG = DebugFlags.DEBUG_SERVICES;

  // ================================ Executors ================================

  /** Thread pool parameter. */
  private static final int CORE_POOL_SIZE = 5,
                           MAXIMUM_POOL_SIZE = 32,
                           KEEP_ALIVE = 1,
                           MAX_QUEUE_LENGTH = 100;

  /** Threads pool. */
  private static final Executor THREAD_POOL_EXECUTOR;
  static {
    // TODO think about rejects
    Executor executor = getAsyncTaskThreadPool();
    if (executor == null) {
      final AtomicInteger threadCounter = new AtomicInteger(1);
      ThreadFactory tFactory = new ThreadFactory() {
        @Override
        public Thread newThread(final Runnable r) {
          return new Thread(r, "Tasks Queue Thread #" + threadCounter.getAndIncrement());
        }
      };
      final LinkedBlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>(MAX_QUEUE_LENGTH);
      executor = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE, TimeUnit.SECONDS, queue, tFactory);
    }
    THREAD_POOL_EXECUTOR = executor;
  }

  /** Executor for the task queue. */
  private static class TaskQueueExecutor implements Executor {
    /** Tasks queue. */
    final LinkedBlockingQueue<Runnable> tasks = new LinkedBlockingQueue<Runnable>();
    /** Active task. */
    Runnable activeTask;

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
        THREAD_POOL_EXECUTOR.execute(activeTask);
      }
    }
  }

  /** Task queue executors map. */
  private static final HashMap<String, Executor> TASK_QUEUE_EXECUTORS = new HashMap<String, Executor>();

  // ================================ Main queue call info data ================================

  // we should consider removing this stuff

  /** Null operation data. */
  static final APICallInfoData NULL_OPERATION_DATA = new APICallInfoData();

  /**
   * Information about last operation.
   * @author Roman Mazur (Stanfy - http://www.stanfy.com)
   */
  static class APICallInfoData {

    /** Preference key names. */
    private static final String ID = "rid",
                                RD_MESSAGE = "msg", RD_ERROR = "errorCode";

    /** Request ID. */
    int id = -1;
    /** Response data. */
    ResponseData<?> responseData = new ResponseData<Object>();

    public void set(final APICallInfoData data) {
      this.id = data.id;
      this.responseData = data.responseData;
    }
    public void set(final ResponseData<?> rd) {
      final ResponseData<?> responseData = this.responseData;
      responseData.setErrorCode(rd.getErrorCode());
      responseData.setMessage(rd.getMessage());
    }
    public void set(final int requestId) {
      this.id = requestId;
    }
    public boolean hasData() { return id != -1; }

    public void save(final SharedPreferences preferences) {
      final Editor lastOperationEditor = preferences.edit();
      lastOperationEditor.putInt(ID, this.id);
      final ResponseData<?> rd = this.responseData;
      if (rd != null) {
        lastOperationEditor
          .putString(RD_MESSAGE, rd.getMessage())
          .putInt(RD_ERROR, rd.getErrorCode());
      }
      lastOperationEditor.commit();
    }

    public void load(final SharedPreferences preferences) {
      final SharedPreferences src = preferences;
      final APICallInfoData dst = this;
      dst.set(src.getInt(ID, -1));
      final ResponseData<?> responseData = new ResponseData<Object>();
      responseData.setMessage(src.getString(RD_MESSAGE, null));
      responseData.setErrorCode(src.getInt(RD_ERROR, -1));
      dst.set(responseData);
      if (DEBUG) { Log.d(TAG, "Loaded last operation: " + dst.id + " / " + dst.responseData.getErrorCode() + " -> " + dst.hasData()); }
    }

  }

  // =======================================================================================

  /** Main thread handler. */
  private static final Handler MAIN_THREAD_HANDLER = new Handler(Looper.getMainLooper());

  /**
   * @author Roman Mazur (Stanfy - http://www.stanfy.com)
   */
  private abstract static class CallbackReporter {
    /** Reporter name. */
    final String name;
    protected CallbackReporter(final String name) {
      this.name = name;
    }
    abstract void report(final ApiMethodCallback callback, final RequestDescription requestDescription, final ResponseData<?> responseData) throws RemoteException;
  }

  /** Calls {@link ApiMethodCallback#reportSuccess(RequestDescription, ResponseData)}. */
  private static final CallbackReporter SUCCESS_REPORTER = new CallbackReporter("success") {
    @Override
    void report(final ApiMethodCallback callback, final RequestDescription requestDescription, final ResponseData<?> responseData) throws RemoteException {
      callback.reportSuccess(requestDescription, responseData);
    }
  };
  /** Calls {@link ApiMethodCallback#reportError(RequestDescription, ResponseData)}. */
  private static final CallbackReporter ERROR_REPORTER = new CallbackReporter("error") {
    @Override
    void report(final ApiMethodCallback callback, final RequestDescription requestDescription, final ResponseData<?> responseData) throws RemoteException {
      callback.reportError(requestDescription, responseData);
    }
  };
  /** Calls {@link ApiMethodCallback#reportCancel(RequestDescription, ResponseData)}. */
  private static final CallbackReporter CANCEL_REPORTER = new CallbackReporter("cancel") {
    @Override
    void report(final ApiMethodCallback callback, final RequestDescription requestDescription, final ResponseData<?> responseData) throws RemoteException {
      callback.reportCancel(requestDescription, responseData);
    }
  };

  /** Task that processes request description. */
  protected final class RequestDescriptionTask implements Callable<Void> {

    /** Invokation flag. */
    final AtomicBoolean invoked = new AtomicBoolean(false);

    /** RD to process. */
    final RequestDescription target;
    /** Processing hooks. */
    final RequestProcessorHooks hooks;

    public RequestDescriptionTask(final RequestDescription target, final RequestProcessorHooks hooks) {
      this.hooks = hooks;
      this.target = target;
    }

    @Override
    public Void call() throws Exception {
      invoked.set(true);
      rdProcessor.process(appService, target, hooks);
      return null;
    }

    void callHooksIfNotInvoked() {
      if (!invoked.get()) {
        hooks.beforeRequestProcessingStarted(target, null);
        hooks.onRequestCancel(target, null);
        hooks.afterRequestProcessingFinished(target, null);
      }
    }

  }

  /**
   * Common hooks implementation. Performs request callbacks reporting.
   * @author Roman Mazur (Stanfy - http://stanfy.com)
   */
  protected class CommonHooks implements RequestProcessorHooks {
    @Override
    public void beforeRequestProcessingStarted(final RequestDescription requestDescription, final RequestMethod requestMethod) {
      // nothing
    }
    @Override
    public void afterRequestProcessingFinished(final RequestDescription requestDescription, final RequestMethod requestMethod) {
      MAIN_THREAD_HANDLER.post(new Runnable() {
        @Override
        public void run() {
          trackersMap.remove(requestDescription.getId());
        }
      });
      if (DEBUG) { Log.d(TAG, "Request trackers count: " + trackersMap.size()); }
    }

    protected void reportToCallbacks(final RequestDescription description, final ResponseData<?> responseData, final CallbackReporter reporter) {
      if (DEBUG) { Log.v(TAG, "Start broadcast"); }
      final ArrayList<ApiMethodCallback> apiCallbacks = ApiMethods.this.apiCallbacks;

      synchronized (apiCallbacks) {

        int callbacksCount = apiCallbacks.size();
        while (callbacksCount > 0) {
          --callbacksCount;
          try {
            final ApiMethodCallback callback = apiCallbacks.get(callbacksCount);
            if (DEBUG) { Log.d(TAG, "Report API " + reporter.name + "/id=" + description.getId() + "/callback=" + callbacksCount + ": " + callback); }
            reporter.report(callback, description, responseData);
          } catch (final RemoteException e) {
            Log.e(TAG, "Cannot run callback report method", e);
          }
        }

      }

      if (DEBUG) { Log.v(TAG, "Finish broadcast"); }
    }

    @Override
    public void onRequestSuccess(final RequestDescription requestDescription, final ResponseData<?> responseData) {
      reportToCallbacks(requestDescription, responseData, SUCCESS_REPORTER);
    }
    @Override
    public void onRequestError(final RequestDescription requestDescription, final ResponseData<?> responseData) {
      reportToCallbacks(requestDescription, responseData, ERROR_REPORTER);
    }
    @Override
    public void onRequestCancel(final RequestDescription requestDescription, final ResponseData<?> responseData) {
      reportToCallbacks(requestDescription, responseData, CANCEL_REPORTER);
    }
  }

  /**
   * Hooks used by {@link ApiMethodsHandler}.
   * @author Roman Mazur (Stanfy - http://stanfy.com)
   */
  private class DefaultQueueRequestHooks implements RequestProcessorHooks {
    /** Main hooks. */
    private final RequestProcessorHooks mainHooks;

    public DefaultQueueRequestHooks(final RequestProcessorHooks mainHooks) {
      this.mainHooks = mainHooks;
    }

    @Override
    public void beforeRequestProcessingStarted(final RequestDescription requestDescription, final RequestMethod requestMethod) {
      try {
        initSync.await();
      } catch (final InterruptedException e) {
        Log.e(TAG, "Worker was interrupted", e);
        appService.checkForStop();
        return;
      }

      mainHooks.beforeRequestProcessingStarted(requestDescription, requestMethod);

      pending.set(NULL_OPERATION_DATA);
      pending.set(requestDescription.getId());

    }
    @Override
    public void afterRequestProcessingFinished(final RequestDescription requestDescription, final RequestMethod requestMethod) {

      mainHooks.afterRequestProcessingFinished(requestDescription, requestMethod);

      if (DEBUG) { Log.d(TAG, "Dump " + lastOperation.id); }
      lastOperation.save(lastOperationStore);
      pending.set(NULL_OPERATION_DATA);

    }

    private void updateLastOperation(final ResponseData<?> rd) {
      final APICallInfoData lastOperation = ApiMethods.this.lastOperation;
      lastOperation.set(pending);
      lastOperation.set(rd);
    }

    @Override
    public void onRequestSuccess(final RequestDescription requestDescription, final ResponseData<?> responseData) {
      updateLastOperation(responseData);
      mainHooks.onRequestSuccess(requestDescription, responseData);
    }
    @Override
    public void onRequestError(final RequestDescription requestDescription, final ResponseData<?> responseData) {
      updateLastOperation(responseData);
      mainHooks.onRequestError(requestDescription, responseData);
    }
    @Override
    public void onRequestCancel(final RequestDescription requestDescription, final ResponseData<?> responseData) {
      mainHooks.onRequestCancel(requestDescription, responseData);
    }

  }

  /**
   * Request tracker. It knows how to start or cancel request.
   */
  protected abstract static class RequestTracker {
    /** Request description. */
    final RequestDescription requestDescription;

    public RequestTracker(final RequestDescription rd) {
      this.requestDescription = rd;
    }

    /** @return request description */
    protected RequestDescription getRequestDescription() {
      return requestDescription;
    }

    /** Start a request. */
    public abstract void performRequest();
    /**
     * Abort a request.
     * @return true id request was aborted
     */
    public abstract boolean cancelRequest();
  }

  /**
   * Tracker for enqueued requests.
   * @author Roman Mazur (Stanfy - http://stanfy.com)
   */
  protected class TaskQueueRequestTracker extends RequestTracker {

    /** Future task. */
    final FutureTask<Void> future;

    public TaskQueueRequestTracker(final RequestDescription rd, final RequestProcessorHooks hooks) {
      super(rd);
      final RequestDescriptionTask worker = new RequestDescriptionTask(rd, hooks);
      future = new FutureTask<Void>(worker) {
        @Override
        protected void done() {
          try {

            get();
            worker.callHooksIfNotInvoked();

          } catch (InterruptedException e) {
            Log.w(TAG, e);
          } catch (ExecutionException e) {
            throw new RuntimeException("An error occured while processing request description", e.getCause());
          } catch (CancellationException e) {

            worker.callHooksIfNotInvoked();

          } catch (Throwable t) {
            throw new RuntimeException("An error occured while processing request description", t);
          }
        }
      };
    }

    @Override
    public void performRequest() {
      String queueName = requestDescription.getTaskQueueName();
      if (queueName == null) { queueName = DEFAULT_QUEUE; }
      if (DEBUG) { Log.d(TAG, "Will process request description in queue " + queueName + ", rd=" + requestDescription); }
      Executor exec = getTaskQueueExecutor(queueName);
      if (DEBUG) { Log.v(TAG, "Executors: " + TASK_QUEUE_EXECUTORS.keySet()); }
      exec.execute(future);
    }

    @Override
    public boolean cancelRequest() {
      requestDescription.setCanceled(true);
      return future.cancel(false); // TODO test with true
    }

  }

  /**
   * Tracker for parallel requests.
   * @author Roman Mazur (Stanfy - http://stanfy.com)
   */
  protected class ParallelRequestTracker extends TaskQueueRequestTracker {

    public ParallelRequestTracker(final RequestDescription rd, final RequestProcessorHooks hooks) {
      super(rd, hooks);
    }

    @Override
    public void performRequest() {
      if (DEBUG) { Log.d(TAG, "Will process request description in parallelly, rd=" + requestDescription); }
      THREAD_POOL_EXECUTOR.execute(future);
    }

  }

  /** Initialization sync point. */
  final CountDownLatch initSync = new CountDownLatch(1);

  /** Request description processing strategy. */
  final RequestDescriptionProcessor rdProcessor;
  /** Processor hooks. */
  private final RequestProcessorHooks defaultQueueProcessorHooks, commonProcessorHooks;

  /** Application service. */
  final ApplicationService appService;

  /** API callbacks. */
  private final ArrayList<ApiMethodCallback> apiCallbacks = new ArrayList<ApiMethodCallback>();
  /** Map of active requests by their IDs. */
  private final SparseArray<RequestTracker> trackersMap = new SparseArray<RequestTracker>();

  /** Last operation dump. */
  private final SharedPreferences lastOperationStore;

  /** Operations info. */
  final APICallInfoData pending = new APICallInfoData(), lastOperation = new APICallInfoData();

  /**
   * Constructs remote API methods implementation.<br/>
   * It creates instances of {@link RequestDescriptionProcessor} and {@link RequestProcessorHooks}.
   * @param appService application service
   */
  protected ApiMethods(final ApplicationService appService) {
    this.appService = appService;

    this.lastOperationStore = appService.getSharedPreferences("last-operation", Context.MODE_PRIVATE);
    loadLastOperation();

    this.commonProcessorHooks = createRequestDescriptionHooks();
    this.defaultQueueProcessorHooks = new DefaultQueueRequestHooks(this.commonProcessorHooks);
    this.rdProcessor = createRequestDescriptionProcessor(appService.getApplication());
  }

  // Note: must be access from the main thread only
  private static Executor getTaskQueueExecutor(final String name) {
    Executor exec = TASK_QUEUE_EXECUTORS.get(name);
    if (exec == null) {
      exec = new TaskQueueExecutor();
      TASK_QUEUE_EXECUTORS.put(name, exec);
    }
    return exec;
  }

  @SuppressLint("NewApi")
  private static Executor getAsyncTaskThreadPool() {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ? AsyncTask.THREAD_POOL_EXECUTOR : null;
  }

  /**
   * This method starts asynchronous read of last operation.
   * Workers must be synchronized with this reading.
   */
  private void loadLastOperation() {
    AppUtils.getSdkDependentUtils().executeAsyncTaskParallel(
      new AsyncTask<Void, Void, Void>() {
        @Override
        protected Void doInBackground(final Void... params) {
          lastOperation.load(lastOperationStore);
          initSync.countDown();
          return null;
        }
      }
    );
  }

  /**
   * Constructor a request description processing strategy.
   * @param app application instance
   * @return processor instance
   */
  protected RequestDescriptionProcessor createRequestDescriptionProcessor(final Application app) { return new RequestDescriptionProcessor(app); }
  /**
   * @return request description processing hooks
   */
  protected RequestProcessorHooks createRequestDescriptionHooks() { return new CommonHooks(); }


  // Note: must be accessed from the main thread
  boolean isWorking() { return trackersMap.size() > 0; }

  protected void destroy() {
    apiCallbacks.clear();
    if (DEBUG) { Log.d(TAG, "API methods destroyed"); }
  }

  /** @return application service that owns this implementation */
  protected ApplicationService getAppService() { return appService; }

  protected RequestTracker createRequestTracker(final RequestDescription description) {
    boolean defaultQueue = description.getTaskQueueName() == null || DEFAULT_QUEUE.equals(description.getTaskQueueName());
    return description.isParallelMode()
      ? new ParallelRequestTracker(description, commonProcessorHooks)                                               // request must be parallel
      : new TaskQueueRequestTracker(description, defaultQueue ? defaultQueueProcessorHooks : commonProcessorHooks); // request must be enqueued
  }

  protected void checkClientThread() {
    if (Looper.myLooper() != Looper.getMainLooper()) {
      throw new IllegalStateException("Wrong thread " + Thread.currentThread() + ". You must use main thread to call ApiMethods.");
    }
  }

  // -------------------------------------------- Client-side API ------------------------------------------------

  public void performRequest(final RequestDescription description) {
    checkClientThread();
    if (DEBUG) { Log.d(TAG, "Perform " + description + " " + this); }

    final RequestTracker tracker = createRequestTracker(description);

    trackersMap.put(description.getId(), tracker);
    tracker.performRequest();
  }

  public boolean cancelRequest(final int id) {
    checkClientThread();
    final RequestTracker tracker = trackersMap.get(id);
    if (tracker != null) {
      trackersMap.remove(id);
      return tracker.cancelRequest();
    }
    return false;
  }

  public void registerCallback(final ApiMethodCallback callback) {
    checkClientThread();
    if (DEBUG) { Log.d(TAG, "Register API callback " + callback + " to " + this); }
    final APICallInfoData b = new APICallInfoData();
    b.set(lastOperation);
    if (b.hasData()) {
      if (DEBUG) { Log.d(TAG, "Report last operation " + b.id); }
      callback.reportLastOperation(b.id, b.responseData);
    }
    b.set(pending);
    if (b.hasData()) { callback.reportPending(b.id); }
    synchronized (apiCallbacks) {
      apiCallbacks.add(callback);
    }
  }

  public void removeCallback(final ApiMethodCallback callback) {
    checkClientThread();
    if (DEBUG) { Log.d(TAG, "Remove API callback " + callback); }
    synchronized (apiCallbacks) {
      apiCallbacks.remove(callback);
    }
  }

  // --------------------------------------------------------------------------------------------

}
