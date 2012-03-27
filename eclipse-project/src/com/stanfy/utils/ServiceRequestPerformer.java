package com.stanfy.utils;

import java.io.Serializable;

import android.content.Context;
import android.os.RemoteException;
import android.util.Log;

import com.stanfy.DebugFlags;
import com.stanfy.serverapi.request.RequestDescription;
import com.stanfy.serverapi.response.ResponseData;

/**
 * Requests performer for the server side.
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
public class ServiceRequestPerformer extends RequestPerformer  {

  /** Logging tag. */
  private static final String TAG = "ServiceRequestPerformer";

  /** Debug flag. */
  private static final boolean DEBUG = DebugFlags.DEBUG_SERVICES;

  public ServiceRequestPerformer(final Context a, final RequestCallback callback) {
    super(a, callback);
    callback.performer = this;
  }

  @Override
  public void performRequest(final RequestDescription description) {
    bind();
    registerListener();
    super.performRequest(description);
  }

  @Override
  protected void doRequest(final RequestDescription description) {
    try {
      if (DEBUG) { Log.d(TAG, "Perform " + description.getOperationCode()); }
      serviceObject.performRequest(description);
    } catch (final RemoteException e) {
      Log.e(TAG, "Cannot perform operation " + description.getOperationCode(), e);
    }
  }

  /**
   * Provides default behavior.
   * @author Roman Mazur - Stanfy (http://www.stanfy.com)
   */
  public abstract static class RequestCallback extends com.stanfy.serverapi.request.RequestCallback<Serializable> {
    /** Performer instance. */
    ServiceRequestPerformer performer;

    @Override
    public boolean isModelInterest() { return false;  }

    @Override
    public final void reportError(final int token, final int operation, final ResponseData responseData) {
      if (!filterOperation(token, operation)) { return; }
      if (DEBUG) {
        Log.d(TAG, "Problems with operation " + operation + ". Error code: "
            + responseData.getErrorCode() + ". Message: " + responseData.getMessage());
      }
      performer.unbind();
      onError(token, operation, responseData);
    }
    @Override
    public final void reportSuccess(final int token, final int operation, final ResponseData responseData, final Serializable model) {
      if (!filterOperation(token, operation)) { return; }
      performer.unbind();
      onSuccess(token, operation, responseData);
    }
    @Override
    public void reportSuccessUnknownModelType(final int token, final int operation, final ResponseData responseData, final Serializable model) {
      reportSuccess(token, operation, responseData, model);
    }

    protected boolean filterOperation(final int token, final int op) { return false; }

    protected void onError(final int token, final int operation, final ResponseData responseData) { /* empty */ }

    protected void onSuccess(final int token, final int operation, final ResponseData responseData) { /* empty */ }

  }

}
