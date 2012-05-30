package com.stanfy.serverapi.request.binary;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.Parcel;

import com.stanfy.http.multipart.Part;
import com.stanfy.http.multipart.android.BitmapPart;

/**
 * Binary data that contains a bitmap.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
public class BitmapBinaryData extends BinaryData<Bitmap> {

  /** Creator instance. */
  public static final Creator<BitmapBinaryData> CREATOR = new Creator<BitmapBinaryData>() {
    @Override
    public BitmapBinaryData createFromParcel(final Parcel source) { return new BitmapBinaryData(source); }
    @Override
    public BitmapBinaryData[] newArray(final int size) { return new BitmapBinaryData[size]; }
  };

  public BitmapBinaryData() {
    // nothing
  }

  public BitmapBinaryData(final Parcel source) {
    super(source);
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
    setData(bitmap);
    setContentName(name);
  }

  protected void configureBitmapPart(final BitmapPart bitmapPart) {
    bitmapPart.setCompressFormat(CompressFormat.JPEG);
    bitmapPart.setCompressQuality(BitmapPart.COMPRESS_QUALITY_DEFAULT);
  }

  @Override
  public Part createHttpPart(final Context context) {
    final BitmapPart bitmapPart = new BitmapPart(getName(), getContentName(), getData());
    configureBitmapPart(bitmapPart);
    return bitmapPart;
  }

}
