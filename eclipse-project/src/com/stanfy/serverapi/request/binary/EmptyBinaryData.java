package com.stanfy.serverapi.request.binary;

import java.io.IOException;
import java.io.OutputStream;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.stanfy.serverapi.request.net.multipart.ByteArrayPartSource;
import com.stanfy.serverapi.request.net.multipart.FilePart;
import com.stanfy.serverapi.request.net.multipart.Part;

/**
 * Empty binary data.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
public class EmptyBinaryData extends BinaryData<Parcelable> {

  /** Empty array. */
  private static final byte[] EMPTY = new byte[0];

  /** Creator instance. */
  public static final Creator<EmptyBinaryData> CREATOR = new Creator<EmptyBinaryData>() {
    @Override
    public EmptyBinaryData createFromParcel(final Parcel source) { return new EmptyBinaryData(source); }
    @Override
    public EmptyBinaryData[] newArray(final int size) { return new EmptyBinaryData[size]; }
  };

  public EmptyBinaryData() {
    // nothing
  }

  public EmptyBinaryData(final Parcel source) {
    super(source);
  }

  @Override
  public Parcelable getData() {
    throw new UnsupportedOperationException("You can get data of empty binary data");
  }

  @Override
  public Part createHttpPart(final Context context) throws IOException {
    return new FilePart(getName(), new ByteArrayPartSource(getContentName(), EMPTY), getContentType(), null);
  }

  @Override
  protected void writeData(final Parcel dest, final Parcelable data, final int flags) {
    // nothing
  }

  @Override
  protected Parcelable readData(final Parcel source) {
    return null;
  }

  @Override
  public void writeContentTo(final Context context, final OutputStream stream) throws IOException {
    // nothing
  }

}
