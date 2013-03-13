package com.stanfy.utils;

import java.lang.ref.WeakReference;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.stanfy.DebugFlags;
import com.stanfy.app.service.ApiMethodCallback;
import com.stanfy.enroscar.rest.ErrorCodes;
import com.stanfy.enroscar.rest.request.RequestDescription;
import com.stanfy.enroscar.rest.response.ResponseData;
import com.stanfy.views.R;

/**
 * A class that takes responsibility of binding to application service and invoking remote API related methods.
 * @author Roman Mazur - Stanfy (http://www.stanfy.com)
 */
public class ApiMethodsSupport extends RequestPerformer {

  /** Logging tag. */
  private static final String TAG = "ApiSupport";
  /** Debug flag. */
  private static final boolean DEBUG = DebugFlags.DEBUG_API;

  /** Support handler messages. */
  static final int MSG_SERVER_ERROR_TOAST = 1, MSG_CONNECTION_ERROR_INFO = 2, MSG_SHOW_TOAST = 5;

  /** Handler. */
  SupportHandler handler;

  public ApiMethodsSupport(final Context context, final ApiMethodCallback callback) {
    super(context, callback);
    if (callback instanceof ApiSupportRequestCallback) {
      ((ApiSupportRequestCallback) callback).setSupport(this);
    }
    handler = new SupportHandler(this);
  }

  @Override
  protected void doRequest(final RequestDescription description) {
    serviceObject.performRequest(description);
  }

  void displayConnectionErrorInformation() {
    final Context context = contextRef.get();
    if (context == null) { return; }
    GUIUtils.shortToast(context, R.string.error_connection);
  }

  void showToast(final String message) {
    final Context context = contextRef.get();
    if (context == null) { return; }
    GUIUtils.shortToast(context, message);
  }

  /** @return GUI handler instance */
  public Handler getHandler() { return handler; }

  /**
   * @author Roman Mazur - Stanfy (http://www.stanfy.com)
   */
  static class SupportHandler extends Handler {

    /** Support reference. */
    private final WeakReference<ApiMethodsSupport> supportRef;

    public SupportHandler(final ApiMethodsSupport support) {
      this.supportRef = new WeakReference<ApiMethodsSupport>(support);
    }

    @Override
    public void handleMessage(final Message msg) {
      final ApiMethodsSupport support = supportRef.get();
      if (support == null) { return; } // we are destroyed

      switch (msg.what) {

      case MSG_SHOW_TOAST:
      case MSG_SERVER_ERROR_TOAST:
        support.showToast((String)msg.obj);
        break;

      case MSG_CONNECTION_ERROR_INFO:
        support.displayConnectionErrorInformation();
        break;

      default:
      }
    }

  }

  /**
   * Provides default behavior.
   * @param <MT> model type
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
      support.handler.obtainMessage(MSG_CONNECTION_ERROR_INFO, responseData.getMessage()).sendToTarget();
    }

    /**
     * Process the error obtained from the server. This method is called <b>outside of the main thread</b>.
     * @param requestDescription request description
     * @param responseData response data
     */
    protected void processServerError(final RequestDescription requestDescription, final ResponseData<?> responseData) {
      Log.e(TAG, "Got an error in server response: " + responseData.getErrorCode() + " / [" + responseData.getMessage() + "]");
      support.handler.obtainMessage(MSG_SERVER_ERROR_TOAST, responseData.getMessage()).sendToTarget();
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

    protected void onError(final RequestDescription requestDescription, final ResponseData<?> responseData) {
    }

    /**
     * @see ApiMethodCallback#onCancel(RequestDescription)
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
