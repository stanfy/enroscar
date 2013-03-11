package com.stanfy.ernoscar.sdkdep;

import java.io.File;

import android.app.Application;
import android.content.ComponentCallbacks;
import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.view.View;
import android.webkit.WebView;

import com.stanfy.ernoscar.sdkdep.notifications.NotificationBuilder;

/**
 * @author Roman Mazur - Stanfy (http://www.stanfy.com)
 */
public interface SdkDependentUtils {

  /**
   * @param context context instance
   * @return cache directory on the external storage
   */
  File getExternalCacheDir(final Context context);

  /**
   * @return standard directory in which to place any audio files
   */
  File getMusicDirectory();

  /**
   * @param view view to set OVER_SCROLL_NEVER
   */
  void setOverscrollNever(final View view);

  /**
   * Enable strict mode.
   */
  void enableStrictMode();

  /**
   * Try to call {@link Editor#apply()} if it's available and call {@link Editor#commit()} if it's not.
   * @param editor shared preferences editor
   */
  void applySharedPreferences(final Editor editor);

  /**
   * Perform pause actions for web view.
   * @param webView web view instance
   */
  void webViewOnPause(final WebView webView);

  /**
   * Perform resume actions for web view.
   * @param webView web view instance
   */
  void webViewOnResume(final WebView webView);

  /**
   * Ensures that this task will be invoked in its own separate thread.
   * @param task async task instance
   * @param params parameters
   */
  <P> void executeAsyncTaskParallel(final AsyncTask<P, ?, ?> task, final P... params);

  /**
   * @return notification builder instance
   */
  NotificationBuilder createNotificationBuilder(final Context context);

  /**
   * @param bitmap bitmap to process
   * @return bitmap size in bytes
   */
  int getBitmapSize(final Bitmap bitmap);

  /**
   * Register application callbacks.
   * @param application application instance
   * @param callbacks callbacks instance
   */
  void registerComponentCallbacks(final Application application, final ComponentCallbacks callbacks);

  /**
   * Set view background.
   * @param view view
   * @param background drawable
   */
  void setBackground(final View view, final Drawable background);

}
