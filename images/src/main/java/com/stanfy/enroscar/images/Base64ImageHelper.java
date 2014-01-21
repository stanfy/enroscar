package com.stanfy.enroscar.images;

import android.text.TextUtils;
import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extracts image bytes from data URI strings.
 * URI format is <code>data:image/(jpeg|gif|png);base64,&lt;data&gt;</code>
 *
 * @author Olexandr Tereshchuk - Stanfy.
 * @since 21.01.14
 */
public class Base64ImageHelper {

  /** Data URI pattern for images. */
  private static final Pattern URI_PATTERN = Pattern.compile("^data:image/(jpeg|gif|png);base64,(([-A-Za-z0-9+/=]|=[^=]|=){3,})$");


  /**
   * @param uri data:image/(jpeg|gif|png);base64,&lt;data&gt;
   * @return image bytes or null if not supported
   */
  public static InputStream decodeIfSupported(final String uri) {
    if (TextUtils.isEmpty(uri)) { return null; }

    final Matcher matcher = URI_PATTERN.matcher(uri);
    if (!matcher.matches()) { return null; }

    final String base64String = matcher.group(2);

    final byte[] bytes = Base64.decode(base64String, Base64.DEFAULT);

    return new ByteArrayInputStream(bytes);
  }

}
