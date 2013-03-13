/**
 *
 */
package com.stanfy.enroscar.rest.response.handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLConnection;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import com.google.gson.GsonBuilder;
import com.stanfy.enroscar.beans.BeansContainer;
import com.stanfy.enroscar.beans.EnroscarBean;
import com.stanfy.enroscar.utils.ModelTypeToken;
import com.stanfy.gsonxml.GsonXml;
import com.stanfy.gsonxml.GsonXmlBuilder;
import com.stanfy.gsonxml.XmlParserCreator;


/**
 * Content handler that uses {@link GsonXml}.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
@EnroscarBean(XmlGsonContentHandler.BEAN_NAME)
public class XmlGsonContentHandler extends BaseContentHandler {

  /** Response handler. */
  public static final String BEAN_NAME = "XmlGsonContentHandler";

  /** Parser factory. */
  protected static final XmlParserCreator PARSER_FACTORY = new XmlParserCreator() {
    @Override
    public XmlPullParser createParser() {
      try {
        return XmlPullParserFactory.newInstance().newPullParser();
      } catch (final XmlPullParserException e) {
        throw new RuntimeException(e);
      }
    }
  };

  /** GsonXml instance. */
  private GsonXml gsonXml;

  /**
   * @return {@link GsonXml} instance for parsing XML
   */
  protected GsonXml createGsonXml() {
    GsonBuilder gsonBuilder = new GsonBuilder().setDateFormat(DEFAULT_DATE_FORMAT);
    return new GsonXmlBuilder().setXmlParserCreator(PARSER_FACTORY).wrap(gsonBuilder).setSameNameLists(true).create();
  }

  @Override
  protected Object getContent(final URLConnection connection, final InputStream source, final ModelTypeToken modelType) throws IOException {
    if (gsonXml == null) {
      throw new IllegalStateException("Gson object is not created");
    }
    return gsonXml.fromXml(new InputStreamReader(source, getCharset()), getModelType(modelType));
  }

  @Override
  public void onInitializationFinished(final BeansContainer beansContainer) {
    super.onInitializationFinished(beansContainer);
    this.gsonXml = createGsonXml();
  }

}
