package com.stanfy.enroscar.sdkdep;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.webkit.WebView;

import com.stanfy.enroscar.sdkdep.notifications.IcsNotificationBuilder;
import com.stanfy.enroscar.sdkdep.notifications.NotificationBuilder;

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

  @Override
  public <P> void executeAsyncTaskParallel(final AsyncTask<P, ?, ?> task, final P... params) {
    task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
  }

  @Override
  public NotificationBuilder createNotificationBuilder(final Context context) {
    return new IcsNotificationBuilder(context);
  }

}
