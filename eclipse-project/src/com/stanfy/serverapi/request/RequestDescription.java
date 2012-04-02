package com.stanfy.serverapi.request;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.message.BasicNameValuePair;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.stanfy.DebugFlags;
import com.stanfy.http.multipart.FilePart;
import com.stanfy.http.multipart.MultipartEntity;
import com.stanfy.http.multipart.Part;
import com.stanfy.http.multipart.StringPart;

/**
 * Request method description. This object is passed to the service describing the request.
 * @author Roman Mazur - Stanfy (http://www.stanfy.com)
 */
public class RequestDescription implements Parcelable {

  /** Logging tag. */
  public static final String TAG = "ReqDesc";

  /** Charset. */
  public  static final String CHARSET = "UTF-8";

  /** Debug flag. */
  public static final boolean DEBUG = DebugFlags.DEBUG_API;

  /** Creator. */
  public static final Creator<RequestDescription> CREATOR = new Creator<RequestDescription>() {
    @Override
    public RequestDescription createFromParcel(final Parcel source) { return new RequestDescription(source); }
    @Override
    public RequestDescription[] newArray(final int size) { return new RequestDescription[size]; }
  };

  /** Token. It can be used to identify a sender. */
  int token;
  /** Operation to execute. */
  int operationCode;
  /** Operation type. */
  int operationType;
  /** URL part. */
  String urlPart;
  /** Simple parameters. */
  ParametersGroup simpleParameters;
  /** A path to the file with binary data. */
  String uploadFile;
  /** Content type. */
  String contentType;
  /** Content language. */
  String contentLanguage;
  /** Meta information. */
  ParametersGroup metaParameters;

  /** Whether request should be performed in parallel. */
  boolean parallelMode = false;

  public static String getParamValue(final String name, final LinkedList<Parameter> param) {
    for (final Parameter p : param) {
      if (p instanceof ParameterValue && name.equals(p.getName())) {
        return ((ParameterValue)p).getValue();
      }
    }
    return null;
  }

  public RequestDescription() { /* nothing */ }

  /**
   * Create from parcel.
   */
  protected RequestDescription(final Parcel source) {
    this.token = source.readInt();
    this.operationCode = source.readInt();
    this.operationType = source.readInt();
    this.urlPart = source.readString();
    final ClassLoader cl = Thread.currentThread().getContextClassLoader();
    this.simpleParameters = source.readParcelable(cl);
    this.uploadFile = source.readString();
    this.contentType = source.readString();
    this.contentLanguage = source.readString();
    this.metaParameters = source.readParcelable(cl);
    this.parallelMode = source.readInt() == 1;
  }

  void setupOperation(final Operation op) {
    this.operationCode = op.getCode();
    this.operationType = op.getType();
    this.urlPart = op.getUrlPart();
    if (DEBUG) { Log.v(TAG, "Setup request operation OPCODE: " + operationCode + " OPTYPE: " + operationType + " URL: " + urlPart); }
  }

  /** @return the operationType */
  public int getOperationType() { return operationType; }

  @Override
  public int describeContents() { return 0; }
  @Override
  public void writeToParcel(final Parcel dest, final int flags) {
    dest.writeInt(token);
    dest.writeInt(operationCode);
    dest.writeInt(operationType);
    dest.writeString(urlPart);
    dest.writeParcelable(simpleParameters, flags);
    dest.writeString(uploadFile);
    dest.writeString(contentType);
    dest.writeString(contentLanguage);
    dest.writeParcelable(metaParameters, flags);
    dest.writeInt(parallelMode ? 1 : 0);
  }

  /** @return the contentLanguage */
  public String getContentLanguage() { return contentLanguage; }
  /** @param contentLanguage the contentLanguage to set */
  public void setContentLanguage(final String contentLanguage) { this.contentLanguage = contentLanguage; }
  /** @return the contentType */
  public String getContentType() { return contentType; }
  /** @param contentType the contentType to set */
  public void setContentType(final String contentType) { this.contentType = contentType; }
  /** @return the urlPart */
  public String getUrlPart() { return urlPart; }
  /** @param urlPart the urlPart to set */
  public void setUrlPart(final String urlPart) { this.urlPart = urlPart; }
  /** @return the uploadFile */
  public String getUploadFile() { return uploadFile; }
  /** @param uploadFile the uploadFile to set */
  public void setUploadFile(final String uploadFile) { this.uploadFile = uploadFile; }

  /** @return whether request is simple. */
  public boolean isSimple() { return operationType == OperationType.SIMPLE_POST || operationType == OperationType.SIMPLE_GET; }

  /** @param parallelMode parallel mode flag */
  public void setParallelMode(final boolean parallelMode) { this.parallelMode = parallelMode; }
  /** @return parallel mode flag */
  public boolean isParallelMode() { return parallelMode; }

  /** @return the metaParameters */
  public ParametersGroup getMetaParameters() { return metaParameters; }

  /** @return the simpleParameters */
  public ParametersGroup getSimpleParameters() { return simpleParameters; }

  public ParametersGroup createMetaParameters() {
    metaParameters = new ParametersGroup();
    metaParameters.name = "meta";
    return metaParameters;
  }

  /**
   * @param name parameter name
   * @param value parameter value
   */
  public void addMetaInfo(final String name, final String value) {
    ParametersGroup metaParameters = this.metaParameters;
    if (metaParameters == null) {
      metaParameters = createMetaParameters();
    }

    final ParameterValue pv = new ParameterValue();
    pv.name = name;
    pv.value = value;
    metaParameters.children.add(pv);
  }

  public String getMetaInfo(final String name) {
    final ParametersGroup metaParameters = this.metaParameters;
    if (metaParameters == null) { return null; }
    for (final Parameter p : metaParameters.children) {
      if (name.equals(p.name)) { return ((ParameterValue)p).value; }
    }
    return null;
  }

  protected String resolveSimpleGetRequest(final long requestId) {
    final Uri.Builder builder = Uri.parse(urlPart).buildUpon();
    for (final Parameter p : this.simpleParameters.children) {
      if (p instanceof ParameterValue) {
        builder.appendQueryParameter(p.getName(), ((ParameterValue) p).getValue());
      }
    }
    final String result = builder.build().toString();
    if (DEBUG) { Log.d(TAG, "(" + requestId + ")" + ": " + result); }
    return result;
  }

  protected void resolveSimpleEntityRequest(final HttpRequestBase request, final long requestId) throws UnsupportedEncodingException {
    final LinkedList<BasicNameValuePair> parameters = new LinkedList<BasicNameValuePair>();
    for (final Parameter p : this.simpleParameters.children) {
      if (p instanceof ParameterValue) {
        parameters.add(new BasicNameValuePair(p.name, ((ParameterValue)p).value));
      }
    }
    if (request instanceof HttpEntityEnclosingRequestBase) {
      ((HttpEntityEnclosingRequestBase)request).setEntity(new UrlEncodedFormEntity(parameters, CHARSET));
    }
    if (DEBUG) { Log.d(TAG, "(" + requestId + ")" + ": " + parameters.toString()); }
  }

  protected void resolveMultipartRequest(final HttpPost request, final long requestId) throws IOException {
    final List<Parameter> params = simpleParameters.children;
    int realCount = 0;
    Part[] parts = new Part[params.size() + 1];
    for (final Parameter p : params) {
      if (p instanceof ParameterValue) {
        final ParameterValue pv = (ParameterValue)p;
        if (pv.value == null) { continue; }
        parts[realCount++] = new StringPart(pv.name, pv.value, CHARSET);
      }
    }
    if (uploadFile != null) {
      parts[realCount++] = new FilePart("content", new File(uploadFile), contentType, null);
    }
    if (realCount < parts.length) {
      final Part[] trim = new Part[realCount];
      System.arraycopy(parts, 0, trim, 0, realCount);
      parts = trim;
    }
    request.setEntity(new MultipartEntity(parts));
    if (DEBUG) { Log.d(TAG, "(" + requestId + ")" + ": " + params); }
  }

  /**
   * @param requestId request identifier
   * @return HTTP request instance
   */
  public HttpUriRequest buildRequest(final long requestId) {
    final HttpRequestBase result;

    try {
      switch (operationType) {
      case OperationType.UPLOAD_POST:
        result = new HttpPost(urlPart);
        resolveMultipartRequest((HttpPost)result, requestId);
        break;
      case OperationType.SIMPLE_GET:
        result = new HttpGet(resolveSimpleGetRequest(requestId));
        break;
      case OperationType.SIMPLE_POST:
        result = new HttpPost(urlPart);
        resolveSimpleEntityRequest(result, requestId);
        break;
      default:
        throw new IllegalArgumentException("Bad operation type for code " + operationCode + ", type " + operationType);
      }
    } catch (final UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
    return result;
  }

  /** @return the operation */
  public int getOperationCode() { return operationCode; }

  /** @return the token */
  public int getToken() { return token; }

}
