/**
 *
 */
package com.stanfy.serverapi.response.handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLConnection;

import com.google.gson.GsonXml;
import com.google.gson.GsonXmlBuilder;
import com.stanfy.app.beans.EnroscarBean;
import com.stanfy.serverapi.response.ModelTypeToken;


/**
 * Content handler that uses {@link GsonXml}.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
@EnroscarBean(XmlGsonContentHandler.BEAN_NAME)
public class XmlGsonContentHandler extends BaseContentHandler {

  /** Response handler. */
  public static final String BEAN_NAME = "XmlGsonContentHandler";

  /** GsonXml instance. */
  private GsonXml gsonXml;

  protected GsonXml createGsonXml() {
    return new GsonXmlBuilder().setDateFormat(DEFAULT_DATE_FORMAT).create();
  }

  @Override
  protected Object getContent(final URLConnection connection, final InputStream source, final ModelTypeToken modelType) throws IOException {
    if (gsonXml == null) {
      throw new IllegalStateException("Gson object is not created");
    }
    return gsonXml.fromXml(new InputStreamReader(source, getCharset()), getModelType(modelType));
  }

  @Override
  public void onInititializationFinished() {
    super.onInititializationFinished();
    this.gsonXml = createGsonXml();
  }

}
