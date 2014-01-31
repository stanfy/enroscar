package com.stanfy.enroscar.net;

import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

import android.content.ContentResolver;
import android.util.Log;

/**
 * Stream handler that deals with content resolver schemes:
 * {@link ContentResolver#SCHEME_CONTENT}, {@link ContentResolver#SCHEME_FILE},
 * {@link ContentResolver#SCHEME_ANDROID_RESOURCE}.
 */
class ContentUriStreamHandler extends URLStreamHandler {

  /** Logging tag. */
  private static final String TAG = ContentUriStreamHandler.class.getSimpleName();

  /** Content resolver instance. */
  private final ContentResolver contentResolver;

  public ContentUriStreamHandler(final ContentResolver contentResolver) {
    if (contentResolver == null) {
      throw new IllegalArgumentException("Content resolver must be not null");
    }
    this.contentResolver = contentResolver;
  }

  @Override
  protected URLConnection openConnection(final URL u) {
    return new ContentUriConnection(u, contentResolver);
  }

  @Override
  protected URLConnection openConnection(final URL u, final Proxy proxy) throws IOException {
    Log.w(TAG, "Proxy settings are ignored for content schemes");
    return openConnection(u);
  }

}
