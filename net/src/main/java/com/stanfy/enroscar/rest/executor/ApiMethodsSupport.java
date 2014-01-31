package com.stanfy.enroscar.rest.executor;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.stanfy.enroscar.content.loader.ResponseData;
import com.stanfy.enroscar.rest.ErrorCodes;
import com.stanfy.enroscar.net.operation.RequestDescription;

/**
 * A class that takes responsibility of binding to application service and invoking remote API related methods.
 * @author Roman Mazur - Stanfy (http://www.stanfy.com)
 */
public class ApiMethodsSupport extends RequestPerformer {

  /** Logging tag. */
  private static final String TAG = "ApiSupport";
  /** Debug flag. */
  private static final boolean DEBUG = DebugFlags.DEBUG;

  /** Handler. */
  Handler handler;

  public ApiMethodsSupport(final Context context, final ApiMethodCallback callback) {
    super(context, callback);
    if (callback instanceof ApiSupportRequestCallback) {
      ((ApiSupportRequestCallback) callback).setSupport(this);
    }
    handler = new Handler(Looper.getMainLooper());
  }

  @Override
  protected void doRequest(final RequestDescription description) {
    serviceObject.performRequest(description);
  }

  /** @return GUI handler instance */
  public Handler getHandler() { return handler; }

  /**
   * Provides default behavior.
   * @author Roman Mazur - Stanfy (http://www.stanfy.com)
   */
  public abstract static class ApiSupportRequestCallback implements ApiMethodCallback {

    /** Support. */
    private ApiMethodsSupport support;

    void setSupport(final ApiMethodsSupport support) { this.support = support; }

    /** @return the support */
    public ApiMethodsSupport getSupport() { return support; }

    public abstract boolean filterOperation(final int requestId, final RequestDescription requestDescription);

    /**
     * Process the connection error. This method is called <b>outside of the main thread</b>.
     * @param requestDescription request description
     * @param responseData response data
     */
    protected void processConnectionError(final RequestDescription requestDescription, final ResponseData<?> responseData) {
      Log.e(TAG, responseData.getMessage());
    }

    /**
     * Process the error obtained from the server. This method is called <b>outside of the main thread</b>.
     * @param requestDescription request description
     * @param responseData response data
     */
    protected void processServerError(final RequestDescription requestDescription, final ResponseData<?> responseData) {
      Log.e(TAG, "Got an error in server response: " + responseData.getErrorCode() + " / [" + responseData.getMessage() + "]");
    }

    /**
     * Process the result data. This method is called <b>outside of the main thread</b>.
     * @param requestDescription request description
     * @param responseData response data
     */
    protected abstract void processSuccess(final RequestDescription requestDescription, final ResponseData<?> responseData);

    /**
     * This method is called <b>outside of the main thread</b>.
     * @param requestDescription request description instance
     */
    protected void onOperationFinished(final RequestDescription requestDescription) {
      if (DEBUG && support != null) {
        Log.d(TAG, requestDescription.getId() + " finished, thread " + Thread.currentThread() + ", context " + support.contextRef.get());
      }
    }

    /**
     * Error happened.
     * @param requestDescription request description instance
     * @param responseData obtained response data
     */
    protected void onError(final RequestDescription requestDescription, final ResponseData<?> responseData) {
    }

    /**
     * @param requestDescription request description instance
     * @param responseData obtained response data (rather likely it is null)
     * @see ApiMethodCallback#reportCancel(RequestDescription, ResponseData)
     */
    protected void onCancel(final RequestDescription requestDescription, final ResponseData<?> responseData) {
      // nothing
    }

    private boolean successProcessing(final RequestDescription requestDescription) {
      if (!filterOperation(requestDescription.getId(), requestDescription)) { return false; }
      if (DEBUG) { Log.d(TAG, requestDescription.getId() + " success"); }
      return true;
    }

    @Override
    public final void reportSuccess(final RequestDescription requestDescription, final ResponseData<?> responseData) {
      if (successProcessing(requestDescription)) {
        processSuccess(requestDescription, responseData);
      }
      onOperationFinished(requestDescription);
    }

    @Override
    public final void reportError(final RequestDescription requestDescription, final ResponseData<?> responseData) {
      if (!filterOperation(requestDescription.getId(), requestDescription)) { return; }
      onError(requestDescription, responseData);
      switch (responseData.getErrorCode()) {
      case ErrorCodes.ERROR_CODE_CONNECTION:
      case ErrorCodes.ERROR_CODE_SERVER_COMUNICATION:
        processConnectionError(requestDescription, responseData);
        if (DEBUG) { Log.d(TAG, requestDescription.getId() + " error, message " + responseData.getMessage()); }
        break;
      default:
        processServerError(requestDescription, responseData);
      }
      onOperationFinished(requestDescription);
    }

    @Override
    public final void reportCancel(final RequestDescription requestDescription, final ResponseData<?> responseData) {
      if (!filterOperation(requestDescription.getId(), requestDescription)) { return; }
      if (DEBUG) { Log.d(TAG, "Canceled " + requestDescription.getId()); }
      onCancel(requestDescription, responseData);
    }

  }

}
