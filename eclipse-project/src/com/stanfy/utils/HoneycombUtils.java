package com.stanfy.utils;

import android.annotation.TargetApi;
import android.os.Build;
import android.webkit.WebView;

/**
 * Honeycomb utilities (API level 11).
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class HoneycombUtils extends GingerbreadUtils {

  @Override
  public void webViewOnPause(final WebView webView) {
    super.webViewOnPause(webView);
    webView.onPause();
  }

  @Override
  public void webViewOnResume(final WebView webView) {
    super.webViewOnResume(webView);
    webView.onResume();
  }

}
