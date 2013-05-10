package com.stanfy.enroscar.rest.request;

import java.util.Locale;
import java.util.TreeMap;

import android.content.Context;

import com.stanfy.enroscar.beans.BeanUtils;
import com.stanfy.enroscar.rest.RequestExecutor;
import com.stanfy.enroscar.rest.response.handler.GsonContentHandler;
import com.stanfy.enroscar.rest.response.handler.StringContentHandler;
import com.stanfy.enroscar.rest.response.handler.XmlGsonContentHandler;

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
    final String name = FORMAT_MAPPINGS.get(format.toLowerCase(Locale.US));
    setRequestContentHandler(name != null ? name : format);
    return this;
  }

  @Override
  public SimpleRequestBuilder<MT> setParallel(final boolean value) {
    super.setParallel(value);
    return this;
  }

  @Override
  public SimpleRequestBuilder<MT> setExecutor(final RequestExecutor executor) {
    super.setExecutor(executor);
    return this;
  }
  
  @Override
  public SimpleRequestBuilder<MT> setTaskQueueName(final String taskQueue) {
    super.setTaskQueueName(taskQueue);
    return this;
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
   * @deprecated this method will be deleted
   */
  @Deprecated
  public SimpleRequestBuilder<MT> setMetaInfo(final String name, final Object value) {
    putMetaInfo(name, value);
    return this;
  }

  public SimpleRequestBuilder<MT> setContentAnalyzer(final String contentAnalyzer) {
    defineContentAnalyzer(contentAnalyzer);
    return this;
  }

  public SimpleRequestBuilder<MT> setContentAnalyzer(final Class<?> clazz) {
    return setContentAnalyzer(BeanUtils.getBeanInfo(clazz).value());
  }

  public SimpleRequestBuilder<MT> setTafficStatsTag(final int tag) {
    getResult().statsTag = tag;
    return this;
  }
  
  public SimpleRequestBuilder<MT> setTrafficStatsTag(final String tag) {
    setConvertedTrafficStatsTag(tag);
    return this;
  }
  
}
