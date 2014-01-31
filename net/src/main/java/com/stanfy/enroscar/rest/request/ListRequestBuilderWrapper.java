package com.stanfy.enroscar.rest.request;

import java.util.List;

import android.content.Context;
import android.support.v4.content.Loader;

import com.stanfy.enroscar.content.loader.ResponseData;
import com.stanfy.enroscar.rest.EntityTypeToken;
import com.stanfy.enroscar.rest.RequestExecutor;
import com.stanfy.enroscar.rest.loader.LoadMoreListLoader;

/**
 * Base request builder to get a list of entities.
 * @author Roman Mazur - Stanfy (http://www.stanfy.com)
 * @param <MT> model type
 * @param <LT> list type
 */
public abstract class ListRequestBuilderWrapper<LT extends List<MT>, MT> implements ListRequestBuilder<LT, MT> {

  /** Standard `offset` parameter name. */
  public static final String PARAM_OFFSET = "offset";
  /** Standard `limit` parameter name. */
  public static final String PARAM_LIMIT = "limit";

  /** Core builder. */
  private final BaseRequestBuilder<LT> core;

  /** Expected type token. */
  private final EntityTypeToken expectedTypeToken;

  /** Parameter name. */
  private String offsetName = PARAM_OFFSET, limitName = PARAM_LIMIT;

  /** Offset value. */
  private ParameterValue offsetValue;
  /** Offset value. */
  private ParameterValue limitValue;

  @SuppressWarnings({ "rawtypes", "unchecked" })
  public ListRequestBuilderWrapper(final BaseRequestBuilder core) {
    this.core = core;
    this.expectedTypeToken = EntityTypeToken.fromClassParameter(getClass());
  }

  @Override
  public ListRequestBuilderWrapper<LT, MT> setLimit(final String limit) {
    if (limitValue == null) {
      limitValue = core.addSimpleParameter(limitName, limit);
    } else {
      limitValue.setValue(limit);
    }
    return this;
  }

  @Override
  public ListRequestBuilderWrapper<LT, MT> setOffset(final String offset) {
    if (offsetValue == null) {
      offsetValue = core.addSimpleParameter(offsetName, offset);
    } else {
      offsetValue.setValue(offset);
    }
    return this;
  }

  public ListRequestBuilderWrapper<LT, MT> setOffset(final int offset) {
    return setOffset(String.valueOf(offset));
  }

  public ListRequestBuilderWrapper<LT, MT> setLimit(final int offset) {
    return setLimit(String.valueOf(offset));
  }

  /** @param offsetName `offset` parameter name */
  public void setOffsetParamName(final String offsetName) {
    this.offsetName = offsetName;
  }
  /** @param limitName `limit` parameter name */
  public void setLimitParamName(final String limitName) {
    this.limitName = limitName;
  }

  /** @return core request builder */
  public RequestBuilder<LT> getCore() { return core; }

  @Override
  public Context getContext() { return core.getContext(); }

  @Override
  public int execute() { return core.execute(); }

  @Override
  public ListRequestBuilderWrapper<LT, MT> setExecutor(final RequestExecutor executor) {
    core.setExecutor(executor);
    return this;
  }

  @Override
  public String getOffset() { return offsetValue != null ? offsetValue.getValue() : "0"; }

  @Override
  public String getLimit() { return limitValue != null ? limitValue.getValue() : "0"; }

  @Override
  public Loader<ResponseData<LT>> getLoader() {
    return new LoadMoreListLoader<MT, LT>(this);
  }

  @Override
  public EntityTypeToken getExpectedModelType() { return expectedTypeToken; }

  @Override
  public String toString() {
    return "wrapper-" + getClass().getName() + "(" + core + ")";
  }

}
