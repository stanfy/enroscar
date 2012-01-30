package com.stanfy.app.fragments;

import java.io.Serializable;

import android.app.Activity;

import com.stanfy.app.Application;
import com.stanfy.app.BaseFragment;
import com.stanfy.serverapi.request.RequestBuilder;
import com.stanfy.utils.ApiMethodsSupport.ApiSupportRequestCallback;
import com.stanfy.utils.OneRequestModelBehavior;
import com.stanfy.views.StateWindowHelper;

/**
 * Fragment that contains a request builder instance and is oriented on processing
 * a particular model type.
 * @param <AT> application type
 * @param <RBT> request builder type
 * @param <MT> model type
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
public abstract class OneRequestModelFragment<AT extends Application, RBT extends RequestBuilder, MT extends Serializable> extends BaseFragment<AT>
    implements OneRequestModelBehavior<RBT, MT> {

  /** Behavior helper. */
  private final OneRequestModelHelper<RBT> helper = new OneRequestModelHelper<RBT>(this);

  /** Request callback. */
  private ApiSupportRequestCallback<MT> requestCallback;

  @Override
  public void onAttach(final Activity activity) {
    super.onAttach(activity);
    requestCallback = createRequestCallback();
  }

  @Override
  public void onStart() {
    super.onStart();
    getOwnerActivity().addRequestCallback(requestCallback);
  }

  @Override
  public void onStop() {
    super.onStop();
    getOwnerActivity().removeRequestCallback(requestCallback);
  }

  @Override
  public void onDetach() {
    super.onDetach();
    helper.onDetach();
    requestCallback = null;
  }

  @Override
  public RBT getRequestBuilder() { return helper.getRequestBuilder(); }
  @Override
  public int getCurrentRequestToken() { return helper.getCurrentRequestToken(); }
  @Override
  public ApiSupportRequestCallback<MT> createRequestCallback() { return new ModelRequestCallback<MT>(this); }

  @Override
  public void fetch() { fetch(-1); }
  @Override
  public void fetch(final int token) { helper.fetch(token); }

  @Override
  public StateWindowHelper getStateWindowHelper() { return null; }

}
