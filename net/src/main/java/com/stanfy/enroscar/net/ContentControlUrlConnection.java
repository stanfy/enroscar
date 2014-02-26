package com.stanfy.enroscar.net;

import java.io.IOException;
import java.net.ContentHandler;
import java.net.URLConnection;

import com.stanfy.enroscar.rest.EntityTypeToken;


/**
 * URL connection wrapper that delegates {@link #getContent()} to an injected
 * {@link java.net.ContentHandler}. Also contains {@link com.stanfy.enroscar.rest.EntityTypeToken}
 * that might be used by the handler to process the stream.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
public class ContentControlUrlConnection extends UrlConnectionWrapper {

  /** Content handler instance. */
  private final ContentHandler contentHandler;

  /** Model type. */
  private final EntityTypeToken entityType;

  ContentControlUrlConnection(final URLConnection urlConnection,
                              final ContentHandler contentHandler,
                              final EntityTypeToken entityType) {
    super(urlConnection);
    this.contentHandler = contentHandler;
    this.entityType = entityType;
  }

  public static ContentControlUrlConnection from(final URLConnection connection) {
    return UrlConnectionWrapper.getWrapper(connection, ContentControlUrlConnection.class);
  }

  public EntityTypeToken getEntityType() {
    return entityType;
  }

  @Override
  public Object getContent() throws IOException {
    return contentHandler.getContent(this);
  }

}
