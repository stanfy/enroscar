package com.stanfy.utils;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.ApplicationInfo;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.stanfy.DebugFlags;
import com.stanfy.views.utils.Animations;

/**
 * A set of GUI utilities.
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
public class GUIUtils {

  /** Logging tag. */
  protected static final String TAG = "GUI";

  /** Debug flag. */
  protected static final boolean DEBUG = DebugFlags.DEBUG_UTILS;

  /** Hidden constructor. Allows subclassing. */
  protected GUIUtils() { /* hide */ }

  /**
   * Create a very simple notification, <b>should be used for debugging only</b>.
   * Use {@link com.stanfy.utils.sdk.SDKDependentUtils#createNotificationBuilder(Context)} for building notification.
   * @param ctx context instance
   * @param message message to display
   * @param contentIntent content intent
   * @return notification instance
   * @see com.stanfy.utils.sdk.SDKDependentUtils#createNotificationBuilder(Context)
   * @see com.stanfy.utils.notifications.NotificationBuilder
   */
  public static Notification createNotification(final Context ctx, final CharSequence message, final PendingIntent contentIntent) {
    final ApplicationInfo appInfo = ctx.getApplicationInfo();
    return AppUtils.getSdkDependentUtils().createNotificationBuilder(ctx)
        .setSmallIcon(appInfo.icon)
        .setTicker(message)
        .setContentTitle(ctx.getPackageManager().getApplicationLabel(appInfo))
        .setContentText(message)
        .setContentIntent(contentIntent)
        .build();
  }

  /**
   * Set text view value or set its visibility to {@link View#GONE}.
   * @param view view instance
   * @param text text value
   */
  public static void setTextOrHide(final TextView view, final CharSequence text) {
    setTextOrHide(view, text, View.GONE);
  }
  /**
   * Set text view value or change its visibility in case of empty value.
   * @param view view instance
   * @param text text value
   * @param hvisibility visibility value
   */
  public static void setTextOrHide(final TextView view, final CharSequence text, final int hvisibility) {
    if (TextUtils.isEmpty(text)) {
      view.setVisibility(hvisibility);
    } else {
      view.setText(text);
      view.setVisibility(View.VISIBLE);
    }
  }

  /**
   * @param view view to hide
   */
  public static void hideWithNextSeparatorSibling(final View view) {
    final ViewGroup vg = (ViewGroup)view.getParent();
    final int index = vg.indexOfChild(view);
    if (index < vg.getChildCount() - 1) {
      final View v = vg.getChildAt(index + 1);
      if (v.getClass() == View.class) { v.setVisibility(View.GONE); }
    }
    view.setVisibility(View.GONE);
  }
  /**
   * @param view view to hide
   */
  public static void hideWithPrevSeparatorSibling(final View view) {
    final ViewGroup vg = (ViewGroup)view.getParent();
    final int index = vg.indexOfChild(view);
    if (index > 0) {
      final View v = vg.getChildAt(index - 1);
      if (v.getClass() == View.class) { v.setVisibility(View.GONE); }
    }
    view.setVisibility(View.GONE);
  }

  /**
   * Hide soft keyboard.
   * @param textView text view containing current window token
   */
  public static void hideSoftInput(final View textView) {
    try {
      final InputMethodManager imm = (InputMethodManager)textView.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
      imm.hideSoftInputFromWindow(textView.getWindowToken(), 0);
    } catch (final Exception e) {
      Log.w(TAG, "Ignore exception", e);
    }
  }
  /**
   * Show soft keyboard.
   * @param textView text view containing current window token
   */
  public static void showSoftInput(final View textView) {
    try {
      final InputMethodManager imm = (InputMethodManager)textView.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
      imm.showSoftInput(textView, InputMethodManager.SHOW_FORCED);
    } catch (final Exception e) {
      Log.w(TAG, "Ignore exception", e);
    }
  }
  /**
   * Toggles keyboard visibility.
   * @param textView text view containing current window token
   */
  public static void toggleSoftInput(final View textView) {
    try {
      final InputMethodManager imm = (InputMethodManager)textView.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
      imm.toggleSoftInputFromWindow(textView.getWindowToken(), 0, 0);
    } catch (final Exception e) {
      Log.w(TAG, "Ignore exception", e);
    }
  }

  public static void shortToast(final Context context, final int messageId) {
    Toast.makeText(context, messageId, Toast.LENGTH_SHORT).show();
  }
  public static void shortToast(final Context context, final CharSequence message) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
  }
  public static void longToast(final Context context, final int messageId) {
    Toast.makeText(context, messageId, Toast.LENGTH_LONG).show();
  }
  public static void longToast(final Context context, final CharSequence message) {
    Toast.makeText(context, message, Toast.LENGTH_LONG).show();
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

  public static CharSequence getApplicationLabel(final Context context) {
    return context.getPackageManager().getApplicationLabel(context.getApplicationInfo());
  }

  public static AlertDialog createConfirm(final Context context, final int message, final OnClickListener yesListener) {
    final String m = context.getString(message);
    return createConfirm(context, m, yesListener, true);
  }
  public static AlertDialog createConfirm(final Context context, final String message, final OnClickListener yesListener, final boolean useNoListener) {
    return new AlertDialog.Builder(context)
        .setTitle(getApplicationLabel(context))
        .setMessage(message)
        .setPositiveButton(android.R.string.yes, yesListener)
        .setNegativeButton(android.R.string.no, useNoListener ? yesListener : null)
        .create();
  }

  public static AlertDialog createDialogMessage(final Context context, final String message) { return createDialogMessage(context, message, null); }
  public static AlertDialog createDialogMessage(final Context context, final String message, final DialogInterface.OnClickListener listener) {
    return new AlertDialog.Builder(context)
      .setTitle(getApplicationLabel(context))
      .setMessage(message)
      .setPositiveButton(android.R.string.ok, listener)
      .create();
  }

  public static ProgressDialog createSpinner(final Context context, final CharSequence text, final OnCancelListener oncancel) {
    final ProgressDialog spinner = new ProgressDialog(context);
    spinner.requestWindowFeature(Window.FEATURE_NO_TITLE);
    spinner.setMessage(text);
    spinner.setOnCancelListener(oncancel);
    return spinner;
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

}
