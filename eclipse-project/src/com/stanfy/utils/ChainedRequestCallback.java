package com.stanfy.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.BitSet;

import com.stanfy.Destroyable;
import com.stanfy.serverapi.response.ResponseData;
import com.stanfy.utils.ApiMethodsSupport.ApiSupportRequestCallback;

/**
 * Chain of callbacks.
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
public class ChainedRequestCallback extends ApiSupportRequestCallback<Serializable> implements Destroyable {

  /** Callbacks. */
  private final ArrayList<ApiSupportRequestCallback<?>> callbacks = new ArrayList<ApiSupportRequestCallback<?>>();

  /** Callbacks after {@link #filterOperation(int, int)}. */
  private final ArrayList<ApiSupportRequestCallback<?>> filterCallbacks = new ArrayList<ApiSupportRequestCallback<?>>();

  /** Bitset for model interest. */
  private final BitSet modelInterest = new BitSet();

  public void addCallback(final ApiSupportRequestCallback<?> callback) {
    if (callback == null) { return; }
    callback.setSupport(getSupport());
    callbacks.add(callback);
    modelInterest.set(modelInterest.size() - 1);
  }

  public void removeCallback(final ApiSupportRequestCallback<?> callback) {
    if (callback == null) { return; }
    callback.setSupport(null);
    callbacks.remove(callback);
    filterCallbacks.remove(callback);
  }

  @Override
  public void destroy() { callbacks.clear(); }

  @Override
  public Class<?> getModelClass(final int token, final int operation) { return Serializable.class; }

  @Override
  public boolean isModelInterest() { return !modelInterest.isEmpty(); }

  @Override
  public boolean filterOperation(final int token, final int o) {
    final ArrayList<ApiSupportRequestCallback<?>> callbacks = this.callbacks, filterCallbacks = this.filterCallbacks;
    filterCallbacks.clear();
    for (int i = callbacks.size() - 1; i >= 0; i--) {
      final ApiSupportRequestCallback<?> rc = callbacks.get(i);
      if (rc.filterOperation(token, o)) { filterCallbacks.add(rc); }
    }
    return !filterCallbacks.isEmpty();
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void processSuccess(final int token, final int operation, final ResponseData responseData, final Serializable model) {
    final ArrayList<ApiSupportRequestCallback<?>> callbacks = this.filterCallbacks;
    for (int i = callbacks.size() - 1; i >= 0; i--) {
      @SuppressWarnings("rawtypes")
      final ApiSupportRequestCallback rc = callbacks.get(i);
      final Class<?> modelClass = rc.getModelClass(token, operation);
      if (model == null || modelClass == null) {
        rc.processSuccess(token, operation, responseData, null);
      } else {
        if (modelClass.isAssignableFrom(model.getClass())) {
          rc.processSuccess(token, operation, responseData, model);
        } else {
          rc.processSuccessUnknownModelType(token, operation, responseData, model);
        }
      }
    }
  }

  /* XXX this method will not be called indeed */
  @Override
  protected void processSuccessUnknownModelType(final int token, final int operation, final ResponseData responseData, final Serializable model) {
    final ArrayList<ApiSupportRequestCallback<?>> callbacks = this.filterCallbacks;
    for (int i = callbacks.size() - 1; i >= 0; i--) {
      callbacks.get(i).processSuccessUnknownModelType(token, operation, responseData, model);
    }
  }

  @Override
  protected void processServerError(final int token, final int operation, final ResponseData responseData) {
    final ArrayList<ApiSupportRequestCallback<?>> callbacks = this.filterCallbacks;
    for (int i = callbacks.size() - 1; i >= 0; i--) {
      final ApiSupportRequestCallback<?> rc = callbacks.get(i);
      rc.processServerError(token, operation, responseData);
    }
  }

  @Override
  protected void processConnectionError(final int token, final int operation, final ResponseData responseData) {
    final ArrayList<ApiSupportRequestCallback<?>> callbacks = this.filterCallbacks;
    for (int i = callbacks.size() - 1; i >= 0; i--) {
      final ApiSupportRequestCallback<?> rc = callbacks.get(i);
      rc.processConnectionError(token, operation, responseData);
    }
  }

  @Override
  protected void onError(final int token, final int operation, final ResponseData responseData) {
    final ArrayList<ApiSupportRequestCallback<?>> callbacks = this.filterCallbacks;
    for (int i = callbacks.size() - 1; i >= 0; i--) {
      final ApiSupportRequestCallback<?> rc = callbacks.get(i);
      rc.onError(token, operation, responseData);
    }
  }

  @Override
  protected void onLastOperationError(final int token, final int operation, final ResponseData responseData) {
    final ArrayList<ApiSupportRequestCallback<?>> callbacks = this.filterCallbacks;
    for (int i = callbacks.size() - 1; i >= 0; i--) {
      final ApiSupportRequestCallback<?> rc = callbacks.get(i);
      rc.onLastOperationError(token, operation, responseData);
    }
  }

  @Override
  protected void onLastOperationSuccess(final int token, final int operation, final ResponseData responseData) {
    final ArrayList<ApiSupportRequestCallback<?>> callbacks = this.filterCallbacks;
    for (int i = callbacks.size() - 1; i >= 0; i--) {
      final ApiSupportRequestCallback<?> rc = callbacks.get(i);
      rc.onLastOperationSuccess(token, operation, responseData);
    }
  }

  @Override
  protected void onOperationFinished(final int token, final int operation) {
    final ArrayList<ApiSupportRequestCallback<?>> callbacks = this.filterCallbacks;
    for (int i = callbacks.size() - 1; i >= 0; i--) {
      final ApiSupportRequestCallback<?> rc = callbacks.get(i);
      rc.onOperationFinished(token, operation);
    }
  }

  @Override
  protected void onOperationPending(final int token, final int operation) {
    final ArrayList<ApiSupportRequestCallback<?>> callbacks = this.filterCallbacks;
    for (int i = callbacks.size() - 1; i >= 0; i--) {
      final ApiSupportRequestCallback<?> rc = callbacks.get(i);
      rc.onOperationPending(token, operation);
    }
  }

}
