package com.stanfy.serverapi.response;

import java.io.Serializable;

import android.content.Context;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Response data that is passed to service callbacks.
 * @author Roman Mazur - Stanfy (http://www.stanfy.com)
 */
public class ResponseData implements Parcelable {

  /** Illegal response code. */
  public static final int RESPONSE_CODE_ILLEGAL = -1;

  /** Creator. */
  public static final Creator<ResponseData> CREATOR = new Creator<ResponseData>() {
    @Override
    public ResponseData createFromParcel(final Parcel source) { return new ResponseData(source); }
    @Override
    public ResponseData[] newArray(final int size) { return new ResponseData[size]; }
  };

  /** Error code. */
  private int errorCode;
  /** Message. */
  private String message;
  /** Data URI. */
  private Uri data;
  /** Model. */
  private Serializable model;

  public static ResponseData withoutModel(final ResponseData source) {
    if (source.model == null) { return source; }
    final ResponseData result = new ResponseData(source.data, null);
    source.setTo(result);
    return result;
  }

  public ResponseData() {
    // nothing
  }
  public ResponseData(final Uri data, final Serializable model) {
    this.model = model;
    this.data = data;
  }

  protected ResponseData(final Parcel source) {
    errorCode = source.readInt();
    message = source.readString();
    data = source.readParcelable(Thread.currentThread().getContextClassLoader());
    model = source.readSerializable();
  }

  @Override
  public int describeContents() { return 0; }

  @Override
  public void writeToParcel(final Parcel dst, final int flags) {
    dst.writeInt(errorCode);
    dst.writeString(message);
    dst.writeParcelable(data, 0);
    dst.writeSerializable(model);
  }

  /** @return serializable model */
  public Serializable getModel() { return model; }
  /** @return the errorCode */
  public int getErrorCode() { return errorCode; }
  /** @return the message */
  public String getMessage() { return message; }
  /** @return the data */
  public Uri getData() { return data; }

  /** @param errorCode the errorCode to set */
  public void setErrorCode(final int errorCode) { this.errorCode = errorCode; }
  /** @param message the message to set */
  public void setMessage(final String message) { this.message = message; }
  /** @param data the data to set */
  public void setData(final Uri data) { this.data = data; }
  /** @param model the model to set */
  public void setModel(final Serializable model) { this.model = model; }

  public void setResponse(final Response response, final Context context) {
    setErrorCode(response.getErrorCode());
    setMessage(response.resolveUserErrorMessage(context));
  }

  /**
   * Set additional response fields to another {@link ResponseData} instance.
   * Default implementation sets error code and message.
   * @param responseData another instance
   */
  protected void setTo(final ResponseData responseData) {
    responseData.errorCode = this.errorCode;
    responseData.message = this.message;
  }

}
