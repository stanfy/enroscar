package com.stanfy.enroscar.net;

import java.io.IOException;
import java.net.ContentHandler;
import java.net.URLConnection;

import com.stanfy.enroscar.beans.BeansManager;

/**
 * Content handler switcher.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 *
 */
public class ContentHandlerSwitcher extends ContentHandler {

  /**
   * Default implementation for {@link #getContent(URLConnection)}.
   * @param uConn connection instance
   * @return input stream provided by the connection
   * @throws IOException if error happens
   */
  protected Object getContentDefault(final URLConnection uConn) throws IOException {
    return uConn.getInputStream();
  }

  @Override
  public Object getContent(final URLConnection uConn) throws IOException {
    final ContentControlUrlConnection connection = UrlConnectionWrapper.getWrapper(uConn, ContentControlUrlConnection.class);
    if (connection == null) {
      return getContentDefault(uConn);
    }

    final String beanName = connection.getContentHandlerName();
    final ContentHandler handler = BeansManager.get(null).getContainer().getBean(beanName, ContentHandler.class);
    return handler != null ? handler.getContent(uConn) : getContentDefault(uConn);
  }

}
