package com.stanfy.utils;

import java.io.Serializable;
import java.util.ArrayList;

import android.content.ComponentName;
import android.content.Context;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.stanfy.DebugFlags;
import com.stanfy.app.service.ApiMethodCallback;
import com.stanfy.app.service.ApiMethods;
import com.stanfy.serverapi.request.RequestCallback;
import com.stanfy.serverapi.request.RequestDescription;
import com.stanfy.serverapi.request.RequestExecutor;
import com.stanfy.serverapi.response.ResponseData;

/**
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
public abstract class RequestPerformer extends ApplicationServiceSupport<ApiMethods> implements RequestExecutor {

  /** Logging tag. */
  private static final String TAG = "RequestPerformer";
  /** Debug flag. */
  private static final boolean DEBUG = DebugFlags.DEBUG_API;

  /** Callback registered flag. */
  boolean registered = false;
  /** Callback. */
  final RemoteCallback callback;

  /** Last descriptions. */
  private final ArrayList<RequestDescription> lastDescriptions = new ArrayList<RequestDescription>();

  public RequestPerformer(final Context a, final RequestCallback<?> callback) {
    super(a);
    this.callback = callback != null ? new RemoteCallback(callback, "RC-" + a.getClass().getSimpleName()) : null;
  }

  @Override
  protected Class<ApiMethods> getInterfaceClass() { return ApiMethods.class; }

  public boolean isRegistered() { return registered; }

  /** Register operation callback. */
  public void registerListener() {
    if (serviceObject != null && !registered) {
      final RemoteCallback callback = this.callback;
      if (callback == null) { return; }
      if (DEBUG) { Log.d(TAG, "Registering callback " + callback); }
      try {
        serviceObject.registerCallback(callback, callback.core.isModelInterest());
        registered = true;
      } catch (final RemoteException e) {
        Log.e(TAG, "Cannot register callback", e);
      }
    }
  }
  /** Remove operation callback. */
  public void removeListener() {
    if (serviceObject != null && registered) {
      if (DEBUG) { Log.d(TAG, "Removing callback"); }
      try {
        serviceObject.removeCallback(callback);
        registered = false;
      } catch (final RemoteException e) {
        Log.e(TAG, "Cannot remove callback", e);
      }
    }
  }

  protected abstract void doRequest(final RequestDescription description);

  /**
   * Perform the request.
   * @param description request description
   */
  @Override
  public int performRequest(final RequestDescription description) {
    if (serviceObject != null) {
      if (DEBUG) { Log.d(TAG, "Call " + description.getOperationCode() + " " + serviceObject); }
      doRequest(description);
    } else {
      if (DEBUG) { Log.d(TAG, "Save last description " + description.getOperationCode() + "(" + serviceObject + "," + registered + ")"); }
      lastDescriptions.add(description);
    }
    return description.getId();
  }

  @Override
  public void onServiceConnected(final ComponentName name, final IBinder service) {
    serviceObject = ApiMethods.Stub.asInterface(service);
    if (DEBUG) { Log.d(TAG, "apiMethods = " + serviceObject + " thread " + Thread.currentThread()); }
    if (!registered) { registerListener(); }
    final ArrayList<RequestDescription> lastDescriptions = this.lastDescriptions;
    final int rCount = lastDescriptions.size();
    if (rCount > 0) {
      for (int i = 0; i < rCount; i++) {
        performRequest(lastDescriptions.get(i));
      }
      lastDescriptions.clear();
    }
  }

  @Override
  public void onServiceDisconnected(final ComponentName name) {
    if (DEBUG) { Log.d(TAG, "apiMethods = " + serviceObject + " thread " + Thread.currentThread()); }
    registered = false;
  }

  @Override
  public void unbind() {
    removeListener();
    if (DEBUG) { Log.d(TAG, "Unbind from service"); }
    super.unbind();
  }

  /** Remote callback. */
  static class RemoteCallback extends ApiMethodCallback.Stub {

    /** Core. */
    final RequestCallback<?> core;

    /** Name. */
    private final String name;

    public RemoteCallback(final RequestCallback<?> c, final String name) {
      core = c;
      this.name = name;
    }

    static void deliverSuccess(final RequestCallback<?> callback, final int token, final int operation, final ResponseData responseData, final Serializable model) {
      final Class<?> clazz = callback.getModelClass(token, operation);
      if (model != null && clazz != null) {
        if (clazz.isAssignableFrom(model.getClass())) {
          callback.castAndReportSuccess(token, operation, responseData, model);
        } else {
          callback.reportSuccessUnknownModelType(token, operation, responseData, model);
        }
      } else {
        callback.reportSuccess(token, operation, responseData, null);
      }
    }

    @Override
    public void reportSuccess(final int token, final int operation, final ResponseData responseData) throws RemoteException {
      if (responseData == null) { throw new IllegalArgumentException("Passing null response data to callback!"); }
      deliverSuccess(core, token, operation, responseData, responseData.getModel());
    }

    @Override
    public void reportError(final int token, final int operation, final ResponseData responseData) throws RemoteException {
      core.reportError(token, operation, responseData);
    }

    @Override
    public void reportPending(final int token, final int operation) throws RemoteException {
      core.reportPending(token, operation);
    }

    @Override
    public void reportLastOperation(final int token, final int operation, final ResponseData responseData) throws RemoteException {
      core.reportLastOperation(token, operation, responseData);
    }

    @Override
    public String toString() { return name; }

    @Override
    public void reportCancel(final int token, final int operation) throws RemoteException {
      // TODO implement onCancel
    }

  }

}
