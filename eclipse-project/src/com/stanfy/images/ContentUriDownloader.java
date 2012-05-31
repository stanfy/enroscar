package com.stanfy.images;

import java.io.IOException;
import java.io.InputStream;

import android.content.ContentResolver;
import android.net.Uri;

/**
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
public class ContentUriDownloader implements Downloader {

  /** Content resolver. */
  private final ContentResolver contentReolver;

  public ContentUriDownloader(final ContentResolver contentResolver) {
    this.contentReolver = contentResolver;
  }

  @Override
  public InputStream download(final String url) throws IOException {
    return contentReolver.openInputStream(Uri.parse(url));
  }

  @Override
  public void finish(final String url) {
    // nothing
  }

  @Override
  public void flush() {
    // nothing
  }

}
