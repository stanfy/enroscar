package com.stanfy.serverapi;

import java.io.InputStream;

import com.stanfy.images.BuffersPool;
import com.stanfy.serverapi.response.ParserContext;
import com.stanfy.serverapi.response.ResponseHanlder;
import com.stanfy.serverapi.response.json.GsonBasedResponseHandler;

/**
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
public class JSONRequestMethod extends RequestMethod {

  public JSONRequestMethod(final String cacheAuthority, final BuffersPool buffersPool) {
    super(cacheAuthority, buffersPool);
  }

  @Override
  protected ResponseHanlder createResponseHandler(final ParserContext context, final InputStream inputStream) {
    return new GsonBasedResponseHandler(inputStream, context);
  }

}
