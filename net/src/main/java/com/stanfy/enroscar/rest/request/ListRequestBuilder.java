package com.stanfy.enroscar.rest.request;

import java.util.List;

/**
 * List request builder.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 * @param <MT> model type
 * @param <LT> list type
 */
public interface ListRequestBuilder<LT extends List<MT>, MT> extends RequestBuilder<LT> {

  ListRequestBuilder<LT, MT> setOffset(final String offset);

  ListRequestBuilder<LT, MT> setLimit(final String limit);

  String getOffset();

  String getLimit();

}
