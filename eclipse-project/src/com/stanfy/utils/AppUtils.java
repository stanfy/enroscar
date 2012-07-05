package com.stanfy.utils;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.internal.$Gson$Types;
import com.stanfy.app.beans.BeansManager;
import com.stanfy.app.beans.EnroscarBean;
import com.stanfy.io.IoUtils;
import com.stanfy.utils.sdk.SDKDependentUtils;

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

  @SuppressWarnings("unchecked")
  public static <K, V> Map<K, V> tuples(final Object[][] tuples) {
    final Map<K, V> result = new HashMap<K, V>(tuples.length);
    for (final Object[] tuple : tuples) { result.put((K)tuple[0], (V)tuple[1]); }
    return result;
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

  public static EnroscarBean getBeanInfo(final Class<?> clazz) {
    final EnroscarBean beanAnnotation = getAnnotationFromHierarchy(clazz, EnroscarBean.class);
    if (beanAnnotation == null) { throw new IllegalArgumentException(clazz + " and its supers are not annotated as @" + EnroscarBean.class.getSimpleName()); }
    return beanAnnotation;
  }

  public static <A extends Annotation> A getAnnotationFromHierarchy(final Class<?> clazz, final Class<A> annotation) {
    Class<?> currentClass = clazz;
    A annotationInstance;
    do {
      annotationInstance = currentClass.getAnnotation(annotation);
      currentClass = currentClass.getSuperclass();
    } while (annotationInstance == null && currentClass != Object.class);
    return annotationInstance;
  }

  public static Class<?> getGenericParameterClass(final Class<?> clazz) {
    final Type superclass = clazz.getGenericSuperclass();
    if (superclass instanceof Class) {
      throw new RuntimeException("Missing type parameter.");
    }
    final ParameterizedType parameterized = (ParameterizedType) superclass;
    final Type type = $Gson$Types.canonicalize(parameterized.getActualTypeArguments()[0]);
    return $Gson$Types.getRawType(type);
  }

  /* ================= SDK depended utils ================= */
  /** Utils instance. */
  private static SDKDependentUtils sdkDependentUtils;

  /**
   * Call to {@link SDKDependentUtils#applySharedPreferences(android.content.SharedPreferences.Editor)}.
   * @param editor shared preferences editor instance
   */
  public static void applySharedPreferences(final Editor editor) {
    getSdkDependentUtils().applySharedPreferences(editor);
  }

  /** @return SDK depended utils */
  public static SDKDependentUtils getSdkDependentUtils() {
    if (sdkDependentUtils == null) {
      final BeansManager beansManager = BeansManager.get(null);
      if (beansManager == null) {
        throw new IllegalStateException("Beans manager must be created before calling getSdkDependentUtils");
      }
      sdkDependentUtils = beansManager.getSdkDependentUtilsFactory().createSdkDependentUtils();
    }
    return sdkDependentUtils;
  }

}
