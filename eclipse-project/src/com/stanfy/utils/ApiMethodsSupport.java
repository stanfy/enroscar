package com.stanfy.utils;

import java.io.Serializable;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;

import com.stanfy.DebugFlags;
import com.stanfy.app.ActionBarActivity;
import com.stanfy.app.ActionBarSupport;
import com.stanfy.app.service.ApiMethods;
import com.stanfy.serverapi.ErrorCodes;
import com.stanfy.serverapi.request.RequestCallback;
import com.stanfy.serverapi.request.RequestDescription;
import com.stanfy.serverapi.response.ResponseData;
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
  static final int MSG_SERVER_ERROR_TOAST = 1, MSG_CONNECTION_ERROR_INFO = 2, MSG_HIDE_PROGRESS = 3, MSG_SHOW_PROGRESS = 4, MSG_SHOW_TOAST = 5;

  /** Handler. */
  SupportHandler handler;

  /** Last operation interest flag. */
  final boolean lastOperationInterest;

  public ApiMethodsSupport(final Context context, final RequestCallback<?> callback) {
    this(context, callback, false);
  }
  public ApiMethodsSupport(final Context context, final RequestCallback<?> callback, final boolean lastOperationInterest) {
    super(context, callback);
    if (callback instanceof SupportAware) { ((SupportAware)callback).setSupport(this); }
    this.lastOperationInterest = lastOperationInterest;
    this.handler = new SupportHandler();
  }

  @Override
  protected void doRequest(final RequestDescription description) {
    new CallTask().execute(description);
  }

  void setProgressVisibility(final boolean value) {
    final Context context = contextRef.get();
    if (context == null) { return; }
    if (!(context instanceof ActionBarActivity)) { return; }
    final ActionBarSupport abs = ((ActionBarActivity)context).getActionBarSupport();
    if (abs != null) { abs.setProgressVisibility(value); }
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

  /**
   * @author Roman Mazur - Stanfy (http://www.stanfy.com)
   */
  class SupportHandler extends Handler {

    @Override
    public void handleMessage(final Message msg) {
      switch (msg.what) {

      case MSG_SHOW_TOAST:
      case MSG_SERVER_ERROR_TOAST:
        showToast((String)msg.obj);
        break;

      case MSG_CONNECTION_ERROR_INFO:
        displayConnectionErrorInformation();
        break;

      case MSG_HIDE_PROGRESS:
        setProgressVisibility(false);
        break;

      case MSG_SHOW_PROGRESS:
        setProgressVisibility(true);
        break;

      default:
      }
    }

  }

  /**
   * @author Roman Mazur - Stanfy (http://www.stanfy.com)
   */
  class CallTask extends AsyncTask<RequestDescription, Void, Boolean> {
    @Override
    protected void onPreExecute() { setProgressVisibility(true); }
    @Override
    protected Boolean doInBackground(final RequestDescription... params) {
      final ApiMethods apiMethods = serviceObject;
      if (apiMethods != null) {
        try {
          apiMethods.performRequest(params[0]);
          return true;
        } catch (final RemoteException e) {
          Log.e(TAG, "Cannot run operation", e);
          return false;
        }
      }
      return false;
    }
    @Override
    protected void onPostExecute(final Boolean result) {
      if (!result) { setProgressVisibility(false); }
    }
  }

  /**
   * @author Roman Mazur (Stanfy - http://www.stanfy.com)
   */
  static interface SupportAware {
    void setSupport(ApiMethodsSupport support);
  }

  /**
   * Provides default behavior.
   * @param <MT> model type
   * @author Roman Mazur - Stanfy (http://www.stanfy.com)
   */
  public abstract static class ApiSupportRequestCallback<MT extends Serializable> extends RequestCallback<MT> implements SupportAware {

    /** Support. */
    private ApiMethodsSupport support;

    @Override
    public void setSupport(final ApiMethodsSupport support) {
      this.support = support;
    }

    /** @return the support */
    public ApiMethodsSupport getSupport() { return support; }

    public abstract boolean filterOperation(final int token, final int o);

    /**
     * Process the connection error. This method is called <b>outside of the main thread</b>.
     * @param operation operation
     * @param token request token
     * @param internalMessage internal message
     */
    protected void processConnectionError(final int token, final int operation, final ResponseData responseData) {
      Log.e(TAG, responseData.getMessage());
      final ApiMethodsSupport support = this.support;
      if (support != null) {
        support.handler.obtainMessage(MSG_CONNECTION_ERROR_INFO, responseData.getMessage()).sendToTarget();
      }
    }

    /**
     * Process the error obtained from the server. This method is called <b>outside of the main thread</b>.
     * @param token request token
     * @param operation operation instance
     * @param code error code
     * @param mesage error message
     */
    protected void processServerError(final int token, final int operation, final ResponseData responseData) {
      Log.e(TAG, "Got an error in server response: " + responseData.getErrorCode() + " / [" + responseData.getMessage() + "]");
      final ApiMethodsSupport support = this.support;
      if (support != null) {
        support.handler.obtainMessage(MSG_SERVER_ERROR_TOAST, responseData.getMessage()).sendToTarget();
      }
    }

    /**
     * Process the result data. This method is called <b>outside of the main thread</b>.
     * @param token request token
     * @param operation operation instance
     * @param server message
     * @param data result data URI
     * @param model instance
     */
    protected abstract void processSuccess(final int token, final int operation, final ResponseData responseData, final MT model);

    protected void processSuccessUnknownModelType(final int token, final int operation, final ResponseData responseData, final Serializable model) { }

    /**
     * This method is called <b>outside of the main thread</b>.
     * @param operation operation instance
     */
    protected void onOperationFinished(final int token, final int operation) {
      if (DEBUG && support != null) {
        Log.d(TAG, operation + " finished, thread " + Thread.currentThread() + ", context " + support.contextRef.get());
      }
    }

    /**
     * This method can be called after the callback was registered in order to notify about running the operation
     * that callback in interested in. Can process incoming GUI actions.
     * @param operation pending operation
     */
    protected void onOperationPending(final int token, final int operation) {
    }

    protected void onError(final int token, final int operation, final ResponseData responseData) {
    }

    protected void onLastOperationSuccess(final int token, final int operation, final ResponseData responseData) {

    }
    protected void onLastOperationError(final int token, final int operation, final ResponseData responseData) {
      switch(responseData.getErrorCode()) {
      case ErrorCodes.ERROR_CODE_CONNECTION:
      case ErrorCodes.ERROR_CODE_SERVER_COMUNICATION:
        processConnectionError(token, operation, responseData);
        break;
      default:
        processServerError(token, operation, responseData);
      }
    }

    private boolean successProcessing(final int token, final int operation, final Uri dataUri) {
      if (!filterOperation(token, operation)) { return false; }
      if (support != null) { support.handler.sendEmptyMessage(MSG_HIDE_PROGRESS); }
      if (DEBUG) { Log.d(TAG, operation + " success, data " + dataUri); }
      return true;
    }

    @Override
    public final synchronized void reportSuccess(final int token, final int operation, final ResponseData responseData, final MT model) {
      if (successProcessing(token, operation, responseData.getData())) {
        processSuccess(token, operation, responseData, model);
      }
      onOperationFinished(token, operation);
    }

    @Override
    public final synchronized void reportSuccessUnknownModelType(final int token, final int operation, final ResponseData responseData, final Serializable model) {
      if (successProcessing(token, operation, responseData.getData())) {
        processSuccessUnknownModelType(token, operation, responseData, model);
      }
      onOperationFinished(token, operation);
    }

    @Override
    public final synchronized void reportError(final int token, final int operation, final ResponseData responseData) {
      if (!filterOperation(token, operation)) { return; }
      if (support != null) { support.handler.sendEmptyMessage(MSG_HIDE_PROGRESS); }
      onError(token, operation, responseData);
      switch (responseData.getErrorCode()) {
      case ErrorCodes.ERROR_CODE_CONNECTION:
      case ErrorCodes.ERROR_CODE_SERVER_COMUNICATION:
        processConnectionError(token, operation, responseData);
        if (DEBUG) { Log.d(TAG, operation + " error, message " + responseData.getMessage()); }
        break;
      default:
        processServerError(token, operation, responseData);
      }
      onOperationFinished(token, operation);
    }

    @Override
    public final synchronized void reportPending(final int token, final int operation) {
      if (!filterOperation(token, operation)) { return; }
      if (support != null) { support.handler.sendEmptyMessage(MSG_SHOW_PROGRESS); }
      if (DEBUG) { Log.d(TAG, "Operation " + operation + " is already pending"); }
      onOperationPending(token, operation);
    }

    @Override
    public final synchronized void reportLastOperation(final int token, final int operation, final ResponseData responseData) {
      if (!support.lastOperationInterest || !filterOperation(token, operation)) { return; }
      if (responseData.getErrorCode() == 0) {
        onLastOperationSuccess(token, operation, responseData);
      } else {
        onLastOperationError(token, operation, responseData);
      }
    }

  }

  /**
   * A request callback for {@link NoModelApiMethodsSupport}.
   * @author Roman Mazur - Stanfy (http://www.stanfy.com)
   */
  public abstract static class NoModelRequestCallback extends ApiSupportRequestCallback<Serializable> {
    @Override
    protected final void processSuccess(final int token, final int operation, final ResponseData responseData, final Serializable model) {
      processSuccess(token, operation, responseData);
    }
    @Override
    protected void processSuccessUnknownModelType(final int token, final int operation, final ResponseData responseData, final Serializable model) {
      processSuccess(token, operation, responseData);
    }

    /**
     * Process the result data. This method is called <b>outside of the main thread</b>.
     * @param operation operation instance
     * @param server message
     * @param data result data URI
     */
    protected abstract void processSuccess(final int token, final int operation, final ResponseData responseData);

    @Override
    public boolean isModelInterest() { return false; }
  }

}
