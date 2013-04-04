package com.stanfy.enroscar.sdkdep.notifications;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Build;

/**
 * Notification builder for Gingerbread platform.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
@TargetApi(Build.VERSION_CODES.GINGERBREAD)
public class GingerbreadNotificationBuilder extends BaseNotificationBuilder {

  /** Full screen intent. */
  private PendingIntent fullScreenIntent;

  public GingerbreadNotificationBuilder(final Context context) {
    super(context);
  }

  @SuppressWarnings("deprecation")
  @Override
  public NotificationBuilder setFullScreenIntent(final PendingIntent intent, final boolean highPriority) {
    this.fullScreenIntent = intent;
    setFlag(Notification.FLAG_HIGH_PRIORITY, highPriority);
    return this;
  }

  @Override
  public Notification build() {
    final Notification n = super.build();
    n.fullScreenIntent = fullScreenIntent;
    return n;
  }

  @Override
  public int getApiVersion() {
    return Build.VERSION_CODES.GINGERBREAD;
  }

}
