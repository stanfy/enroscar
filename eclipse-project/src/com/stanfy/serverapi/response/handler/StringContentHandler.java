package com.stanfy.serverapi.response.handler;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;

import com.stanfy.enroscar.beans.EnroscarBean;
import com.stanfy.enroscar.utils.ModelTypeToken;
import com.stanfy.io.IoUtils;


/**
 * String content handler.
 */
@EnroscarBean(StringContentHandler.BEAN_NAME)
public class StringContentHandler extends BaseContentHandler {

  /** Bean name. */
  public static final String BEAN_NAME = "StringContentHandler";

  @Override
  protected String getContent(final URLConnection connection, final InputStream source, final ModelTypeToken modelType) throws IOException {
    return IoUtils.streamToString(source);
  }

}
