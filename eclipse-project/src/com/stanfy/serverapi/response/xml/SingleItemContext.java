package com.stanfy.serverapi.response.xml;

import java.io.Serializable;

import com.stanfy.serverapi.response.ParserContext;

/**
 * One item context.
 * @param <T> item type
 * @author Olexandr Tereshchuk - Stanfy (http://www.stanfy.com)
 */
public class SingleItemContext<T extends Serializable> extends ParserContext {
  
  /** Data. */
  private T item = null;

  @SuppressWarnings("unchecked")
  @Override
  public void postData(final Object data) {
    item = (T)data;
  }
  
  @Override
  public T getModel() { return item; }
  
}
