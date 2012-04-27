package com.stanfy.serverapi.request;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.message.BasicNameValuePair;

import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;

import com.stanfy.DebugFlags;
import com.stanfy.http.multipart.FilePart;
import com.stanfy.http.multipart.MultipartEntity;
import com.stanfy.http.multipart.Part;
import com.stanfy.http.multipart.StringPart;
import com.stanfy.http.multipart.android.AssetFileDescriptorPartSource;
import com.stanfy.http.multipart.android.BitmapPart;

/**
 * Request method description. This object is passed to the service describing the request.
 * @author Roman Mazur - Stanfy (http://www.stanfy.com)
 */
public class RequestDescription implements Parcelable {

  /** Indicates {@link #binaryData} to be empty. */
  public static final int BINARY_TYPE_EMPTY = 0;
  /** Indicates {@link #binaryData} to be {@link Uri}. */
  public static final int BINARY_TYPE_FILE_URI = 1;
  /** Indicates {@link #binaryData} to be {@link Bitmap}. */
  public static final int BINARY_TYPE_BITMAP = 2;
  /** Indicates {@link #binaryData} to be {@link AssetFileDescriptor}. */
  public static final int BINARY_TYPE_FILE_DESCRITPOR = 3;
  /** First value for user types. */
  public static final int BINARY_TYPE_USER = 100;

  /** Default name for binary content. */
  public static final String BINARY_NAME_DEFAULT = "content";

  /** ID counter. */
  private static int idCounter = 0;

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

  private static synchronized int nextId() {
    ++idCounter;
    if (idCounter < 0) { idCounter = 1; }
    return idCounter;
  }

  /** Request ID. */
  final int id;

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

  /** Content type. */
  String contentType;
  /** Content language. */
  String contentLanguage;

  /** Meta information. */
  ParametersGroup metaParameters;

  /** Binary data name. */
  private String binaryDataName;
  /** Binary data content name. */
  private String binaryDataContentName;
  /** Binary data type. */
  private int binaryDataType = BINARY_TYPE_EMPTY;
  /** Binary data. */
  private Parcelable binaryData;

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

  public RequestDescription() {
    this.id = nextId();
  }

  /**
   * Create from parcel.
   */
  protected RequestDescription(final Parcel source) {
    this.id = source.readInt();
    this.token = source.readInt();
    this.operationCode = source.readInt();
    this.operationType = source.readInt();
    this.urlPart = source.readString();
    final ClassLoader cl = Thread.currentThread().getContextClassLoader();
    this.simpleParameters = source.readParcelable(cl);
    this.contentType = source.readString();
    this.contentLanguage = source.readString();
    this.metaParameters = source.readParcelable(cl);
    this.parallelMode = source.readInt() == 1;

    // binary content fields
    this.binaryDataName = source.readString();
    this.binaryDataContentName = source.readString();
    this.binaryDataType = source.readInt();
    this.binaryData = source.readParcelable(null);
  }

  @Override
  public void writeToParcel(final Parcel dest, final int flags) {
    dest.writeInt(id);
    dest.writeInt(token);
    dest.writeInt(operationCode);
    dest.writeInt(operationType);
    dest.writeString(urlPart);
    dest.writeParcelable(simpleParameters, flags);
    dest.writeString(contentType);
    dest.writeString(contentLanguage);
    dest.writeParcelable(metaParameters, flags);
    dest.writeInt(parallelMode ? 1 : 0);

    // binary content fields
    dest.writeString(binaryDataName);
    dest.writeString(binaryDataContentName);
    dest.writeInt(binaryDataType);
    dest.writeParcelable(binaryData, 0);
  }

  @Override
  public final int describeContents() {
    switch (binaryDataType) {

    case BINARY_TYPE_EMPTY:
    case BINARY_TYPE_BITMAP:
    case BINARY_TYPE_FILE_URI:
      return 0;

    case BINARY_TYPE_FILE_DESCRITPOR:
      return Parcelable.CONTENTS_FILE_DESCRIPTOR;

    default:
      return resolveParcelDescription();
    }
  }

  void setupOperation(final Operation op) {
    this.operationCode = op.getCode();
    this.operationType = op.getType();
    this.urlPart = op.getUrlPart();
    if (DEBUG) { Log.v(TAG, "Setup request operation OPCODE: " + operationCode + " OPTYPE: " + operationType + " URL: " + urlPart); }
  }

  protected int resolveParcelDescription() { return 0; }

  /** @return request identifier */
  public int getId() { return id; }

  /** @return the operationType */
  public int getOperationType() { return operationType; }

  /** @return the operation */
  public int getOperationCode() { return operationCode; }
  /** @return the token */
  public int getToken() { return token; }

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

  // ============================ BINARY DATA ACCESS ============================

  private File getBinaryDataAsFile() {
    try {
      return new File(new URI(((Uri)binaryData).toString()));
    } catch (final URISyntaxException e) {
      Log.e(TAG, "bad file URI: " + binaryData, e);
      return null;
    }
  }

  /** @param binaryDataName string used to name binary data */
  public void setBinaryDataName(final String binaryDataName) { this.binaryDataName = binaryDataName; }
  /** @return string used to name binary data */
  public String getBinaryDataName() { return binaryDataName; }

  /** @return the uploadFile */
  public String getUploadFile() {
    if (binaryDataType != BINARY_TYPE_FILE_URI) { return null; }
    final File file = getBinaryDataAsFile();
    return file != null ? file.getAbsolutePath() : null;
  }
  /** @param uploadFile the uploadFile to set */
  public void setUploadFile(final String uploadFile) {
    this.binaryDataType = BINARY_TYPE_FILE_URI;
    final File file = new File(uploadFile);
    this.binaryDataContentName = file.getName();
    this.binaryData = Uri.fromFile(file);
  }

  /** @param bitmap bitmap to transfer */
  public final void setBitmap(final Bitmap bitmap) {
    setBitmap(null, bitmap);
  }
  /**
   * @param name bitmap file name
   * @param bitmap bitmap to transfer
   */
  public void setBitmap(final String name, final Bitmap bitmap) {
    this.binaryDataType = BINARY_TYPE_BITMAP;
    this.binaryData = bitmap;
    this.binaryDataContentName = name;
  }
  /** @return bitmap instance if binary data type is {@link #BINARY_TYPE_BITMAP} */
  public Bitmap getBitmap() { return binaryDataType == BINARY_TYPE_BITMAP ? (Bitmap)binaryData : null; }

  /**
   * @param fd file descriptor required to open data to be transfered
   * @param name content name
   */
  public void setFileDescriptor(final String name, final AssetFileDescriptor fd) {
    setFileDescriptor(null, name, fd);
  }
  /**
   * @param type content type (ignored if null)
   * @param fd file descriptor required to open data to be transfered
   */
  public void setFileDescriptor(final String type, final String name, final AssetFileDescriptor fd) {
    if (type != null) { this.contentType = type; }
    this.binaryDataType = BINARY_TYPE_FILE_DESCRITPOR;
    this.binaryDataContentName = name;
    this.binaryData = fd;
  }

  /** @return binary data type identifier */
  public int getBinaryDataType() { return binaryDataType; }
  /** @return file name of the binary data */
  public String getBinaryDataContentName() { return binaryDataContentName; }

  protected void setBinaryData(final int type, final Parcelable data) {
    this.binaryDataType = type;
    this.binaryData = data;
  }
  protected Parcelable getBinaryData() { return binaryData; }

  public void clearBinaryData() {
    this.binaryData = null;
    this.binaryDataType = BINARY_TYPE_EMPTY;
    this.binaryDataName = null;
  }

  // ============================================================================

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

  // ============================ HTTP REQUESTS ============================

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
    if (binaryDataType != BINARY_TYPE_EMPTY) {
      final Part binaryPart = createBinaryPart();
      if (binaryPart != null) {
        parts[realCount++] = binaryPart;
      }
    }
    if (realCount < parts.length) {
      final Part[] trim = new Part[realCount];
      System.arraycopy(parts, 0, trim, 0, realCount);
      parts = trim;
    }
    request.setEntity(new MultipartEntity(parts));
    if (DEBUG) { Log.d(TAG, "(" + requestId + ")" + ": " + params); }
  }

  protected final Part createBinaryPart() throws IOException {
    final String name = TextUtils.isEmpty(binaryDataName) ? BINARY_NAME_DEFAULT : binaryDataName;

    switch (binaryDataType) {

    case BINARY_TYPE_EMPTY:
      throw new IllegalStateException("We are creating a binary part though binary data is empty");

    case BINARY_TYPE_FILE_URI:
      return new FilePart(name, getBinaryDataAsFile(), contentType, null);

    case BINARY_TYPE_FILE_DESCRITPOR:
      return new FilePart(
          name,
          new AssetFileDescriptorPartSource(binaryDataContentName, (AssetFileDescriptor)binaryData),
          contentType,
          null
      );

    case BINARY_TYPE_BITMAP:
      final BitmapPart bitmapPart = new BitmapPart(name, contentType, binaryDataContentName, (Bitmap)binaryData);
      configureBitmapPart(bitmapPart);
      return bitmapPart;

    default:
      return createUserBinaryTypePart();

    }
  }

  protected void configureBitmapPart(final BitmapPart bitmapPart) {
    bitmapPart.setCompressFormat(CompressFormat.JPEG);
    bitmapPart.setCompressQuality(BitmapPart.COMPRESS_QUALITY_DEFAULT);
  }

  protected Part createUserBinaryTypePart() throws IOException { return null; }

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

}
