package com.stanfy.enroscar.rest.executor;

import java.util.ArrayList;

import com.stanfy.enroscar.content.loader.ResponseData;
import com.stanfy.enroscar.rest.executor.ApiMethodsSupport.ApiSupportRequestCallback;
import com.stanfy.enroscar.net.operation.RequestDescription;

/**
 * Chain of callbacks.
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
public class ChainedRequestCallback extends ApiSupportRequestCallback {

  /** Callbacks. */
  private final ArrayList<ApiSupportRequestCallback> callbacks = new ArrayList<ApiSupportRequestCallback>();

  /** Callbacks after {@link #filterOperation(int, int)}. */
  private final ThreadLocal<ArrayList<ApiSupportRequestCallback>> filterCallbacks = new ThreadLocal<ArrayList<ApiSupportRequestCallback>>();

  public void addCallback(final ApiSupportRequestCallback callback) {
    if (callback == null) { return; }
    callback.setSupport(getSupport());
    callbacks.add(callback);
  }

  public void removeCallback(final ApiSupportRequestCallback callback) {
    if (callback == null) { return; }
    callback.setSupport(null);
    callbacks.remove(callback);
    final ArrayList<ApiSupportRequestCallback> filterCallbacks = this.filterCallbacks.get();
    if (filterCallbacks != null) {
      filterCallbacks.remove(callback);
    }
  }


  public ArrayList<ApiSupportRequestCallback> getFilterCallbacks() {
    ArrayList<ApiSupportRequestCallback> callbacks = this.filterCallbacks.get();
    if (callbacks == null) {
      callbacks = new ArrayList<ApiMethodsSupport.ApiSupportRequestCallback>();
      this.filterCallbacks.set(callbacks);
    }
    return callbacks;
  }

  @Override
  public boolean filterOperation(final int requestId, final RequestDescription requestDescription) {
    final ArrayList<ApiSupportRequestCallback> callbacks = this.callbacks, filterCallbacks = getFilterCallbacks();
    filterCallbacks.clear();
    for (int i = callbacks.size() - 1; i >= 0; i--) {
      final ApiSupportRequestCallback rc = callbacks.get(i);
      if (rc.filterOperation(requestId, requestDescription)) { filterCallbacks.add(rc); }
    }
    return !filterCallbacks.isEmpty();
  }

  @Override
  protected void processSuccess(final RequestDescription requestDescription, final ResponseData<?> responseData) {
    final ArrayList<ApiSupportRequestCallback> callbacks = getFilterCallbacks();
    for (int i = callbacks.size() - 1; i >= 0; i--) {
      final ApiSupportRequestCallback rc = callbacks.get(i);
      rc.processSuccess(requestDescription, responseData);
    }
  }

  @Override
  protected void processServerError(final RequestDescription requestDescription, final ResponseData<?> responseData) {
    final ArrayList<ApiSupportRequestCallback> callbacks = getFilterCallbacks();
    for (int i = callbacks.size() - 1; i >= 0; i--) {
      final ApiSupportRequestCallback rc = callbacks.get(i);
      rc.processServerError(requestDescription, responseData);
    }
  }

  @Override
  protected void processConnectionError(final RequestDescription requestDescription, final ResponseData<?> responseData) {
    final ArrayList<ApiSupportRequestCallback> callbacks = getFilterCallbacks();
    for (int i = callbacks.size() - 1; i >= 0; i--) {
      final ApiSupportRequestCallback rc = callbacks.get(i);
      rc.processConnectionError(requestDescription, responseData);
    }
  }

  @Override
  protected void onError(final RequestDescription requestDescription, final ResponseData<?> responseData) {
    final ArrayList<ApiSupportRequestCallback> callbacks = getFilterCallbacks();
    for (int i = callbacks.size() - 1; i >= 0; i--) {
      final ApiSupportRequestCallback rc = callbacks.get(i);
      rc.onError(requestDescription, responseData);
    }
  }

  @Override
  protected void onOperationFinished(final RequestDescription requestDescription) {
    final ArrayList<ApiSupportRequestCallback> callbacks = getFilterCallbacks();
    for (int i = callbacks.size() - 1; i >= 0; i--) {
      final ApiSupportRequestCallback rc = callbacks.get(i);
      rc.onOperationFinished(requestDescription);
    }
  }

  @Override
  public void onCancel(final RequestDescription requestDescription, final ResponseData<?> responseData) {
    final ArrayList<ApiSupportRequestCallback> callbacks = getFilterCallbacks();
    for (int i = callbacks.size() - 1; i >= 0; i--) {
      final ApiSupportRequestCallback rc = callbacks.get(i);
      rc.onCancel(requestDescription, responseData);
    }
  }

}
