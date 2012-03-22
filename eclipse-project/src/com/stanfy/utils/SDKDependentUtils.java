package com.stanfy.utils;

import java.io.File;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.view.View;
import android.webkit.WebView;

/**
 * @author Roman Mazur - Stanfy (http://www.stanfy.com)
 */
public interface SDKDependentUtils {

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

}
