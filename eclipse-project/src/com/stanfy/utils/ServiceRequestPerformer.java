package com.stanfy.utils;

import android.content.Context;
import android.util.Log;

import com.stanfy.DebugFlags;
import com.stanfy.app.service.ApiMethodCallback;
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
  }

  @Override
  public int performRequest(final RequestDescription description) {
    bind();
    registerCallback();
    return super.performRequest(description);
  }

  /**
   * Provides default behavior.
   * @author Roman Mazur - Stanfy (http://www.stanfy.com)
   */
  public abstract class RequestCallback implements ApiMethodCallback {

    @Override
    public void reportCancel(final RequestDescription requestDescription, final ResponseData<?> responseData) {
      // nothing
    }
    @Override
    public void reportLastOperation(final int requestId, final ResponseData<?> responseData) {
      // nothing
    }
    @Override
    public void reportPending(final int requestId) {
      // nothing
    }

    @Override
    public final void reportError(final RequestDescription requestDescription, final ResponseData<?> responseData) {
      if (!filterOperation(requestDescription.getId(), requestDescription)) { return; }
      if (DEBUG) {
        Log.d(TAG, "Problems with operation " + requestDescription.getId() + ". Error code: "
            + responseData.getErrorCode() + ". Message: " + responseData.getMessage());
      }
      removeCallback();
      unbind();
      onError(requestDescription, responseData);
    }
    @Override
    public final void reportSuccess(final RequestDescription requestDescription, final ResponseData<?> responseData) {
      if (!filterOperation(requestDescription.getId(), requestDescription)) { return; }
      removeCallback();
      unbind();
      onSuccess(requestDescription, responseData);
    }

    protected abstract boolean filterOperation(final int requestId, final RequestDescription requestDescription);

    protected void onError(final RequestDescription requestDescription, final ResponseData<?> responseData) { /* empty */ }

    protected void onSuccess(final RequestDescription requestDescription, final ResponseData<?> responseData) { /* empty */ }

  }

}
