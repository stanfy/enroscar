package com.stanfy.enroscar.utils;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.stanfy.enroscar.io.IoUtils;

/**
 * Application utilities.
 * @author Roman Mazur - Stanfy (http://www.stanfy.com)
 */
public class AppUtils {

  /** App utils. */
  private static final String TAG = "AppUtils";

  /** Hidden constructor. */
  protected AppUtils() { /* nothing to do */ }

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
   * Get default preferences.
   * @param ctx context instance
   * @return default preferences for the specified context
   * @see PreferenceManager#getDefaultSharedPreferences(android.content.Context)
   */
  public static SharedPreferences getPreferences(final Context ctx) {
    return PreferenceManager.getDefaultSharedPreferences(ctx);
  }

  /**
   * Check whether there is an activity that can respond to the specified intent action in the system.
   * @param context context instance
   * @param action intent action
   * @return true if there is an activity that can respond to the specified intent action
   */
  public static boolean isIntentAvailable(final Context context, final String action) {
    final PackageManager packageManager = context.getPackageManager();
    final List<?> list = packageManager.queryIntentActivities(new Intent(action), PackageManager.MATCH_DEFAULT_ONLY);
    return list.size() > 0;
  }

  /**
   * Log intent details.
   * @param tag logcat tag
   * @param intent intent instance to log about
   */
  public static void logIntent(final String tag, final Intent intent) {
    Log.d(tag, "========================================================");
    Log.d(tag, "action=" + intent.getAction());
    Log.d(tag, "data=" + intent.getData());
    Log.d(tag, "type=" + intent.getType());
    Log.d(tag, "categories=" + intent.getCategories());
    Log.d(tag, "flags=" + Integer.toHexString(intent.getFlags()));
    Log.d(tag, "sourceBounds=" + intent.getSourceBounds());
    Log.d(tag, "extras:");
    final Bundle extras = intent.getExtras();
    if (extras != null) {
      for (final String key : extras.keySet()) {
        final Object o = intent.getExtras().get(key);
        Log.d(tag, "  " + key + "=" + (o != null ? o.getClass() : null) + "/" + o);
      }
    }
  }

  @SuppressLint("NewApi")
  public static int bitmapSize(final Bitmap bitmap) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR1) {
      return bitmap.getRowBytes() * bitmap.getHeight();
    }
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
      return bitmap.getByteCount();
    }
    return bitmap.getAllocationByteCount();
  }
}
