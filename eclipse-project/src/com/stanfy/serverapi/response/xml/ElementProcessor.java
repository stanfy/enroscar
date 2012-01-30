package com.stanfy.serverapi.response.xml;


import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TreeMap;

import org.xmlpull.v1.XmlPullParser;

import com.stanfy.DebugFlags;
import com.stanfy.serverapi.response.ParserContext;

/**
 * XML response elements processor.
 * @author Roman Mazur - Stanfy (http://www.stanfy.com)
 */
public abstract class ElementProcessor {

  /** Logging tag. */
  protected static final String TAG = "ElementProcessor";

  /** Debug flag. */
  protected static final boolean DEBUG = DebugFlags.DEBUG_API;

  /** Format for parsing dates. */
  private final DateFormat sdf = createDateFormat();

  /** Parser context. */
  private ParserContext context;

  /** Where the processor was called. */
  private int depth;

  /**
   * @return date format to be used by {@link #parseDate(String)}
   */
  protected DateFormat createDateFormat() {
    return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z", Locale.US);
  }

  protected Date parseDate(final String date) {
    try {
      return sdf.parse(date);
    } catch (final ParseException e) {
      return null;
    }
  }

  protected static int parseInt(final String value) {
    try {
      return Integer.parseInt(value);
    } catch (final NumberFormatException e) {
      return 0;
    }
  }

  protected static long parseLong(final String value) {
    try {
      return Long.parseLong(value);
    } catch (final NumberFormatException e) {
      return 0;
    }
  }

  protected static double parseDouble(final String value) {
    try {
      return Double.parseDouble(value);
    } catch (final NumberFormatException e) {
      return 0;
    }
  }

  protected boolean parseBoolean(final String value) {
    return Boolean.parseBoolean(value) || parseInt(value) != 0;
  }

  /** @param context the context to set */
  public void setContext(final ParserContext context) { this.context = context; }
  /** @return context instance */
  public ParserContext getContext() { return context; }

  /** @return true if attributes should be ignored */
  protected boolean ignoreAttributes() { return true; }

  /** @param depth the depth to set */
  public void setDepth(final int depth) { this.depth = depth; }
  /** @return the depth */
  public int getDepth() { return depth; }

  /**
   * Process attributes of the current element. Namespaces are not supported.
   * @param parser parser instance
   */
  public void processAttributes(final XmlPullParser parser) {
    if (ignoreAttributes()) { return; }
    final int count = parser.getAttributeCount();
    if (count == 0) { return; }
    boolean cont = true;
    for (int i = count - 1; cont && i >= 0; i--) {
      final String name = parser.getAttributeName(i);
      final String value = parser.getAttributeValue(i);
      cont = processAttribute(name, value);
    }
  }

  /**
   * Process the element attribute.
   * @param name attribute name
   * @param value attribute value
   * @return flag to continue processing
   */
  protected boolean processAttribute(final String name, final String value) { return false; }

  /**
   * @param name element name
   * @param value element value
   * @return flag whether value was processed
   */
  public boolean processValue(final String name, final String value) { return false; }

  /** Callback to sync retrieved data. */
  public void syncData() { /* empty */ }

  /**
   * @return context for the child
   */
  public ParserContext childContext() { return context; }

  /**
   * Processor descriptor.
   * @author Roman Mazur - Stanfy (http://www.stanfy.com)
   */
  public abstract static class Descriptor<P extends ElementProcessor> {

    /** Parent descriptor. */
    private Descriptor<?> parentDescriptor;
    /** Child descriptors. */
    private TreeMap<String, Descriptor<?>> childDescriptors = new TreeMap<String, Descriptor<?>>();

    /** @return new processor instance */
    public abstract P createProcessor();

    public void addChild(final String name, final Descriptor<?> d) {
      d.parentDescriptor = this;
      childDescriptors.put(name, d);
    }

    /** @return the childDescriptors */
    public TreeMap<String, Descriptor<?>> getChildDescriptors() { return childDescriptors; }
    /** @return the parentDescriptor */
    public Descriptor<?> getParentDescriptor() { return parentDescriptor; }

  }

  /**
   * @author Roman Mazur - Stanfy (http://www.stanfy.com)
   */
  public static class DescriptorBuilder {

    /** Root descriptor. */
    private Descriptor<ElementProcessor> rootDescriptor = new Descriptor<ElementProcessor>() {
                          @Override
                          public ElementProcessor createProcessor() { return null; }
                       };
    /** Current descriptor. */
    private Descriptor<?> currentDescriptor = rootDescriptor;

    /** List element. */
    private final Descriptor<? extends BaseListProcessor> listElement;
    /** Root element. */
    private Descriptor<?> rootElement;

    /**
     * Basic constructor.
     * Has no default support for {@link #list(String)} method.
     */
    public DescriptorBuilder() {
      this(null);
    }

    /**
     * Basic constructor.
     * @param list list element processor
     */
    public DescriptorBuilder(final Descriptor<? extends BaseListProcessor> list) {
      this.listElement = list;
    }

    public Descriptor<ElementProcessor> build() { return rootDescriptor; }

    public DescriptorBuilder element(final String name) { return element(name, new Descriptor<ElementProcessor>() {
      @Override
      public ElementProcessor createProcessor() { return new EasyElementProcessor(); }
    }); }

    public DescriptorBuilder root(final String name) { return root(name, EasyElementProcessor.ROOT_DESCRITPOR); }
    public DescriptorBuilder root(final String name, final Descriptor<?> descriptor) {
      rootElement = descriptor;
      return element(name, descriptor);
    }

    public DescriptorBuilder list(final String name) { return list(name, listElement); }
    public DescriptorBuilder list(final String name, final Descriptor<? extends BaseListProcessor> descriptor) { return element(name, descriptor); }

    public DescriptorBuilder element(final String name, final Descriptor<?> d) {
      currentDescriptor.addChild(name, d);
      currentDescriptor = d;
      return this;
    }

    public DescriptorBuilder to(final Descriptor<?> d) {
      currentDescriptor = d;
      return this;
    }
    public DescriptorBuilder up() {
      currentDescriptor = currentDescriptor.parentDescriptor;
      return this;
    }

    public DescriptorBuilder toRoot() {
      currentDescriptor = rootElement;
      return this;
    }

  }

}
