package com.stanfy.utils;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;

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

  /** Random generator. */
  private static Random random = new Random();

  public static String createTableSQL(final String table, final String primaryColumn, final String[] columns, final Map<String, String> types) {
    final StringBuilder result = new StringBuilder().append("CREATE TABLE IF NOT EXISTS ").append(table).append(" (")
        .append(primaryColumn).append(" INTEGER PRIMARY KEY AUTOINCREMENT");
    for (final String col : columns) {
      if (primaryColumn.equals(col)) { continue; }
      result.append(", ").append(col).append(' ');
      final String type = types.get(col);
      result.append(type == null ? DB_TEXT : type);
    }
    result.append(')');
    return result.toString();
  }

  public static Uri getUri(final String authority, final String path) {
    return Uri.parse("content://" + authority + "/" + path);
  }

  /**
   * @param ctx context
   * @return notification manager
   */
  public static NotificationManager getNotficationManager(final Context ctx) {
    return (NotificationManager)ctx.getSystemService(Context.NOTIFICATION_SERVICE);
  }

  public static int rand() { return random.nextInt(); }

  public static String convertToHex(final byte[] data) {
    final StringBuilder buf = new StringBuilder();
    final int mask = 0x0F, ten = 10, nine = 9, shiftLength = 4;
    for (int i = 0; i < data.length; i++) {
      int halfbyte = (data[i] >>> shiftLength) & mask;
      int twoHalfs = 0;
      do {
        if ((0 <= halfbyte) && (halfbyte <= nine)) {
          buf.append((char) ('0' + halfbyte));
        } else {
          buf.append((char) ('a' + (halfbyte - ten)));
        }
        halfbyte = data[i] & mask;
      } while (twoHalfs++ < 1);
    }
    return buf.toString();
  }

  public static String getMd5(final String text)  {
    try {
      final int md5hashSize = 32;

      MessageDigest md;
      md = MessageDigest.getInstance("MD5");
      byte[] md5hash = new byte[md5hashSize];
      final byte[] utf8Bytes = text.getBytes("UTF-8");
      md.update(utf8Bytes, 0, utf8Bytes.length);
      md5hash = md.digest();
      return convertToHex(md5hash);

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

  public static String buildFilePathById(final long id, final String name) {
    final StringBuilder sb = new StringBuilder();
    final int divider = 100;
    long rest = id;
    do {
      final int value = (int)(rest % divider);
      rest /= divider;
      sb.append(value).append('/');
    } while (rest != 0);
    sb.append(name);
    return sb.toString();
  }

  @SuppressWarnings("unchecked")
  public static <K, V> Map<K, V> tuples(final Object[][] tuples) {
    final Map<K, V> result = new HashMap<K, V>(tuples.length);
    for (final Object[] tuple : tuples) { result.put((K)tuple[0], (V)tuple[1]); }
    return result;
  }

  /**
   * Converts device independent points to actual pixels.
   * @param context - context
   * @param dip - dip value
   * @return pixels count
   */
  public static int pixelsWidth(final DisplayMetrics displayMetrics, final int dip) {
    final float scale = displayMetrics.density;
    final float alpha = 0.5f;
    return (int)(dip * scale + alpha);
  }
  /**
   * Converts device independent points to actual pixels.
   * @param context - context
   * @param dip - dip value
   * @return pixels count
   */
  public static int pixelsOffset(final DisplayMetrics displayMetrics, final int dip) {
    final float scale = displayMetrics.density;
    return (int)(dip * scale);
  }

  /**
   * @param directory directory
   * @return directory size
   */
  public static long sizeOfDirectory(final File directory) {
    if (directory == null || !directory.exists()) { return 0; }
    if (!directory.isDirectory()) { return directory.length(); }
    final File[] files = directory.listFiles();
    int result = 0;
    for (final File f : files) {
      if (f.isDirectory()) {
        result += sizeOfDirectory(f);
      } else {
        result += f.length();
      }
    }
    return result;
  }

  /**
   * @param a activity instance
   * @return whether the activity was started from launcher
   */
  public static boolean isStartedFromLauncher(final Activity a) {
    final Intent intent = a.getIntent();
    final String intentAction = intent.getAction();
    return intent.hasCategory(Intent.CATEGORY_LAUNCHER)
        && intentAction != null && intentAction.equals(Intent.ACTION_MAIN);
  }

  /* ================= SDK depended utils ================= */
  /** Utils instance. */
  private static SDKDependentUtils sdkDependentUtils;

  /** @return SDK depended utils */
  public static SDKDependentUtils getSdkDependentUtils() { return sdkDependentUtils; }

  static {
    final int version = Build.VERSION.SDK_INT;
    String classsName = null;
    if (version >= Build.VERSION_CODES.GINGERBREAD) {
      classsName = "com.stanfy.utils.GingerbreadUtils";
    } else if (version >= Build.VERSION_CODES.ECLAIR) {
      classsName = "com.stanfy.utils.EclairUtils";
    } else {
      classsName = "com.stanfy.utils.LowestSDKDependentUtils";
    }
    try {
      sdkDependentUtils = (SDKDependentUtils)Class.forName(classsName).newInstance();
    } catch (final Exception e) {
      sdkDependentUtils = new LowestSDKDependentUtils();
    } finally {
      Log.d(TAG, "SDK depended utils: " + sdkDependentUtils);
    }
  }

}
