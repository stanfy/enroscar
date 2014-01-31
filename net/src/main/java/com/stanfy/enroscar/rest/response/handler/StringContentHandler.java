package com.stanfy.enroscar.rest.response.handler;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;

import android.content.Context;

import com.stanfy.enroscar.beans.EnroscarBean;
import com.stanfy.enroscar.io.IoUtils;
import com.stanfy.enroscar.rest.EntityTypeToken;


/**
 * String content handler.
 */
@EnroscarBean(value = StringContentHandler.BEAN_NAME, contextDependent = true)
public class StringContentHandler extends BaseContentHandler {

  /** Bean name. */
  public static final String BEAN_NAME = "StringContentHandler";

  public StringContentHandler(final Context context) {
    super(context);
  }
  
  @Override
  protected String getContent(final URLConnection connection, final InputStream source, final EntityTypeToken modelType) throws IOException {
    return IoUtils.streamToString(source, getBuffersPool());
  }

}
