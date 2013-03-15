package com.stanfy.enroscar.rest.request.binary;

import java.io.IOException;
import java.io.OutputStream;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.os.Parcel;

import com.stanfy.enroscar.rest.request.net.multipart.FilePart;
import com.stanfy.enroscar.rest.request.net.multipart.Part;
import com.stanfy.enroscar.rest.request.net.multipart.android.AssetFileDescriptorPartSource;

/**
 * Binary data that contains assets file descriptor. The latter can be returned by ContentResolver.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
public class AssetFdBinaryData extends BinaryData<AssetFileDescriptor> {

  /** Creator instance. */
  public static final Creator<AssetFdBinaryData> CREATOR = new Creator<AssetFdBinaryData>() {
    @Override
    public AssetFdBinaryData createFromParcel(final Parcel source) { return new AssetFdBinaryData(source); }
    @Override
    public AssetFdBinaryData[] newArray(final int size) { return new AssetFdBinaryData[size]; }
  };

  public AssetFdBinaryData() {
    // nothing
  }

  public AssetFdBinaryData(final Parcel source) {
    super(source);
  }

  /**
   * @param fd file descriptor required to open data to be transfered
   * @param name content name
   */
  public void setFileDescriptor(final String name, final AssetFileDescriptor fd) {
    setContentName(name);
    setData(fd);
  }

  @Override
  public Part createHttpPart(final Context context) throws IOException {
    return new FilePart(
        getName(),
        new AssetFileDescriptorPartSource(getContentName(), getData()),
        getContentType(),
        null
    );
  }

  @Override
  public void writeContentTo(final Context context, final OutputStream stream) throws IOException {
    writeInputStreamToOutput(context, getData().createInputStream(), stream);
  }

}
