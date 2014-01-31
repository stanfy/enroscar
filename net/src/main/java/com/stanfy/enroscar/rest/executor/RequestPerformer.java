package com.stanfy.enroscar.rest.executor;

import java.util.ArrayList;

import android.content.ComponentName;
import android.content.Context;
import android.os.IBinder;
import android.util.Log;

import com.stanfy.enroscar.net.operation.executor.RequestExecutor;
import com.stanfy.enroscar.rest.executor.ApplicationService.ApiMethodsBinder;
import com.stanfy.enroscar.net.operation.RequestDescription;

/**
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
public abstract class RequestPerformer extends ApplicationServiceSupport<ApiMethods> implements RequestExecutor {

  /** Logging tag. */
  private static final String TAG = "RequestPerformer";
  /** Debug flag. */
  private static final boolean DEBUG = DebugFlags.DEBUG;

  /** Callback registered flag. */
  boolean registered = false;
  /** Callback. */
  private final ApiMethodCallback callback;

  /** Last descriptions. */
  private final ArrayList<RequestDescription> lastDescriptions = new ArrayList<RequestDescription>();
  /** Last cancel IDs. */
  private final ArrayList<Integer> lastCancelIds = new ArrayList<Integer>();
  /** Saved callback operation. */
  private boolean addCallbackRequest = false, removeCallbackRequest = false;

  public RequestPerformer(final Context a, final ApiMethodCallback callback) {
    super(a);
    this.callback = callback;
  }

  @Override
  protected Class<ApiMethods> getInterfaceClass() { return ApiMethods.class; }

  public boolean isRegistered() { return registered; }

  public ApiMethodCallback getCallback() { return callback; }

  /** Register operation callback. */
  public void registerCallback() {
    if (registered) { return; }
    if (serviceObject != null) {
      if (DEBUG) { Log.d(TAG, "Registering callback " + callback); }
      serviceObject.registerCallback(callback);
      registered = true;
      addCallbackRequest = false;
    } else {
      if (DEBUG) { Log.d(TAG, "Save add callback request"); }
      addCallbackRequest = true;
    }
  }
  /** Remove operation callback. */
  public void removeCallback() {
    if (!registered) { return; }
    if (serviceObject != null) {
      if (DEBUG) { Log.d(TAG, "Removing callback"); }
      serviceObject.removeCallback(callback);
      registered = false;
      removeCallbackRequest = false;
    } else {
      if (DEBUG) { Log.d(TAG, "Save remove callback request"); }
      removeCallbackRequest = true;
    }
  }

  /**
   * Send request to the service.
   * @param description request description instance
   */
  protected void doRequest(final RequestDescription description) {
    if (DEBUG) { Log.d(TAG, "Perform " + description.getId()); }
    serviceObject.performRequest(description);
  }

  /**
   * Perform the request.
   * @param description request description
   */
  @Override
  public int performRequest(final RequestDescription description) {
    if (serviceObject != null) {
      if (DEBUG) { Log.d(TAG, "Call id= " + description.getId() + " " + serviceObject); }
      doRequest(description);
    } else {
      if (DEBUG) { Log.d(TAG, "Save last description " + description.getId() + "(" + serviceObject + "," + registered + ")"); }
      lastDescriptions.add(description);
    }
    return description.getId();
  }

  public boolean cancelRequest(final int requestId) {
    if (serviceObject != null) {
      if (DEBUG) { Log.d(TAG, "Cancel " + requestId); }
      return serviceObject.cancelRequest(requestId);
    } else {
      if (!lastDescriptions.isEmpty()) {
        final boolean removed = lastDescriptions.remove(new RequestDescription(requestId));
        if (removed) {
          if (DEBUG) { Log.d(TAG, "Remove from last descriptions " + requestId); }
          return false;
        }
      }
      if (DEBUG) { Log.d(TAG, "Save last cancel " + requestId); }
      lastCancelIds.add(requestId);
      return false;
    }
  }

  @Override
  public void onServiceConnected(final ComponentName name, final IBinder service) {
    serviceObject = ((ApiMethodsBinder)service).getApiMethods();
    if (DEBUG) { Log.d(TAG, "apiMethods = " + serviceObject + " thread " + Thread.currentThread()); }

    final boolean callbackOperation = addCallbackRequest ^ removeCallbackRequest;
    if (callbackOperation) {
      if (addCallbackRequest) {
        registerCallback();
      } else {
        removeCallback();
      }
    }

    final ArrayList<RequestDescription> lastDescriptions = this.lastDescriptions;
    final int rCount = lastDescriptions.size();
    if (rCount > 0) {
      for (int i = 0; i < rCount; i++) {
        performRequest(lastDescriptions.get(i));
      }
      lastDescriptions.clear();
    }

    final ArrayList<Integer> lastCancels = this.lastCancelIds;
    final int cCount = lastCancels.size();
    if (cCount > 0) {
      for (int i = 0; i < cCount; i++) {
        cancelRequest(lastCancels.get(i));
      }
      lastCancels.clear();
    }
  }

  @Override
  public void onServiceDisconnected(final ComponentName name) {
    if (DEBUG) { Log.d(TAG, "apiMethods = " + serviceObject + " thread " + Thread.currentThread()); }
    // service disconnected => listener is removed, we'll have to register it again
    registered = false;
  }

}
