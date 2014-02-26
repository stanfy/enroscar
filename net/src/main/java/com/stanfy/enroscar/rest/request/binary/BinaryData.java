package com.stanfy.enroscar.rest.request.binary;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.stanfy.enroscar.beans.BeansManager;
import com.stanfy.enroscar.io.BuffersPool;
import com.stanfy.enroscar.io.IoUtils;
import com.stanfy.enroscar.net.operation.RequestDescription;
import com.stanfy.enroscar.rest.request.net.multipart.Part;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Binary data info.
 * @param <T> binary data
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
public abstract class BinaryData<T extends Parcelable> implements Parcelable {

  /** Logging tag. */
  protected static final String TAG = RequestDescription.TAG;

  /** Binary data name. */
  private String name;
  /** Binary data content name. */
  private String contentName;
  /** Binary data. */
  private T data;
  /** Content type. */
  private String contentType;

  public BinaryData() {
    // nothing
  }

  public BinaryData(final Parcel source) {
    this.name = source.readString();
    this.contentName = source.readString();
    this.data = readData(source);
  }

  @Override
  public int describeContents() { return data != null ? data.describeContents() : 0; }

  @Override
  public void writeToParcel(final Parcel dest, final int flags) {
    dest.writeString(name);
    dest.writeString(contentName);
    writeData(dest, data, flags);
  }

  /** @param binaryDataName string used to name binary data */
  public void setName(final String binaryDataName) { this.name = binaryDataName; }
  /** @return string used to name binary data */
  public String getName() {
    return TextUtils.isEmpty(name) ? RequestDescription.BINARY_NAME_DEFAULT : name;
  }

  public void setContentName(final String contentName) { this.contentName = contentName; }
  /** @return file name of the binary data */
  public String getContentName() { return contentName; }

  /**
   * @param contentType content type value
   */
  public void setContentType(final String contentType) {
    this.contentType = contentType;
  }
  /**
   * @return content type
   */
  public String getContentType() {
    return contentType;
  }

  /** @param data internal data object */
  protected void setData(final T data) { this.data = data; }
  public T getData() { return data; }

  public void clear() {
    this.data = null;
    this.name = null;
    this.contentName = null;
  }

  /** @return part instance */
  public abstract Part createHttpPart(final Context context) throws IOException;

  /**
   * @param stream output
   * @throws IOException if I/O error happens
   */
  public abstract void writeContentTo(final Context context, final OutputStream stream) throws IOException;

  /**
   * @param dest destination parcel
   * @param data internal data object to write to the parcel
   * @param flags write options
   * @see Parcel#writeParcelable(Parcelable, int)
   */
  protected void writeData(final Parcel dest, final T data, final int flags) {
    dest.writeParcelable(data, flags);
  }

  /**
   * @param source source parcel
   * @return internal data object read from the parcel
   */
  protected T readData(final Parcel source) {
    return source.readParcelable(getClass().getClassLoader());
  }

  /**
   * Utility method for transferring input stream to output.
   * @param context application context (needed to retrieve a buffers pool)
   * @param source input stream
   * @param output output stream
   * @throws IOException if I/O error happens
   */
  protected static void writeInputStreamToOutput(final Context context, final InputStream source, final OutputStream output) throws IOException {
    BuffersPool pool = BeansManager.get(context).getContainer().getBean(BuffersPool.class);
    IoUtils.transfer(source, output, pool);
  }

}
