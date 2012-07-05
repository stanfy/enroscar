package com.stanfy.utils.notifications;

import android.annotation.TargetApi;
import android.app.Notification;
import android.content.Context;
import android.os.Build;

/**
 * Notification builder for Jelly Bean.
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class JellyBeanNotificationBuilder extends IcsNotificationBuilder {

  public JellyBeanNotificationBuilder(final Context context) {
    super(context);
  }

  @Override
  public Notification build() { return core.build(); }

  @Override
  public int getApiVersion() { return Build.VERSION_CODES.JELLY_BEAN; }

}
