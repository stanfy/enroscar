package com.stanfy.views.utils;

import java.util.HashMap;
import java.util.Map;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.widget.TextView;
import android.widget.Toast;

import com.stanfy.views.R;

/**
 * Application utils.
 * @author Roman Mazur - Stanfy (http://www.stanfy.com)
 */
public class AppUtils {

  /** App utils. */
  private static final String TAG = "AppUtils";

  /** Hidden constructor. */
  protected AppUtils() { /* nothing to do */ }

  /**
   * @param ctx context
   * @return notification manager
   */
  public static NotificationManager getNotficationManager(final Context ctx) {
    return (NotificationManager)ctx.getSystemService(Context.NOTIFICATION_SERVICE);
  }

  /**
   * @param ctx context
   * @return application preferences
   */
  public static SharedPreferences getPreferences(final Context ctx) {
    return PreferenceManager.getDefaultSharedPreferences(ctx);
  }

  public static Bundle resolveDataBundle(final android.app.Activity a, final Bundle savedState) {
    return savedState != null
      ? savedState
      : a.getIntent() != null ? a.getIntent().getExtras() : new Bundle();
  }

  public static Notification createNotification(final Context ctx, final CharSequence message, final PendingIntent contentIntent) {
    final Notification n = new Notification(R.drawable.icon, message, System.currentTimeMillis());
    n.setLatestEventInfo(ctx, ctx.getText(R.string.app_name), message, contentIntent);
    return n;
  }

  public static void setTextOrHide(final TextView view, final String text) {
    if (TextUtils.isEmpty(text)) {
      view.setVisibility(View.GONE);
    } else {
      view.setText(text);
      view.setVisibility(View.VISIBLE);
    }
  }

  public static void logIntent(final String tag, final Intent intent) {
    Log.d(tag, "========================================================");
    Log.d(tag, "action=" + intent.getAction());
    Log.d(tag, "data=" + intent.getData());
    Log.d(tag, "type=" + intent.getType());
    Log.d(tag, "categories=" + intent.getCategories());
    // Log.d(tag, "sourceBounds=" + intent.getSourceBounds());
    Log.d(tag, "extras:");
    for (final String key : intent.getExtras().keySet()) {
      final Object o = intent.getExtras().get(key);
      Log.d(tag, "  " + key + "=" + o.getClass() + "/" + o);
    }
  }

  public static void showMovingView(final View view) {
    view.setVisibility(View.VISIBLE);
    view.startAnimation(Animations.goDownAnimation());
  }
  public static void hideMovingView(final View view) {
    final Animation a = Animations.goUpAnimation();
    a.setAnimationListener(new Animations.AnimationListner() {
      @Override
      public void onAnimationEnd(final Animation animation) {
        view.setVisibility(View.GONE);
      }
    });
    view.startAnimation(a);
  }

  public static ProgressDialog createSpinner(final Context context, final CharSequence text, final OnCancelListener oncancel) {
    final ProgressDialog spinner = new ProgressDialog(context);
    spinner.requestWindowFeature(Window.FEATURE_NO_TITLE);
    spinner.setMessage(text);
    spinner.setOnCancelListener(oncancel);
    return spinner;
  }

  public static void shortToast(final Context context, final int messageId) {
    Toast.makeText(context, messageId, Toast.LENGTH_SHORT).show();
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
   * Progress listener.
   * @author Roman Mazur - Stanfy (http://www.stanfy.com)
   */
  public interface ProgressListener {
    void updateProgress(final float value);
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
      classsName = "com.stanfy.views.utils.GingerbreadUtils";
    } else if (version >= Build.VERSION_CODES.ECLAIR) {
      classsName = "com.stanfy.views.utils.EclairUtils";
    } else {
      classsName = "com.stanfy.views.utils.LowestSDKDependentUtils";
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
