package com.stanfy.serverapi.request.binary;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.Parcel;
import android.util.Log;

import com.stanfy.serverapi.request.net.multipart.FilePart;
import com.stanfy.serverapi.request.net.multipart.Part;
import com.stanfy.serverapi.request.net.multipart.android.AssetFileDescriptorPartSource;

/**
 * Binary data with file. Contains {@link Uri} that reference to the actual file.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
public class ContentUriBinaryData extends BinaryData<Uri> {

  /** Creator instance. */
  public static final Creator<ContentUriBinaryData> CREATOR = new Creator<ContentUriBinaryData>() {
    @Override
    public ContentUriBinaryData createFromParcel(final Parcel source) { return new ContentUriBinaryData(source); }
    @Override
    public ContentUriBinaryData[] newArray(final int size) { return new ContentUriBinaryData[size]; }
  };

  public ContentUriBinaryData() {
    // nothing
  }

  public ContentUriBinaryData(final Parcel source) {
    super(source);
  }

  /**
   * @return binary data file object
   */
  public File getBinaryDataAsFile() {
    final Uri data = getData();
    if (!ContentResolver.SCHEME_FILE.equals(data.getScheme())) { return null; }
    try {
      return new File(new URI(data.toString()));
    } catch (final URISyntaxException e) {
      Log.e(TAG, "bad file URI: " + data, e);
      return null;
    }
  }

  /**
   * @param data content URI
   * @param contentName content name
   */
  public void setContentUri(final Uri data, final String contentName) {
    final String scheme = data.getScheme();
    if (!ContentResolver.SCHEME_ANDROID_RESOURCE.equals(scheme)
      && !ContentResolver.SCHEME_CONTENT.equals(scheme)
      && !ContentResolver.SCHEME_FILE.equals(scheme)) {
      throw new IllegalArgumentException("Illegal URI scheme: " + scheme);
    }
    setData(data);
    setContentName(contentName);
  }

  @Override
  public Part createHttpPart(final Context context) throws IOException {
    final ContentResolver resolver = context.getContentResolver();
    return new FilePart(
        getName(),
        new AssetFileDescriptorPartSource(getContentName(), resolver.openAssetFileDescriptor(getData(), "r")),
        getContentType(),
        null
    );
  }

  @Override
  public void writeContentTo(final Context context, final OutputStream stream) throws IOException {
    final ContentResolver resolver = context.getContentResolver();
    writeInputStreamToOutput(context, resolver.openInputStream(getData()), stream);
  }

}
