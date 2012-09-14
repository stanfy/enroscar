package com.stanfy.app.loader;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import android.os.SystemClock;
import android.support.v4.content.Loader;
import android.util.Log;

import com.stanfy.DebugFlags;
import com.stanfy.serverapi.request.RequestBuilder;
import com.stanfy.serverapi.request.RequestDescription;
import com.stanfy.serverapi.response.ResponseData;
import com.stanfy.utils.ApiMethodsSupport;
import com.stanfy.utils.ApiMethodsSupport.ApiSupportRequestCallback;

/**
 * Loader that uses a request builder.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 * @param <MT> model type
 */
public class RequestBuilderLoader<MT> extends Loader<ResponseData<MT>> {

  /** Request builder loader. */
  protected static final String TAG = "RBLoader";

  /** Debug flag. */
  protected static final boolean DEBUG = DebugFlags.DEBUG_API;

  /** Request builder instance. */
  private final RequestBuilder<MT> requestBuilder;

  /** API support. */
  final ApiMethodsExecutor apiSupport;

  /** Current request ID. */
  private int requestId = -1;
  /** Canceling request ID. */
  private int cancelingRequestId = -1;
  /** Request is waiting for execution time. */
  private boolean requestWaiting = false;
  /** Update requested flag. */
  private boolean updateRequested = false;

  /** Latch used for waiting. */
  private final CountDownLatch done = new CountDownLatch(1);

  /** Update throttle. */
  long updateThrottle;
  /** Last load complete time. */
  long lastLoadCompleteTime = -1;

  /** Received response. */
  private ResponseData<MT> receivedResponse;

  /** Runnable for {@link #executeRequestNow()}. */
  private final Runnable performRequestRunnable = new Runnable() {
    @Override
    public void run() { executeRequestNow(); }
  };

  public RequestBuilderLoader(final RequestBuilder<MT> requestBuilder) {
    super(requestBuilder.getContext());
    this.requestBuilder = requestBuilder;
    this.apiSupport = new ApiMethodsExecutor();
    requestBuilder.setExecutor(this.apiSupport);
  }

  public RequestBuilder<MT> getRequestBuilder() { return requestBuilder; }

  /** @return busy state indicator */
  public boolean isBusy() { return requestWaiting || requestId != -1; }

  /**
   * Set amount to throttle updates by.  This is the minimum time from
   * when the last {@link #onLoadInBackground()} call has completed until
   * a new load is scheduled.
   *
   * @param delayMS Amount of delay, in milliseconds.
   */
  public void setUpdateThrottle(final long delayMS) {
    updateThrottle = delayMS;
  }

  /**
   * @param token request token
   * @param operationCode request operation code
   * @see {@link ApiSupportRequestCallback#filterOperation(int, int)}
   * @return whether we are interested in the incoming data
   */
  protected boolean filterOperation(final int requestId, final RequestDescription requestDescription) {
    return this.requestId == requestId || this.cancelingRequestId == requestId;
  }

  @Override
  protected void onForceLoad() {
    super.onForceLoad();
    cancelLoad();
    executePendingRequest();
  }

  /**
   * Attempt to cancel the current request. See {@link com.stanfy.app.service.ApiMethods#cancelRequest(int)}
   * for more info.  Must be called on the main thread of the process.
   */
  public void cancelLoad() {
    if (requestId != -1) {
      final boolean willCancel = apiSupport.cancelRequest(requestId);
      requestId = -1;
      if (willCancel) {
        // we will wait for #onCancel for further actions
        cancelingRequestId = requestId;
      } else if (updateRequested) {
        // we have a new request, let it live
        executePendingRequest();
      }
    } else if (requestWaiting) {
      removeWaitingRequest();
    }
  }

  private void removeWaitingRequest() {
    requestWaiting = false;
    apiSupport.getHandler().removeCallbacks(performRequestRunnable);
  }

  private void resetStateAfterComplete() {
    apiSupport.unbindAndStopListening();
    requestId = -1;
    cancelingRequestId = -1;
    lastLoadCompleteTime = SystemClock.uptimeMillis();
  }

  private void executePendingRequest() {
    if (requestId == -1) {
      updateRequested = false;

      if (requestWaiting) {
        removeWaitingRequest();
      }

      if (updateThrottle > 0) {
        final long now = SystemClock.uptimeMillis();
        final long timeMargin = lastLoadCompleteTime + updateThrottle;
        if (now < timeMargin) {
          // Not yet time to do another load.
          if (DEBUG) {
            Log.v(TAG, "Waiting until " + timeMargin + " to execute");
          }
          requestWaiting = true;
          apiSupport.getHandler().postAtTime(performRequestRunnable, timeMargin);
          return;
        }
      }

      executeRequestNow();
    } else {
      updateRequested = true;
    }
  }

  void executeRequestNow() {
    requestWaiting = false;
    requestId = requestBuilder.execute();
    if (DEBUG) { Log.v(TAG, "executeRequestNow, " + this); }
  }

  private void checkForUpdateRequest() {
    if (updateRequested) {
      if (DEBUG) { Log.d(TAG, "try to execute pending request"); }
      executePendingRequest();
    }
  }

  protected void dispatchLoadedData(final RequestDescription request, final ResponseData<MT> data) {
    if (requestId != request.getId() || isAbandoned()) {
      dispatchCanceledData(data);
      return;
    }

    resetStateAfterComplete();
    deliverResult(data);

    if (DEBUG) { Log.d(TAG, "Request data delivered, " + this); }
    checkForUpdateRequest();
  }

  protected void dispatchCanceledData(final ResponseData<MT> data) {
    resetStateAfterComplete();
    onCanceled(data);
    if (DEBUG) { Log.d(TAG, "Request data canceled, " + this); }
    checkForUpdateRequest();
  }

  /**
   * Called if the task was canceled before it was completed.  Gives the class a chance
   * to properly dispose of the result.
   * @param responseData can be null
   */
  protected void onCanceled(final ResponseData<MT> responseData) {
    if (responseData != null) {
      onReleaseData(responseData);
    }
  }

  /**
   * Helper function to take care of releasing resources associated
   * with an actively loaded data set.
   * @param responseData received response
   */
  protected void onReleaseData(final ResponseData<MT> responseData) {
    if (DEBUG) { Log.v(TAG, "onReleaseData, " + this); }
  }

  protected ResponseData<MT> onAcceptData(final ResponseData<MT> previousData, final ResponseData<MT> responseData) {
    return responseData;
  }

  @Override
  public void deliverResult(final ResponseData<MT> data) {
    if (isReset()) {
      // data is not needed more
      onReleaseData(data);
      return;
    }

    final ResponseData<MT> oldData = receivedResponse;
    if (oldData != data) {
      receivedResponse = onAcceptData(oldData, data);
    }

    if (isStarted()) {
      super.deliverResult(receivedResponse);
    }

    if (oldData != null && oldData != receivedResponse) {
      onReleaseData(oldData);
    }
    if (data != null && data != receivedResponse) {
      onReleaseData(data);
    }
  }

  @Override
  protected void onStartLoading() {
    if (receivedResponse != null) {
      deliverResult(receivedResponse);
    }

    if (takeContentChanged() || receivedResponse == null) {
      forceLoad();
    }
  }

  @Override
  protected void onStopLoading() {
    cancelLoad();
  }

  // request to completely reset the loader
  @Override
  protected void onReset() {
    if (DEBUG) { Log.v(TAG, "onReset " + this); }
    super.onReset();
    onStopLoading();

    if (receivedResponse != null) {
      onReleaseData(receivedResponse);
      receivedResponse = null;
    }
  }

  /**
   * For testing only.
   */
  void waitForLoader(final long time) {
    try {
      done.await(time, TimeUnit.MILLISECONDS);
    } catch (final InterruptedException e) {
      Log.e(TAG, "waitForLoader() ininterrupted", e);
    }
  }

  @Override
  public void dump(final String prefix, final FileDescriptor fd, final PrintWriter writer, final String[] args) {
    super.dump(prefix, fd, writer, args);
    writer.write(prefix);
    writer.write(" requestId=" + requestId);
    writer.write(" cancelingRequestId=" + cancelingRequestId);
    writer.write(" requestWaiting=" + requestWaiting);
    writer.write(" updateRequested=" + updateRequested);
    writer.write(" binded=" + apiSupport.isRegistered());
    writer.println();
    if (updateThrottle != 0) {
      writer.print(prefix);
      writer.print(" updateThrottle=" + updateThrottle);
      writer.print(" lastLoadCompleteTime=" + lastLoadCompleteTime);
      writer.println();
    }
  }

  @Override
  public String toString() {
    return getClass().getName() + "{id=" + getId() + ", reqId=" + requestId + ", rb=" + requestBuilder + "}";
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  ResponseData<MT> castResponseData(final RequestDescription requestDescription, final ResponseData<?> responseData) {
    if (responseData == null) { return null; }
    final Object model = responseData.getModel();
    if (model == null || requestBuilder.getExpectedModelType().getRawClass().isInstance(model)) {
      final ResponseData result = responseData;
      return result;
    }
    throw new IllegalArgumentException("Response data contains model of illegal type: " + model.getClass()
        + ", expected is " + requestBuilder.getExpectedModelType().getRawClass());
  }

  /** For posting loaded data to the main thread. */
  private final class DispatchLoadedDataRunnable implements Runnable {
    /** Request. */
    final RequestDescription request;
    /** Response data. */
    final ResponseData<MT> data;
    /** Canceled result flag. */
    final boolean canceled;

    public DispatchLoadedDataRunnable(final RequestDescription request, final ResponseData<MT> data, final boolean canceled) {
      this.request = request;
      this.data = data;
      this.canceled = canceled;
    }

    @Override
    public void run() {
      try {
        if (canceled) {
          dispatchCanceledData(data);
        } else {
          if (data == null) { throw new IllegalStateException("ResponseData is null but request was not canceled!"); }
          dispatchLoadedData(request, data);
        }
      } finally {
        done.countDown();
      }
    }
  }

  /**
   * Special executor for this loader. Has similar behavior to {@link com.stanfy.utils.ServiceRequestPerformer}.
   * @author Roman Mazur (Stanfy - http://stanfy.com)
   */
  protected class ApiMethodsExecutor extends ApiMethodsSupport {

    public ApiMethodsExecutor() {
      super(getContext(), new ApiSupportRequestCallback() {
        @Override
        public boolean filterOperation(final int requestId, final RequestDescription requestDescription) {
          return RequestBuilderLoader.this.filterOperation(requestId, requestDescription);
        }

        // prevent any default processing
        @Override
        protected void processServerError(final RequestDescription requestDescription, final ResponseData<?> responseData) {
          // nothing
        }
        @Override
        protected void processConnectionError(final RequestDescription requestDescription, final ResponseData<?> responseData) {
          // nothing
        }

        // deliver results
        @Override
        protected void processSuccess(final RequestDescription requestDescription, final ResponseData<?> responseData) {
          apiSupport.getHandler().post(new DispatchLoadedDataRunnable(requestDescription, castResponseData(requestDescription, responseData), false));
        }
        @Override
        protected void onError(final RequestDescription requestDescription, final ResponseData<?> responseData) {
          apiSupport.getHandler().post(new DispatchLoadedDataRunnable(requestDescription, castResponseData(requestDescription, responseData), false));
        }
        @Override
        protected void onCancel(final RequestDescription requestDescription, final ResponseData<?> responseData) {
          apiSupport.getHandler().post(new DispatchLoadedDataRunnable(requestDescription, castResponseData(requestDescription, responseData), true));
        }
      });
    }

    @Override
    public int performRequest(final RequestDescription description) {
      bindAndListen();
      return super.performRequest(description);
    }

    void bindAndListen() {
      bind();
      registerCallback();
    }
    void unbindAndStopListening() {
      removeCallback();
      unbind();
    }

  }

}
