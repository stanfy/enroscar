package com.stanfy.serverapi.request;

import java.util.List;

/**
 * List request builder.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 * @param <MT> model type
 * @param <LT> list type
 */
public interface ListRequestBuilder<LT extends List<MT>, MT> extends RequestBuilder<LT> {

  ListRequestBuilder<LT, MT> setOffset(final int offset);

  ListRequestBuilder<LT, MT> setLimit(final int limit);

  int getOffset();

  int getLimit();

}
