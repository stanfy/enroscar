package com.stanfy.serverapi.request;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.util.Log;

import com.stanfy.app.Application;
import com.stanfy.app.RequestExecutorProvider;

/**
 * Base class for request builders.
 * @author Roman Mazur - Stanfy (http://www.stanfy.com)
 */
public abstract class RequestBuilder {

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

  protected void addBinaryContent(final String path, final String contentType) {
    if (result.operationType == OperationType.UPLOAD_POST) {
      result.uploadFile = path;
      result.contentType = contentType;
    }
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
    result.simpleParameters.children.clear();
    final ParametersGroup meta = result.metaParameters;
    if (meta != null) { meta.children.clear(); }
    result.uploadFile = null;
    result.contentType = null;
    result.metaParameters = null;
  }

  public void execute(final int token) {
    result.token = token;
    if (executor != null) {
      executor.performRequest(result);
    } else {
      Log.w(TAG, "Don't know how to perform operation " + getOperation());
    }
  }

  public final void execute() { execute(-1); }

  public boolean checkOperation(final int code) { return code == getOperationCode(); }

}
