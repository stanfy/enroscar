package com.stanfy.serverapi.request;

import java.util.TreeMap;

import android.content.Context;

import com.stanfy.serverapi.response.handler.GsonContentHandler;
import com.stanfy.serverapi.response.handler.StringContentHandler;
import com.stanfy.serverapi.response.handler.XmlGsonContentHandler;

/**
 * Simple request builder.
 * @param <MT> model type
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
public abstract class SimpleRequestBuilder<MT> extends BaseRequestBuilder<MT> {

  /** Format. */
  public static final String JSON = "json", XML = "xml", STRING = "string";

  /** Content format mappings. */
  private static final TreeMap<String, String> FORMAT_MAPPINGS = new TreeMap<String, String>();
  static {
    FORMAT_MAPPINGS.put(JSON, GsonContentHandler.BEAN_NAME);
    FORMAT_MAPPINGS.put(XML, XmlGsonContentHandler.BEAN_NAME);
    FORMAT_MAPPINGS.put(STRING, StringContentHandler.BEAN_NAME);
  }

  public SimpleRequestBuilder(final Context context) {
    super(context);
    setRequestContentHandler(GsonContentHandler.BEAN_NAME);
  }

  /**
   * Set target URL.
   * @param url URL to set
   * @return this builder for chaining
   */
  public SimpleRequestBuilder<MT> setUrl(final String url) {
    setTargetUrl(url);
    return this;
  }

  /**
   * Set request operation type.
   * @param type request operation type
   * @return this builder for chaining
   */
  public SimpleRequestBuilder<MT> setOperationType(final int type) {
    setRequestOperationType(type);
    return this;
  }

  /**
   * @param name cache manager bean name
   * @return this builder for chaining
   */
  public SimpleRequestBuilder<MT> setCacheName(final String name) {
    setRequestCacheName(name);
    return this;
  }

  public SimpleRequestBuilder<MT> setFormat(final String format) {
    final String name = FORMAT_MAPPINGS.get(format.toLowerCase());
    setRequestContentHandler(name != null ? name : format);
    return this;
  }

  @SuppressWarnings("unchecked")
  @Override
  public SimpleRequestBuilder<MT> setParallel(final boolean value) {
    return (SimpleRequestBuilder<MT>)super.setParallel(value);
  }


  /**
   * Add string parameter.
   * @param name parameter name
   * @param value parameter value
   * @return this builder for chaining
   */
  public SimpleRequestBuilder<MT> addParam(final String name, final String value) {
    addSimpleParameter(name, value);
    return this;
  }

  /**
   * Add boolean parameter (true &lt;-&gt; 1, false &lt;-&gt; 0).
   * @param name parameter name
   * @param value parameter value
   * @return this builder for chaining
   */
  public SimpleRequestBuilder<MT> addParam(final String name, final boolean value) {
    addSimpleParameter(name, value);
    return this;
  }

  /**
   * Add integer parameter.
   * @param name parameter name
   * @param value parameter value
   * @return this builder for chaining
   */
  public SimpleRequestBuilder<MT> addParam(final String name, final int value) {
    addSimpleParameter(name, value);
    return this;
  }

  /**
   * Add long integer parameter.
   * @param name parameter name
   * @param value parameter value
   * @return this builder for chaining
   */
  public SimpleRequestBuilder<MT> addParam(final String name, final long value) {
    addSimpleParameter(name, value);
    return this;
  }

  /**
   * @see {@link BaseRequestBuilder#putMetaInfo(String, Object)}
   */
  public SimpleRequestBuilder<MT> setMetaInfo(final String name, final Object value) {
    putMetaInfo(name, value);
    return this;
  }

  /**
   * @see {@link BaseRequestBuilder#defineContentAnalyzer(String)}
   */
  public SimpleRequestBuilder<MT> setContentAnalyzer(final String contentAnalyzer) {
    defineContentAnalyzer(contentAnalyzer);
    return this;
  }

}
