package com.stanfy.utils;

import android.webkit.WebView;

/**
 * Honeycomb utilities (API level 11).
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
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
