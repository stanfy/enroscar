package com.stanfy.serverapi.request;

import java.io.File;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import com.stanfy.app.beans.BeansManager;
import com.stanfy.app.loader.RequestBuilderLoader;
import com.stanfy.serverapi.RemoteServerApiConfiguration;
import com.stanfy.serverapi.request.binary.AssetFdBinaryData;
import com.stanfy.serverapi.request.binary.BitmapBinaryData;
import com.stanfy.serverapi.request.binary.ContentUriBinaryData;
import com.stanfy.serverapi.request.binary.EmptyBinaryData;
import com.stanfy.serverapi.response.ModelTypeToken;
import com.stanfy.utils.RequestExecutor;

/**
 * Base class for request builders.
 * @param <MT> model type
 * @author Roman Mazur - Stanfy (http://www.stanfy.com)
 */
public abstract class BaseRequestBuilder<MT> implements RequestBuilder<MT> {

  /** Invalid request identifier. */
  public static final int INVALID_REQUEST_IDENTIFIER = -1;

  /** Logging tag. */
  private static final String TAG = "RequestBuilder";

  /** Date format. */
  private final SimpleDateFormat dateFormat = new SimpleDateFormat(getDateTimeFormat());

  /** Configuration. */
  private final RemoteServerApiConfiguration config;
  /** Result object. */
  private final RequestDescription result;

  /** Context. */
  private final Context context;

  /** Class of the expected model. */
  private final ModelTypeToken expectedModelType;

  /** Performer. */
  private RequestExecutor executor;

  public BaseRequestBuilder(final Context context) {
    this.config = BeansManager.get(context).getRemoteServerApiConfiguration();
    this.context = context.getApplicationContext();
    this.result = config.createRequestDescription();

    result.simpleParameters = new ParametersGroup();
    result.simpleParameters.name = "stub";
    result.contentLanguage = Locale.getDefault().getLanguage();

    this.expectedModelType = ModelTypeToken.fromRequestBuilderClass(getClass());
    result.modelType = this.expectedModelType;
  }

  protected String getDateTimeFormat() { return "yyyy-MM-dd HH:mm:ss Z"; }

  protected String formatDate(final Date d) { return d != null ? dateFormat.format(d) : null; }
  protected Date parseDate(final String d) {
    if (d == null) { return null; }
    try {
      return dateFormat.parse(d);
    } catch (final ParseException e) {
      Log.e(TAG, "Cannot parse date " + d, e);
      return null;
    }
  }

  protected void setModelType(final Type type) {
    result.modelType = ModelTypeToken.fromModelType(type);
  }

  /**
   * @param executor executor instance
   */
  @Override
  public void setExecutor(final RequestExecutor executor) {
    this.executor = executor;
  }

  /**
   * @param url URL to set
   * @return this instance for chaining
   */
  protected void setTargetUrl(final String url) {
    result.url = url;
  }

  /**
   * @param url URL to set
   * @return this instance for chaining
   */
  protected void setRequestOperationType(final int operationType) {
    result.operationType = operationType;
  }

  /**
   * @param name cache manager bean name
   */
  protected void setRequestCacheName(final String name) {
    result.cacheName = name;
  }
  /**
   * @param name content handler name
   */
  protected void setRequestContentHandler(final String name) {
    result.contentHandler = name;
  }

  /**
   * @param name content analyzer name
   */
  protected void setContentAnalyzerName(final String name) {
    result.contentAnalyzer = name;
  }

  /**
   * Setup binary content from the local file. Parameter name will be equal to {@link RequestDescription#BINARY_NAME_DEFAULT}.
   * @param data content URI
   * @param contentType content MIME-type
   */
  protected void addBinaryContent(final Uri data, final String contentType) {
    addBinaryContent(null, data, contentType);
  }

  /**
   * Setup binary content with the local file.
   * @param name parameter name
   * @param data content URI
   * @param contentType content MIME-type
   */
  protected void addBinaryContent(final String name, final Uri data, final String contentType) {
    String contentName = RequestDescription.BINARY_NAME_DEFAULT;
    if (ContentResolver.SCHEME_FILE.equals(data.getScheme())) {
      try {
        contentName = new File(new URI(data.toString())).getName();
      } catch (final URISyntaxException e) {
        Log.e(TAG, "Bad file URI: " + data, e);
      }
    }
    addBinaryContent(name, contentName, data, contentType);
  }

  /**
   * Setup binary content with the local file.
   * @param name parameter name
   * @param contentName content name
   * @param data content URI
   * @param contentType content MIME-type
   */
  protected void addBinaryContent(final String name, final String contentName, final Uri data, final String contentType) {
    final ContentUriBinaryData bdata = new ContentUriBinaryData();
    bdata.setName(name);
    bdata.setContentUri(data, contentName);
    bdata.setContentType(contentType);
    result.addBinaryData(bdata);
  }

  /**
   * Setup binary content with the bitmap.
   * @param name parameter name
   * @param bitmap bitmap object
   * @param contentType content MIME-type
   * @param bitmap file name
   */
  protected void addBitmap(final String name, final Bitmap bitmap, final String fileName) {
    final BitmapBinaryData bdata = new BitmapBinaryData();
    bdata.setName(name);
    bdata.setContentName(fileName);
    bdata.setBitmap(bitmap);
    result.addBinaryData(bdata);
  }

  /**
   * Setup binary content with the file descriptor.
   * @param name parameter name
   * @param fd file descriptor
   * @param contentType content MIME-type
   * @param bitmap file name
   */
  protected void addFileDescriptor(final String name, final AssetFileDescriptor fd, final String contentType, final String fileName) {
    final AssetFdBinaryData bdata = new AssetFdBinaryData();
    bdata.setFileDescriptor(fileName, fd);
    bdata.setName(name);
    bdata.setContentType(contentType);
    result.addBinaryData(bdata);
  }

  /**
   * @param name name for empty binary type
   */
  protected void addEmptyBinary(final String name) {
    final EmptyBinaryData bdata = new EmptyBinaryData();
    bdata.setName(name);
    result.addBinaryData(bdata);
  }

  protected ParameterValue addSimpleParameter(final String name, final long value) {
    return addSimpleParameter(name, String.valueOf(value));
  }
  protected ParameterValue addSimpleParameter(final String name, final int value) {
    return addSimpleParameter(name, String.valueOf(value));
  }
  protected ParameterValue addSimpleParameter(final String name, final boolean value) {
    return addSimpleParameter(name, value ? "1" : "0");
  }
  protected ParameterValue addSimpleParameter(final String name, final String value) {
    return result.simpleParameters.addSimpleParameter(name, value);
  }

  protected void addParameter(final Parameter p) {
    result.simpleParameters.addParameter(p);
  }

  /**
   * Add meta information to request which could be retrieved in {@link com.stanfy.serverapi.response.ContentAnalyzer}.
   * @param name info name
   * @param value info value
   */
  protected void putMetaInfo(final String name, final Object value) {
    result.putMetaInfo(name, value);
  }

  /**
   * @param contentAnalyzer bean name of {@link com.stanfy.serverapi.response.ContentAnalyzer} instance
   */
  protected void defineContentAnalyzer(final String contentAnalyzer) {
    result.setContentAnalyzer(contentAnalyzer);
  }

  protected RequestDescription getResult() { return result; }

  /** @return the context */
  @Override
  public Context getContext() { return context; }

  /**
   * Clear the builder.
   */
  public void clear() {
    final RequestDescription result = this.result;
    result.simpleParameters.children.clear();
    final Map<String, Object> meta = result.metaParameters;
    if (meta != null) { meta.clear(); }
    result.clearBinaryData();
    result.contentType = null;
    result.metaParameters = null;
  }

  public BaseRequestBuilder<?> setParallel(final boolean value) {
    result.parallelMode = value;
    return this;
  }

  @Override
  public ModelTypeToken getExpectedModelType() { return expectedModelType; }

  @Override
  public int execute() {
    if (result.url == null) {
      throw new IllegalStateException("URL is not specified!");
    }
    if (result.modelType == null) {
      throw new IllegalStateException("Model is not specified!");
    }

    if (result.contentHandler == null) {
      result.contentHandler = config.getDefaultContentHandlerName();
    }
    if (result.contentHandler == null) {
      throw new IllegalStateException("Content handler is not specified");
    }

    if (result.cacheName == null) {
      result.cacheName = config.getDefaultCacheBeanName();
    }

    result.setCanceled(false);

    if (executor != null) {
      return executor.performRequest(result);
    } else {
      Log.w(TAG, "Don't know how to perform operation " + result.getUrl());
      return INVALID_REQUEST_IDENTIFIER;
    }
  }

  /**
   * Create an appropriate loader instance.<br/>
   * Basic usage:<br/>
   * <pre>
   * public Loader onCreateLoader(int id, Bundle args) {
   *   return new RequestBuilder(this)
   *     .addParam("aaa", "bbb")
   *     .getLoader();
   * }
   * </pre>
   * @return loader instance that uses this request builder
   */
  @Override
  public RequestBuilderLoader<MT> getLoader() {
    return new RequestBuilderLoader<MT>(this);
  }

  protected <T, LT extends List<T>> ListRequestBuilderWrapper<LT, T> createLoadMoreListWrapper() {
    return new ListRequestBuilderWrapper<LT, T>(this) { };
  }

  public <T, LT extends List<T>> ListRequestBuilderWrapper<LT, T> asLoadMoreList(final String offset, final String limit) {
    final ListRequestBuilderWrapper<LT, T> wrapper = createLoadMoreListWrapper();
    if (offset != null) {
      wrapper.setOffsetParamName(offset);
    }
    if (limit != null) {
      wrapper.setLimitParamName(limit);
    }
    return wrapper;
  }
  public <T, LT extends List<T>> ListRequestBuilderWrapper<LT, T> asLoadMoreList() {
    return asLoadMoreList(ListRequestBuilderWrapper.PARAM_OFFSET, ListRequestBuilderWrapper.PARAM_LIMIT);
  }

}
