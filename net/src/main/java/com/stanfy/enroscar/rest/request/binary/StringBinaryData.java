/**
 *
 */
package com.stanfy.enroscar.rest.request.binary;

import java.io.IOException;
import java.io.OutputStream;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.stanfy.enroscar.io.IoUtils;
import com.stanfy.enroscar.rest.request.net.multipart.Part;


/**
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
public class StringBinaryData extends BinaryData<Parcelable> {

  /** Creator instance. */
  public static final Creator<StringBinaryData> CREATOR = new Creator<StringBinaryData>() {
    @Override
    public StringBinaryData createFromParcel(final Parcel source) { return new StringBinaryData(source); }
    @Override
    public StringBinaryData[] newArray(final int size) { return new StringBinaryData[size]; }
  };

  /** String. */
  private String str;

  public StringBinaryData(final String str) {
    this.str = str;
  }

  StringBinaryData(final Parcel source) {
    super(source);
  }

  @Override
  protected Parcelable readData(final Parcel source) {
    str = source.readString();
    return null;
  }

  @Override
  protected void writeData(final Parcel dest, final Parcelable data, final int flags) {
    dest.writeString(str);
  }

  @Override
  public Part createHttpPart(final Context context) throws IOException {
    return null;
  }

  @Override
  public void writeContentTo(final Context context, final OutputStream stream) throws IOException {
    stream.write(str.getBytes(IoUtils.UTF_8_NAME));
  }

}
