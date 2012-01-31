package com.stanfy.serverapi.response.xml;

import static org.xmlpull.v1.XmlPullParser.END_DOCUMENT;
import static org.xmlpull.v1.XmlPullParser.END_TAG;
import static org.xmlpull.v1.XmlPullParser.START_TAG;
import static org.xmlpull.v1.XmlPullParser.TEXT;

import java.io.IOException;
import java.util.LinkedList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Log;

import com.stanfy.DebugFlags;
import com.stanfy.serverapi.RequestMethod.RequestMethodException;
import com.stanfy.serverapi.response.ParserContext;
import com.stanfy.serverapi.response.ParserContext.SimpleResultHandler;
import com.stanfy.serverapi.response.ResponseHanlder;
import com.stanfy.serverapi.response.xml.ElementProcessor.Descriptor;

/**
 * XML response handler.
 * @author Roman Mazur - Stanfy (http://www.stanfy.com)
 */
public class XMLHandler extends ResponseHanlder {

  /** Logging tag. */
  private static final String TAG = "XMLHandler";

  /** Debug flag. */
  private static final boolean DEBUG = DebugFlags.DEBUG_PARSER;

  /** Parser instance. */
  private final XmlPullParser parser;

  /** Processors. */
  private LinkedList<Descriptor<?>> descriptors;
  /** Last tag instance. */
  private String lastTag;
  /** Current text. */
  private StringBuilder currentText;
  /** Processors. */
  private LinkedList<ElementProcessor> processors;

  /** Root descriptor. */
  private final Descriptor<?> rootDescriptor;

  public XMLHandler(final XmlPullParser parser, final ParserContext context, final Descriptor<?> rootDescriptor) {
    super(context);
    this.parser = parser;
    this.rootDescriptor = rootDescriptor;
  }

  private Descriptor<?> getCurrentDescriptor() { return descriptors.isEmpty() ? null : descriptors.getLast(); }
  private ElementProcessor getCurrentElementProcessor() { return processors.isEmpty() ? null : processors.getLast(); }

  @Override
  public String dumpState() {
    return "lastTag=<" + lastTag + ">, currentDescriptor=" + getCurrentDescriptor() + ", processors=" + processors
        + ", currentText='" + currentText + "', currentProcessor=" + getCurrentElementProcessor();
  }

  private void initializeRoot() {
    final int minTextSize = 500;
    currentText = new StringBuilder(minTextSize);
    processors = new LinkedList<ElementProcessor>();
    descriptors = new LinkedList<Descriptor<?>>();
    descriptors.addLast(rootDescriptor);
  }

  private void runProcessor() {
    final XmlPullParser parser = this.parser;
    final ElementProcessor p = getCurrentDescriptor().createProcessor(), last = getCurrentElementProcessor();
    p.setContext(last == null ? getContext() : last.childContext());
    p.setDepth(parser.getDepth());
    p.processAttributes(parser);
    processors.addLast(p);
  }

  private void stopProcessor() {
    processors.removeLast().syncData();
  }

  @Override
  public void handleResponse() throws RequestMethodException {
    try {
      initializeRoot();
      final XmlPullParser parser = this.parser;
      final StringBuilder currentText = this.currentText;
      int type = parser.next();
      Descriptor<?> currentDescriptor = null;
      while (type != END_DOCUMENT) {
        currentDescriptor = getCurrentDescriptor();
        if (DEBUG) { Log.d(TAG, "lastTag=<" + lastTag + ">, " + currentText + ", " + currentDescriptor + ", type " + type); }

        switch (type) {

        case START_TAG:
          if (currentText.length() > 0) { currentText.delete(0, currentText.length()); }
          final String name = parser.getName().trim();
          lastTag = name;
          final Descriptor<?> d = currentDescriptor.getChildDescriptors().get(name);
          if (d != null) {
            descriptors.addLast(d);
            runProcessor();
            if (DEBUG) { Log.d(TAG, "Run processor for " + name); }
          }
          break;

        case TEXT:
          currentText.append(parser.getText());
          break;

        case END_TAG:
          final ElementProcessor p = getCurrentElementProcessor();
          if (p == null) {
            //throw new IllegalStateException("Cannot end tag. " + dumpState());
            Log.e(TAG, "Cannot end tag " + parser.getName() + " / " + dumpState());
            break;
          }
          if (parser.getDepth() > p.getDepth()) { // set a value
            if (lastTag == null) { throw new IllegalStateException("Cannot set value. " + dumpState()); }
            if (lastTag.equals(parser.getName())) {
              final String value = currentText.toString().trim();
              final boolean done = p.processValue(lastTag, value);
              final SimpleResultHandler h = getContext().getSimpleResultHandler();
              if (!done && h != null) { h.handleValue(lastTag, value); }
            }
          } else { // return to the parent processor
            stopProcessor();
            if (DEBUG) { Log.d(TAG, "Stop processor for " + parser.getName()); }
            descriptors.removeLast();
          }
          break;

        default:

        }
        type = parser.next();
      }
      if (currentText.length() > 0) { currentText.delete(0, currentText.length()); }
    } catch (final XmlPullParserException e) {
      throw new RequestMethodException(e, this);
    } catch (final IOException e) {
      throw new RequestMethodException(e);
    }
  }

}
