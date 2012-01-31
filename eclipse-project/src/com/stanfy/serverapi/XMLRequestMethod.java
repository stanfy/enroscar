package com.stanfy.serverapi;

import java.io.InputStream;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import com.stanfy.images.BuffersPool;
import com.stanfy.serverapi.response.ParserContext;
import com.stanfy.serverapi.response.ResponseHanlder;
import com.stanfy.serverapi.response.xml.ElementProcessor.Descriptor;
import com.stanfy.serverapi.response.xml.XMLHandler;

/**
 * Request method with {@link XMLHandler}.
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
public class XMLRequestMethod extends RequestMethod {

  /** Description. */
  private final Descriptor<?> description;

  public XMLRequestMethod(final String cacheAuthority, final BuffersPool buffersPool, final Descriptor<?> description) {
    super(cacheAuthority, buffersPool);
    this.description = description;
  }

  /** Parsers factory. */
  private static XmlPullParserFactory pFactory;

  @Override
  protected ResponseHanlder createResponseHandler(final ParserContext context, final InputStream inputStream) {
    try {
      if (pFactory == null) { pFactory = XmlPullParserFactory.newInstance(); }
      final XmlPullParser parser = pFactory.newPullParser();
      parser.setInput(inputStream, null);
      return new XMLHandler(parser, context, description);
    } catch (final XmlPullParserException e) {
      throw new RuntimeException("Cannot create a pull parser", e);
    }
  }

}
