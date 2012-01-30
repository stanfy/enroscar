package com.stanfy.utils;

import java.io.Serializable;

import com.stanfy.serverapi.request.RequestBuilder;
import com.stanfy.serverapi.response.ResponseData;
import com.stanfy.utils.ApiMethodsSupport.ApiSupportRequestCallback;
import com.stanfy.views.StateWindowHelper;

/**
 * Methods of activities and fragments that are oriented to operating with one request/model.
 * @param <RBT> request builder type
 * @param <MT> model type
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
public interface OneRequestModelBehavior<RBT extends RequestBuilder, MT extends Serializable> {

  /**
   * Execute an operation described with a request builder.
   */
  void fetch();
  /**
   * Execute an operation described with a request builder.
   * @param token token value to identify the request
   */
  void fetch(final int token);

  /** @return the requestBuilder */
  RBT getRequestBuilder();
  /** @return new request builder instance used to get the model instance */
  RBT createRequestBuilder();

  /** @return model class */
  Class<?> getModelClass();

  /** @return request callback instance */
  ApiSupportRequestCallback<MT> createRequestCallback();

  /**
   * Process response results. Called in GUI thread.
   * @param data response date
   * @return true - if data successfully processed and false otherwise
   */
  boolean processModel(MT data);

  /**
   * @return state window helper to operate with progress bar view during request performance
   */
  StateWindowHelper getStateWindowHelper();

  /** @return current request token. */
  int getCurrentRequestToken();

  /**
   * @param work runnable to execute in UI thread
   */
  void runOnUiThread(final Runnable work);


  /**
   * @param <RBT> request builder type
   * @author Roman Mazur (Stanfy - http://www.stanfy.com)
   */
  public static final class OneRequestModelHelper<RBT extends RequestBuilder> {
    /** Behavior instance. */
    private final OneRequestModelBehavior<RBT, ?> behavior;
    /** Current request token. */
    private int currentRequestToken;
    /** Request builder. */
    private RBT requestBuilder;
    public OneRequestModelHelper(final OneRequestModelBehavior<RBT, ?> behavior) { this.behavior = behavior; }
    public void fetch(final int token) {
      final StateWindowHelper stateHelper = behavior.getStateWindowHelper();
      if (stateHelper != null) { stateHelper.showProgress(); }
      currentRequestToken = token;
      requestBuilder = behavior.createRequestBuilder();
      requestBuilder.execute(token);
    }
    /** @return the currentRequestToken */
    public int getCurrentRequestToken() { return currentRequestToken; }
    /** @return the requestBuilder */
    public RBT getRequestBuilder() { return requestBuilder; }
    public void onDetach() { this.requestBuilder = null; }
  }

  /**
   * Request callback for components that implement {@link OneRequestModelBehavior}.
   * @author Roman Mazur (Stanfy - http://www.stanfy.com)
   */
  public static class ModelRequestCallback<MT extends Serializable> extends ApiSupportRequestCallback<MT> {
    /** Behavior instance. */
    private final OneRequestModelBehavior<?, MT> behavior;

    public ModelRequestCallback(final OneRequestModelBehavior<?, MT> behavior) { this.behavior = behavior; }

    @Override
    public boolean filterOperation(final int token, final int o) {
      final RequestBuilder rb = behavior.getRequestBuilder();
      if (rb == null) { return false; }
      return behavior.getCurrentRequestToken() == token && rb.checkOperation(o);
    }
    @Override
    protected void processSuccess(final int token, final int operation, final ResponseData responseData, final MT model) {
      behavior.runOnUiThread(new Runnable() {
        @Override
        public void run() {
          behavior.processModel(model);
          final StateWindowHelper stateHelper = behavior.getStateWindowHelper();
          if (stateHelper != null) { stateHelper.showMain(); }
        }
      });
    }
    @Override
    public Class<?> getModelClass(final int token, final int operation) { return behavior.getModelClass(); }

    protected OneRequestModelBehavior<?, MT> getBehavior() { return behavior; }
  }

}
