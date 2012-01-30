package com.stanfy.serverapi.response;

import com.stanfy.serverapi.RequestMethod.RequestMethodException;

/**
 * @author Roman Mazur (mailto: mazur.roman@gmail.com)
 */
public abstract class ResponseHanlder {

  /** Context. */
  private final ParserContext context;

  /** @return string with dumped state used for debugging */
  public abstract String dumpState();

  public ResponseHanlder(final ParserContext context) {
    this.context = context;
  }

  public ParserContext getContext() { return context; }

  public abstract void handleResponse() throws RequestMethodException;

}
