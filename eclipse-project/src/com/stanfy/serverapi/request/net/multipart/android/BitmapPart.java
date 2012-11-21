package com.stanfy.serverapi.request.net.multipart.android;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;

import com.stanfy.serverapi.request.net.multipart.FilePart;
import com.stanfy.serverapi.request.net.multipart.PartSource;
import com.stanfy.utils.AppUtils;

/**
 * {@link PartSource} based on {@link Bitmap} content.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 *
 */
public class BitmapPart extends FilePart {

  /** Default compress quality. */
  public static final int COMPRESS_QUALITY_DEFAULT = 75;

  /** Bitmap. */
  final Bitmap bitmap;

  /** Compress format. */
  private CompressFormat compressFormat = CompressFormat.JPEG;
  /** Compress quality. */
  private int compressQuality = COMPRESS_QUALITY_DEFAULT;

  public BitmapPart(final String name, final String fileName, final Bitmap bitmap) {
    super(name, new BitmapSource(fileName, AppUtils.getSdkDependentUtils().getBitmapSize(bitmap)), "image/jpeg", null);
    this.bitmap = bitmap;
  }

  /**
   * @see Bitmap#compress(CompressFormat, int, OutputStream)
   */
  public void setCompressFormat(final CompressFormat compressFormat) {
    this.compressFormat = compressFormat;
    setContentType("image/" + compressFormat.name().toLowerCase(Locale.US));
  }
  /**
   * @see Bitmap#compress(CompressFormat, int, OutputStream)
   */
  public void setCompressQuality(final int compressQuality) {
    this.compressQuality = compressQuality;
  }

  @Override
  protected void sendData(final OutputStream out) throws IOException {
    if (compressFormat == null) { throw new IllegalStateException("Compress format is not set"); }
    bitmap.compress(compressFormat, compressQuality, out);
  }

  /**
   * @author Roman Mazur (Stanfy - http://stanfy.com)
   */
  private static class BitmapSource implements PartSource {
    /** Name. */
    private final String fileName;
    /** Size. */
    private final int size;

    public BitmapSource(final String fileName, final int size) {
      this.fileName = fileName;
      this.size = size;
    }

    @Override
    public long getLength() { return size; }
    @Override
    public String getFileName() { return fileName; }
    @Override
    public InputStream createInputStream() throws IOException { return null; }
  }

}
