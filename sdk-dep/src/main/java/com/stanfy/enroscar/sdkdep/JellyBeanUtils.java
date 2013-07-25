package com.stanfy.enroscar.sdkdep;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.view.ViewTreeObserver;

import com.stanfy.enroscar.sdkdep.notifications.JellyBeanNotificationBuilder;
import com.stanfy.enroscar.sdkdep.notifications.NotificationBuilder;

/**
 * Jelly bean utilities (API level 16).
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class JellyBeanUtils extends IcsUtils {

  @Override
  public NotificationBuilder createNotificationBuilder(final Context context) {
    return new JellyBeanNotificationBuilder(context);
  }

  @Override
  public void setBackground(final View view, final Drawable background) {
    view.setBackground(background);
  }

  @Override
  public void removeOnGlobalLayoutListener(final ViewTreeObserver observer, final ViewTreeObserver.OnGlobalLayoutListener listener) {
    observer.removeOnGlobalLayoutListener(listener);
  }

}
