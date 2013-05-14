package com.stanfy.enroscar.utils;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.stanfy.enroscar.io.IoUtils;

/**
 * Application utilities.
 * @author Roman Mazur - Stanfy (http://www.stanfy.com)
 */
public class AppUtils {

  /** DB data types. */
  public static final String DB_INTEGER = "INTEGER", DB_REAL = "REAL", DB_TEXT = "TEXT", DB_BLOB = "BLOB";

  /** App utils. */
  private static final String TAG = "AppUtils";

  /** Hidden constructor. */
  protected AppUtils() { /* nothing to do */ }

  public static String convertToHex(final byte[] data) {
    final StringBuilder buf = new StringBuilder();
    final int mask = 0x0F, ten = 10, nine = 9, shiftLength = 4;
    for (final byte element : data) {
      int halfbyte = element >>> shiftLength & mask;
      int twoHalfs = 0;
      do {
        if (0 <= halfbyte && halfbyte <= nine) {
          buf.append((char) ('0' + halfbyte));
        } else {
          buf.append((char) ('a' + (halfbyte - ten)));
        }
        halfbyte = element & mask;
      } while (twoHalfs++ < 1);
    }
    return buf.toString();
  }

  /**
   * Calculate 32 bytes length MD5 digest.
   * @param text input text
   * @return MD5 digest
   */
  public static String getMd5(final String text)  {
    try {

      final MessageDigest md = MessageDigest.getInstance("MD5");
      final byte[] utf8Bytes = text.getBytes(IoUtils.UTF_8_NAME);
      md.update(utf8Bytes, 0, utf8Bytes.length);
      final byte[] md5hash = md.digest();
      final int radix = 16;
      final int length = 32;

      final StringBuilder result = new StringBuilder(length).append(new BigInteger(1, md5hash).toString(radix));

      final int zeroBeginLen = length - result.length();
      if (zeroBeginLen > 0) {
        final char [] zeroBegin = new char[zeroBeginLen];
        Arrays.fill(zeroBegin, Character.forDigit(0, radix));
        result.insert(0, zeroBegin);
      }

      return result.toString();

    } catch (final NoSuchAlgorithmException e) {
      Log.e(TAG, "MD5 error", e);
      return "";
    } catch (final UnsupportedEncodingException e) {
      Log.e(TAG, "MD5 error", e);
      return "";
    }
  }

  /**
   * @param ctx context
   * @return application preferences
   */
  public static SharedPreferences getPreferences(final Context ctx) {
    return PreferenceManager.getDefaultSharedPreferences(ctx);
  }

  public static boolean isIntentAvailable(final Context context, final String action) {
    final PackageManager packageManager = context.getPackageManager();
    final List<?> list = packageManager.queryIntentActivities(new Intent(action), PackageManager.MATCH_DEFAULT_ONLY);
    return list.size() > 0;
  }

  public static void logIntent(final String tag, final Intent intent) {
    Log.d(tag, "========================================================");
    Log.d(tag, "action=" + intent.getAction());
    Log.d(tag, "data=" + intent.getData());
    Log.d(tag, "type=" + intent.getType());
    Log.d(tag, "categories=" + intent.getCategories());
    Log.d(tag, "flags=" + Integer.toHexString(intent.getFlags()));
    // Log.d(tag, "sourceBounds=" + intent.getSourceBounds());
    Log.d(tag, "extras:");
    final Bundle extras = intent.getExtras();
    if (extras != null) {
      for (final String key : extras.keySet()) {
        final Object o = intent.getExtras().get(key);
        Log.d(tag, "  " + key + "=" + (o != null ? o.getClass() : null) + "/" + o);
      }
    }
  }

}
