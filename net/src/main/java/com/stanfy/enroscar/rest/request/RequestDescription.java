package com.stanfy.enroscar.rest.request;

import java.io.IOException;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.SparseArray;

import com.stanfy.enroscar.beans.BeansManager;
import com.stanfy.enroscar.io.IoUtils;
import com.stanfy.enroscar.net.UrlConnectionBuilder;
import com.stanfy.enroscar.rest.ModelTypeToken;
import com.stanfy.enroscar.rest.request.binary.BinaryData;
import com.stanfy.enroscar.rest.request.net.BaseRequestDescriptionConverter;
import com.stanfy.enroscar.rest.request.net.BaseRequestDescriptionConverter.ConverterFactory;
import com.stanfy.enroscar.rest.request.net.PayloadPostConverter;
import com.stanfy.enroscar.rest.request.net.SimpleGetConverter;
import com.stanfy.enroscar.rest.request.net.SimplePostConverter;
import com.stanfy.enroscar.rest.request.net.UploadPostConverter;
import com.stanfy.enroscar.rest.response.Model;

/**
 * Request method description. This object is passed to the service describing the request.
 * @author Roman Mazur - Stanfy (http://www.stanfy.com)
 */
public class RequestDescription implements Parcelable {

  /** Default name for binary content. */
  public static final String BINARY_NAME_DEFAULT = "content";

  /** ID counter. */
  private static int idCounter = 0;

  /** Logging tag. */
  public static final String TAG = "ReqDesc";

  /** Charset. */
  public  static final String CHARSET = "UTF-8";

  /** Creator. */
  public static final Creator<RequestDescription> CREATOR = new Creator<RequestDescription>() {
    @Override
    public RequestDescription createFromParcel(final Parcel source) { return new RequestDescription(source); }
    @Override
    public RequestDescription[] newArray(final int size) { return new RequestDescription[size]; }
  };

  /** Converters to {@link URLConnection}. */
  private static final SparseArray<ConverterFactory> CONVERTER_FACTORIES = new SparseArray<ConverterFactory>(3);
  static {
    registerConverterFactory(OperationType.SIMPLE_GET, SimpleGetConverter.FACTORY);
    registerConverterFactory(OperationType.SIMPLE_POST, SimplePostConverter.FACTORY);
    registerConverterFactory(OperationType.UPLOAD_POST, UploadPostConverter.FACTORY);
    registerConverterFactory(OperationType.PAYLOAD_POST, PayloadPostConverter.FACTORY);
  }

  /** Request ID. */
  final int id;

  /** Operation type. */
  int operationType = OperationType.SIMPLE_GET;

  /** URL part. */
  String url;
  /** Cache instance name. */
  String cacheName;
  /** Simple parameters. */
  ParametersGroup simpleParameters;

  /** Content type. */
  String contentType;
  /** Request data encoding. */
  Charset encoding = IoUtils.UTF_8;
  /** Content language. */
  String contentLanguage;

  /** Meta information. */
  Map<String, Object> metaParameters;

  /** Binary data array. */
  ArrayList<BinaryData<?>> binaryData;

  /** Whether request should be performed in parallel. */
  boolean parallelMode = false;

  /** Canceled state flag. */
  volatile boolean canceled = false;

  /** Model class. */
  ModelTypeToken modelType;
  /** Content handler name. */
  String contentHandler;
  /** Content analyzer. */
  String contentAnalyzer;

  /** Task queue name. */
  String taskQueueName;

  /** Statistics tag. */
  int statsTag;
  
  /**
   * Create with predefined ID.
   * @param id request ID
   */
  public RequestDescription(final int id) {
    this.id = id;
  }

  /**
   * Create new description with new request ID.
   */
  public RequestDescription() {
    this(nextId());
  }

  /**
   * Create from parcel.
   * @param source source parcel
   */
  protected RequestDescription(final Parcel source) {
    this(source.readInt());
    final ClassLoader cl = getClass().getClassLoader();

    this.operationType = source.readInt();
    this.url = source.readString();
    this.cacheName = source.readString();
    this.simpleParameters = source.readParcelable(cl);
    this.contentType = source.readString();
    this.encoding = Charset.forName(source.readString());
    this.contentLanguage = source.readString();

    this.parallelMode = source.readInt() == 1;
    this.taskQueueName = source.readString();

    this.canceled = source.readInt() == 1;

    this.modelType = source.readParcelable(cl);
    this.contentHandler = source.readString();
    this.contentAnalyzer = source.readString();

    // binary content fields
    final Parcelable[] binary = source.readParcelableArray(cl);
    if (binary != null) {
      this.binaryData = new ArrayList<BinaryData<?>>(binary.length);
      for (Parcelable b : binary) {
        this.binaryData.add((BinaryData<?>) b);
      }
    }
    
    this.statsTag = source.readInt();
  }

  public static void registerConverterFactory(final int opertationType, final ConverterFactory factory) {
    CONVERTER_FACTORIES.put(opertationType, factory);
  }

  private static synchronized int nextId() {
    ++idCounter;
    if (idCounter < 0) { idCounter = 1; }
    return idCounter;
  }

  public static String getParamValue(final String name, final LinkedList<Parameter> param) {
    for (final Parameter p : param) {
      if (p instanceof ParameterValue && name.equals(p.getName())) {
        return ((ParameterValue)p).getValue();
      }
    }
    return null;
  }

  @Override
  public void writeToParcel(final Parcel dest, final int flags) {
    dest.writeInt(id);
    dest.writeInt(operationType);
    dest.writeString(url);
    dest.writeString(cacheName);
    dest.writeParcelable(simpleParameters, flags);
    dest.writeString(contentType);
    dest.writeString(encoding.name());
    dest.writeString(contentLanguage);

    dest.writeInt(parallelMode ? 1 : 0);
    dest.writeString(taskQueueName);

    dest.writeInt(canceled ? 1 : 0);

    dest.writeParcelable(modelType, 0);
    dest.writeString(contentHandler);
    dest.writeString(contentAnalyzer);

    // binary content fields
    if (binaryData != null) {
      final BinaryData<?>[] binary = new BinaryData<?>[binaryData.size()];
      dest.writeParcelableArray(binaryData.toArray(binary), flags);
    } else {
      dest.writeParcelableArray(null, flags);
    }
    
    dest.writeInt(statsTag);
  }

  @Override
  public boolean equals(final Object o) {
    if (!(o instanceof RequestDescription)) { return false; }
    return id == ((RequestDescription)o).id;
  }

  @Override
  public int hashCode() { return id; }

  @Override
  public final int describeContents() {
    final ArrayList<BinaryData<?>> binaryData = this.binaryData;
    if (binaryData == null) { return 0; }
    final int count = binaryData.size();
    int result = 0;
    for (int i = 0; i < count; i++) {
      result |= binaryData.get(i).describeContents();
    }
    return result;
  }

  /** @return request identifier */
  public final int getId() { return id; }

  /** @param operationType operation type */
  public void setOperationType(final int operationType) { this.operationType = operationType; }
  /** @return the operationType */
  public int getOperationType() { return operationType; }

  /** @return the contentLanguage */
  public String getContentLanguage() { return contentLanguage; }
  /** @param contentLanguage the contentLanguage to set */
  public void setContentLanguage(final String contentLanguage) { this.contentLanguage = contentLanguage; }
  /** @return the contentType */
  public String getContentType() { return contentType; }
  /** @param contentType the contentType to set */
  public void setContentType(final String contentType) { this.contentType = contentType; }
  /** @return URL */
  public String getUrl() { return url; }
  /** @param url URL to set */
  public void setUrl(final String url) { this.url = url; }
  /** @param encoding request encoding */
  public void setEncoding(final Charset encoding) { this.encoding = encoding; }
  /** @return request encoding */
  public Charset getEncoding() { return encoding; }

  /** @param typeToken type token the response model */
  public void setModelType(final ModelTypeToken typeToken) { this.modelType = typeToken; }
  /** @return type token the response model */
  public ModelTypeToken getModelType() { return modelType; }

  private void checkBeanExists(final String name) {
    if (name != null && !BeansManager.get(null).getContainer().containsBean(name)) {
      throw new IllegalArgumentException("Bean " + name + " is not registered");
    }
  }

  /** @param cacheName cache manager name */
  public void setCacheName(final String cacheName) {
    checkBeanExists(cacheName);
    this.cacheName = cacheName;
  }
  /** @return cache manager name */
  public String getCacheName() { return cacheName; }
  /** @param contentHandler content handler name */
  public void setContentHandler(final String contentHandler) {
    checkBeanExists(contentHandler);
    this.contentHandler = contentHandler;
  }
  /** @return content handler name */
  public String getContentHandler() { return contentHandler; }
  /** @param contentAnalyzer content analyzer bean name */
  public void setContentAnalyzer(final String contentAnalyzer) {
    checkBeanExists(contentAnalyzer);
    this.contentAnalyzer = contentAnalyzer;
  }
  /** @return content analyzer bean name */
  public String getContentAnalyzer() {
    synchronized (this) {
      if (contentAnalyzer == null && modelType != null) {
        final Model modelAnnotation = modelType.getRawClass().getAnnotation(Model.class);
        if (modelAnnotation != null) {
          final String analyzer = modelAnnotation.analyzer();
          if (analyzer.length() > 0) { contentAnalyzer = analyzer; }
        }
      }
    }
    return contentAnalyzer;
  }

  public void setCanceled(final boolean canceled) { this.canceled = canceled; }
  public boolean isCanceled() { return canceled; }

  public ArrayList<BinaryData<?>> getBinaryData() { return binaryData; }
  public void addBinaryData(final BinaryData<?> bdata) {
    if (binaryData == null) { binaryData = new ArrayList<BinaryData<?>>(); }
    binaryData.add(bdata);
  }
  public void clearBinaryData() {
    if (binaryData != null) {
      binaryData.clear();
    }
  }


  /** @return whether request is simple. */
  public boolean isSimple() { return operationType == OperationType.SIMPLE_POST || operationType == OperationType.SIMPLE_GET; }

  /** @param parallelMode parallel mode flag */
  public void setParallelMode(final boolean parallelMode) { this.parallelMode = parallelMode; }
  /** @return parallel mode flag */
  public boolean isParallelMode() { return parallelMode; }

  /** @param taskQueueName task queue name */
  public void setTaskQueueName(final String taskQueueName) { this.taskQueueName = taskQueueName; }
  /** @return task queue name */
  public String getTaskQueueName() { return taskQueueName; }

  /** @return the metaParameters */
  public Map<String, Object> getMetaParameters() { return metaParameters; }

  /** @return the simpleParameters */
  public ParametersGroup getSimpleParameters() { return simpleParameters; }

  /** @return new meta parameters instance */
  protected Map<String, Object> createMetaParameters() { return new HashMap<String, Object>(); }

  /**
   * @param name parameter name
   * @param value parameter value
   */
  public void putMetaInfo(final String name, final Object value) {
    if (this.metaParameters == null) { this.metaParameters = createMetaParameters(); }
    this.metaParameters.put(name, value);
  }

  /**
   * @deprecated try to avoid use of it, it's going to be deleted
   */
  @Deprecated
  public Object getMetaInfo(final String name) {
    return this.metaParameters == null ? null : this.metaParameters.get(name);
  }

  /**
   * @deprecated try to avoid use of it, it's going to be deleted
   */
  @Deprecated
  public boolean hasMetaInfo(final String name) {
    return this.metaParameters == null ? false : this.metaParameters.containsKey(name);
  }

  /**
   * Set a tag that will be used as a parameter for {@link android.net.TrafficStats}.
   * @param statsTag tag value
   */
  public void setStatsTag(final int statsTag) {
    this.statsTag = statsTag;
  }
  
  /**
   * @return network traffic statistics tag
   */
  public int getStatsTag() {
    return statsTag;
  }
  
  // ============================ HTTP REQUESTS ============================

  /**
   * @param context context instance
   * @return user agent string
   */
  public static String buildUserAgent(final Context context) {
    if (context == null) { return null; }
    try {
      final PackageManager manager = context.getPackageManager();
      final StringBuilder agentString = new StringBuilder();
      agentString.append("Android client (").append(Build.VERSION.RELEASE).append(" / api ").append(Build.VERSION.SDK_INT).append("), ");
      if (manager != null) {
        final PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
        agentString.append(info.packageName).append("/").append(info.versionName)
            .append(" (").append(info.versionCode).append("), ").append(IoUtils.ENCODING_GZIP);
      } else {
        // test environment only
        agentString.append(" test");
      }
      return agentString.toString();
    } catch (final NameNotFoundException e) {
      return null;
    }
  }

  /**
   * A good place to set custom request headers.
   * @param context system context
   * @param urlConnection URL connection instance
   */
  protected void onURLConnectionPrepared(final Context context, final URLConnection urlConnection) {
    if (contentType != null) {
      urlConnection.addRequestProperty("Content-Type", contentType);
    }
    if (contentLanguage != null) {
      urlConnection.addRequestProperty("Accept-Language", contentLanguage);
    }
    urlConnection.addRequestProperty("Accept-Encoding", IoUtils.ENCODING_GZIP);
    urlConnection.addRequestProperty("User-Agent", buildUserAgent(context));
  }

  /**
   * Pass cache and content control parameters to URL connection builder.
   * @param context context instance
   * @return URL connection builder instance
   */
  public UrlConnectionBuilder prepareConnectionBuilder(final Context context) {
    return new UrlConnectionBuilder()
      .setCacheManagerName(cacheName)
      .setContentHandlerName(contentHandler)
      .setModelType(modelType);
  }

  /**
   * Build {@link URLConnection} instance, connect, write request.
   * @param context context instance
   * @return {@link URLConnection} instance, ready for {@link URLConnection#getInputStream()} call
   * @throws IOException in case of I/O errors
   */
  public URLConnection makeConnection(final Context context) throws IOException {
    ConverterFactory factory = CONVERTER_FACTORIES.get(operationType);
    if (factory == null) {
      throw new IllegalArgumentException("Don't know how to convert operation type " + operationType);
    }
    final BaseRequestDescriptionConverter converter = factory.createConverter(this, context);

    // create instance
    final URLConnection connection = converter.prepareConnectionInstance();
    // setup headers
    onURLConnectionPrepared(context, connection);
    // make a connection
    converter.connect(connection);
    // send data, if required
    converter.sendRequest(connection);

    return connection;
  }

  @Override
  public String toString() {
    return "RequestDescription[id=" + id + ", url=" + url + "]";
  }

}
