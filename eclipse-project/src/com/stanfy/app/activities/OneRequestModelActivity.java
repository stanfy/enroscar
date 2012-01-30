package com.stanfy.app.activities;

import java.io.Serializable;

import com.stanfy.app.Application;
import com.stanfy.app.BaseActivity;
import com.stanfy.serverapi.request.RequestBuilder;
import com.stanfy.utils.ApiMethodsSupport;
import com.stanfy.utils.ApiMethodsSupport.ApiSupportRequestCallback;
import com.stanfy.utils.OneRequestModelBehavior;
import com.stanfy.views.StateWindowHelper;

/**
 * Activity that contains a request builder instance and
 * is oriented to processing a particular model type.
 * @param <AT> application type
 * @param <MT> model type
 * @param <RBT> request builder type
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
public abstract class OneRequestModelActivity<AT extends Application, RBT extends RequestBuilder, MT extends Serializable> extends BaseActivity<AT>
    implements OneRequestModelBehavior<RBT, MT> {

  /** Behavior helper. */
  private final OneRequestModelHelper<RBT> helper = new OneRequestModelHelper<RBT>(this);

  @Override
  public int getCurrentRequestToken() { return helper.getCurrentRequestToken(); }
  @Override
  public final RBT getRequestBuilder() { return helper.getRequestBuilder(); }
  @Override
  public ApiSupportRequestCallback<MT> createRequestCallback() { return new ModelRequestCallback<MT>(this); }

  @Override
  public void fetch() { fetch(-1); }
  @Override
  public void fetch(final int token) { helper.fetch(token); }

  @Override
  protected final ApiMethodsSupport createApiMethodsSupport() { return new ApiMethodsSupport(this, createRequestCallback()); }

  @Override
  public StateWindowHelper getStateWindowHelper() { return null; }

}
