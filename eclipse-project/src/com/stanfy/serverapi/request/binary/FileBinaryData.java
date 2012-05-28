package com.stanfy.serverapi.request.binary;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import android.net.Uri;
import android.os.Parcel;
import android.util.Log;

import com.stanfy.http.multipart.FilePart;
import com.stanfy.http.multipart.Part;

/**
 * Binary data with file. Contains {@link Uri} that reference to the actual file.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
public class FileBinaryData extends BinaryData<Uri> {

  /** Creator instance. */
  public static final Creator<FileBinaryData> CREATOR = new Creator<FileBinaryData>() {
    @Override
    public FileBinaryData createFromParcel(final Parcel source) { return new FileBinaryData(source); }
    @Override
    public FileBinaryData[] newArray(final int size) { return new FileBinaryData[size]; }
  };

  public FileBinaryData() {
    // nothing
  }

  public FileBinaryData(final Parcel source) {
    super(source);
  }

  /**
   * @return binary data file object
   */
  public File getBinaryDataAsFile() {
    final Uri data = getData();
    try {
      return new File(new URI(data.toString()));
    } catch (final URISyntaxException e) {
      Log.e(TAG, "bad file URI: " + data, e);
      return null;
    }
  }

  /** @return path to the upload file */
  public String getUploadFilePath() {
    final File file = getBinaryDataAsFile();
    return file != null ? file.getAbsolutePath() : null;
  }

  /** @param uploadFile the uploadFile to set */
  public void setUploadFilePath(final String uploadFilePath) {
    final File file = new File(uploadFilePath);
    setContentName(file.getName());
    setData(Uri.fromFile(file));
  }

  @Override
  public Part createHttpPart() throws IOException {
    return new FilePart(getName(), getBinaryDataAsFile(), getContentType(), null);
  }

}
