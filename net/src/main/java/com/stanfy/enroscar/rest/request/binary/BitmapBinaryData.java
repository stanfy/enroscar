package com.stanfy.enroscar.rest.request.binary;

import java.io.IOException;
import java.io.OutputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.Parcel;

import com.stanfy.enroscar.rest.request.net.multipart.Part;
import com.stanfy.enroscar.rest.request.net.multipart.android.BitmapPart;

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

  /** Default compress options. */
  protected static final Options DEFAULT_OPTIONS = new Options(CompressFormat.JPEG, BitmapPart.COMPRESS_QUALITY_DEFAULT);

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

  /**
   * Override this method to provide custom bitmap serialization options: format, quality.
   * @return bitmap serialization options
   */
  protected Options getOptions() { return DEFAULT_OPTIONS; }

  @Override
  public Part createHttpPart(final Context context) {
    final BitmapPart bitmapPart = new BitmapPart(getName(), getContentName(), getData());
    Options opts = getOptions();
    bitmapPart.setCompressQuality(opts.quality);
    bitmapPart.setCompressFormat(opts.format);
    return bitmapPart;
  }

  @Override
  public void writeContentTo(final Context context, final OutputStream stream) throws IOException {
    Options opts = getOptions();
    getData().compress(opts.format, opts.quality, stream);
  }

  /** Options object. */
  public static class Options {
    /** Format. */
    final CompressFormat format;
    /** Quality. */
    final int quality;

    public Options(final CompressFormat format, final int quality) {
      this.format = format;
      this.quality = quality;
    }


    public CompressFormat getFormat() {
      return format;
    }
    public int getQuality() {
      return quality;
    }
  }

}
