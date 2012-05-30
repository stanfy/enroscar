package com.stanfy.serverapi.request;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import com.stanfy.app.Application;
import com.stanfy.app.RequestExecutorProvider;
import com.stanfy.serverapi.request.binary.AssetFdBinaryData;
import com.stanfy.serverapi.request.binary.BitmapBinaryData;
import com.stanfy.serverapi.request.binary.ContentUriBinaryData;
import com.stanfy.serverapi.request.binary.EmptyBinaryData;

/**
 * Base class for request builders.
 * @author Roman Mazur - Stanfy (http://www.stanfy.com)
 */
public abstract class RequestBuilder {

  /** Invalid request identifier. */
  public static final int INVALID_REQUEST_IDENTIFIER = -1;

  /** Logging tag. */
  private static final String TAG = "RequestBuilder";

  /** Date format. */
  private final SimpleDateFormat dateFormat = new SimpleDateFormat(getDateTimeFormat());

  /** Result object. */
  private RequestDescription result;

  /** Context. */
  private final Context context;

  /** Performer. */
  private final RequestExecutor executor;

  /** Operation. */
  private final Operation operation = getOperation();

  public RequestBuilder(final Context context) {
    this(context, context instanceof RequestExecutorProvider ? ((RequestExecutorProvider)context).getRequestExecutor() : null);
  }

  public RequestBuilder(final Context context, final RequestExecutor executor) {
    final Application app = (Application)context.getApplicationContext();
    result = app.getRequestMethodHelper().createRequestDescription();
    result.setupOperation(operation);
    this.context = context;
    result.simpleParameters = new ParametersGroup();
    result.simpleParameters.name = "stub";
    result.contentLanguage = Locale.getDefault().getLanguage();
    this.executor = executor;
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

  /** @return request operation */
  public abstract Operation getOperation();

  public final int getOperationCode() { return result.operationCode; }

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

  protected void addSimpleParameter(final String name, final boolean value) {
    addSimpleParameter(name, value ? "1" : "0");
  }

  protected void addSimpleParameter(final String name, final String value) {
    result.simpleParameters.addSimpleParameter(name, value);
  }

  protected void addParameter(final Parameter p) {
    result.simpleParameters.addParameter(p);
  }

  protected void setUrl(final String url) {
    result.urlPart = url;
  }

  protected RequestDescription getResult() { return result; }

  /** @return the context */
  protected Context getContext() { return context; }

  /**
   * Clear the builder.
   */
  public void clear() {
    final RequestDescription result = this.result;
    result.simpleParameters.children.clear();
    final ParametersGroup meta = result.metaParameters;
    if (meta != null) { meta.children.clear(); }
    result.clearBinaryData();
    result.contentType = null;
    result.metaParameters = null;
  }

  public int execute(final int token) {
    return execute(token, false);
  }

  public final int execute() { return execute(-1); }

  public int executeParallel(final int token) {
    return execute(token, true);
  }

  public final int executeParallel() { return executeParallel(-1); }

  protected int execute(final int token, final boolean parallelMode) {
    result.token = token;
    result.parallelMode = parallelMode;
    if (executor != null) {
      return executor.performRequest(result);
    } else {
      Log.w(TAG, "Don't know how to perform operation " + getOperation());
      return INVALID_REQUEST_IDENTIFIER;
    }
  }

  public boolean checkOperation(final int code) { return code == getOperationCode(); }

}
