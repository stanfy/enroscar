package com.stanfy.serverapi.request;


import android.content.Context;

/**
 * Base request builder to get a list of entities.
 * @author Roman Mazur - Stanfy (http://www.stanfy.com)
 */
public abstract class ListRequestBuilder extends RequestBuilder {

  /** Standard `offset` parameter name. */
  public static final String PARAM_OFFSET = "offset";
  /** Standard `limit` parameter name. */
  public static final String PARAM_LIMIT = "limit";

  /** Offset value. */
  private ParameterValue offsetValue;

  public ListRequestBuilder(final Context context, final RequestExecutor executor) {
    super(context, executor);
  }

  public ListRequestBuilder setLimit(final int limit) {
    addSimpleParameter(getLimitParamName(), String.valueOf(limit));
    return this;
  }

  public ListRequestBuilder setOffset(final int offset) {
    final String value = String.valueOf(offset);
    if (offsetValue == null) {
      final ParameterValue offsetValue = new ParameterValue();
      offsetValue.setName(getOffsetParamName());
      offsetValue.setValue(value);
      getResult().getSimpleParameters().getChildren().add(offsetValue);
      this.offsetValue = offsetValue;
    } else {
      offsetValue.setValue(value);
    }
    return this;
  }

  /** @return `offset` parameter name */
  public String getOffsetParamName() { return PARAM_OFFSET; }
  /** @return `limit` parameter name */
  public String getLimitParamName() { return PARAM_LIMIT; }

  @Override
  public void clear() {
    super.clear();
    offsetValue = null;
  }

}
