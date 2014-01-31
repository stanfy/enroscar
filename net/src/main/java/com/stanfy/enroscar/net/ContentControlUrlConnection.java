package com.stanfy.enroscar.net;

import java.io.IOException;
import java.net.ContentHandler;
import java.net.URLConnection;

import com.stanfy.enroscar.rest.EntityTypeToken;


/**
 * URL connection wrapper that contains information about what handler should be used
 * to parse the received content. Also contains {@link com.stanfy.enroscar.rest.EntityTypeToken} that might be used
 * by the handler to process the stream.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
public class ContentControlUrlConnection extends UrlConnectionWrapper {

  /** Content handler name. */
  private String contentHandlerName;

  /** Model type. */
  private EntityTypeToken modelType;

  public ContentControlUrlConnection(final URLConnection urlConnection) {
    super(urlConnection);
  }

  public String getContentHandlerName() {
    return contentHandlerName;
  }
  public void setContentHandlerName(final String contentHandlerName) {
    this.contentHandlerName = contentHandlerName;
  }

  public void setModelType(final EntityTypeToken modelType) {
    this.modelType = modelType;
  }
  public EntityTypeToken getModelType() {
    return modelType;
  }

  @Override
  public Object getContent() throws IOException {
    EnroscarConnectionsEngine engine = EnroscarConnectionsEngine.get();
    if (engine == null) {
      throw new IllegalStateException("EnroscarConnectionsEngine seems to be not configured. Have you called EnroscarConnectionsEngine.config().install()?");
    }
    final ContentHandler handler = engine.contentHandlerFactory.createContentHandler("");
    return handler != null ? handler.getContent(this) : super.getContent();
  }

}
